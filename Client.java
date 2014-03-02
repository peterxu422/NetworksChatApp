import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Client program for the chat server. Connect to the chat server and send messages to other
 * connected peers.
 * @author Peter
 *
 */
public class Client {
	
	public static void main(String args[]) {
		
		if(args.length != 2) {
			System.err.println("Usage: java Client <hostname> <port number>");
			System.exit(1);
		}
		
		String hostName = args[0];
		int portNumber = Integer.parseInt(args[1]);
		
		try {
			/* Socket and IO initializations */
			Socket servSocket = new Socket(hostName, portNumber);										//Create socket to the server
			PrintWriter out = new PrintWriter(servSocket.getOutputStream(), true);						//Read and Write streams to the server socket
			BufferedReader in = new BufferedReader(new InputStreamReader(servSocket.getInputStream())); //Server's response comes in through here
			
			Sender sender = new Sender(out);
			sender.setDaemon(true);
			sender.start();
			
			String servMsg;
			char[] buf = new char[1024];
			int n;
			
			// Listens for messages from server
			while( (n = in.read(buf)) != -1) {
				servMsg = new String(buf, 0, n);
				System.out.println(servMsg);
			}
			
			servSocket.close();
			out.close();
			in.close();

		} catch(IOException ioe) {
			System.out.println("Connection has been closed.");
			//ioe.printStackTrace();
		}
		
	}
	
}
