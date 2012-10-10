/*
 * Created on 25-Jan-2005
 */
package server.cardgame;

/**
 * @author Pj O' Halloran
 */

public class Card {
	/*
	 * suitID [0=hearts, 1=diamonds, 2=spades, 3=clubs]
	 * 
	 * NB For the Joker card, suitID = -1
	 */
	private int suitID;
	
	/*
	 * nonTrumpCardValue
	 * 1 = ace 		(diamonds)
	 * 
	 * 2 = two		(hearts and diamonds)
	 * 3 = three
	 * 4 = four
	 * 5 = five
	 * 6 = six
	 * 7 = seven
	 * 8 = eight
	 * 9 = nine
	 * 10 = ten
	 * 
	 * 10 = two		(spades and clubs)
	 * 9 = three
	 * 8 = four
	 * 7 = five
	 * 6 = six
	 * 5 = seven
	 * 4 = eight
	 * 3 = nine
	 * 2 = ten
	 * 
	 * 11 = ace 	(spades and clubs)
	 * 
	 * 12 = jack
	 * 13 = queen
	 * 14 = king
	 * 
	 * NB The Joker Card and the Ace of Hearts are Trump cards by default
	 * 		(nonTrumpValue = -1)
	 */
	private int nonTrumpCardValue;
	
	/*
	 * trumpCardValue
	 * 
	 * 2 = two 		(hearts and diamonds)
	 * 3 = three
	 * 4 = four
	 * 6 = six
	 * 7 = seven
	 * 8 = eight
	 * 9 = nine
	 * 10 = ten
	 * 
	 * 10 = two 		(spades and clubs)
	 * 9 = three
	 * 8 = four
	 * 6 = six
	 * 5 = seven
	 * 4 = eight
	 * 3 = nine
	 * 2 = ten
	 * 
	 * 11 = queen
	 * 12 = king
	 * 13 = ace 	(spades, clubs and diamonds)
	 * 14 = ace		(hearts)
	 * 15 = joker
	 * 16 = jack
	 * 17 = Five
	 */
	private int trumpCardValue;
	
	/*
	 * The card identifier that the client and server will
	 * use to communicate with each other.
	 */
	private String clientCardID;
	
	/**
	 * Constructs a card when the suit and card id's are known
	 * 
	 * NB The Deck constructor uses this Card constructor
	 * 
	 * @param cardI Any Number between and including [1 and 14]
	 * @param suitI Any Number between and including [0 and 3]
	 */
	public Card(int cardI, int suitI){
		String buf = "";
		boolean enter = true;
		
		suitID = suitI;
		nonTrumpCardValue = cardI;
		switch(suitID){
			case 0:{
				buf = "h";
			}break;
			case 1:{
				buf = "d";
			}break;
			case 2:{
				buf = "s";
			}break;
			case 3:{
				buf = "c";
			}break;
			default:{
				//Joker Card, (suitID = -1) 
				clientCardID = "jok";
				suitID = -1;
				nonTrumpCardValue = -1;
				trumpCardValue = 15;
				enter = false;//dont enter the builder method, its done already
			}break;
		}
		if(enter == true)
			assignClientCardID(buf);
	}
	
	private void assignClientCardID(String buf){
		switch(nonTrumpCardValue){
			case 1:{	//Ace of Diamonds
				if(buf.equals("d")){
					trumpCardValue = 13;
					clientCardID = "a"+buf;
				}
			}break;
			case 2:{	//10 Clubs & Spades, 2 Hearts & Diamonds
				initCardID(2, buf, "2", "t");
			}break;
			case 3:{	//9 Clubs & Spades, 3 Hearts & Diamonds
				initCardID(3, buf, "3", "9");
			}break;
			case 4:{	//8 Clubs & Spades, 4 Hearts & Diamonds
				initCardID(4, buf, "4", "8");
			}break;
			case 5:{	//7 Clubs & Spades, 5 Hearts & Diamonds
				if( (buf.equals("d")) || (buf.equals("h")) ){
					trumpCardValue = 17;
					clientCardID = "5"+buf;
				}
				else{
					trumpCardValue = 5;
					clientCardID = "7"+buf;
				}
			}break;
			case 6:{	//6 Clubs & Spades, 6 Hearts & Diamonds
				initCardID(6, buf, "6", "6");
			}break;
			case 7:{	//5 Clubs & Spades, 7 Hearts & Diamonds
				if( (buf.equals("d")) || (buf.equals("h")) ){
					trumpCardValue = 7;
					clientCardID = "7"+buf;
				}
				else{
					trumpCardValue = 17;
					clientCardID = "5"+buf;
				}
			}break;
			case 8:{	//4 Clubs & Spades, 8 Hearts & Diamonds
				initCardID(8, buf, "8", "4");
			}break;
			case 9:{	//3 Clubs & Spades, 9 Hearts & Diamonds
				initCardID(9, buf, "9", "3");
			}break;
			case 10:{	//2 Clubs & Spades, 10 Hearts & Diamonds
				initCardID(10, buf, "t", "2");
			}break;
			case 11:{	//Ace of Clubs or Spades
				if( (buf.equals("s")) || (buf.equals("c")) ){
					trumpCardValue = 13;
					clientCardID = "a"+buf;
				}
			}break;
			case 12:{	//Jack
				trumpCardValue = 16;
				clientCardID = "j"+buf;
			}break;
			case 13:{	//Queen
				trumpCardValue = 11;
				clientCardID = "q"+buf;
			}break;
			case 14:{	//King
				trumpCardValue = 12;
				clientCardID = "k"+buf;
			}break;
			default:{
				//Ace of Hearts Card, (nonTrumpValue = -1)
				trumpCardValue = 14;
				clientCardID = "a"+buf;
			}break;
		}
	}
	
	private void initCardID(int itsTrumpValue, String buf, String one, String two){
		trumpCardValue = itsTrumpValue;
		if( (buf.equals("d")) || (buf.equals("h")) )
			clientCardID = one+buf;
		else
			clientCardID = two+buf;
	}
	
	/**
	 * Constructs a card from the message sent over by the client
	 *
	 * NB The CardGame object uses this Card constructor
	 *  
	 * @param clientID The String value of the card
	 */
	public Card(String clientID){
		if(clientID.equals("jok")){
			nonTrumpCardValue = -1;
			trumpCardValue = 15;
			suitID = -1;
		}
		else if(clientID.equals("ah")){
			nonTrumpCardValue = -1;
			trumpCardValue = 14;
			suitID = 0;
		}
		else if(clientID.equals("ad")){
			nonTrumpCardValue = 1;
			trumpCardValue = 13;
			suitID = 1;
		}
		else{
			char card = clientID.charAt(0);
			char suit = clientID.charAt(1);
			switch(suit){
				case 'h':{
					suitID = 0;
					assignDetails(card);
				}break;
				case 'd':{
					suitID = 1;
					assignDetails(card);
				}break;
				case 's':{
					suitID = 2;
					assignDetails(card);
				}break;
				case 'c':{
					suitID = 3;
					assignDetails(card);
				}break;
			}
		}
		clientCardID = clientID;
	}
	
	private void assignDetails(char card){
		switch(card){
			case '2':{
				if( (suitID == 0) || (suitID == 1) )
					nonTrumpCardValue = trumpCardValue = 2;
				else
					nonTrumpCardValue = trumpCardValue = 10;
			}break;
			case '3':{
				if( (suitID == 0) || (suitID == 1) )
					nonTrumpCardValue = trumpCardValue = 3;
				else
					nonTrumpCardValue = trumpCardValue = 9;
			}break;
			case '4':{
				if( (suitID == 0) || (suitID == 1) )
					nonTrumpCardValue = trumpCardValue = 4;
				else
					nonTrumpCardValue = trumpCardValue = 8;
			}break;
			case '5':{
				if( (suitID == 0) || (suitID == 1) ){
					nonTrumpCardValue = 5;
					trumpCardValue = 17;
				}
				else{
					nonTrumpCardValue = 7; 
					trumpCardValue = 17;
				}
			}break;
			case '6':{
				nonTrumpCardValue = trumpCardValue = 6;
			}break;
			case '7':{
				if( (suitID == 0) || (suitID == 1) )
					nonTrumpCardValue = trumpCardValue = 7;
				else
					nonTrumpCardValue = trumpCardValue = 5;
			}break;
			case '8':{
				if( (suitID == 0) || (suitID == 1) )
					nonTrumpCardValue = trumpCardValue = 8;
				else
					nonTrumpCardValue = trumpCardValue = 4;
			}break;
			case '9':{
				if( (suitID == 0) || (suitID == 1) )
					nonTrumpCardValue = trumpCardValue = 9;
				else
					nonTrumpCardValue = trumpCardValue = 3;
			}break;
			case 't':{
				if( (suitID == 0) || (suitID == 1) )
					nonTrumpCardValue = trumpCardValue = 10;
				else
					nonTrumpCardValue = trumpCardValue = 2;
			}break;
			case 'a':{
				if( (suitID == 0) || (suitID == 1) )
					;	//System.err.println("");
				else
					nonTrumpCardValue = 11;
					trumpCardValue = 13;
			}break;
			case 'j':{
				nonTrumpCardValue = 12;
				trumpCardValue = 16;
			}break;
			case 'q':{
				nonTrumpCardValue = 13;
				trumpCardValue = 11;
			}break;
			case 'k':{
				nonTrumpCardValue = 14;
				trumpCardValue = 12;
			}break;
			default:
				//System.err.println("ERROR: Should never get as far as here");
			break;
		}
	}
	
	/**
	 * Gets the suit id number of the card
	 * 
	 * @return The suit id of the card
	 */
	public int getCardSuit(){
		return suitID;
	}
	
	/**
	 * Gets the unique value of the card when its suit is NOT the trump suit
	 * 
	 * @return
	 */
	public int getNonTrumpCardValue(){
		return nonTrumpCardValue;
	}
	
	/**
	 * Gets the unique value of the card when its suit IS the trump suit
	 *  
	 * @return
	 */
	public int getTrumpCardValue(){
		return trumpCardValue;
	}
	
	/**
	 * Returns true if the card passed in is the same suit as this card
	 * 
	 * @param c The card passed in
	 * @return True if the suit of this card is equal to the suit of the passed in card
	 */
	public boolean isSameSuit(Card c){
		if(c.suitID == this.suitID)
			return true;
		
		return false;
	}
	
	/**
	 * Returns true if "this" Card is 1 of the cards which may be witheld 
	 * if a player wishes to reneg (depending on what card was led)
	 * 
	 * @return True if the cards value is 1 of the 4 best trump values
	 */
	public boolean isATopTrumpCard(Card turnedUpCard) {
		if( (isSameSuit(turnedUpCard) == true) && (trumpCardValue >= 14) )
			return true;
		
		return false;
	}
	
	/**
	 * Returns true if "this" card is a trump card
	 * 
	 * @param turnedUpCard
	 * @return
	 */
	public boolean isTrumpCard(Card turnedUpCard) {
		if( (isSameSuit(turnedUpCard) == true) || (trumpCardValue == 15) /*Joker*/ 
				|| (trumpCardValue == 14) /*Ace of Hearts*/)
			//TODO: make this a | operation to fix when a non trump card is led and then a trump card is played and the trump card loses
			return true;
		
		return false;
	}
	
	/**
	 * Finds out if 2 cards are the same
	 * 
	 * @param o the object you are comparing with this card
	 * @return True if the cards are equal
	 */
	public boolean equals(Object o){
	    //return true if they refer to the same object
	    if(o == this)
	        return true;
	    
	    //return false if its not Castable to this class
	    if(!(o instanceof Card))
	        return false;
	    Card aCard = (Card)o;
	    
	    //The cards are equal if:
	    if( (aCard.suitID == this.suitID) &&
	            	(aCard.nonTrumpCardValue == this.nonTrumpCardValue) &&
	            		(aCard.trumpCardValue == this.trumpCardValue) )
	        return true;
	    
	    return false;
	}

	/**
	 * For passing over to the client via the network
	 */
	public String toString(){
		return clientCardID;
	}
}