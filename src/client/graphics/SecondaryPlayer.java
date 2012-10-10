/*
 * Created on 02-Mar-2005
 */
package client.graphics;

/**
 * @author Pj O' Halloran
 */
public class SecondaryPlayer {

    //Name of the player
    private String name;
    //total score so far in the game of the player
    private int totalScore;
    //score for the player during the current round
    private int roundScore;
    //The number of card back faces to paint in each frame, 5 if the player has not played a card yet
    private int cardsToPaint;
    //A marker which indicates if a player is in this position
    private boolean playerHere; 
    
    /**
     * Creates a player
     * 
     * @param name The name of the player
     * @param taken Indicates if this player should be drawn onto the game screen or not 
     */
    public SecondaryPlayer(String name, boolean taken) {
        this.name = name;
        playerHere = taken;
    }

    /**
     * Creates a default player
     * 
     * @param taken Indicates if this player should be drawn onto the game screen or not
     */
    public SecondaryPlayer(boolean taken){
        this("", taken);
    }
    
    /**
     * True if a player occupies this spot on screen
     * 
     * @return
     */
    synchronized public boolean isPlayerHere(){
        return playerHere;
    }
    
    /**
     * Sets a playerHere marker to true or false
     * 
     * @param taken Passed in as true if a player is currently in this spot on screen
     */
    synchronized public void setPlayerHere(boolean taken){
        playerHere = taken;
    }
    
    /**
     * 
     * @return The name of a player
     */
    synchronized public String getName(){
        return name;
    }
    
    /**
     * Sets the player's name
     * @param temp
     */
    synchronized public void setName(String temp){
        name = temp;
    }
    
    /**
     * Increases the players score by some value, usually 5 at a time
     * @param increase
     */
    synchronized public void increaseScore(int increase){
        totalScore += increase;
        roundScore += increase;
    }
    
    /**
     * @return The total score of the player
     */
    synchronized public int getTotalScore(){
        return totalScore;
    }
    
    /**
     * @return The round score of the player
     */
    synchronized public int getRoundScore(){
        return roundScore;
    }
    
    /**
     * @return The number of cards that should be painted for this player at each frame
     */
    synchronized public int getCardsToPaint(){
        return cardsToPaint;
    }
    
    /**
     * Decrements the card to paint by 1 for this player
     *
     */
    synchronized public void decrementCardsToPaint(){
        cardsToPaint--;
    }
    
    /**
     * Sets the cardsToPaint to a new number (between 0 and 5)
     * @param num
     */
    synchronized public void setCardsToPaint(int num){
        cardsToPaint = num;
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
}