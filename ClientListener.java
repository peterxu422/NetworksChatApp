/**
 * Listens for incoming messages and commands from the client
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class ClientListener extends Thread {
	
	private ServerSender servSender;
	private Socket clntSock;
	private BufferedReader in;
	private User client;
	private char[] buf;
	ArrayList<User> online;
	
	private String[] cmds = {"whoelse", "wholasthr", "message", "broadcast", "block", "unblock", "logout"};
	
	public ClientListener(ServerSender servSender, Socket clntSock, User client, BufferedReader in) {
		this.client = client;
		this.in = in;
		this.clntSock = clntSock;
		buf = new char[1024];
		this.servSender = servSender;
		online = servSender.getOnlineUsers();
	}
	
	public void run() {
		
		try {
			if(authenticate(client))
				servSender.addClient(client);
			
			int n;
			while( (n = in.read(buf)) != -1) {
				String line = new String(buf, 0, n-2);	/* Do not include \r\n at the end of the input */
				System.out.println("len:"+line.length() + ",|"+line+"|");
				//System.out.println("CListner's run(): ");
				String[] toks = line.split(" ");
				
				for(String c : cmds) {
					if(toks[0].equals(c)) {
						exec(c, line);
						break;
					}
				}
				
				client.addRcvQ(new String(buf, 0, n));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.err.println("read error");
		}
	}
	
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
				
				//client.addRcvQ(new String(buf, 0, n));
				return line;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.err.println("read error");
		}
		
		return null;
	}
	
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
			broadcast(tok[1]);
		}
		else if(cmd.equals("message")) {
			System.out.println("message exec'd");
			tok = line.split(p, 3);
			message(tok[1], tok[2]);
		}
		else if(cmd.equals("block")) {
			tok = line.split(p, 2);
			block(tok[1]);
		}
		else if(cmd.equals("unblock")) {
			tok = line.split(p, 2);
			unblock(tok[1]);
		}
		else if(cmd.equals("logout")) {
			logout();
		}
		else {
			//Invalid command
		}
	}
	
	private void whoelse() throws IOException {
		// May need to use synchronize here
		synchronized(online) {
			System.err.println("online size: " + online.size());
			
			for(User u : online) {
				if(u != client)	
					client.send(u.getUname());
			}
		}
	}
	
	private void wholasthr() throws IOException {
		long since;
		Date now = new Date();
		
		for(User u : online) {
			since = now.getTime() - u.getStart().getTime();
			if(since < 1000 * 60 * 60 && u != client) {
				client.send(u.getUname());
			}
		}
	}
	
	private void broadcast(String msg) throws IOException {
		for(User u : online) {
			if(u != client)
				u.peerMsg(client, msg);
		}
	}
	
	private void message(String usr, String msg) throws IOException {
		
		for(User u : online) {
			if(u.getUname().equals(usr)) {
				if(u.isBlocked(client.getUname())) {
					client.send(usr + " has blocked you.");
					break;
				}
				
				u.peerMsg(client, msg);
				break;
			}
		}
	}
	
	private void block(String usr) throws IOException {
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
	
	private synchronized void logout() throws IOException {
		servSender.removeClient(client);
		clntSock.close();
		in.close();
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
		u.send("password: ");
		pw = in.readLine();

		if(userdb.containsKey(uname) && userdb.get(uname).equals(pw)) {
			u.send("Welcome back " + uname + "!\r\n");
			u.setUname(uname);
			return true;
		}

		u.send("Invalid login");
		return false;
	}
}
