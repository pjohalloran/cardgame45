/*
 * Created on 27-Jan-2005
 */
package server.network;

import java.net.Socket;

import server.cardgame.CardGame45;
import server.cardgame.Game;

/**
 *  A PlayerHandler45 thread which deals with recieving messages 
 * 	from a player. It deals with sending messages to the player by 
 *  passing them onto the CardGame object
 *
 *  Details about a game is maintained in a CardGame
 *  object, which is referenced by all the threads.
 * 
 * @author Pj O' Halloran
 */
public class PlayerHandler45 extends PlayerHandler {

	//CardGame object shared by all clients
	private CardGame45 cGame45;
	
	/**
	 * Creates a PlayerHandler thread specifically for the game of 45.
	 * 
	 * @param socket The socket through which information is passed to and fro from the client connected to the socket
	 * @param cGame45 The CardGame45 object shared by all players.
	 */
	public PlayerHandler45(Socket socket, CardGame45 cGame45) {
	    super(socket);
		
		this.cGame45 = cGame45;
	}
	
	/**
	 * This is called when the thread is started. When this function finishes,
	 * the thread dies.
	 * 
	 */
	public void run() {
	    super.run();
	    
	    closeClient();
	}
	
	/**
	 * This method is responsible for recieving all communication 
	 * from the client, it only finshes when the client closes up
	 * 
	 */
	public void processPlayer(){
	    super.processPlayer();
	}
	
	/**
	 * This method takes the input just recieved from the player. The input can
	 * be an instruction to eithir:-
	 * <br><p>
	 * (a) Choose a game and Pick the game rules		- e.g  "cGAME=45,cGRULES=10110"<br>
	 * (b) Send the nickname							- e.g. "cName,Harry"
	 * (c) "Ready To Begin" game message				- e.g. "cReady"<br>
	 * (d) Request "How to Order screen" details		- e.g. "cORDER"<br>
	 * (e) Client wants to quit during a game			- e.g. "cQuit"<br>
	 * (f) Indicating number of cards to cut			- e.g. "cCut,3"<br>
	 * (g) Indicate to the server to begin dealing		- e.g. "cDealOut"<br>
	 * (h) Indicate to server that player is robbing	- e.g. "cRobTrump,jok"<br>
	 * (i) Tell the server what card is being dicarded	- e.g. "cDiscard,10s"
	 * 		as a result of robbing<br>
	 * (j) Indicate what card the player is playing		- e.g. "cCard,2d"<br>
	 * (k) Request game and order information			- e.g. "cGInfo"<br>
	 * 
	 * NB All messages sent by the players begin with a 'c'.
	 * 		**(Any messages sent by the server to the players begin with an 's')
	 * </p>
	 * 
	 * @param command The message recieved by the server from the player
	 */
	public void doRequest(String command){

		switch(cGame45.getGameState()){
			//Will only accept certain messages during each game state
			case Game.GAME_STARTUP:{
				if(command.startsWith("cGame")){
					//should never come in here at present
				}
				else if(command.startsWith("cName")){
					String[] name = command.split("\\,");
					//TODO: If any other players already have the same name, make this name slightly different
					cGame45.getPlayerGroup().addPlayer(this.ipAddress, this.portNumber, name[1].trim(), out);
				}
				else if(command.startsWith("cReady")){
					//Set the player to be ready
					cGame45.getPlayerGroup().getPlayer(ipAddress, portNumber).setReadyToPlay(true);
				}
				else if(command.startsWith("cQuit")){
					//TODO: Handle a player quiting mid game
				}
				else if(command.startsWith("cGInfo")){
				    //send back game details and player order info
					cGame45.getPlayerGroup().sendOrderOfPlayers(ipAddress, portNumber);
					cGame45.getPlayerGroup().privateMessage(cGame45.toString(), ipAddress, portNumber);
					//TODO: If the cardGame hasnt been made at this point then send back a null message
				}
				else{
					//its an invalid message for this stage of the game
					cGame45.getPlayerGroup().privateMessage("sInvalid,Start", ipAddress, portNumber);
				}
			}break;
			
			case Game.GAME_ROUND_SETUP:{
				//No messages allowed at this time
				cGame45.getPlayerGroup().privateMessage("sInvalid,Setup", ipAddress, portNumber);
			}break;
			
			case Game.GAME_DEAL:{
				if(command.startsWith("cDealOut")){
					if(cGame45.getCurrentDealer() == cGame45.getPlayerGroup().getPlayerIndex(ipAddress, portNumber))
						cGame45.makeDeal(ipAddress, portNumber);
					else
					    //inform player that its not their turn to deal
						cGame45.getPlayerGroup().privateMessage("sInvalid,Deal", ipAddress, portNumber);
				}
				else if(command.startsWith("cQuit")){
					//TODO: Handle a player quiting mid game
				}
				else{
					//its an invalid message for this stage of the game
					cGame45.getPlayerGroup().privateMessage("sInvalid,NotTime", ipAddress, portNumber);
				}
			}break;
			
			case Game.GAME_CUT:{
				if(command.startsWith("cCut")){
					if(cGame45.getSrvrWaitingOnPlayerIndex() == cGame45.getPlayerGroup().getPlayerIndex(ipAddress, portNumber)){
						String[] cut = command.split("\\,");
						int cardsToCut = Integer.parseInt(cut[1].trim());
						cGame45.setCardsToCut(cardsToCut);
					}
					else{
						//inform player that hes not the cutter
						cGame45.getPlayerGroup().privateMessage("sInvalid,Cut", ipAddress, portNumber);
					}
				}
				else if(command.startsWith("cQuit")){
					//TODO: Handle a player quiting mid game
				}
				else{
					//its an invalid message for this stage of the game
					cGame45.getPlayerGroup().privateMessage("sInvalid,NotTime", ipAddress, portNumber);
				}
			}break;
			
			case Game.GAME_ROB:{
				if(command.startsWith("cCard")){
					if(cGame45.getSrvrWaitingOnPlayerIndex() == cGame45.getPlayerGroup().getPlayerIndex(ipAddress, portNumber)){
						String[] card = command.split("\\,");
						cGame45.storeMove(card[1].trim(), ipAddress, portNumber);
					}
					else{
						//Its not this players turn
						cGame45.getPlayerGroup().privateMessage("sInvalid,Card", ipAddress, portNumber);
					}
				}
				else if(command.startsWith("cRobTrump")){
					boolean robSuccess = false;
					String[] card = command.split("\\,");
					robSuccess = cGame45.requestForRob(card[1].trim(), ipAddress, portNumber);
				}
				else if(command.startsWith("cQuit")){
					//TODO: Handle a player quiting mid game
				}
				else{
					//its an invalid message for this stage of the game
					cGame45.getPlayerGroup().privateMessage("sInvalid,Rob", ipAddress, portNumber);
				}
			}break;
			
			case Game.GAME_PLAY:{
				if(command.startsWith("cCard")){
					if(cGame45.getSrvrWaitingOnPlayerIndex() == cGame45.getPlayerGroup().getPlayerIndex(ipAddress, portNumber)){
						String[] card = command.split("\\,");
						cGame45.storeMove(card[1].trim(), ipAddress, portNumber);
					}
					else{
						//Its not this players turn
						cGame45.getPlayerGroup().privateMessage("sInvalid,Card", ipAddress, portNumber);
					}
				}
				else if(command.startsWith("cQuit")){
					//TODO: Handle a player quiting mid game
				}
				else{
					//its an invalid message for this stage of the game
					cGame45.getPlayerGroup().privateMessage("sInvalid,NotTime", ipAddress, portNumber);
				}
			}break;
			
			case Game.GAME_ROUND_END:{
				//No messages allowed at this time
				cGame45.getPlayerGroup().privateMessage("sInvalid,End", ipAddress, portNumber);
			}break;
			
			default:{
			    System.out.println("Unknown message recieved from the Client(IP="+ipAddress+",PORT="+portNumber+"): "+command);
			}break;
		}//end switch
	}
	
	/**
	 * This method closes off the player when they have finished playing
	 *  
	 */
	public void closeClient() {
		cGame45.getPlayerGroup().removePlayer(ipAddress, portNumber);
		
		super.closeClient();
	}
}