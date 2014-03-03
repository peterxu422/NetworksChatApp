a.
My Chat Server consists of 7 classes and their descriptions are as follows:

ChatServer - The main program that is run to start the server. It continuously waits to accept a connection with a client.

ServerSender - This class handles all data structures that need to be shared and needed between the clients such as the list of online users, the database of users, the block list of each user, the queue of messages sent offline for each user, the ban list, list of recently logged out users, and the number of logon attempts by each user.

ClientListener - This is a thread created on the server side for each connected client. It listens for commands being sent by the client and processes the commands. All of the time constants specified in the assignment are declared in this class. All of the commands specified work as specified in the assignment. Authentication of the client also takes place in this class. Commands sent from the user are parsed using regular expressions. Inactivity of the user is checked using an embedded private class called ActivityChecker, which searches through the list of online users and determines when was the last time they received a command from the user. This is performed on that online client every 30 seconds so as not to waste CPU resources performing this on every loop. Please see the class and function for more details.

ClientSender - This is a thread created on the server side for each connected client. It is responsible for sending messages from the server to the client.

User - This class contains details about a connected user such as the username, his/her block list, when they logged in, and when was the last time they sent a command.

Client - This is the program that is run to connect to the server. Once connected, it listens for messages coming from the server.

Sender - This is a thread spawned for each client on the client side. It is responsible for reading the input from the client and sending it to the server.

Additional Comments:
	- The client should properly log out of the chat server by using the 'logout' command, not using Ctrl+C. Doing so can cause issues for other connected clients when running commands.
	- If you do not see the command prompt '>', just hit Enter a few times.
	- 'whoelse' and 'wholasthr' commands might have some strange formatting issues (such as the usernames appearing on the same line or being separated by one or two \n characers) when you're running the server and client on the same machine. I suspect it may have to do with how the buffer is being flushed through the socket stream which may differ between flushing it to another machine and to the same machine. This issue does not occur if the client is running on a different machine.

b. Wrote code on Eclipse in Windows and tested on CLIC Machine.

c.
Type 'make' in the prompt.
Run the ChatServer program 'java ChatServer <port number>'
In another terminal run the Client Program 'java Client <ip address or host name> <port number>'
Type Chat server commands.

d. The commands are invoked as specified in the Project description

e. None.
