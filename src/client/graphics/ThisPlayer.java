package client.graphics;

import java.awt.Point;

/**
 * Infomation about the player, playing cards at this computer
 * 
 * @author Pj O' Halloran
 */
public class ThisPlayer{
    
    //This players name
    private String name;
    //This players total score
    private int totalScore;
    //This players round score
    private int roundScore;
    //The index of the card that you wish to replace later on
    private int replaceCardIndex;
    //This players hand of cards
    private PlayingCard[] handOfCards;
    
    /**
     * Constructs this player
     * 
     * @param name The players name
     */
    public ThisPlayer(String name, Point[] locationInfo, CardInfo temp){
        this.name = name;
        totalScore = roundScore = 0;
        replaceCardIndex = -1;
        handOfCards = new PlayingCard[5];
        for(int i=0; i<handOfCards.length; i++){
            //Constructs the players cards with no pictures for now(also not visible on the table)
            handOfCards[i] = new PlayingCard(temp, locationInfo[i].x, locationInfo[i].y, false, true);
        }
    }
    
    /**
     * Gets the name of this player
     * @return
     */
    synchronized public String getName(){
        return name;
    }
    
    /**
     * Sets the name of this player
     * @param name
     */
    synchronized public void setName(String name){
        this.name = name;
    }
    
    /**
     * gets the total score of this player
     * @return
     */
    synchronized public int getTotalScore(){
        return totalScore;
    }
    
    /**
     * gets the round score of this player
     * @return
     */
    synchronized public int getRoundScore(){
        return roundScore;
    }
    
    /**
     * increases the total and round score at the end of a trick
     * @param increase
     */
    synchronized public void increaseScore(int increase){
        roundScore += increase;
        totalScore += increase;
    }
    
    /**
     * resets the round score at the beginning of a round
     *
     */
    synchronized public void resetRound(){
        roundScore = 0;
    }
    
    /**
     * resets both scores at the end of a game
     *
     */
    synchronized public void resetGame(){
        totalScore = 0;
        roundScore = 0;
    }
    
    /**
     * Get the card belonging to the player at the specified index
     * @param index
     * @return
     */
    synchronized public PlayingCard getCard(int index){
        return handOfCards[index];
    }
    
    /**
     * Replaces the card belonging to the player at the specified index
     * @return
     */
    synchronized public int getReplaceCardIndex(){
        return replaceCardIndex;
    }
    
    /**
     * 
     * @param index
     */
    synchronized public void setReplaceCardIndex(int index){
        replaceCardIndex = index;
    }
    
    /**
     * Get the array of cards belonging to the player
     * @return
     */
    synchronized public PlayingCard[] getHandArr(){
        return handOfCards;
    }
    
    /**
     * Sets the hand of cards for the player
     * 
     * @param cards An array of a datatype CardInfo which contains the image of the card and its unique id
     */
    synchronized public void setHandArr(CardInfo[] cards){
        for(int i=0; i<cards.length; i++){
            setCard(i, cards[i], true);
        }
    }
    
    /**
     * Sets the card
     * 
     * @param index The index of the card
     * @param card The new card image
     * @param vis True if you want the card to be painted each frame
     */
    synchronized public void setCard(int index, CardInfo card, boolean vis){
        handOfCards[index].setCardFace(card);
        handOfCards[index].setVisible(vis);
    } 
    
    /**
     * Returns the index of a card, returns -1 if there has been a problem
     * 
     * @param cardID The card you want to find the index for
     * @return
     */
    synchronized public int getCardIndex(String cardID){
        for(int i=0; i<handOfCards.length; i++){
            if(cardID.equals(handOfCards[i].getCardID()))
                return i;
        }
        
        return -1;
    }
    
    /**
     * Freezes or doesnt allow the hand of cards on screen to be
     * interacted it 
     * @param freeze True if you dont want the player to be able to move the cards at present
     */
    synchronized public void freezeHand(boolean freeze){
    	for(int i=0; i<this.handOfCards.length; i++)
    		handOfCards[i].setMovable(!freeze);
    }
}