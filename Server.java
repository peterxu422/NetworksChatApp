import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.ArrayList;

public class Server implements Runnable {
	
	private static HashMap<String, String> userdb;
	private static ArrayList<ChatThread> onlineUsers;
	private static final int BLOCK_TIME = 60;
	
	private static ServerSocket serverSocket;

	private static PrintWriter out = null;
	private static BufferedReader in;
	private static Thread thread = null;
	
	public Server(int portNumber) {
		try {
			serverSocket = new ServerSocket(portNumber);
			System.out.println("Server started on port " + portNumber + "...");
			onlineUsers = new ArrayList<ChatThread>();
			start();
		} catch(IOException ioe) {
			ioe.printStackTrace();
		}
	}
	
	public static void main(String args[]) {
		if(args.length != 1) {
			System.out.println("Usage: <port number>");
			System.exit(1);
		}
		
		//args[0] = "4118";
		int portNumber = Integer.parseInt(args[0]);
		Server s = new Server(portNumber);
	}

	@Override
	public void run() {
		while(thread != null) {
			// TODO Auto-generated method stub
			try {
				System.out.println("Waiting for client to connect...");
				//clientSocket = serverSocket.accept();			//Blocks until receives/accepts connection from a client. Returns socket object and bind to client's address and port
				addChatThread(serverSocket.accept());
				
				
				//out = new PrintWriter(clientSocket.getOutputStream(), true);	//Open's Readers and Writers to client's io stream
				//out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
				//in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));	
				
				readUsers();
				int tries = 0;
				while(tries < 3) {
					if(authenticate())
						break;
					
					tries++;
					System.out.println(tries);
					if(tries == 3) { //Block this user for 60 seconds BLOCK_TIME
						out.println("You're going to be blocked for " + BLOCK_TIME + " seconds!");
						//out.write("You're going to be blocked for " + BLOCK_TIME + " seconds!");
					}
				}
				
				//User authenticated, may participate in chat
				String inputLine, outputLine;
				outputLine = "server: Hello Client";
				out.println(outputLine);
				//out.write(outputLine);
				
				while((inputLine = in.readLine()) != null) {	//Waits for client to respond 
					System.out.println("client: " + inputLine);
					out.println("server: " + inputLine);
					//out.write("server: " + inputLine);
				}
				
				serverSocket.close();
				//clientSocket.close();
				out.close();
				in.close();
				
			} catch (IOException ioe) {
				//ioe.printStackTrace();
				System.out.println("Server connection terminated");
			}
		}
	}
	
	public void start() {
		if(thread == null) {
			thread = new Thread(this);
			thread.start();
		}
	}
	
	public static void readUsers() {
		userdb = new HashMap<String, String>();
		try {
			BufferedReader br = new BufferedReader(new FileReader("user_pass.txt"));
			
			String cred;
			
			while( (cred = br.readLine()) != null) {
				String up[] = cred.split(" ");
				userdb.put(up[0], up[1]);
			}
			br.close();
			
		} catch(FileNotFoundException fnfe) {
			fnfe.printStackTrace();
			System.err.println("file \'user_pass.txt\' not found");
			System.exit(1);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
	
	/**
	 * 
	 * @return true if valid user, false otherwise
	 */
	public static boolean authenticate() {
		try {
			String uname, pw;
			out.println("username: ");
			//out.write("username: ");
			uname = in.readLine();
			out.println("password: ");
			//out.write("password: ");
			pw = in.readLine();
			
			if(userdb.containsKey(uname) && userdb.get(uname).equals(pw)) {
				out.println("Welcome back " + uname + "!");
				//out.write("Welcome back " + uname + "!");
				return true;
			}

			out.println("Invalid login");
			//out.write("Invalid login");
			
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		

		return false;
	}
	
	public static void addChatThread(Socket clt) {
		onlineUsers.add(new ChatThread(clt));
	}
}
