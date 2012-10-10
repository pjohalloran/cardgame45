/*
 * Created on 17-Mar-2005
 */
package server.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * This class is a thread which handles messages to and from the client
 * 
 * @author Pj O' Halloran
 */
public abstract class PlayerHandler extends Thread {

	//Server-side IP address of the client
	protected String ipAddress;
	//Server-side port number of the client
	protected int portNumber;
	//The Servers endpoint of communication with the socket
	protected Socket clientSocket;
	//The input stream from the remote client
	protected BufferedReader in;
	//The output stream to the remote client
	protected PrintWriter out;
    
    /**
     * Creates a Playerhandler thread for a client.
     */
    public PlayerHandler(Socket socket) {
		//Constructing Objects required for Network communication with the Client 
		clientSocket = socket;
		ipAddress = clientSocket.getInetAddress().getHostAddress();
		portNumber = clientSocket.getPort();
    }

    /**
     * Called when the thread starts. Finishes when the thread dies.
     *
     */
    public void run() {
		boolean autoFlushStream = true;
		
		try {
			// Get the Input stream from the socket and wrap it in a Buffered reader
			in  = new BufferedReader( new InputStreamReader( clientSocket.getInputStream() ) );
			//Get the Output stream from the socket
			out = new PrintWriter( clientSocket.getOutputStream(), autoFlushStream );
			
			//Tells the Client to wait to be added to the game
			out.println("sWait");
			
			//processPlayer will return only when the player is finished playing
			processPlayer();
	  }
	  catch(IOException ioe) {
	     System.out.println("Some I/O error occured..." + ioe.getMessage());
	  }
    }
    
    /**
     * Reads in messages from the client. When a message is recieved it is processed in subclasses of PlayerHandler
     *
     */
    public void processPlayer() {
	    String inputBuffer;
	    boolean finishedProcessing = false;
	    int currentClientId;
	    
	    try {
	    	while (!finishedProcessing) {
	    		//in.readLine() - reads in a line of characters at a time from the input stream
	    		if((inputBuffer = in.readLine()) == null)
	    			//InputStream has closed, the player has finshed communicating and therefore playing the game
	    			finishedProcessing = true;
	    		else {
	    			//Else a message has been recieved, so deal with it
	    			System.out.println("Message from Client: (" 
	    					+ ipAddress + ", " + portNumber + ") to Server : " + inputBuffer);
	    			
	    			//calls doRequest() in subclasses
	    			doRequest(inputBuffer);
	    		}
	    	}//end while
	    }
	    catch(IOException e){
	        System.out.println("The stream from Client to Server has closed");  
	    }
    }
    
    /**
     * 
     *
     */
    public void doRequest(String command){
        //Overridden in subclasses
    }
    
    /**
     * Closes up the client when the game is over
     * 
     */
    public void closeClient(){
        
		try {
	  		in.close();
	  		out.close();
	  		clientSocket.close();
	  	}
	  	catch(IOException ioe) {
	    	System.err.println("Some I/O error occured when closing:("+ ipAddress + ", " + portNumber +") thread:/n" 
	    			+ ioe.getMessage());
	  	}
  		System.out.println("Player (" + ipAddress + ", " + portNumber + ") connection closed\n");
    }
}