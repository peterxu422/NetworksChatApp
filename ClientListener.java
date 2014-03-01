import java.io.BufferedReader;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Listens for incoming messages and commands from the client
 * @author Peter
 *
 */
public class ClientListener extends Thread {
	
	/* Constants */
	private String[] cmds = {"whoelse", "wholasthr", "message", "broadcast", "block", "unblock", "logout"};
	
	/* The following values are in milliseconds */
	public static final long BLOCK_TIME = 60 * 1000; 		/* 1 minute */		
	public static final long LAST_HOUR = 60 * 60 * 1000; 	/* 1 hour */
	public static final long TIME_OUT = 30 * 60 * 1000; 	/* 30 minutes */
	
	/* Checks activity of client */
	private ActivityChecker checker;
	
	/* Socket and client info */
	private ServerSender servSender;
	private Socket clntSock;
	private User client;
	
	/* Socket IO */
	private BufferedReader in;
	private char[] buf;
	
	/* Data structures */
	private ArrayList<User> online;
	private HashMap<String, Queue<String>> offlineMsgs;
	
	/**
	 * Private Thread to logout inactive users
	 * @author Peter
	 *
	 */
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
		this.clntSock = clntSock;
		this.servSender = servSender;
		this.in = in;
		buf = new char[1024];
		online = servSender.getOnlineUsers();
		offlineMsgs = servSender.getOfflineMsgs();
		
		checker = new ActivityChecker();
		checker.start();
	}
	
	/**
	 * The main execution thread of the Client Listener thread
	 */
	public void run() {
		try {
			
			/* Authentication */
			while(true) {
				if(authenticate(client)) {
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
			
			/* Read messages sent from the client socket */
			int n;
			while( (n = in.read(buf)) != -1) {
				boolean iscmd = false;
				String line = new String(buf, 0, n-2);	/* Do not include \r\n at the end of the input */
				
				/* THIS MUST BE CHANGED 
				 * TO N-1 ON LINUX SYSTEM */
				
				if(line.isEmpty())
					continue;
				
				/* If something was read from the buffer, then user is still active */
				client.setActive();
				String[] toks = line.split(" ");
				
				if(toks.length == 0)
					continue;
				
				/* Check if the command sent is a valid one */
				for(String c : cmds) {
					if( (iscmd = toks[0].equals(c))) {
						exec(c, line);
						break;
					}
				}
				
				if(!iscmd) {
					client.send("Error: '" + toks[0] + "' is not a valid command.");
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			System.err.println("read error");
			logout();	/* If connection terminates because of client, just log them out */
		}
	}
	
	/**
	 * Executes the command sent by the client
	 * @param cmd The command
	 * @param line The entire message line that was sent
	 * @throws IOException
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
			client.send("Error: Invalid command");
		}
	}
	
	private synchronized void whoelse() throws IOException {
		for(User u : online) {
			if(u != client)	{
				if(client.isBlocked(u.getUname()))
					client.send(u.getUname() + " (blocked)");
				else
					client.send(u.getUname());
			}
		}
	}
	
	private synchronized void wholasthr() throws IOException {
		String clntName = client.getUname();
		long since;
		
		// List the users currently online
		for(User u : online) {
			if(u == client)
				continue;
			if(client.isBlocked(u.getUname()))
				client.send(u.getUname() + " (blocked)");
			else
				client.send(u.getUname());		
		}
		
		// List the users who signed off less than 1 hour ago
		HashMap<String, Date> logouts = servSender.getRecentLogouts();
		Iterator it = logouts.keySet().iterator();
		Date now = new Date();
		
		while(it.hasNext()) {
			String name = (String) it.next();
			if(name.equals(clntName))
				continue;
			
			Date loggedOut = logouts.get(name);
			since = now.getTime() - loggedOut.getTime();
			
			if(since < LAST_HOUR) {
				if(client.isBlocked(name))
					client.send(name + " (blocked) (offline)");
				else
					client.send(name + " (offline)");
			}
			else {	/* Remove them if they were logged out more than an hour ago */
				logouts.remove(name);
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
		
		HashMap<String,String> userdb = servSender.getUserDB();
		
		if(!userdb.containsKey(usr)) {		/* Check if the person they want to message is a user */
			client.send("Error: The user you wish to send a message to does not exist.");
			return;
		}
		
		synchronized(online) {
			
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
		}
		
		/* For messaging offline users */
		
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
		client.send(usr + " is not online. Your message has been sent offline.");
	}
	
	private synchronized void block(String usr) throws IOException {
		if(usr.equals(client.getUname())) {
			client.send("Error: You cannot block yourself!");
			return;
		}
		
		HashMap<String,String> userdb = servSender.getUserDB();
		
		if(!userdb.containsKey(usr)) {		/* Check if the person they want to block is a user */
			client.send("Error: The user you wish to block does not exist.");
			return;
		}
		
		if(client.addBlock(usr)) {
			client.send(usr + " has been blocked.");
			return;
		}
		client.send(usr + " was already blocked.");
	}
	
	private synchronized void unblock(String usr) throws IOException {
		if(client.removeBlock(usr)) {
			client.send(usr + " is unblocked.");
			return;
		}
			
		client.send(usr + " was never blocked.");
	}
	
	private void logout() {
		servSender.removeClient(client);
		servSender.addLogout(client.getUname());
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
	private boolean authenticate(User u) throws IOException {
		HashMap<String,String> userdb = servSender.getUserDB();
		
		String uname, pw;
		u.send("username: ");
		uname = in.readLine();
		
		if(!userdb.containsKey(uname)) {
			client.send("Error: Username '" + uname + "' does not exist.");
			return false;			
		}
			
		
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
		
		/* Valid Login */
		if(userdb.containsKey(uname) && userdb.get(uname).equals(pw)) {
			u.send("Welcome back " + uname + "!\r\n");
			u.setUname(uname);
			client.setBlockedList(servSender.getBlocked(client.getUname()));
			servSender.resetTries(uname);
			return true;
		}
		
		/* Invalid Login */
		u.send("Invalid login");
		servSender.addTries(uname);
		if(servSender.getNumLogonTries(uname) == 3) {
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
