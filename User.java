import java.util.ArrayList;
import java.util.Date;

public class User {
	private String uname;
	private ClientListener listener;
	private ClientSender sender;
	private Date start;
	private ArrayList<String> blocked;
	private Date active;
	
	public User() {
		start = new Date();
		active = new Date();
	}
	
	public String getUname()			{return uname;}
	public Date getStart()				{return start;}
	public Date getActive()				{return active;}
	
	/**
	 * Resets the active variable to indicate that the user is still active.
	 */
	public void setActive()				{active = new Date();}
	
	public void setBlockedList(ArrayList<String> blocked) {
		this.blocked = blocked;
	}
	
	/**
	 * 
	 * @param usr Name of user
	 * @return true if usr is in blocked list of the client
	 */
	public boolean isBlocked(String usr) {
		return blocked.contains(usr);
	}
	
	/**
	 * Adds a username to the blocked list
	 * @param usr The name of the user to be blocked
	 * @return returns true if successfully blocked user, false otherwise.
	 */
	public boolean addBlock(String usr) {
		if(blocked.contains(usr))
			return false;
		
		blocked.add(usr);
		return true;
	}
	
	/**
	 * Removes the username from the blocked list
	 * @param usr The name of the user to be blocked
	 * @return returns true of successfully removed blocked user, false otherwise.
	 */
	public boolean removeBlock(String usr) {
		if(blocked.contains(usr)) {
			blocked.remove(usr);
			return true;
		}
		
		return false;
	}
	
	/**
	 * Add the listeners and senders for the client connections
	 * @param listener ClientListner object run as a thread to listen for incoming messages for the client
	 * @param sender ClientSender object run as a thread to send messages to the server for the client
	 */
	public void addListenerSender(ClientListener listener, ClientSender sender) {
		this.listener = listener;
		this.sender = sender;
	}
	
	public void setUname(String uname) {
		this.uname = uname;
	}
	
	/**
	 * A direct message sent by @param from. Different from send()
	 * @param from User who is sending the message
	 * @param msg The message sent
	 */
	public void peerMsg(User from, String msg) {
		sender.send(from.getUname() + ": " + msg);
	}
	
	/**
	 * Send a response message to the client.
	 * @param msg
	 */
	public void send(String msg) {
		sender.send(msg);
	}
}
