/*
 * Created on 27-Jan-2005
 */
package server.network;

import java.io.PrintWriter;

import server.cardgame.Card;

/**
 * This class stores the Server side information for the player.
 * 
 * @author Pj O' Halloran
 */
public class Player {
	private static final int CARDS_IN_HAND = 5;
	
	//Players Server side IP address
	private String ipAddress;
	//Players Server side Port Number
	private int portNumber;
	//Players nickname
	private String nickName;
	//Out stream connection to this Player
	private PrintWriter outStream;
	//Players hand of cards
	private Card[] hand;
	//The players current score in the game
	private int currScore;
	//The players position in the PlayerGroup, unique to each player
	private int posInGroup;
	//Indicates if the player is ready to start the game on the client side
	private boolean readyToPlay;
	
	/**
	 * Creates a blank player
	 *
	 */
	public Player(){
		ipAddress = null;
		portNumber = -1;
		nickName = null;
		outStream = null;
		currScore = 0;
		posInGroup = -1;
		readyToPlay = false;
	}
	
	/**
	 * Creates a player
	 * 
	 * @param ip The Ip address of the player
	 * @param port The port number of the player
	 * @param name The nickname of the player
	 * @param out The outstream belonging to the player which is used to communicate messages to this player at their remote location
	 */
	public Player(String ip, int port, String name, PrintWriter out){
		ipAddress = ip;
		portNumber = port;
		nickName = name;
		outStream = out;
		hand = new Card[CARDS_IN_HAND];
		for(int i=0; i<CARDS_IN_HAND; i++)
			hand[i] = null;
		
		posInGroup = -1;
		currScore = 0;
		readyToPlay = false;
	}
	
	/**
	 * Indicates if the remote player is ready to play
	 *  
	 * @return True if the player is ready to begin play at the client side
	 */
	public boolean isReadyToPlay(){
		return this.readyToPlay;
	}
	
	/**
	 * When a "cReady" message is recieved from the remote
	 * player this value is set to true
	 * 
	 * @param ready
	 */
	public void setReadyToPlay(boolean ready){
		this.readyToPlay = ready;
	}
	
	/**
	 * Gets the nickname of the player
	 * 
	 * @return The nickname of the player
	 */
	public String getNickName(){
		return nickName;
	}
	
	/**
	 * @return The IP address of the player
	 * 
	 */
	public String getIPAddress(){
		return ipAddress;
	}
	
	/**
	 * @return The port number of the player
	 */
	public int getPortNumber(){
		return portNumber;
	}
	
	/**
	 * @return The players current score
	 */
	public int getCurrScore(){
		return currScore;
	}
	
	/**
	 * Adds to the players current score
	 * 
	 * @param increase The amount the score is increasing by
	 */
	public void addToCurrScore(int increase){
		currScore += increase;
	}
	
	/**
	 * Gets the players hand
	 * 
	 * @return The players hand of cards
	 */
	public Card[] getHand(){
		return hand;
	}
	
	/**
	 * Sets the players hand of cards
	 * 
	 * @param arr The new hand of cards
	 */
	public void setHand(Card[] arr){
		hand = arr;
	}
	
	/**
	 * Gets a players card.
	 * 
	 * @param pos The position of the card you which to obtain
	 * @return The Card
	 */
	public Card getCard(int pos){
		return hand[pos];
	}
	
	/**
	 * Sets a card in the persons hand to a new card
	 * 
	 * @param replacement The replacement card
	 * @param pos The position to place the card in the array of cards
	 */
	public void setCard(Card replacement, int pos){
		hand[pos] = replacement;
	}
	
	/**
	 * Gets this players position in the PlayerGroup array
	 * 
	 * @return The position in the array
	 */
	public int getPosInGroup(){
		return posInGroup;
	}
	
	/**
	 * Sets the position of the player int the group array
	 * 
	 * @param pos The position the player will be in the array
	 */
	public void setPosInGroup(int pos){
		posInGroup = pos;
	}
	
	/**
	 * Returns true if the IP address and port number passed in
	 * are the same as "this" Players information.
	 * 
	 * @param address: 	The clients IP address
	 * 
	 * @param pNum:		The clients port number
	 */
	  public boolean matches(String address, int pNum){ 
	  	if (ipAddress.equals(address) && (portNumber == pNum))
	      return true;
	  	
	    return false;
	  }
	
	/**
	 * Sends a message from the Server to this player
	 * 
	 * @param msg The message to be sent to this client
	 */
	public void sendMessage(String msg){
		outStream.println(msg);
	}
	
	/**
	 * String representation of this player
	 */
	public String toString(){
		return "Name of Player: " + nickName 
					+ "/nIP Address: " + ipAddress 
						+ "/nPort Number: " + portNumber;
	}
}