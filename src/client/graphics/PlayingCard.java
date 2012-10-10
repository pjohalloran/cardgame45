/*
 * Created on 21-Feb-2005
 */
package client.graphics;

import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;

/**
 * @author Pj O' Halloran
 */
public class PlayingCard extends Canvas {

    //The cards face image
    CardInfo cardFace;
    //The image used to stop flickering of the card during repaints
    Image doubleBuffer;
    
    //Cards set X location
    private int originalX;
    //Cards set Y location
    private int originalY;
    
    //Cards current X position on the gamePanel
    public int locX;
    //Cards current Y position on the gamePanel
    public int locY;
    
    //Is the card visibleOnTable on the game Panel?
    boolean visibleOnTable;
    //True if the card is eligible to be moved
    private boolean movable;
    
    /**
     * Constructs a playing card with:
     * 
     * @param picture The image of the card
     * @param x The cards initial x position
     * @param y The cards initial y position 
     * @param vis The inital visibility of the card on the game panel
     */
    public PlayingCard(CardInfo picture, int x, int y, boolean vis, boolean movable){
        cardFace = picture;
        
        setSize(cardFace.card.getWidth(null), cardFace.card.getHeight(null));
        
        originalX = locX = x;
        originalY = locY = y;
        
        setLocation(originalX, originalY);
        
        this.movable = movable;
        setVisible(vis);
    }
    
    /**
     * Sets the playing card to have a Cardinfo object which itself contains an image of the card
     * 
     * @param x
     */
    synchronized public void setCardFace(CardInfo x){
        cardFace = x;
    }
    
    /**
     * Paints the playing card
     */
    synchronized public void paint(Graphics g){
        g.drawImage(cardFace.card, 0, 0, null);
    }
    
    /**
     * Updates the playing card with a Double Buffering strategy attached
     */
    synchronized public void update(Graphics g){
        Dimension size = getSize();
        
        if( (doubleBuffer == null) || (doubleBuffer.getWidth(null)!=size.width) || 
                (doubleBuffer.getHeight(null)!=size.height) )
            doubleBuffer = createImage(size.width, size.height);
        
        if(doubleBuffer != null){
            Graphics g2 = doubleBuffer.getGraphics();
            paint(g2);
            g2.dispose();
            
            g.drawImage(doubleBuffer, 0, 0, null);
        }
        else{
            g.clearRect(0, 0, getWidth(), getHeight());
            paint(g);
            g.dispose();
        }
    }
    
    /**
     * Draws the card in a new location while a person is dragging the card with the mouse
     * 
     * @param x The new x location
     * @param y The new y location
     */
    synchronized public void updateLocation(int x, int y){
        locX = x;
        locY = y;
        setLocation(locX, locY);
        
        update(getGraphics());
    }
    
    /**
     * Sends the card back to its original location if it was dragged to an invalid place
     *
     */
    synchronized public void goBackToOriginalLocation(){
        updateLocation(originalX, originalY);
    }
    
    /**
     * Sets the original x location
     * @param x
     */
    public void setOriginalX(int x){
        originalX = x;
    }
    
    /**
     * Sets the original y location
     * @param y
     */
    public void setOriginalY(int y){
        originalY = y;
    }
    
    /**
     * Sets the card to be eligible to be moved or not
     * @param movable True if you currently want the card to be able to be moved by the player
     */
    public void setMovable(boolean movable){
        this.movable = movable;
    }
    
    /**
     * 
     * @return If the card is movable or not
     */
    public boolean isMovable(){
        return movable;
    }
    
    /**
     * Returns the client side id of the card
     * @return
     */
    public String getCardID(){
        return cardFace.keyID;
    }
}