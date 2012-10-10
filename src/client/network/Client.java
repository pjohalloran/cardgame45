/*
 * Created on 21-Feb-2005
 */
package client.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import client.graphics.PlayScreen;

/**
 * @author Pj O' Halloran
 */
public class Client {

    //The default network identifier of the Server
    private static final int DEFAULT_SRVR_PORT = 5555;
    private static final String DEFAULT_SRVR_IP = "localhost";
    
    //Servers ip address
    private String serverIP;
    //Servers port number
    private int serverPort;
    //True if you wish the out stream to be automatically flushed after each call to println
    private boolean autoFlushStream;
    //The clients endpoint in communication with the Server
    private Socket socket;
    //The outputstream for sending messages to the Server
    private PrintWriter out;
    //The clients nickname picked in the ServerMenu GUI
    private String nickName;
    //True if i am debugging, prints out messages to the command line
    private boolean debug;
    
    //The screen which updates the player on the current status of the game while waiting for it to start
    private GameWaitScreen waitScreen;
    //The actual game panel the player plays on and recieves feedback from
    private PlayScreen mainGame;
    
    /**
     * Constructs a Client object
     *
     */
    public Client(boolean debugMode){
        debug = debugMode;
        serverIP = "";
        nickName = "";
        serverPort = 0;
        mainGame = new PlayScreen(this, debug);
        waitScreen = new GameWaitScreen(this, debug, mainGame);
        autoFlushStream = true;
        
    	if(debug){
    	    mainGame.display();
    	}
    }
    
    /**
     * Connects to the server
     * (Use Case 3)
     *
     */
    public void connect(){
	    try {
	    	socket = new Socket(serverIP, serverPort);
	    	
	    	BufferedReader in  = new BufferedReader( new InputStreamReader( socket.getInputStream() ) );
	    	out = new PrintWriter( socket.getOutputStream(), autoFlushStream );
	    	
	    	new ServerWatcher(in, this, mainGame, waitScreen).start();
	    }
	    catch(IOException ioe) {
	    	System.out.println("IO Exception in Menu.makeContact()" + ioe.toString());
	    }
    }
    
    /**
     * Sends a message to the server
     * 
     * @param message The message you wish to send to the server
     */
    public void sendMessage(String message){
        synchronized(out){
	        if(debug)
	            System.out.println(message);
	        else
	            out.println(message);
        }
    }
    
    /**
     * Stores the ip address and port number of the Server entered
     * by the player
     * 
     * @param ip The ip address of the server
     * @param port The port number of the server
     */
    public void storeFields(String name, String ip, int port){
        if( (ip.equals("")) && (port == -1) ){
            //Set to be default values
            serverIP = Client.DEFAULT_SRVR_IP;
            serverPort = Client.DEFAULT_SRVR_PORT;
        }
        else{
            //could be an ip address or a string "localhost" to connect locally
            serverIP = ip;
            serverPort = port;
        }
        nickName = name;
    }
    
    /**
     * Returns the nick name of the client
     * 
     * @return
     */
    public String getNickName(){
        return this.nickName;
    }
    
    /**
     * Closes up the client connection to the Server
     * 
     */
    public void closeClient(){
	    try {
	    	out.close();
	    	socket.close();
	    }
	    catch(IOException e){
	    	System.out.println("Exception in Client.closeClient(), " + e.toString());
	    }
    }
}