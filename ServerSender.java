import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class ServerSender extends Thread {
	
	private ArrayList<User> onlineUsers;
	public static HashMap<String, String> userdb;
	
	public ServerSender() {
		onlineUsers = new ArrayList<User>();
		userdb = new HashMap<String, String>();
		readUsers();
	}
	
	public void run() {
		while(true) {
			
		}
	}
	
	public ArrayList<User> getOnlineUsers() 		{return onlineUsers;}
	public HashMap<String,String> getUserDB()				{return userdb;}
	
	public synchronized void addClient(User u) {
		//run authenticate before adding	
		System.out.println("addingClient: " + u.getUname());
		onlineUsers.add(u);
	}
	
	public synchronized void removeClient(User u) {
		onlineUsers.remove(u);
	}
	
	public void broadcast() {
		
	}
	
	public void readUsers() {
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
	/*
	private boolean authenticate(User u) {
		
		String uname, pw;
		u.send("username: ");
		uname = u.rcv();
		u.send("password: ");
		pw = u.rcv();

		if(userdb.containsKey(uname) && userdb.get(uname).equals(pw)) {
			u.send("Welcome back " + uname + "!\r\n");
			u.setUname(uname);
			return true;
		}

		u.send("Invalid login");
		return false;
	}
	*/
}
