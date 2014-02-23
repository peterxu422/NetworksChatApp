import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public class Sender extends Thread {
	private PrintWriter writer;
	private BufferedReader stdin;
	
	public Sender(PrintWriter writer) {
		this.writer = writer;
		stdin = new BufferedReader(new InputStreamReader(System.in));
	}
	
	public void run() {
		String buf;
		try {
			while(!isInterrupted()) {
				System.out.print("> "); /* User prompt */
				buf = stdin.readLine(); /* Reads user's input from stdin */
				writer.println(buf);
				writer.flush();
			}
			
			stdin.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
