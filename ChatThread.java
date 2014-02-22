import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;


public class ChatThread implements Runnable {
	
	private static final int BLOCK_TIME = 60;
	
	private static HashMap<String, String> userdb;
	private Socket clientSocket;
	
	private static PrintWriter out = null;
	//private static BufferedWriter out;
	private static BufferedReader in;
	
	public String user;	// The User/Client associated with this chat thread
	
	private enum Cmd {
		WHOELSE, WHOLASTHR, BROADCAST, MESSAGE, BLOCK, UNBLOCK, LOGOUT;
	}
	
	public ChatThread(Socket client) {
		user = "";
		userdb = Server.userdb;
		clientSocket = client;
		try {
			out = new PrintWriter(clientSocket.getOutputStream(), true);	//Open's Readers and Writers to client's io stream
			in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		} catch(IOException ioe) {
			ioe.printStackTrace();
		}
	}
	
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
					}
				}
				
				//User authenticated, may participate in chat
				String inputLine, outputLine;
				outputLine = "";
				out.println(outputLine);
				
				
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
	
	// Processes the client's command. Assume checks for valid commands and args are performed on Client side
	private void processCommand(String inputLine) {
		if(inputLine.isEmpty()) {
			return;
		}
		
		System.err.println("processCommand()");
		
		String cmdToks[] = inputLine.split(" ");
		int len = cmdToks.length;
		
		switch(Cmd.valueOf(cmdToks[0].toUpperCase())) {
		
		case WHOELSE:
			whoelse();
			break;
		case WHOLASTHR:
			wholasthr();
			break;
		case BROADCAST:
			if(len == 2)
				broadcast(cmdToks[1]);
			else {}
				// print error msg to client
			break;
		case MESSAGE:
			if(len == 3) {
				//message()
			}
			else {}
				
			break;
		case BLOCK:
			if(len == 2) {}
				// block()
			else {}
			break;
			
		case UNBLOCK:
			if(len == 2) {}
				// unblock()
			else {
				
			}
			break;
			
		case LOGOUT:
			logout();
			break;
			
		default:
			//Print error message to client
			break;
		}
	}
	
	private void whoelse() {
		// May need to use synchronize here
		
		ArrayList<ChatThread> users = Server.onlineUsers;
		String online = "";
		
		synchronized(users) {
			System.err.println("ct lenght: " + users.size());
			System.err.println("users: " + users.toString());
			
			for(ChatThread ct : users)
				online += ct.user + ", ";
		}
		
		out.println(online);
	}
	
	private void wholasthr() {
		
	}
	
	private void broadcast(String msg) {
		
	}
	
	private void message(String usr, String msg) {
		
	}
	
	private void block(String usr) {
		
	}
	
	private void unblock(String usr) {
		
	}
	
	private void logout() {
		
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
