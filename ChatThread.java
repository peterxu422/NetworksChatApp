import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Date;

public class ChatThread implements Runnable {
	
	private static final int BLOCK_TIME = 60;
	
	private ArrayList<String> blocked;
	private static HashMap<String, String> userdb;
	private Socket clientSocket;
	public Server serv;
	
	private static PrintWriter out = null;
	//private static BufferedWriter out;
	private static BufferedReader in;
	
	private String user;	// The User/Client associated with this chat thread
	private Date start;
	
	private enum Cmd {
		WHOELSE, WHOLASTHR, BROADCAST, MESSAGE, BLOCK, UNBLOCK, LOGOUT;
	}
	
	public ChatThread(Server serv, Socket client) {
		start = new Date();
		this.serv = serv;
		user = "";
		userdb = Server.userdb;
		clientSocket = client;
		blocked = new ArrayList<String>();
		
		try {
			out = new PrintWriter(clientSocket.getOutputStream(), true);	//Open's Readers and Writers to client's io stream
			in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		} catch(IOException ioe) {
			ioe.printStackTrace();
		}
	}
	
	public String getUser()				{return user;}
	public Date getStart()				{return start;}
	
	@Override
	public void run() {
		while(true) {
			// TODO Auto-generated method stub
			try {	
				int tries = 0;
				while(tries < 3) {
					if(authenticate())
						break;
					
					tries++;
					System.out.println(tries);
					if(tries == 3) { //Block this user for 60 seconds BLOCK_TIME
						out.println("You're going to be blocked for " + BLOCK_TIME + " seconds!");
						out.flush();
					}
				}
				
				//User authenticated, may participate in chat
				String inputLine, outputLine;
				outputLine = "";
				out.println(outputLine);
				//out.flush();
				
				while((inputLine = in.readLine()) != null) {	// Waits for client to respond 
					System.out.println("client: " + inputLine);	// Client's message
					
					processCommand(inputLine);
					
					//out.println("server: " + inputLine);		// Server's response
				}
				
				clientSocket.close();
				out.close();
				in.close();
				
			} catch (IOException ioe) {
				ioe.printStackTrace();
				System.err.println("Client terminated");
				break;
			}
		}
	}
	
	public void send(String msg) {
		out.print("# ");
		out.println(msg);
	}
	
	public boolean isBlocked(String usr) {
		return blocked.contains(usr);
	}
	
	// Processes the client's command. Assume checks for valid commands and args are performed on Client side
	private void processCommand(String inputLine) throws IOException {
		if(inputLine.isEmpty()) {
			return;
		}
		
		String cmdToks[] = inputLine.split(" ");
		int len = cmdToks.length;
		
		int port = clientSocket.getPort();
		
		switch(Cmd.valueOf(cmdToks[0].toUpperCase())) {
		
		case WHOELSE:
			System.err.println(user + " on port " + port + " called whoelse");
			whoelse();
			break;
		case WHOLASTHR:
			System.err.println(user + " on port " + port + " called wholasthr");
			wholasthr();
			break;
		case BROADCAST:
			System.err.println(user + " on port " + port + " called broadcast");
			if(len == 2)
				broadcast(cmdToks[1]);
			else {}
				// print error msg to client
			break;
		case MESSAGE:
			System.err.println(user + " on port " + port + " called message");
			if(len == 3) {
				message(cmdToks[1], cmdToks[2]);
			}
			else {}
				
			break;
		case BLOCK:
			System.err.println(user + " on port " + port + " called block");
			if(len == 2) {
				block(cmdToks[1]);
			}
			else {}
			break;
			
		case UNBLOCK:
			System.err.println(user + " on port " + port + " called unblock");
			if(len == 2) {
				unblock(cmdToks[1]);
			}
			else {
				
			}
			break;
			
		case LOGOUT:
			System.err.println(user + " on port " + port + " called logout");
			logout();
			break;
			
		default:
			//Print error message to client
			send("# Invalid command");
			break;
		}
	}
	
	private void whoelse() {
		// May need to use synchronize here
		
		ArrayList<ChatThread> users = serv.getOnlineUsers();
		
		synchronized(users) {
			System.err.println("ct lenght: " + users.size());
			
			for(ChatThread ct : users) {
				System.err.println("users: " + ct.getUser() + ", inetaddr: " + ct.clientSocket.getInetAddress() + ", port: " + ct.clientSocket.getPort());		
			}
			String online = "";
			
			for(ChatThread ct : users)
				online += ct.getUser() + ", ";
				
			out.println(online);
		}
	}
	
	private void wholasthr() {
		ArrayList<ChatThread> users = serv.getOnlineUsers();
		long since;
		Date now = new Date();
		
		String lasthr = "";
		
		for(ChatThread ct : users) {
			since = now.getTime() - ct.getStart().getTime();
			if(since < 1000 * 60 * 60) {
				lasthr += ct.getUser() + ", ";
			}
		}
		
		out.println(lasthr);
	}
	
	private void broadcast(String msg) {
		ArrayList<ChatThread> users = serv.getOnlineUsers();
		
		for(ChatThread ct : users) {
			ct.send(user + ": " + msg);
		}
	}
	
	private void message(String usr, String msg) {
		ArrayList<ChatThread> users = serv.getOnlineUsers();
		
		for(ChatThread ct : users) {
			if(ct.getUser().equals(usr)) {
				if(ct.isBlocked(usr)) {
					send(usr + " has blocked you.");
					break;
				}

				ct.send(user + ": " + msg);
				break;
			}
		}
	}
	
	private void block(String usr) {
		if(isBlocked(usr)) {
			send(usr + " is already blocked.");
			return;
		}
		
		blocked.add(usr);			
	}
	
	private void unblock(String usr) {
		if(blocked.remove(usr)) {
			send(usr + " is unblocked.");
			return;
		}
		
		send(usr + " was never blocked.");
	}
	
	private void logout() throws IOException {
		clientSocket.close();
	}
	
	/**
	 * 
	 * @return true if valid user, false otherwise
	 */
	private boolean authenticate() {
		try {
			String uname, pw;
			out.println("username: ");
			uname = in.readLine();
			out.println("password: ");
			pw = in.readLine();
			
			if(userdb.containsKey(uname) && userdb.get(uname).equals(pw)) {
				out.println("Welcome back " + uname + "!");
				user = uname;
				return true;
			}

			out.println("Invalid login");

		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

		return false;
	}
}
