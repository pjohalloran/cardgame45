/*
 * Created on 10-Nov-2004
 */
package server.cardgame;

import server.network.PlayerGroup;

/**
 * @author PJ O' Halloran
 * 
 * <b>Game States</b><br>
 * Game is in this state when the:-
 * @param GAME_SETUP first player who connects is picking the game and the game rules
 * @param GAME_STARTUP player's and the server are making preparations for the game to start
 * @param GAME_DEAL_CUT dealing and cutting process is happening
 * @param GAME_ROB robbing process is happening
 * @param GAME_PLAY card play process is happening
 */
public abstract class CardGame {
	//Some self explainatory constants
	protected static final int NUM_OF_TRICKS = 5;
	protected static final int NUM_OF_CARDS = 5;
	protected static final int MAX_NUM_OF_PLAYERS = 4;
	
	//Group of players
	protected PlayerGroup pg;
	//Array of cards played during the Trick 
	protected Card[][] playedCards;
	//The stack of cards
	protected Deck deckOfCards;
	//The turned up card, after the cards have been dealed out
	protected Card turnedUpCard;
	//The first card led during a trick
	protected Card ledCard;
	//Holds the best trump card played so far in the game
	protected Card currBestTrump;
	
	//Current game state
	protected int currGameState;
	//The current round number
	protected int roundNumber;
	//The number of tricks played during a round so far
	protected int trickNumber;
	//The number of cards played during a trick so far
	protected int cardPlayedCounter;
	//The winner of the last trick
	protected int lastTrickWinnerIndex;
	//The index of the current dealer
	protected int currDealerIndex;
	//The index of the player the server is currently waiting on input from
	protected int srvrWaitingOnPlayerIndex;
	//The index of the person who has played the current best trump card
	protected int bestTrumpOwnerIndex;
	//Number of cards to cut
	protected int cardsToCut;
	//Set to true if the joker card is turned up after dealing
	protected boolean jokerRobbed;
	//Holds the winner's index of each of the five tricks played during a round, used to see if the same person
	// won all 5 tricks and so automatically won the game
	protected int[] trickWinnersIndexArr;
	
	//To include the joker or not, defaults to true
	protected boolean incJoker;
	//To strip the deck or not, defaults to false
	protected boolean stripDeck;
	//The position of the player who cuts and plays first, relative to the dealer, defaults to true
	protected boolean rightCutsLeftPlays; 
	//To include reneging or not, defaults to true
	protected boolean incReneging;
	//A player to win automatically if they win all five tricks, defaults to true
	protected boolean allTricksAutoWin;
	//If true, the server deals out 3 cards to each player, then 2. If its false, it deals out 4, then 1 card 
	protected boolean dealOut3And2;
	
	/****************************************************************************************************************/
	/*************************************************Construtors****************************************************/
	/****************************************************************************************************************/
	
	/**
	 * Creates the general CardGame object
	 * 
	 * @param rules The rules the player wants included in the game
	 * @param startupPG The playergroup set up in the Server class
	 */
	public CardGame(boolean[] rules, PlayerGroup startupPG){
		this.currGameState = Game.GAME_STARTUP;
		this.pg = startupPG;
		if(rules.length == 6){
			this.incJoker = rules[0];
			this.stripDeck = rules[1];
			this.rightCutsLeftPlays = rules[2];
			this.incReneging = rules[3];
			this.allTricksAutoWin = rules[4];
			this.dealOut3And2 = rules[5];
		}
		else{
			System.err.println("There is a problem with the rules the first player sent over");
		}
		init();
	}
	
	/****************************************************************************************************************/
	/****************************************Beginning Round/Dealing/Cutting*****************************************/
	/****************************************************************************************************************/
	
	/**
	 * Initialises the CardGame objects
	 * 
	 */
	protected void init(){
		this.deckOfCards = new Deck(this.incJoker, this.stripDeck);
		this.turnedUpCard = null;
		this.ledCard = null;
		this.currBestTrump = null;
		
		this.roundNumber = 1;
		this.trickNumber = 0;
		this.cardPlayedCounter = 0;
		this.lastTrickWinnerIndex = -1;
		this.currDealerIndex = -1;
		this.srvrWaitingOnPlayerIndex = -1;
		this.bestTrumpOwnerIndex = -1;
		this.cardsToCut = -1;
		this.jokerRobbed = false;
		
		this.trickWinnersIndexArr = new int[CardGame.NUM_OF_TRICKS];
		for(int i=0; i<trickWinnersIndexArr.length; i++)
			this.trickWinnersIndexArr[i] = -1;
		
		this.playedCards = new Card[CardGame.NUM_OF_TRICKS][CardGame.MAX_NUM_OF_PLAYERS];
		for(int trick=0; trick<playedCards.length; trick++){
			for(int playerIndex=0; playerIndex<playedCards[0].length; playerIndex++)
				this.playedCards[trick][playerIndex] = null;
		}
	}
	
	/**
	 * Gets the playergroup
	 * 
	 * @return
	 */
	public PlayerGroup getPlayerGroup(){
		return this.pg;
	}
	
	/**
	 * Begins the game.
	 * 
	 * This is called only by 
	 * 1) The postRound() method
	 * 		or
	 * 2) The server when:-
	 * 		a) A timeout occurs and there is 2 or more players
	 * 		b) Four players have joined
	 */
	public void beginRound(){
		currGameState = Game.GAME_ROUND_SETUP;
		chooseDealer();
		deckOfCards.shuffle();
		this.srvrWaitingOnPlayerIndex = this.currDealerIndex;
		//Set the current state of the game to be in Dealing mode
		this.currGameState = Game.GAME_DEAL;
		//NEXT STEP: wait for the dealer to get back to the server which will start off the makeDeal() method
	}
	
	/**
	 * At the beginning of every round this function is called to determine
	 * the dealer. If its the first round then a dealer is chosen randomly, 
	 * else the next person to deal is the person to the left/right of the 
	 * current dealer.
	 * (Use Case 5A)
	 * 
	 */
	protected final void chooseDealer(){
		boolean searchLeft = true;
		int index = -1;
		
		if(this.roundNumber == 1) {
			//if its the first round, then pick a random person
			double randomIndex = Math.random()*CardGame.MAX_NUM_OF_PLAYERS;
			index = (int)randomIndex;
			//int randomIndex = (int)Math.random()*this.pg.getNumOfPlayers();
			if(pg.isPlayerAt(index) == false){
				//If there is no player in this position then get the next player to the RIGHT of the randomIndex
				index = pg.findNextAvailablePlayer(index, !searchLeft);
			}
			//The next dealer becomes the current dealer
			this.currDealerIndex = index;
		}
		else{
			//Else its a round other than the first one, pick the person to the LEFT of the currDealerIndex
			index = pg.findNextAvailablePlayer(currDealerIndex, searchLeft);
			this.currDealerIndex = index;
		}
		pg.broadcastMessage("sDealer," + pg.getPlayer(this.currDealerIndex).getNickName());
	}
	
	/**
	 * Returns the index of the current dealer
	 * (Use Case 5B)
	 * 
	 * @return Returns the index of the current dealer
	 */
	public int getCurrentDealer(){
		return this.currDealerIndex;
	}
	
	/**
	 * This method is called by the dealing player in its thread. It kicks
	 * off the dealing and cutting process
	 * (Use Case 6)
	 *
	 */
	public void makeDeal(String dealerIP, int dealerPort){
		Card[][] dealedCards = new Card[CardGame.MAX_NUM_OF_PLAYERS][CardGame.NUM_OF_CARDS];
		Card[] temp;
		String[] hand = new String[CardGame.MAX_NUM_OF_PLAYERS];
		String playerIP;
		int nextPlayerPos, roundOfDealing = 1, cardIndex, playerPort, firstPlayerToGetCardsPos;
		int[] lumpCards = { ( (this.dealOut3And2 == true) ? 3 : 4), 
								( (this.dealOut3And2 == true) ? 2 : 1) };
		boolean dealLeft = this.rightCutsLeftPlays, playLeft = this.rightCutsLeftPlays;
		
		//Step 1 - take care of cutting the cards before dealing begins
		cutCards();
		
		//Set the current state of the game to be in Dealing mode
		this.currGameState = Game.GAME_DEAL;
		//If (dealLeft == true) then deal clockwise to the left
		firstPlayerToGetCardsPos = nextPlayerPos = pg.findNextAvailablePlayer(this.currDealerIndex, dealLeft);
		
		//Step 2 - Fill up the array - dealedCards[]
		while(roundOfDealing <= 2) {
			//Step 2B - Remove (3 or 4) cards from the deck
			temp = this.deckOfCards.removeCards(lumpCards[roundOfDealing-1]);
			
			//Step 2C - Copy the cards into the local 2D array of dealed cards
			cardIndex = (roundOfDealing==1) ? 0 : lumpCards[0];
			for(int tempI=0; tempI<temp.length; cardIndex++, tempI++){
				dealedCards[nextPlayerPos][cardIndex] = temp[tempI];
			}
			
			//Step 2D - get the position of the next player to recieve cards
			nextPlayerPos = pg.findNextAvailablePlayer(nextPlayerPos, dealLeft);
			
			//Step 2A - if we are back to the first player who got cards 
			//then we are onto the second of 2 rounds of dealing
			if(nextPlayerPos == firstPlayerToGetCardsPos)
				roundOfDealing++;
		}//end while()
		
		//Step 3 - Make the String representations of the hand of cards so it can be sent to the remote player
		for(int playerI = 0; playerI<CardGame.MAX_NUM_OF_PLAYERS; playerI++) {
			if(pg.isPlayerAt(playerI)){
				hand[playerI] = "sDeal";
				for(int cardI=0; cardI<CardGame.NUM_OF_CARDS; cardI++){
					hand[playerI] += "," + dealedCards[playerI][cardI].toString();
				}
			}
		}
		
		//Step 4 - Set every players server-side hand to what they have been dealt out 
		// and send the hand to each participating player
		for(int playerIndex=0; playerIndex<CardGame.MAX_NUM_OF_PLAYERS; playerIndex++){
			if(pg.isPlayerAt(playerIndex) == true){
				//set the server-side hand
				pg.getPlayer(playerIndex).setHand(dealedCards[playerIndex]);
				//Send each player their hand
				playerIP = pg.getPlayer(playerIndex).getIPAddress();
				playerPort = pg.getPlayer(playerIndex).getPortNumber();
				//Send this player their cards
				pg.privateMessage(hand[playerIndex], playerIP, playerPort);
				//Send message to everyone else telling them that this player just got their cards
				pg.broadcastExOriginal("sDeal,"+pg.getPlayer(playerIP, playerPort).getNickName(), playerIP, playerPort);
			}
		}
		
		//Step 5 - Turn up the trump card and tell every player what it is
		this.turnedUpCard = this.deckOfCards.removeCard();
		pg.broadcastMessage("sTrump," + this.turnedUpCard.toString());
		
		if(this.turnedUpCard.getTrumpCardValue() == 15){
			this.turnedUpCard = this.deckOfCards.removeCard();
			pg.broadcastMessage("sTrump," + this.turnedUpCard.toString());
		}
		
		//Tell the first player thats its their turn to move after the deal has been completed
		informNextPlayerToGo(dealerIP, dealerPort);
		
		//NEXT STEP: Wait for the first player to send over their move, until then the server will accept rob attempts,
		//				while the (game state == GAME_ROB), if they are valid
		this.currGameState = Game.GAME_ROB;
	}
	
	/**
	 * Takes care of cutting the cards for makeDeal()
	 *
	 */
	protected final void cutCards(){
		String cuttersIP, cuttersName;
		int cutterPosition, cuttersPort, numOfCardsToCut;
	    
	    synchronized(this){
			boolean cutLeft = !(this.rightCutsLeftPlays);
			
			//Set the current state of the game to be in Cutting mode
			this.currGameState = Game.GAME_CUT;
			//Find out where the cutter is, gets the player to the right if (cutLeft == false)
			cutterPosition = pg.findNextAvailablePlayer(this.currDealerIndex, cutLeft);
			this.srvrWaitingOnPlayerIndex = cutterPosition;
			cuttersIP = pg.getPlayer(cutterPosition).getIPAddress();
			cuttersPort = pg.getPlayer(cutterPosition).getPortNumber();
			cuttersName = pg.getPlayer(cutterPosition).getNickName();
			//Send them a message to pick how many cards to cut
			pg.privateMessage("sCut", cuttersIP, cuttersPort);
		
			while(this.cardsToCut==-1) {
				//Poll and Wait repeatidly until the "Cutter" thread sets the number of cards to be cut
				try{
				    wait();
				}
				catch(InterruptedException ie){
					System.err.println("Dealer interuppted while waiting for the Cutter to send the number of cards to cut\n");
				}
			}
		
			numOfCardsToCut = this.cardsToCut;
			if(numOfCardsToCut > 0) {
				deckOfCards.cutCards(numOfCardsToCut);
				//notify all players x cards have been cut
				pg.broadcastMessage("sCut," + numOfCardsToCut + "," + cuttersName);
			}
		}
	}
	
	/**
	 * Called by the "Cutter" to set the number of cards to cut
	 * 
	 * @param cut The number of cards the player wishes to cut, between 0 and MAX
	 */
	public void setCardsToCut(int cut){
		this.cardsToCut = cut;
		//notify any wating threads that the cards have been cut
		notifyAll();
	}
	
	/****************************************************************************************************************/
	/****************************************Playing*****************************************************************/
	/****************************************************************************************************************/
	
	/**
	 * This method is called when a move is sent by a player to the server. It will
	 * only be called by the players thread if its their turn. It checks to see if 
	 * this is a valid move by the player. If it is, it gets stored away, else it is 
	 * rejected and the player is notified why
	 * (Use Case 9)
	 * 
	 * @param moveMessage The move message the player wishes to make
	 * @param ip The Ip address of the player to move
	 * @param port The port number of the player to move
	 */
	public boolean storeMove(String moveMessage, String ip, int port){
		int playerIndex;
		boolean isValidMove = false, moveCompleted = false;
		String rejReason = "";
		Card cardPlayed = new Card(moveMessage);
		
		playerIndex = pg.getPlayerIndex(ip, port);
		Card[] hand = pg.getPlayer(playerIndex).getHand();
		
		//Step 1 - If its the first card played
		if(this.cardPlayedCounter == 0){
			if(this.currGameState != Game.GAME_PLAY)
				//Set the game state onto game play
				this.currGameState = Game.GAME_PLAY;
			//Step 1A - Complete the move
			moveCompleted = doMove(playerIndex, cardPlayed, hand);
			//Step 1B - Record the card led for future reference
			this.ledCard = cardPlayed;
		}
		else {
		    rejReason = checkForValidMove(cardPlayed, playerIndex, ip, port, hand);
		    if(rejReason.equals(""))
				//Step 2A - if its a valid move then complete the move
				moveCompleted = doMove(playerIndex, cardPlayed, hand);
			else{
				//send a reject message to the player
				pg.privateMessage("sCardRej," + rejReason, ip, port);
			}
		}
		
		return moveCompleted;
		//NB The notification of the next player to go next is taken care of by subclasses  
	}
	
	/**
	 * Called by storeMove() only, completes the move if it has been accepted 
	 * 
	 * @param playerIndex The index of the player in the PlayerGroup
	 * @param cardPlayed The card played by the player
	 * @param hand The players hand of cards
	 */
	protected final boolean doMove(int playerIndex, Card cardPlayed, Card[] hand){
		//Step 1A - Accept it and store it away
		this.playedCards[trickNumber][playerIndex] = cardPlayed;
		//Step 1B - Increment the cardPlayedCounter
		this.cardPlayedCounter++;
		//Step 1C - Notify all players what move has just occured and who made it
		pg.broadcastMessage("sCard," + pg.getPlayer(playerIndex).getNickName() + "," + cardPlayed.toString());
		//Step 1D - Remove this card from the players server side hand
		for(int cardI=0; cardI<hand.length; cardI++) {
			if( (hand[cardI] != null) && (hand[cardI].equals( cardPlayed ) ) ){
				hand[cardI] = null;
				break;
			}
		}
		
		return true;
	}
	
	/**
	 * Checks the card just played by the player. The card is deemed as eithir:-
	 * <br>
	 * <b>1 Ruffing </b>When a card of non-trump is led, then subsequent players must
	 * eithir (a) Follow suit 	or 	(b) Play a trump card. If the player can't do eithir (1a)
	 * or (1b), then they may play any card which is known as RUFFING. Ruffing is playing a 
	 * trump when a card of non trump is led
	 *
	 * <br>
	 * <b>2 Reneging </b>When a card of Trump is led, subsequent players must follow suit 
	 * if they can. The exception is the '5', 'J', 'Joker' and 'AH'. A player may
	 * withhold any one of these cards for later in the trick so long as the card led is of 
	 * lesser trump value than the card that you are withholding.
	 * <br>
	 * e.g. (1) Trump suit = Hearts, Led: The '5H'
	 * 		in this case reneging is not allowed as the 5H is the most powerful card that could
	 * 		have been led.
	 * 		
	 * 		(2) Trump suit = Diamonds, Led: The Joker
	 * 		in this case a player may withhold the '5D' or the 'JackD' if they are in possesion
	 * 		of them as they are of greater Trump value than the Joker. However the 'AH' must be 
	 * 		played as it is of lesser value than the Joker
	 * 
	 * NB For the purposes of following suit, the Joker and the Ace of Heart cards are counted
	 * 		as Trumps
	 * 
	 * @param cardToCheck The played card
	 * @param playerIndex The index of the player who played the card
	 * @param playerIP The ip address of the player who played the card
	 * @param playerPort The port address of the player who played the card
	 * @param hand The players hand of cards
	 * 
	 * @return A string indicating if the card was accepted and if not, why it wasnt accepted
	 */
	protected final String checkForValidMove(Card cardToCheck, int playerIndex, String playerIP, int playerPort, Card[] hand){
	    String reject = "";
		int valueToBeat;
		
		if(this.ledCard.isTrumpCard(this.turnedUpCard)) {
			//If reneging is turned off then don't perform this check
			if(this.incReneging) {
				//Reneg operation
				if(cardToCheck.isTrumpCard(this.turnedUpCard))
					//Check if the card they played is ANY Trump card, if it is then its ok
				    reject = "";
				else {
					//They have played a non-trump card and can only play a non-trump card if 
						//they don't have any Trumps or if they are withholding a top trump card
					for(int cardI=0; cardI<hand.length; cardI++) {
						if(hand[cardI] != null) {
							//if there is a card here
							if(hand[cardI].equals(cardToCheck))
								//if the current card in the hand that you are checking is the one just played,
									//then ignore it, we dont check this
								continue;
							
							if(hand[cardI].isTrumpCard(this.turnedUpCard)) {
								//if a trump card found, next check if its one of the 4 or less exceptional cards,
								if( (hand[cardI].isATopTrumpCard(this.turnedUpCard)) 
										& (hand[cardI].getTrumpCardValue() > ledCard.getTrumpCardValue()) )
									//If it is then the player has the right to withhold it for later
									continue;
								
								reject = "Reneg";
								break;
							}
						}
					} //End for
				}
			}
		}//End Reneg operation
		/////////////////////////////////////////////////////////////////////////////////////
		else { //ruff operation
			if( (cardToCheck.isSameSuit(this.ledCard)) || (cardToCheck.isTrumpCard(this.turnedUpCard)) ){
				//if the card played is of the same suit as the led card OR its a trump card,
					//then its ok
				reject = "";
			}
			else{
				//else if 
				for(int cardI=0; cardI<hand.length; cardI++){
					if(hand[cardI] != null){
						//if there is a card here
						if(hand[cardI].equals(cardToCheck))
							//if the current card in the hand that you are checking is the one just played,
								//then ignore it
							continue;
						
						if( (hand[cardI].isSameSuit(this.ledCard)) || 
								(hand[cardI].isTrumpCard(this.turnedUpCard)) ){
						    reject = "Ruff";
							break;
						}
					}
				}
			}
		}
		
		return reject;
	}

	/**
	 * This method gets the index in the PlayerGroup of the player to make the next
	 * card move during a game. If trick number = 1st, then the first player to go 
	 * is the player to the left/right of the dealer. Else its the player who won 
	 * the last trick
	 * (Use Case 8)
	 * 
	 * @return The index of the player to go next, -1 if no player was found
	 */
	protected int getNextTurnPlayerID(int currentPlayerIndex) {
		boolean isNextPlayerLeft = this.rightCutsLeftPlays;
		int nextTurnIndex = -1;
		
		if(this.cardPlayedCounter == 0) {
		    if(this.trickNumber == 0)
		        //player to the left of the dealer
		        nextTurnIndex = pg.findNextAvailablePlayer(this.currDealerIndex, isNextPlayerLeft);
		    else
		        //the winner of the last trick
		        nextTurnIndex = this.lastTrickWinnerIndex;
		}
		else
		    //the player to the left of the player who just played
		    nextTurnIndex = pg.findNextAvailablePlayer(currentPlayerIndex, isNextPlayerLeft);
		
		return nextTurnIndex;
	}
	
	/**
	 * Informs the next player that it is their turn to take a turn
	 *
	 */
	protected void informNextPlayerToGo(String currentIP, int currentPort){
		int nextPlayerToGo = getNextTurnPlayerID(pg.getPlayerIndex(currentIP, currentPort));
		
		srvrWaitingOnPlayerIndex = nextPlayerToGo;
		pg.broadcastMessage("sTurn," + pg.getPlayer(nextPlayerToGo).getNickName()); 
	}
	
	/**
	 * 
	 * @return The index of the player that the server is currently waiting on a message from
	 */
	public int getSrvrWaitingOnPlayerIndex(){
		return this.srvrWaitingOnPlayerIndex;
	}
	
	/****************************************************************************************************************/
	/****************************************Scoring*****************************************************************/
	/****************************************************************************************************************/
	
	/**
	 * This method is called when all players have played a card.
	 * It decides (A) who won the trick and (B) who is the current best trump.
	 * It then updates everyones score
	 * 
	 * NB Subclasses must see if anyone has reached the target score. If they did then 
	 * they must send out the results to each player.
	 * (Use Case 10A)
	 * 
	 */
	public void endTrick() {
		//The array of up to 4 cards played during this trick which will be analysed
		Card[] trickCards = playedCards[trickNumber];
		//The current best played card is at first assumed to be the led card 
		Card currentBestCard = this.ledCard;
		boolean trumpFoundAlready = false, overwriteBestTrump = false;
		int winnerPosition = 0;
		
		for(int i=0; i<trickCards.length; i++) {
		    if( (trickCards[i] != null) && (trickCards[i].equals(currentBestCard)))
		        //Default the winner position to the ledCard
		        winnerPosition = i;
		}
		
		/****************Check which card won********************/
		if(this.ledCard.isTrumpCard(this.turnedUpCard)) {
			//1) If a trump card was led then we are looking for a better trump card
			for(int i=0; i<trickCards.length; i++) {
				if( (trickCards[i] != null) && (trickCards[i].isTrumpCard(this.turnedUpCard)) ) {
					if( (trickCards[i].isTrumpCard(this.turnedUpCard)) && (trickCards[i].getTrumpCardValue() > currentBestCard.getTrumpCardValue()) ) {
						currentBestCard = trickCards[i];
						winnerPosition = i;
					}
				}
			}
		}
		else {
			//2) Else if a non trump was led then we are looking for a better card
			//    of that suit OR we are looking for the best trump card
			for(int i=0; i<trickCards.length; i++) {
				if(trickCards[i] != null) {
					
					if(currentBestCard.isTrumpCard(this.turnedUpCard)){
						//1 current best card is a trump
						//You are looking for a better trump
						if( (trickCards[i].isSameSuit(currentBestCard)) && (trickCards[i].getTrumpCardValue() > currentBestCard.getTrumpCardValue()) ) {
							currentBestCard = trickCards[i];
							winnerPosition = i;
						}
					}
					else{
						//2 current best card is a non trump
							//You are looking for a better non-trump card OR you are looking for any trump card
						if(trickCards[i].isTrumpCard(this.turnedUpCard)){
							currentBestCard = trickCards[i];
							winnerPosition = i;
						}
						else{
							if( (trickCards[i].isSameSuit(currentBestCard)) && (trickCards[i].getNonTrumpCardValue() > currentBestCard.getNonTrumpCardValue()) ) {
								currentBestCard = trickCards[i];
								winnerPosition = i;
							}
						}
					}
				}
			}//end for
		}//end else
		
		this.trickWinnersIndexArr[this.trickNumber] = winnerPosition;
		this.lastTrickWinnerIndex = winnerPosition;
		/****************END Check which card won********************/
		
		/******************Check for Best trump*****************************/
		//3) find out if the card which won this trick is better than the previous best trump
		if(trickNumber == 0){
			//if its the first trick then set the best card in this trick to the current best trump
			overwriteBestTrump = true;  
		}
		else{
			//we are on the 2nd or greater trick
			if(this.currBestTrump.isTrumpCard(this.turnedUpCard)){
				//The curentBestTrump is a Trump card
				if( (trickCards[winnerPosition].isTrumpCard(this.turnedUpCard)) && 
						(trickCards[winnerPosition].getTrumpCardValue() > this.currBestTrump.getTrumpCardValue()) )
					//if the winning card of this trick is also a trump card and if its better than the current
					// best trump then the winning card of this trick is the new best trump
					overwriteBestTrump = true;
				else
					overwriteBestTrump = false;
			}
			else{
				//The curentBestTrump is a Non-Trump card
				if(trickCards[winnerPosition].isTrumpCard(this.turnedUpCard))
					//If the winning card of this trick is a trump card, then the winning card 
					// of this trick is the new best trump as a trump always beats a non-trump card
					overwriteBestTrump = true;
				else if( (trickCards[winnerPosition].isSameSuit(this.currBestTrump)) && 
						(trickCards[winnerPosition].getNonTrumpCardValue() > this.currBestTrump.getNonTrumpCardValue()) )
					//If the winning card of this trick is the same suit as the current best trump and it is better
					// then it, then it is the new best trump
					overwriteBestTrump = true;
				else
					overwriteBestTrump = false;
			}
		}
		
		if(overwriteBestTrump == true){
			this.currBestTrump = trickCards[winnerPosition];
			this.bestTrumpOwnerIndex = winnerPosition;
		}
		/******************END Check for Best trump*****************************/
		
		// Trick cleanup
		this.cardPlayedCounter = 0;
		this.ledCard = null;
	}
	
	/**
	 * Checks if anyone has reached the limit of 41/45/110.
	 * If they have then a winner is announced and is sent
	 * out to each client
	 * (Use Case 10B)
	 * 
	 * @return
	 */
	protected final boolean doScoreUpdate(int playerIndex, int scoreIncrease, int limit, boolean trumpBonus){
		boolean gameFinished = false;
		
		pg.getPlayer(playerIndex).addToCurrScore(scoreIncrease);
		
		if(trumpBonus == true)
		    this.pg.broadcastMessage("sScore,"+scoreIncrease+","+this.pg.getPlayer(playerIndex).getNickName()
		            +",BTBonus");
		else
		    this.pg.broadcastMessage("sScore,"+scoreIncrease+","+this.pg.getPlayer(playerIndex).getNickName());
		
		if(this.pg.getPlayer(playerIndex).getCurrScore() >= limit){
			//Tell all players that the game has been won by NAME
			this.pg.broadcastMessage("sWin,"+this.pg.getPlayer(playerIndex).getNickName());
			postGame();
			gameFinished = true;
		}
		
		return gameFinished;
	}
	
	/**
	 * First it checks did anyone win all 5 tricks in a round. If they did
	 * then the game ends immediately with this player declared the winner.
	 * If no winner, it resets the deck, the tricknumber, the winnerOfTheLastTrick,
	 * the playedCards Arr and the current state of the game. 
	 *(Use Case 11A)
	 *
	 */
	protected final void postRound(){
		this.currGameState = Game.GAME_ROUND_END;
		if( (this.allTricksAutoWin == true) && (hasAnyoneAutoWon() == true) ){
			pg.broadcastMessage("sAutoWin,"+pg.getPlayer(this.lastTrickWinnerIndex).getNickName());
			postGame();
			return;
		}
		this.deckOfCards.postRound();
		this.cardsToCut = -1;
		this.lastTrickWinnerIndex = -1;
		this.roundNumber++;
		this.trickNumber = 0;
		for(int trick=0; trick<NUM_OF_TRICKS; trick++){
			for(int playerIndex=0; playerIndex<MAX_NUM_OF_PLAYERS; playerIndex++)
				this.playedCards[trick][playerIndex] = null;
		}
		this.turnedUpCard = null;
		this.bestTrumpOwnerIndex = -1;
		this.currBestTrump = null;
		
		beginRound();
	}
	
	/**
	 * Checks if the same player won all 5 tricks
	 * 
	 * (Use Case 11B)
	 * @return True if the same player won all five tricks
	 */
	protected final boolean hasAnyoneAutoWon(){
		boolean sameWinner = false;
		
		for(int i=0; i<(this.trickWinnersIndexArr.length-1); i++){
			if(this.trickWinnersIndexArr[i] != this.trickWinnersIndexArr[i+1]){
				sameWinner = false;
				break;
			}
			sameWinner = true;
		}
		
		return sameWinner;
	}
	
	/****************************************************************************************************************/
	/****************************************A Player Quiting********************************************************/
	/****************************************************************************************************************/
	
	/**
	 * This method is called if a player leaves the game during play
	 * (Use Case 4)
	 * 
	 * @param ip The Ip address of the player leaving
	 * @param port The port number of the player who is leaving
	 */
	protected void quitGame(String ip, int port){
		
	}
	
	/**
	 * 
	 *
	 */
	protected void postGame(){
		//reset pg, roundNumber, currDealerIndex, 6 options
		
		//Should call PlayerHandler45.closeClient()
	}
	
	/****************************************************************************************************************/
	/****************************************Game State and Game ID**************************************************/
	/****************************************************************************************************************/
	
	/**
	 * This method returns the current Game state
	 * 
	 * @return The current Game state
	 */
	public int getGameState(){
		return currGameState;
	}
	
	/**
	 * Gets the ID of the game
	 * 
	 * @return 41/45/110 depending on the game in progress
	 */
	public abstract int getGameID();
	
}