import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;


public class ChatThread implements Runnable {
	
	private static final int BLOCK_TIME = 60;
	
	private static HashMap<String, String> userdb;
	private static Socket clientSocket;
	
	private static PrintWriter out = null;
	private static BufferedReader in;
	
	public ChatThread(Socket client, HashMap<String, String> userdb) {
		this.userdb = userdb;
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
				outputLine = "server: Hello Client";
				out.println(outputLine);
				
				while((inputLine = in.readLine()) != null) {	//Waits for client to respond 
					System.out.println("client: " + inputLine);
					out.println("server: " + inputLine);
				}
				
				//clientSocket.close();
				out.close();
				in.close();
				
			} catch (IOException ioe) {

			}
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
			uname = in.readLine();
			out.println("password: ");
			pw = in.readLine();
			
			if(userdb.containsKey(uname) && userdb.get(uname).equals(pw)) {
				out.println("Welcome back " + uname + "!");
				return true;
			}

			out.println("Invalid login");
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

		return false;
	}
}
