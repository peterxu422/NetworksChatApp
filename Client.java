import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {
	
	public static void main(String args[]) {
		
		if(args.length != 2) {
			System.err.println("Usage: java Client <hostname> <port number>");
			System.exit(1);
		}
		
		String hostName = args[0];
		int portNumber = Integer.parseInt(args[1]);
		
		try {
			Socket servSocket = new Socket(hostName, portNumber);		//Create socket to the server
			PrintWriter out = new PrintWriter(servSocket.getOutputStream(), true);	//Read and Write streams to the server socket
			BufferedReader in = new BufferedReader(new InputStreamReader(servSocket.getInputStream())); //Server's response comes in through here
			BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));	//User input through here
			
			String userInput, servMsg;
			while( (servMsg = in.readLine()) != null) {
				System.out.println(servMsg);
				
				while( (userInput = stdin.readLine()) != null) { /* Reads user's input from stdin */
					out.println(userInput);
					System.out.println(in.readLine());	//readLine() Blocks until server echoes line back to client.
					System.out.print("$ ");			//Command prompt
				}
			}
			servSocket.close();
			out.close();
			in.close();
			stdin.close();
		} catch(IOException ioe) {
			System.out.println("Client failed to connect");
			ioe.printStackTrace();
		}
		
	}
	
}
