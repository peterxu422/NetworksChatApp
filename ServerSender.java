import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Queue;
import java.util.Date;
import java.util.HashMap;

public class ServerSender extends Thread {
	private ArrayList<User> onlineUsers;
	private HashMap<String, Date> recentLogouts;
	private HashMap<String, String> userdb;
	private HashMap<String, ArrayList<String>> blockLists;
	private HashMap<String, Date> banlist;
	private HashMap<String, Queue<String>> offlineMsgs;	/* key: Username, value: Message Queue */
	private HashMap<String, Integer> logonTries;
	
	public ServerSender() {
		onlineUsers = new ArrayList<User>();
		offlineMsgs = new HashMap<String, Queue<String>>();
		userdb = new HashMap<String, String>();
		banlist = new HashMap<String, Date>();
		blockLists = new HashMap<String, ArrayList<String>>();
		recentLogouts = new HashMap<String, Date>();
		logonTries = new HashMap<String, Integer>();
		readUsers();
	}
	
	public void run() {
		while(true) {
		}
	}
	
	public ArrayList<User> getOnlineUsers() 				{return onlineUsers;}
	public HashMap<String, Date> getRecentLogouts() 		{return recentLogouts;}
	public HashMap<String, Queue<String>> getOfflineMsgs()	{return offlineMsgs;}
	public HashMap<String,String> getUserDB()				{return userdb;}
	public HashMap<String, Date> getBanList()				{return banlist;}
	public HashMap<String, Integer> getLogonTries()			{return logonTries;}
	
	public synchronized ArrayList<String> getBlocked(String usr) {
		return blockLists.get(usr);
	}					
	
	public synchronized void addClient(User u) {
		System.out.println("addingClient: " + u.getUname());
		onlineUsers.add(u);
	}
	
	public synchronized void removeClient(User u) {
		onlineUsers.remove(u);
	}
	
	public synchronized void addLogout(String name) {
		recentLogouts.put(name, new Date());
	}
	
	public synchronized int getNumLogonTries(String usr) {
		Integer tries;
		if( (tries = logonTries.get(usr)) == null )
			return -1;
		
		return tries.intValue();
	}
	
	public synchronized void addTries(String usr) {
		Integer tries = logonTries.get(usr);
		logonTries.put(usr, new Integer(tries.intValue() + 1));
	}
	
	public synchronized void resetTries(String usr) {
		logonTries.put(usr, new Integer(0));
	}
	
	/**
	 * Checks if user u at ip address ipa was banned.
	 * @param u username
	 * @param ipa IP Address from which s/he connected
	 * @return true if banned, false otherwise.
	 */
	public synchronized boolean isBanned(String uname, InetAddress ipa) {
		String key = uname + ipa.toString();
		
		if(banlist.containsKey(key)) {  			/* Username at this ip was banned previously but not necessarily still banned */	
			Date st = banlist.get(key);
			Date now = new Date();
			long diff = now.getTime() - st.getTime();
			
			System.err.println("key: "+key + ", diff:"+diff);
			
			if(diff < ClientListener.BLOCK_TIME)	/* Check if they exceeded their ban time */
				return true;
			
			banlist.remove(key);					/* If they have, remove them from the list */
		}
		return false;
	}
	
	public synchronized void addBannedClient(String uname, InetAddress ipa) {
		System.out.println("inside addban:"+uname + ipa.toString());
		banlist.put(uname + ipa.toString(), new Date());
	}
	
	public void readUsers() {
		userdb = new HashMap<String, String>();
		try {
			BufferedReader br = new BufferedReader(new FileReader("user_pass.txt"));
			String cred;
			
			while( (cred = br.readLine()) != null) {
				String up[] = cred.split(" ");
				userdb.put(up[0], up[1]);
				blockLists.put(up[0], new ArrayList<String>());
				logonTries.put(up[0], new Integer(0));
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
