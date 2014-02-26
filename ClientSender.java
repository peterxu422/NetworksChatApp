/**
 * Sends messages directed to the client
 */
import java.io.PrintWriter;
import java.util.Queue;

public class ClientSender extends Thread {
	private PrintWriter out;
	private User client;
	
	public ClientSender(User client, PrintWriter out) {
		this.client = client;
		this.out = out;
	}
	
	public void send(String msg) {
		out.print(msg); // Goes to Client Program
		out.flush();
	}
	
	public void run() {
		
	}
}
