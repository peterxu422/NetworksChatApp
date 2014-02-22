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

public class Server {
	
	// Protected variables?
	public static HashMap<String, String> userdb;
	private ArrayList<ChatThread> onlineUsers;
	
	private static ServerSocket serverSocket;

	private static PrintWriter out = null;
	private static BufferedReader in;
	
	public Server(int portNumber) {
		try {
			serverSocket = new ServerSocket(portNumber);
			System.out.println("Server started on port " + portNumber + "...");
			onlineUsers = new ArrayList<ChatThread>();
			readUsers();
			start();
		} catch(IOException ioe) {
			ioe.printStackTrace();
		}
	}
	
	public ArrayList<ChatThread> getOnlineUsers()		{return onlineUsers;}
	
	public void start() {
		for(;;) {
			try {
				System.out.println("Waiting for client to connect...");
				
				ChatThread ct = new ChatThread(this, serverSocket.accept());
				Thread t = new Thread(ct);
				t.start();
				onlineUsers.add(ct);
			} catch (IOException ioe) {
				System.out.println("Server connection terminated");
			}
		}
	}
	
	public static void main(String args[]) {
		if(args.length != 1) {
			System.out.println("Usage: <port number>");
			System.exit(1);
		}
		
		//args[0] = "4118";
		int portNumber = Integer.parseInt(args[0]);
		Server s = new Server(portNumber); //Create Server socket
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

}
