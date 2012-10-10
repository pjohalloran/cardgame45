/*
 * Created on 08-Feb-2005
 */
package server.cardgame;

import server.network.PlayerGroup;


/**
 * @author Pj O' Halloran
 */
public class CardGame45 extends CardGame {

	private static final int GAME_WIN = 45;
	private static final int SCORE_FOR_TRICK_WIN = 5;
	private static final int BEST_TRUMP_BONUS = 5;
	
	/**
	 * Creates a CardGame45 object
	 * 
	 * @param rules The rules required for this game of 45
	 * @param initialPG The playergroup created by the Server class
	 */
	public CardGame45(boolean[] rules, PlayerGroup pg) {
		super(rules, pg);
	}

	/**
	 * Calls begin round in CardGame
	 */
	synchronized public void beginRound(){
		super.beginRound();
	}
	
	/**
	 * Calls getCurrentDealer in CardGame
	 */
	synchronized public int getCurrentDealer(){
		return super.getCurrentDealer();
	}
	
	/**
	 * Calls makeDeal in CardGame 
	 */
	synchronized public void makeDeal(String dealersIP, int dealersPort){
		super.makeDeal(dealersIP, dealersPort);
	}
	
	/**
	 * Calls function in CardGame
	 */
	synchronized public void setCardsToCut(int cutNumber){
		super.setCardsToCut(cutNumber);
	}
	
	/**
	 * Handles a rob message when it comes in, If it doesn't then the game continues
	 * *NB* Unique to 41 and 45
	 *(Use Case 7)
	 *
	 *@param cardToDiscard The message containing the card the player wants to discard
	 *@param robbersIP The IP Adress of the player
	 *@param robbersPort The port number of the player
	 *
	 *@return True if the rob was successful
	 */
	synchronized public boolean requestForRob(String cardToDiscard, String robbersIP, int robbersPort){
		int robbersIndex = super.pg.getPlayerIndex(robbersIP, robbersPort);
		boolean isDealerRobbing = (robbersIndex == super.currDealerIndex) ? true : false;
		boolean isRobbed = false;
		Card[] robbersHand = super.pg.getPlayer(robbersIndex).getHand();
		
		if(isDealerRobbing == true){
			if( (super.turnedUpCard.toString().charAt(0) == 'a') || 
					(super.turnedUpCard.toString().equals("jok")) ){
				//i.e. If its the dealer and the card is eithir 1) Any 'Ace' or 2) The 'Joker' then do the rob
				isRobbed = doRob(robbersIP, robbersPort, robbersHand, cardToDiscard, 0);
			}
			else{
				for(int cardI=0; cardI<robbersHand.length; cardI++){
					if( (robbersHand[cardI] != null ) && 
							( (robbersHand[cardI].toString().charAt(0) == 'a') & 
								(robbersHand[cardI].isSameSuit(turnedUpCard) ) ) ){
						//If the robber has an Ace and its the same suit as the turned up card, then the player can rob
						isRobbed = doRob(robbersIP, robbersPort, robbersHand, cardToDiscard, cardI);
						break;
					}
				}
			}
		}
		else{
			for(int cardI=0; cardI<robbersHand.length; cardI++){
				if( (robbersHand[cardI] != null ) && 
						( (robbersHand[cardI].toString().charAt(0) == 'a') & 
							(robbersHand[cardI].isSameSuit(turnedUpCard) ) ) ){
					//If the robber has an Ace and its the same suit as the turned up card, then the player can rob
					isRobbed = doRob(robbersIP, robbersPort, robbersHand, cardToDiscard, cardI);
					break;
				}
			}
		}
		if(isRobbed == false)
			//the attempted rob was unsuccessful, let the player know
			super.pg.privateMessage("sNoRob", robbersIP, robbersPort);
		else
			super.currGameState = Game.GAME_PLAY;
		
		return isRobbed;
	}
	
	/**
	 * Used by requestForRob() to carry out the rob
	 * 
	 * @param robbersIP The IP address of the robber
	 * @param robbersPort The port number of the robber
	 * @param robbersHand The hand of cards belong to the robber
	 * @param discardedCard The card to be discarded
	 * @param replacePos The position in the players hand to replace
	 * @return
	 */
	protected final boolean doRob(String robbersIP, int robbersPort, Card[] robbersHand, String discardedCard, int replacePos){
		int playerIndex;
		
		//Send a message to the client telling them that their rob was successful
		super.pg.privateMessage("sRobTrump," + super.turnedUpCard.toString() + "," + discardedCard, robbersIP, robbersPort);
		
		//Replace the card they sent over with the trump card
		if(replacePos != 0){
			//If there was no replace position sent over then search for the card
			for(int cardI=0; cardI<robbersHand.length; cardI++){
				if(robbersHand[cardI].toString().equals(discardedCard)){
					replacePos = cardI;
					break;
				}
			}
		}
		
		playerIndex = super.pg.getPlayerIndex(robbersIP, robbersPort);
		super.pg.getPlayer(playerIndex).setCard(super.turnedUpCard, replacePos);
		//Tell everyone that NAME robbed TURNED_UP_CARD 
		super.pg.broadcastExOriginal("sRobTrump,"+super.turnedUpCard.toString()+","+super.pg.getPlayer(playerIndex).getNickName(), 
									robbersIP, robbersPort);
		
		return true;
	}
	
	/**
	 * Stores the move just made and eithir ends the trick or informs the next player that its
	 * their turn to go.
	 * 
	 */
	synchronized public boolean storeMove(String move, String moverIP, int moverPort){
		boolean moveDone = super.storeMove(move, moverIP, moverPort);
		
		if(moveDone == true) {
			if(super.cardPlayedCounter<super.pg.getNumOfPlayers()){
				//Step 1E - Inform the next player that it is their go
				super.informNextPlayerToGo(moverIP, moverPort);
				//Array index exception occured here
			}
			else
				//end trick, decide winner of it
				this.endTrick(moverIP, moverPort);
		}
		else
			System.err.println("ERROR: Move was not accepted");
		
		return moveDone;
	}
	
	/**
	 * The index of the player the server is waiting on input from
	 */
	public int getSrvrWaitingOnPlayerIndex(){
		return super.getSrvrWaitingOnPlayerIndex();
	}
	
	/**
	 * The superclass decides who won the trick and who is the current best trump.
	 * 
	 * NB Here in the subclass the score must be updated and it must see
	 * if anyone has reached the target score. If someone has it must send it out the 
	 * results to each player.
	 * (Use Case 10A)
	 * 
	 */
	synchronized public void endTrick(String ip, int port){
		int nextPlayerToGo;
		boolean gameOver = false, bestTrumpBonus = false;
		
		super.endTrick();
		
		//Update the score of the winner of this trick
		gameOver = super.doScoreUpdate(super.lastTrickWinnerIndex, 
				CardGame45.SCORE_FOR_TRICK_WIN, CardGame45.GAME_WIN, bestTrumpBonus);
		if(gameOver == true){
			super.postGame();
			return;
		}
		if(super.trickNumber >= 4){
		    bestTrumpBonus = true;
			//If its the last trick then award the best trump bonus
			gameOver = super.doScoreUpdate(super.bestTrumpOwnerIndex, 
					CardGame45.BEST_TRUMP_BONUS, CardGame45.GAME_WIN, bestTrumpBonus);
			if(gameOver == true){
				super.postGame();
				return;
			}
			//call the postRound cleanup
			super.postRound();
			return;
		}
		
		/*final trick clean up*/
		super.trickNumber++;
		//NEXT STEP: Inform the next player that its there go
		super.informNextPlayerToGo(ip, port);
	}
	
	/**
	 * 
	 */
	synchronized public void quitGame(String quitIP, int quitPort){
		super.quitGame(quitIP, quitPort);
	}
	
	/**
	 * 
	 */
	public void postGame(){
		super.postGame();
	}

	/**
	 * 
	 */
	synchronized public int getGameState(){
		return super.getGameState();
	}
	
	/**
	 * 
	 */
	synchronized public int getGameID() {
		return CardGame45.GAME_WIN;
	}
	
	/**
	 * "sGame=45,sGRules=100011"
	 * "The Joker is", "Stripping the deck is", "Reneging is", 
            							"Cut Right, Play Left is", "Automatic Win is", "Dealing out 3 and 2 is" 
	 * 
	 */
	synchronized public String toString(){
	    String buffer = "sGame="+GAME_WIN+",sGRules=";
	    
	    buffer += (super.incJoker == true) ? "1" : "0";
	    buffer += (super.stripDeck == true) ? "1" : "0";
	    buffer += (super.incReneging == true) ? "1" : "0";
	    buffer += (super.rightCutsLeftPlays == true) ? "1" : "0";
	    buffer += (super.allTricksAutoWin == true) ? "1" : "0";
	    buffer += (super.dealOut3And2 == true) ? "1" : "0";
	    
	    return buffer;
	}
}