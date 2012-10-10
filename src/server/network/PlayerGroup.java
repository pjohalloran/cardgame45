/*
 * Created on 27-Jan-2005
 */
package server.network;

import java.io.PrintWriter;

/**
 * Maintains info about all the current Players.
 *
 * A single PlayerGroup object is used by all the 
 * PlayerHandler threads, so methods which
 * manipulate the Array of Players must be synchronised
 * so that concurrent updates are prevented.
 * 
 * @author Pj O' Halloran
 */
public class PlayerGroup {
	//Array of players, an element is eithir "null" or "some_Player"
	private Player[] playersArr;
	//Number of players currently in the Group(NB This number is not always equal to playersArr.length)
	private int numOfPlayers;
	
	public PlayerGroup(){
		playersArr = new Player[4];
		for(int i=0; i<4; i++){
			playersArr[i] = null;
		}
		numOfPlayers = 0;
	}
		
	/**
	 * The nuber of players in the group. 
	 * NB 	This number is not always equal to the length of the group so its
	 * 		useful for finding out when only 1 person is in the group
	 * 
	 * @return The number of players 
	 */
	synchronized public int getNumOfPlayers(){
		return numOfPlayers;
	}
	
	/**
	 * Gets the Player object at the specified index
	 * <br>
	 * <B>NB</B> The CardGame gets access to the Player Object Class
	 * and its instance variables through this method so as to
	 * avoid thread problems
	 * 
	 * @param index The index in the PlayerGroup of the player
	 * @return The player you want access to
	 */
	synchronized public Player getPlayer(int index){
		return playersArr[index];
	}
	
	/**
	 * Returns a player whose ip address and port number match those passed in
	 * 
	 * @param ip The ip address of the player you want access to
	 * @param port The port number of the player you want access to
	 * @return
	 */
	synchronized public Player getPlayer(String ip, int port){
		for(int index=0; index<playersArr.length; index++) {
			if( (playersArr[index] != null) && (playersArr[index].matches(ip, port)) )
				return playersArr[index];
		}
		
		return null;
	}
	
	/**
	 * Adds a Player into the PlayerGroup in the first available position.
	 * The Player will be notified of this by a message.
	 * 
	 * @param ip The players server side ip adddress
	 * @param portN The server side port number of the player
	 * @param name The players nickname during the game
	 * @param out The outstream of the player
	 * 
	 * @return True if the player was added and false otherwise
	 */
	synchronized public boolean addPlayer(String ip, int portN, String name, PrintWriter out){
		for(int i=0; i<playersArr.length; i++) {
			if(playersArr[i] == null) {
				numOfPlayers++;
				playersArr[i] = new Player(ip, portN, name, out);
				playersArr[i].setPosInGroup(i);
				privateMessage("sInGame", ip, portN);
				return true;
			}
		}
		privateMessage("sNotInGame", ip, portN);
		return false;
	}
	
	/**
	 * Removes a player from the group
	 * 
	 * @param player The player to be removed
	 * @return True if the player existed in the group and was removed from it
	 */
	synchronized public boolean removePlayer(Player player) {
		int pIndex = player.getPosInGroup();
		
		if(playersArr[pIndex] == player){
			numOfPlayers--;
			playersArr[pIndex] = null;
			//TODO: Will i include this?
			//Server.clientLeft();//reduces the num of clients the server has by 1
			return true;
		}
		return false;
	}
	
	/**
	 * Removes a player from the group with its unique key
	 * 
	 * @param ip The nickname of the player to be removed
	 * @return True if the player existed in the group and was removed from it
	 */
	synchronized public boolean removePlayer(String ip, int port) {
		for(int i=0; i<playersArr.length; i++) {
			if( (playersArr[i] != null) && (playersArr[i].matches(ip, port)) ) {
				numOfPlayers--;
				playersArr[i] = null;
				
				//TODO: Will i include this?
				//Server.clientLeft();//reduces the num of clients the server has by 1
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Gets the index of the player in the PlayerGroup
	 * 
	 * @param ip IP address of the player
	 * @param port port number of the player
	 * @return Number between 0 and 3 if the player was found and -1 otherwise
	 */
	synchronized public int getPlayerIndex(String ip, int port) {
		for(int i=0; i<playersArr.length; i++) {
			if( (playersArr[i] != null) && (playersArr[i].matches(ip, port)) )
				return i;
		}
		return -1;
	}
	
	/**
	 * Returns true if a player occupies an index.
	 * 
	 * @param pos The position in the PlayerGroup object you wish to query
	 * @return True if there is a player at the pos in this group 
	 */
	synchronized public boolean isPlayerAt(int pos){
		if(playersArr[pos] != null)
			return true;
		
		return false;
	}
	
	/**
	 * Players are ready to play if they are in the PlayerGroup and
	 * each player is ready to play
	 * 
	 * @return True if all players are ready to play
	 */
	synchronized public boolean arePlayersReadyToPlay(){
	    
	    if( (this.numOfPlayers == 0) || (numOfPlayers <= 1) )
	        //if there are no players added yet OR if there is only 1 player added yet
	        return false;
	    
		for(int index=0; index<this.playersArr.length; index++){
			if((playersArr[index] != null) && (playersArr[index].isReadyToPlay() == false))
				return false;
		}
		
		return true;
	}
	
	/**
	 * Finds the index of the next available player
	 * 
	 * @param fromIndex The index of the player you wish to find the next player from
	 * @param left If true, the method searches for the next available player left-ways along the PlayerGroup. If it is false the group is searched for the next available player right-ways through the group
	 * 
	 * @return The index of the next closest player 
	 */
	synchronized public int findNextAvailablePlayer(int fromIndex, boolean left){
		int pos = -1, nextIndex = fromIndex;
		//If left is true, direction must be a decrement, else it must be a increment
		int direction = (left==true) ? -1 : 1;
		boolean found = false;
		
		while(found == false){
			//If nextIndex is at the end of the array, then reset it back to 0,
			// else go left/right by 1
		    if(left == true)
				nextIndex = (nextIndex==0) ? playersArr.length-1 : nextIndex-1;
		    else
				nextIndex = (nextIndex==playersArr.length-1) ? 0 : nextIndex+1;
		    
			if(playersArr[nextIndex] != null){
				pos = nextIndex;
				found = true;
			}
			else{
			    //skip if this position is null
			    continue;
			}
		}
		return pos;
	}
	
	/**
	 * Sends a private message to a player identified by its unique
	 * key i.e. IP address and port number
	 * 
	 * @param msg The message to be sent to the client
	 * @param ip The ip address of the player
	 * @param port The port number of the player
	 */
	synchronized public void privateMessage(String msg, String ip, int port) {
		for(int i=0; i<playersArr.length; i++) {
			if( (playersArr[i] != null) && (playersArr[i].matches(ip, port)) )
				playersArr[i].sendMessage(msg);
		}
	}
	
	/**
	 * Sends a message to every player
	 * 
	 * @param msg The message to be sent to all
	 */
	synchronized public void broadcastMessage(String msg){
		for(int i=0; i<playersArr.length; i++) {
			if(playersArr[i] != null)
				playersArr[i].sendMessage(msg);
		}
	}
	
	/**
	 * Sends a message to every players except the player uniquely 
	 * identified by the IP address and port number
	 * 
	 * @param msg The message to be sent
	 * @param ip The ip address of the player NOT to be sent the message
	 * @param port The port number of the player NOT to be sent the message
	 */
	synchronized public void broadcastExOriginal(String msg, String ip, int port) {
		for(int i=0; i<playersArr.length; i++) {
			if( (playersArr[i] != null) && !( playersArr[i].matches(ip, port) ) )
				playersArr[i].sendMessage(msg);
		}
	}
	
	/**
	 * <p>
	 * This function must pass back a String which tells the client how to order 
	 * the other clients on the game screen as the client/player him/herself
	 * is always located on the bottom of the screen
	 * </p>
	 * EXAMPLE 
	 * <br>		1)playersArr = { John, null, Pat, Joe }
	 * <br>
	 * 			2)Player "Pat" requests order infomation 
	 * 			  of the clients on his remote game screen
	 * <br>
	 * 			3)This is the layout of players that should 
	 * 			  be on his screen
	 * 							John(top_screen_player)<br>
	 * 
	 * 			null(Left_Screen_P)		Joe(Right_Screen_Player)<br>
	 * 
	 * 							*Pat
	 * <br>
	 * 			4)This function must pass back a String which
	 * 			  tells the client how to order the other clients
	 * 			  on the game screen as (Pat) the client/player him/herself
	 * 			  is always located on the bottom of the screen
	 * <br>
	 * 			5)String = "ORDER,Joe,John,no_one"
	 * 							or generally...
	 * 					   "ORDER,right_Screen_player,top_screen_player,left_screen_player"
	 * <br>
	 * @param ip The Ip Address of the player you wish to get the order for
	 * @param port The port number of the player you wish to get the order for
	 * 
	 * @return The String which tells the client how to order its screen(NB (5) )
	 */
	public void sendOrderOfPlayers(String ip, int port){
		//Position of the player who wants the order information
		int positionOfPlayer = 0;
		String[] tempRight = null, tempLeft = null, playerNamesArr = new String[playersArr.length];
		//String which will be sent to the remote player instructing it how to lay out its screen
		String buffer = "sORDER";
		
		//Step 1: Find the position of the player who requests the order info
		//		  and store all names in an array
		for(int playerIndex=0; playerIndex<playersArr.length; playerIndex++) {
			if( (playersArr[playerIndex] != null) && (playersArr[playerIndex].matches(ip, port)) ) {
				//This is the player who is requesting the Order info from the server
				//Dont send this players name, just record their position
				playerNamesArr[playerIndex] = "";
				positionOfPlayer = playerIndex;
			}
			else if( (playersArr[playerIndex] == null) ){
				//ie If this space in the group is currently empty
				playerNamesArr[playerIndex] = ",no_one";
			}
			else
				//There is a connected player in this place
				playerNamesArr[playerIndex] = "," + playersArr[playerIndex].getNickName();
		}
		
		//Step 2: Depending on if the client is
		//			a)The Last position
		//			b)The first position
		//				or
		//			c)Somewhere in the middle of the group
		//		  construct the String to be sent to the remote client
		
		//2 (a)
		if(positionOfPlayer == playersArr.length-1) {
			tempLeft = new String[playersArr.length-1];
			for(int leftIndex=0; leftIndex<tempLeft.length; leftIndex++)
				tempLeft[leftIndex] = playerNamesArr[leftIndex];
			
			buffer = buildOrderString(buffer, tempLeft);
		}
		//2 (b)
		else if (positionOfPlayer == 0) {
			tempRight = new String[playersArr.length-1];
			for(int rightIndex=0; rightIndex<tempRight.length; rightIndex++)
				tempRight[rightIndex] = playerNamesArr[rightIndex+1];
			
			buffer = buildOrderString(buffer, tempRight);
		}
		//2 (c)
		else {
			//Array of strings right of the requesting client
			tempRight = new String[playersArr.length - positionOfPlayer - 1];
			//Array of strings left of the requesting client
			tempLeft = new String[positionOfPlayer];
			
			for( int i=0; i<tempLeft.length; i++)
				tempLeft[i] = playerNamesArr[i];
			
			int rStartIndex = positionOfPlayer+1;
			for( int i=0; i<tempRight.length; rStartIndex++, i++)
				tempRight[i] = playerNamesArr[rStartIndex];
			
			//order of these calls are very important
			buffer = buildOrderString(buffer, tempRight);
			buffer = buildOrderString(buffer, tempLeft);
		}
		privateMessage(buffer, ip, port);
	}
	
	/**
	 * Private function which builds the Order string
	 */
	private String buildOrderString(String str, String[] arr){
		for(int i=0; i<arr.length; i++)
			str += arr[i];
		return str;
	}
}