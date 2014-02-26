/**
 * Listens for incoming messages and commands from the client
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

public class ClientListener extends Thread {
	
	/* Constants */
	private String[] cmds = {"whoelse", "wholasthr", "message", "broadcast", "block", "unblock", "logout"};
	
	/* The following values are in milliseconds */
	public static final long BLOCK_TIME = 60 * 1000; 		/* 1 minute */		
	public static final long LAST_HOUR = 60 * 60 * 1000; 	/* 1 hour */
	public static final long TIME_OUT = 30 * 60 * 1000; 	/* 30 minutes */
	
	private ActivityChecker checker;
	private ServerSender servSender;
	private Socket clntSock;
	private BufferedReader in;
	private User client;
	private char[] buf;
	private ArrayList<User> online;
	private HashMap<String, Date> banlist;
	private HashMap<String, Queue<String>> offlineMsgs;
	
	private class ActivityChecker extends Thread {
		private static final long SLEEP_TIME = 30000;
		
		private ActivityChecker() {
		}
		
		public void run() {
			while(true) {
				Date now = new Date();
				long t = now.getTime() - client.getActive().getTime();
				
				if(t >= TIME_OUT) {
					client.send("You have been logged out due to inactivity.");
					logout();
					return;
				}
				
				/* Have the Thread check every half minute whether the Client is inactive so
				 * as not to waste CPU resources.
				 */
				try {
					Thread.sleep(SLEEP_TIME);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	public ClientListener(ServerSender servSender, Socket clntSock, User client, BufferedReader in) {
		this.client = client;
		this.in = in;
		this.clntSock = clntSock;
		buf = new char[1024];
		this.servSender = servSender;
		online = servSender.getOnlineUsers();
		offlineMsgs = servSender.getOfflineMsgs();
		banlist = servSender.getBanList();
		
		checker = new ActivityChecker();
		checker.start();
	}
	
	public void run() {
		try {			
			
			// Check for Consecutive failure attempts overall, not just in order.
			int tries;
			for(tries = 0; tries < 3; tries++) {
				if(authenticate(client, tries)) {
					servSender.addClient(client);
					break;
				}
			}
			
			/* Send the client any pending offline messages */
			Queue<String> offmsgs = offlineMsgs.get(client.getUname());
			if(offmsgs != null && offmsgs.peek() != null) {
				client.send("Messages sent while you were offline.\n");
				String s;
				while( (s = offlineMsgs.get(client.getUname()).poll()) != null) {
					client.send(s + "\n");
				}
			}
	
			int n;
			while( (n = in.read(buf)) != -1) {
				String line = new String(buf, 0, n-2);	/* Do not include \r\n at the end of the input */
				
				/* THIS MUST BE CHANGED 
				 * TO N-1 ON LINUX SYSTEM */
				
				//System.out.println("len:"+line.length() + ",|"+line+"|");
				//System.out.println("CListner's run(): ");
				
				/* If something was read from the buffer, then user is still active */
				client.setActive();
				String[] toks = line.split(" ");
				
				for(String c : cmds) {
					if(toks[0].equals(c)) {
						exec(c, line);
						break;
					}
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.err.println("read error");
		}
	}
	
	/*
	public String getLine() {
		int n;
		try {
			while( (n = in.read(buf)) != -1) {
				String line = new String(buf, 0, n);
				String[] toks = line.split("\\s+", 2);
				
				for(String c : cmds) {
					if(toks[0].equals(c)) {
						exec(c, line);
						break;
					}
				}
				
				return line;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.err.println("read error");
		}
		
		return null;
	}
	*/
	
	private void exec(String cmd, String line) throws IOException {
		String tok[];
		String p = "\\s+";
		if(cmd.equals("whoelse")) {
			whoelse();
		}
		else if(cmd.equals("wholasthr")) {
			wholasthr();
		}
		else if(cmd.equals("broadcast")) {
			tok = line.split(p, 2);
			if(tok.length == 2)
				broadcast(tok[1]);
		}
		else if(cmd.equals("message")) {
			tok = line.split(p, 3);
			if(tok.length == 3)
				message(tok[1], tok[2]);
		}
		else if(cmd.equals("block")) {
			tok = line.split(p, 2);
			if(tok.length == 2)
				block(tok[1]);
		}
		else if(cmd.equals("unblock")) {
			tok = line.split(p, 2);
			if(tok.length == 2)
				unblock(tok[1]);
		}
		else if(cmd.equals("logout")) {
			logout();
		}
		else {
			//Invalid command
			client.send("Error: Invalid command");
		}
	}
	
	private void whoelse() throws IOException {
		// May need to use synchronize here
		synchronized(online) {
			//System.err.println("online size: " + online.size());
			
			for(User u : online) {
				if(u != client)	{
					if(client.isBlocked(u.getUname()))
						client.send(u.getUname() + " (blocked)");
					else
						client.send(u.getUname());
				}
			}
		}
	}
	
	//Activity within the last hour
	private void wholasthr() throws IOException {
		long since;
		Date now = new Date();
		
		for(User u : online) {
			since = now.getTime() - u.getStart().getTime();
			if(since < LAST_HOUR && u != client) {
				if(client.isBlocked(u.getUname()))
					client.send(u.getUname() + " (blocked)");
				else
					client.send(u.getUname());
			}
		}
	}
	
	private void broadcast(String msg) throws IOException {
		for(User u : online)
			u.peerMsg(client, msg);
	}
	
	private void message(String usr, String msg) throws IOException {
		if(usr.equals(client.getUname())) {
			client.send("Error: You cannot send a message to yourself");
			return;
		}
		
		for(User u : online) {
			if(u.getUname().equals(usr)) {
				if(u.isBlocked(client.getUname())) {
					client.send(usr + " has blocked you.");
					return;
				}
				
				u.peerMsg(client, msg);
				return;
			}
		}
		
		synchronized(offlineMsgs) {
			/* User was not online, so add the message to their offline Queue */
			if(offlineMsgs.containsKey(usr))
				offlineMsgs.get(usr).add(client.getUname() + ": " + msg);
			else {
				Queue<String> msgs = new LinkedList<String>();
				msgs.add(client.getUname() + ": " + msg);
				offlineMsgs.put(usr, msgs);
			}
		}
	}
	
	private void block(String usr) throws IOException {
		if(usr.equals(client.getUname())) {
			client.send("Error: You cannot block yourself!");
			return;
		}
			
		
		if(client.addBlock(usr)) {
			client.send(usr + " has been blocked.");
			return;
		}
		client.send(usr + " was already blocked.");
	}
	
	private void unblock(String usr) throws IOException {
		if(client.removeBlock(usr)) {
			client.send(usr + " is unblocked.");
			return;
		}
			
		client.send(usr + " was never blocked.");
	}
	
	private synchronized void logout() {
		servSender.removeClient(client);
		close();
	}
	
	private synchronized void close() {
		try {
			clntSock.close();
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.err.println("Client connection has been closed.");
		}
	}
		
	/**
	 * 
	 * @return true if valid user, false otherwise
	 * @throws IOException 
	 */
	private boolean authenticate(User u, int tries) throws IOException {
		HashMap<String,String> userdb = servSender.getUserDB();
		
		String uname, pw;
		u.send("username: ");
		uname = in.readLine();
		
		/* Check if the user is on the ban list
		 * Terminates connection if so and closes sockets and writers/readers.
		 */
		if(servSender.isBanned(uname, clntSock.getInetAddress())) {
			client.send("Your username at this IP address was banned. Reconnect later.");
			close();
		}
		
		/*
		 * Check for duplicate user
		 */
		for(User usr : online) {
			if(usr.getUname().equals(uname)) {
				client.send(uname + " is already logged in. Disconnecting...");
				close();
			}
		}
		
		u.send("password: ");
		pw = in.readLine();

		if(userdb.containsKey(uname) && userdb.get(uname).equals(pw)) {
			u.send("Welcome back " + uname + "!\r\n");
			u.setUname(uname);
			client.setBlockedList(servSender.getBlocked(client.getUname()));
			return true;
		}
		
		u.send("Invalid login");
		System.out.println("tries: "+ tries);
		if(tries == 2) {
			System.out.println("inside tries3");
			if(!servSender.isBanned(uname, clntSock.getInetAddress())) {
				System.out.println("before addban:"+uname + clntSock.getInetAddress());
				servSender.addBannedClient(uname, clntSock.getInetAddress());
				client.send("Your username at this IP address will be banned for " + BLOCK_TIME/1000 + " seconds.");
			}	
			close();
		}
		return false;
	}
}
