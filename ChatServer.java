import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.ArrayList;

public class ChatServer {

	private ServerSocket serverSocket;
	
	public ChatServer(int portNumber) {
		try {
			serverSocket = new ServerSocket(portNumber);
			System.out.println("Server started on port " + portNumber + "...");
			start();
		} catch(IOException ioe) {
			ioe.printStackTrace();
		}
	}
	
	public ServerSocket getServSock()					{return serverSocket;}
	
	public void start() {
		ServerSender servSender = new ServerSender();
		servSender.start();
		
		while(true) {
			try {
				System.out.println("Waiting for client to connect...");
				Socket clntSock = serverSocket.accept();
				System.out.println("Client connected (" + clntSock.getInetAddress() + ":" + clntSock.getPort()+")");
				BufferedReader in = new BufferedReader(new InputStreamReader(clntSock.getInputStream()));
				PrintWriter out = new PrintWriter(clntSock.getOutputStream(), true);
				
				User client = new User();
				ClientListener listener = new ClientListener(servSender, clntSock, client, in);
				ClientSender sender = new ClientSender(client, out);
				client.addListenerSender(listener, sender);
				listener.start();
				sender.start();
			} catch (IOException ioe) {
				System.out.println("Server connection terminated");
			}
		}
	}
	
	public static void main(String args[]) {
		/*
		String line = "message   user   msg";
		String tok[] = line.split("\\s+", 3);
		
		for(String s : tok) {
			System.out.println(s);
		}
		*/
		if(args.length != 1) {
			System.out.println("Usage: <port number>");
			System.exit(1);
		}
		
		//args[0] = "4118";
		int portNumber = Integer.parseInt(args[0]);
		ChatServer s = new ChatServer(portNumber); //Create Server socket
	}
	


}
