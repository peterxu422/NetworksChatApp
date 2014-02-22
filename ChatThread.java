import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;


public class ChatThread implements Runnable {
	
	private static Socket clientSocket;
	
	private static PrintWriter out = null;
	private static BufferedReader in;
	
	
	public ChatThread(Socket client) {
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
		// TODO Auto-generated method stub
		
	}
	
}
