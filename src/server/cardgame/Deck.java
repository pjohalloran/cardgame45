/*
 * Created on 25-Jan-2005
 */
package server.cardgame;

import java.util.ArrayList;
import java.util.Collections;

/**
 * @author Pj O' Halloran
 * 
 * A Deck of cards object. Cards can only be taken off the 
 * top of the deck. 
 */
public class Deck {

	private static final int INITIAL_DEFAULT_CAPACITY = 53;//Joker card included
	private static final int NUM_OF_SUITS = 4;
	
	//deck
	private ArrayList cardDeck;
	//cards in play
	private ArrayList removedCards;
	//Boolean marker to indicate if a deck is stripped or not
	private boolean stripped;
	
	/**
	 * Creates the default deck, which has the Joker included and 
	 * is stripped of all low value cards
	 *
	 */
	public Deck(){
		this(true, true);
	}
	
	/**
	 * Creates a custom deck
	 * 
	 * @param jokerIncluded True if the joker card is included
	 * @param strip True if the deck must be stripped, ie number of players = 2 
	 */
	public Deck(boolean includeJoker, boolean strip){
		removedCards = new ArrayList();
		stripped = strip;
		if(includeJoker == true)
			cardDeck = new ArrayList(INITIAL_DEFAULT_CAPACITY);
		else
			cardDeck = new ArrayList(INITIAL_DEFAULT_CAPACITY-1);
		init(includeJoker, strip);
	}
	
	/**
	 * Initialises the cards and places them in the deck
	 *
	 */
	public void init(boolean includeJoker, boolean toStrip){
		for(int suitI=0; suitI<NUM_OF_SUITS; suitI++){
			//for(int cardI=2; cardI<14; cardI++){
			for(int cardI=2; cardI<15; cardI++) {
				if( (toStrip==true) & (cardI<5) )
					//don't add unneeded cards i.e. the 2, 3 or 4 of "SUIT_NAME"
					continue;
				
				//Temp Bug fix
				Card x = new Card(cardI, suitI);
				if(x.toString() == null)
					continue;
				//End temp bug fix
				
				cardDeck.add(new Card(cardI, suitI));
			}
		}
		//add the special case cards, ie 1) the Ace Of Hearts, 2) Joker and 3) the Ace of Diamonds
		if(includeJoker == true)
			cardDeck.add(new Card("jok"));
		
		cardDeck.add(new Card("ah"));
		cardDeck.add(new Card("ad"));
		shuffle();
	}
	
	public int getCurrentDeckSize(){
		return cardDeck.size();
	}
	
	/**
	 * Performs post round clean up of the deck.
	 * Must be called when a round is over or else
	 * there will be cards missing from the deck
	 * when they are dealed out again.
	 *
	 */
	public void postRound(){
		cardDeck.addAll(removedCards);
		removedCards.clear();
		shuffle();
	}
	
	/**
	 * Shuffles the deck
	 *
	 */
	public void shuffle(){
		Collections.shuffle(cardDeck);
	}
	
	/**
	 * Reinserts the low cards into the deck
	 *
	 */
	public void insertStrippedCards(){
		if(stripped == true){
			for(int suitI=0; suitI<NUM_OF_SUITS; suitI++){
				for(int cardI=0; cardI<5; cardI++){
					cardDeck.add(new Card(cardI, suitI));
				}
			}
			shuffle();
		}
	}
	
	/**
	 * Taking off 1 card at a time off the deck
	 * 
	 * @return the Card on the top of the deck
	 */
	public Card removeCard(){
		Card topCard;
		topCard = (Card)cardDeck.remove(0);
		removedCards.add(topCard);
		
		return topCard;
	}
	
	/**
	 * Used when taking off multiple cards from the deck,
	 * as when dealing is taking place.
	 * 
	 * @param number Number of cards to be taken off the deck
	 * @return the Cards 
	 */
	public Card[] removeCards(int number){
		Card cardArr[] = new Card[number];
		
		for(int i=0; i<number; i++){
			//cardArr[i] = (Card)cardDeck.remove(i);
			cardArr[i] = (Card)cardDeck.remove(0);
			removedCards.add(cardArr[i]);
		}
		
		return cardArr;
	}
	
	/**
	 * Used for cutting cards from the deck
	 * 
	 * @param number The number of cards to be cut
	 */
	public void cutCards(int number){
		Card temp;
		
		for(int i=0; i<number; i++){
			//temp = (Card)cardDeck.remove(i);
			temp = (Card)cardDeck.remove(0);
			removedCards.add(temp);
		}
	}
	
	/**
	 * for debugging
	 *
	 */
	public void printDeck(){
		for(int i=0; i<this.cardDeck.size(); i++){
			Card x = (Card)cardDeck.get(i);
			System.out.println(i+" " +x.toString());
		}
	}
}