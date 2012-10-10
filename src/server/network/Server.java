/*
 * Created on 27-Jan-2005
 */
package server.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.channels.IllegalBlockingModeException;

import server.cardgame.CardGame45;

/**
 *  The top-level server and the servers MAIN thread, 
 *  which waits for client connections and creates 
 *  PlayerHandler threads to handle them.
 *
 *  Details about each client is maintained in a PlayerGroup object
 *  This PlayerGroup object is enclosed in the CardGame object 
 *  which is referenced by each thread.
 * 
 * @author Pj O' Halloran
 */
public class Server {

	//The port number of the Server
	private static final int PORT = 5555;
	//The time between accepting players that the server allows for
	private static final int TIMEOUT = 60000;
	
	//Reference to the CardGame Object that all threads share, it won't be instanciated until 
	//the first client has connected and chosen a game
	private CardGame45 cGame45;
	//This PlayerGroup refers to the same PlayerGroup contained in the CardGame object
	private PlayerGroup startupPG;
	//Num of clients currently connected
	private static int numOfClients;

	/**
	 *  Initial construction of the server
	 *
	 */
	  private Server() {
	  	cGame45 = null;
	  	this.startupPG = new PlayerGroup();
	  	numOfClients = 0;
	  }
	  
	  /**
	   *  The main method, when the program is executed this method will kick off
	   *  the server program.
	   *
	   */
	  public static void main(String args[]) {
		Server svr = new Server();
		svr.waitForAndConnectClients();
	  }

	  /**
	   *  Waits for a client connection, creates a thread for it and then repeats the process
	   *
	   */
	  private void waitForAndConnectClients() {
	  	/*
	  	 * A timeout is on the accept() method, if a client connects and then a minute passes
	  	 * without another client connecting, then the server should:-
	  	 * 
	  	 * (a) if there is 2 or more connected clients, then the game should proceed
	  	 * 
	  	 * (b) if there is only 1 connected clients, then this client should be notified 
	  	 * that a timeout has occured and they are being disconnected as a result
	  	 */
	  	boolean afterFirstPlayer;
	  	
	    try {
	        ServerSocket serverSocket = new ServerSocket(PORT);
	        Socket clientSocket = new Socket();

	        acceptConnections(serverSocket, clientSocket);
	     }
	     catch(SocketTimeoutException ste){
	     	System.out.println("SocketTimeoutException occured in Server");
	     	//TODO: When a timout exception occurs then you must deal with it
	     	boolean exitLoop = false;
	     	
//	     	if(this.startupPG.getNumOfPlayers() >= 2){
	     	if(Server.numOfClients >= 2) {
	     		//TODO: Notify all remote players game is beginning
	     		this.startupPG.broadcastMessage("sBegin");
	     		
	     		while(exitLoop == false){
	     			if(this.startupPG.arePlayersReadyToPlay() == true){ 
	     				cGame45.beginRound();//starts off the cardgame!!
	     				exitLoop = true;
	     			}
	     			else{
	     				//Wait for the players to come back and tell the server they are ready to start
	     				try{
	     					Thread.sleep(1000);
	     				}
	     				catch(InterruptedException ie){
	     					System.err.println("Server interupted while waiting on all remote players to set up their screens");
	     				}
	     			}
	     		}	//end while
	     	}
	     	else{
	     	    System.out.println("Timeout exception occured and there was not enough players to begin playing");
	     	}
	     }
	     catch(IOException ioe){
		     System.out.println("IO Exception occured in Server");
	     }
	     catch(SecurityException sc){
		    System.out.println("SecurityExceptionException occured in Server");
	     }
	     catch(IllegalBlockingModeException ibme){
		    System.out.println("IllegalBlockingModeException occured in Server");
	     }
	  }
	  
	  /**
	   * 
	   * @param serverSocket
	   * @param clientSocket
	   * @throws IOException
	   * @throws SocketTimeoutException
	   * @throws SecurityException
	   * @throws IllegalBlockingModeException
	   */
	  private void acceptConnections(ServerSocket serverSocket, Socket clientSocket) 
	  		throws IOException, SocketTimeoutException, SecurityException, IllegalBlockingModeException {
	  		
	        while (startupPG.getNumOfPlayers() < 4) {
		     	System.out.println("Server, Waiting for a client...");
	        	
		  		//NB This is a blocking network communication method
		  		clientSocket = serverSocket.accept();
		  		numOfClients++;
		  		
		  		if(numOfClients == 1){
			  		//Sets the server socket to have a 60 second timeout once the first client has joined 
			  		serverSocket.setSoTimeout(TIMEOUT);
			  		//if its the first client have it set up the game and its rules
			  		setUpGame(clientSocket);
		  		}
		  		else
	          		new PlayerHandler45(clientSocket, cGame45).start();
	        }	//end while
	  }
	  
	  /**
	   * 
	   * @param clientSocket
	   */
	  private void setUpGame(Socket clientSocket){
	  	String inBuf;
	  	boolean rulesRecievedAndGameSetUp = false;
	  	
	  	try{
			BufferedReader in  = new BufferedReader(new InputStreamReader(clientSocket.getInputStream() ));
			PrintWriter out = new PrintWriter( clientSocket.getOutputStream(), true );
			
			//ask the first joined client to pick the game rules
		  	out.println("sGRules");
		  	
	    	while (rulesRecievedAndGameSetUp == false) {
	    		if((inBuf = in.readLine() ) == null)
	    			break;
	    		
	    		System.out.println("Message from First Client:"+inBuf);
				if(inBuf.startsWith("cGAME")) {
					//If the message starts with "cGame" then set up the game object and its default rules
					createCardGame(inBuf, clientSocket);
					rulesRecievedAndGameSetUp = true;
				}
				else
					rulesRecievedAndGameSetUp = false;
	    	}
	  	}
	  	catch(IOException ioe){
	     	System.out.println("IOException occured while the first client was setting up the game");
	  	}
	  }
	  
		/**
		 * This function instanciates the CardGame object when the first player
		 * joins the game. The first player picks the game and the game rules.
		 * This information is recieved by the server and is used to make the game.
		 * 
		 * (a) Choose a game and Pick the game rules		- e.g  "cGAME=45,cGRULES=101101"<br>
		 */
		private void createCardGame(String setupDetails, Socket clientSocket) {
			//The id of the game to be created
			int gameIDNumber = 0;
			String temp;
			boolean[] options;
			String[] gameDetails, gameID, gameOptions;
		    
			//This should be split up into 2 arrays, 
		    // 1 for the type of game to make and 2 for the game rules
			gameDetails = setupDetails.split("\\,");
			gameID = gameDetails[0].split("\\=");
			gameIDNumber = Integer.parseInt(gameID[1]);
			
			gameOptions = gameDetails[1].split("\\=");
			options = new boolean[gameOptions[1].length()];
			
			//if the default rules are picked
			if(gameOptions[1].equals("default")){
				for(int i=0; i<options.length; i++){
					if(i == 1)
						options[i] = false;
					options[i] = true;
				}
			}
			else{
				for(int index=0; index<options.length; index++){
					if(gameOptions[1].charAt(index) == '0')
						options[index] = false;
					else
						options[index] = true;
				}
			}
			
			cGame45 = new CardGame45(options, startupPG);
      		new PlayerHandler45(clientSocket, cGame45).start();
			
//			switch(gameIDNumber){
//				case 41:{
//					CardGame41 cGame41 = new CardGame41(options, startupPG);
//	          		new PlayerHandler45(clientSocket, cGame41, 41).start();
//	          		cGame = cGame41;
//				}break;
//				case 45:{
//					CardGame45 cGame45 = new CardGame45(options, startupPG);
//	          		new PlayerHandler45(clientSocket, cGame45, 45).start();
//	          		cGame = cGame45;
//				}break;
////					case 110:{
////						//Not implementing this at the moment. I'm concentrating on 45 and 41
////						//cGame = new CardGame110();
////					}break;
//				default:{
//					System.err.println("Error: No Game was created");
//				}break;
//			}
		}
			
		/**
		 * Called by removePlayer in PlayerGroup
		 *
		 */
		public static void clientLeft(){
			numOfClients--;
		}
}