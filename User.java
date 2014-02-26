import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;

public class User {
	private String uname;
	private ClientListener listener;
	private ClientSender sender;
	private Date start;
	private ArrayList<String> blocked;
	
	//Message Queues
	private Queue<String> sendQ;	/* Messages that the client wants to send */
	private Queue<String> rcvQ;		/* Messages that were intended for the client */
	
	public User() {
		sendQ = new LinkedList<String>();
		rcvQ = new LinkedList<String>();
		start = new Date();
	}
	
	public Queue<String> getRcvQ() 		{return rcvQ;}
	public String getUname()			{return uname;}
	public Date getStart()				{return start;}
	
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
	
	public void send(String msg) {
		sender.send(msg);
	}
	
	/*
	public String rcv() {
		return listener.getLine();
	}
	
	public void addSendQ(String msg) {
		sendQ.add(msg);
	}
	
	public void addRcvQ(String msg) {
		rcvQ.add(msg);
	}
	*/
}
