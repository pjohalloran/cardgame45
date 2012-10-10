/*
 * Created on 26-Feb-2005
 */
package client.graphics;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.Timer;

import client.network.Client;

/**
 * This class takes care of painting and updating the sub panel where the card play takes place
 * 
 * @author Pj O' Halloran
 */
public class GameArea extends DoubleBuffer 
									implements ActionListener{

    //Starts the animation loop every 50 milliseconds
    private static final int FRAME_UPDATE_GAP = 15;
    //Title of the play area box
    private static final String PLAY_AREA_TITLE = "Play Area";
    //Title of the robbing area
    private static final String ROB_AREA_TITLE = "Robbing Area";
    
    //A hashmap of all the images that i will need
    private CardGameImages imageHashMap;
    //A refernece to the client object which contains the important outstream
    private Client myClient;
    //A refernece to the message area also displayed on the main game panel
    private MessageArea gameMessages;
//    //True if i am debugging
//    private boolean debug;
    
    //The time in milliseconds that the game object was created
    private long startTime;
    //The animation loop timer
    private Timer animationTimer;
    
    //centre point of the JPanel
    private Point centre;
    //array of valid positions that a card can be set to in the play area
    private Point[] positions;
    
    //The deck
	private PlayingCard deck;
	//The pile of cut cards
	private PlayingCard cutCards;
	//The card turned up after dealing
	private PlayingCard turnedUpCard;
    //The four Cards played that appear in the play area
	private PlayingCard[] playedCards;
	
	//The index of the position of the next card to play
	private int playedCardIndex;
	
    //The image of the back of a card
    private CardInfo backCardFace;
    //the image of the back of a card on its side
    private CardInfo revBackCardFace;
    //The backgound image of the screen
    private Image backGround;
    //The image of the deck normally
    private CardInfo deckU;
    //The image of the deck when a user is clicking on it
    private CardInfo deckD;
    //The image of the deck when a user places the mouse cursor over it
    private CardInfo deckR;
    
	//An array of the other players in the game **Same order as the order string from the server**
	private SecondaryPlayer[] otherPlayers;
	//Object containing information about the player at this location 
	private ThisPlayer myPlayer;
	//boolean marker indicating if the game is over yet
	private boolean gameOver;
	
	
    /**
     * Constructs the GameArea object 
     */
    public GameArea(String orderInfo, int width, int height, Client c, boolean debugC, MessageArea g, CardGameImages cardMap) {
        super();
        
        //misc stuff
        startTime = System.currentTimeMillis();
        myClient = c;
//        this.debug = debugC;
        gameMessages = g;
        
        //create and store all the images
        setUpImages(cardMap);
        
        //set up the Secondary players
        otherPlayers = new SecondaryPlayer[3];
        for(int i=0; i<3; i++){
            otherPlayers[i] = new SecondaryPlayer("", false);
        }
        
        //Set up this player
        Point[] placeCardsArrInfo = new Point[5];
        placeCardsArrInfo[0] = new Point(250, height-120);
        placeCardsArrInfo[1] = new Point(330, height-120);
        placeCardsArrInfo[2] = new Point(410, height-120);
        placeCardsArrInfo[3] = new Point(490, height-120);
        placeCardsArrInfo[4] = new Point(570, height-120);
        
        myPlayer = new ThisPlayer("", placeCardsArrInfo, imageHashMap.getCard("temp"));
        for(int i=0; i<5; i++){
            setUpEventHandling(i);
            add(myPlayer.getCard(i));
        }
        
        //centre point of the JPanel
        centre = new Point(width/2, height/2);
        
        //positions that the cards may be placed in
        positions = new Point[4];
        positions[0] = new Point(centre.x+75, centre.y-120);
        positions[1] = new Point(centre.x+75, centre.y-70);
        positions[2] = new Point(centre.x+75, centre.y-20);
        positions[3] = new Point(centre.x+75, centre.y+30);
        
        playedCards = new PlayingCard[4];
        for(int i=0; i<playedCards.length; i++) {
            playedCards[i] = new PlayingCard(imageHashMap.getCard("temp"), positions[i].x, positions[i].y, false, false);
        }
        for(int i=playedCards.length-1; i>=0; i--){
        	add(playedCards[i]);
        }
        playedCardIndex = 0;
        
        //Set up the deck
        deck = new PlayingCard(imageHashMap.getCard("deckU"), centre.x-175, centre.y-135, true, false);
        deck.addMouseListener(new MouseAdapter(){
            
            public void mouseEntered(MouseEvent me){
                deck.setCardFace(deckR);
                deck.repaint();
            }
            
            public void mouseExited(MouseEvent me){
                deck.setCardFace(deckU);
                deck.repaint();
            }
            
            public void mousePressed(MouseEvent me){
                deck.setCardFace(deckD);
                deck.repaint();
            }
            
            public void mouseReleased(MouseEvent me){
                deck.setCardFace(deckR);
                deck.repaint();
            }
            
            public void mouseClicked(MouseEvent me){
//	            if(debug)
//	                System.out.println("cDealOut");
//	            else
	                myClient.sendMessage("cDealOut");
	            gameMessages.update("A deal is in progress");
            }
        });
        add(deck);
        
        cutCards = new PlayingCard(backCardFace, centre.x-160+deck.getWidth(), centre.y-135, false, false);
        add(cutCards);
        
        turnedUpCard = new PlayingCard(imageHashMap.getCard("temp"), centre.x-175, centre.y+135-backCardFace.card.getHeight(null), false, false);
        add(turnedUpCard);
        
        //Size up the gamepanel and make it visible
        setPreferredSize(new Dimension(width, height));
        setMinimumSize(new Dimension(width, height));
        setMaximumSize(new Dimension(width, height));
        setVisible(true);
        
        //Sets the timer off, every 1/20 of a second it will call actionPerformed which will repaint the screen
        animationTimer = new Timer(FRAME_UPDATE_GAP, this);
        animationTimer.start();
        
        gameOver = false;
    }
    
    private void setUpEventHandling(final int index){
        final PlayingCard card = myPlayer.getCard(index);
        myPlayer.getCard(index).addMouseMotionListener(new MouseMotionAdapter(){
            int i = index;
            
            public void mouseDragged(MouseEvent me) {
                if( (myPlayer.getCard(index).isMovable()) && 
                        ( (me.getX() != myPlayer.getCard(index).locX) || (me.getX() != myPlayer.getCard(index).locY) ) ){
                    myPlayer.getCard(index).updateLocation(myPlayer.getCard(index).locX + me.getX(), myPlayer.getCard(index).locY + me.getY());
                }
            }
        });
        
        myPlayer.getCard(index).addMouseListener(new MouseAdapter(){
            int i = index;
            
            public void mouseReleased(MouseEvent me){
                int halfWidth = myPlayer.getCard(index).getWidth()/2;
                int halfHeight = myPlayer.getCard(index).getHeight()/2;

                if( (myPlayer.getCard(index).locX+halfWidth<320) || (myPlayer.getCard(index).locX+halfWidth>680) || 
                        (myPlayer.getCard(index).locY+halfHeight<160) || (myPlayer.getCard(index).locY+halfHeight>440) ){
                    //if it hasnt been dragged into the playarea then send it back to its original location
                    myPlayer.getCard(index).goBackToOriginalLocation();
                    gameMessages.update("Please drag your card into the Play Box to play it or into the Robbing Box to attempt to rob the trump card");
                }
                else{
                    if( (myPlayer.getCard(index).locX+halfWidth>320) && (myPlayer.getCard(index).locX+halfWidth<320+turnedUpCard.getWidth()) 
                            && (myPlayer.getCard(index).locY+halfHeight>440-turnedUpCard.getHeight()) && (myPlayer.getCard(index).locY+halfHeight<440) ){
                        //If the card was dragged into the robbing area
//                        if(debug)
//                            System.out.println("cRobTrump,"+myPlayer.getCard(index).getCardID());
//                        else
                            myClient.sendMessage("cRobTrump,"+myPlayer.getCard(index).getCardID());
                        gameMessages.update("Attempting to rob the turned up card, please wait.");
                        myPlayer.setReplaceCardIndex(index);
                        myPlayer.getCard(index).goBackToOriginalLocation();
                    }
                    else{
//	                    if(debug)
//	                        System.out.println("cCard,"+myPlayer.getCard(index).cardFace.keyID);
//	                    else
	                        myClient.sendMessage("cCard,"+myPlayer.getCard(index).cardFace.keyID);
	                    
	                    myPlayer.getCard(index).goBackToOriginalLocation();
	                    gameMessages.update("You just attempted the play your card. Please wait for the Servers response.");
                    }
                }
            }
        });           
    }
    
    /**
     * Constructs the image map and loads in all the images needed initially by the game
     *
     */
    private void setUpImages(CardGameImages cardMap){
        imageHashMap = cardMap;
        imageHashMap.loadInAllImages();
        backCardFace = imageHashMap.getCard("cardV");
        revBackCardFace = imageHashMap.getCard("cardH");
        backGround = imageHashMap.getCardImage("bg");
        deckD = imageHashMap.getCard("deckD");
        deckU = imageHashMap.getCard("deckU");
        deckR = imageHashMap.getCard("deckR");
    }
    
    /**
     * Sets up all the secondry players on the screen
     * 
     * @param order The string sent by the server containing information on how to setup the players on the screen 
     */
    public void setOrderInfo(String order){
        String[] temp = order.split("\\,");
        String[] temp1 = {temp[1], temp[2], temp[3]};
        
        for(int i=0; i<temp1.length; i++){
        	//If there is no one at this position, then dont set up details for this player
            if(temp1[i].equals("no_one"))
                continue;
            
            otherPlayers[i].setName(temp1[i]);
            otherPlayers[i].setPlayerHere(true);
        }
    }
    
    /**
     * Paints the game panel, displaying all players cards, static objects 
     * such as the deck and the turned up card and game information
     * 
     */
    synchronized public void paint(Graphics g){
    	//Graphics object
        Graphics2D goodGraphObj = (Graphics2D)g;
        //Time elapsed since the GameArea was created
        double timeElapsed = (System.currentTimeMillis() - startTime)/1000;
        //Values used to layout the cards on screen
        int spaceY = 40, spaceX = 50,  startY = 150, startX = 360, textSpace = 20;
        
        /** Set the rendering hints of the Graphics2D object **/
        goodGraphObj.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        /** Draw the background picture every frame first thing **/
        goodGraphObj.drawImage(backGround, 0, 0, getWidth(), getHeight(), null);
        
        /** Draw the Game Over Picture here if the game is over **/
        if(gameOver){
	        goodGraphObj.setPaint(new GradientPaint(0, 0,
	                Color.lightGray,
	                getWidth()-250, getHeight(), Color.blue, false));
	        goodGraphObj.setFont(new Font("Serif", Font.BOLD, 90));
	        goodGraphObj.drawString("Game Over", getWidth()/2-230, getHeight()/2+50);
        }
        
        /** Sets the color and font for the text **/
        goodGraphObj.setColor(Color.BLACK);
        goodGraphObj.setFont(new Font("Serif", Font.BOLD, 16));
        
        /** Player 1 - This player's cards and information - Located on the Bottom  **/
        for(int i=4; i>-1; i--){
            if(myPlayer.getCard(i).isVisible())
                myPlayer.getCard(i).update(myPlayer.getCard(i).getGraphics());
        }
        goodGraphObj.drawString(myPlayer.getName(), 650, getHeight()-120);
        goodGraphObj.drawString("Total Score: "+myPlayer.getTotalScore(), 650, getHeight()-100);
        goodGraphObj.drawString("Round Score: "+myPlayer.getRoundScore(), 650, getHeight()-75);
        
        /** Player 2 - Cards and info - Located on the right **/
        if(otherPlayers[0].isPlayerHere()) {
	        for(int i=0; i<otherPlayers[0].getCardsToPaint(); i++) {
	            goodGraphObj.drawImage(revBackCardFace.card, 825, startY+(spaceY*i), null);
	        }
	        goodGraphObj.drawString(otherPlayers[0].getName(), 825, (revBackCardFace.card.getHeight(null)*5) + spaceY);
	        goodGraphObj.drawString("Total Score: "+otherPlayers[0].getTotalScore(), 825, (revBackCardFace.card.getHeight(null)*5) + spaceY + textSpace);
	        goodGraphObj.drawString("Round Score: "+otherPlayers[0].getRoundScore(), 825, (revBackCardFace.card.getHeight(null)*5) + spaceY + (textSpace*2));
        }
        
        /** Player 3 - Cards and info - Located on the top **/
        if(otherPlayers[1].isPlayerHere()) {
	        for(int i=0; i<otherPlayers[1].getCardsToPaint(); i++) {
	            goodGraphObj.drawImage(backCardFace.card, startX+(spaceX*i), 40, null);
	        }
	        goodGraphObj.drawString(otherPlayers[1].getName(), (backCardFace.card.getWidth(null)*9), 60);
	        goodGraphObj.drawString("Total Score: "+otherPlayers[1].getTotalScore(), (backCardFace.card.getWidth(null)*9), 60+textSpace);
	        goodGraphObj.drawString("Round Score: "+otherPlayers[1].getRoundScore(), (backCardFace.card.getWidth(null)*9), 60+(textSpace*2));
        }
        
        /** Player 4 - Cards and info - Located on the left **/
        if(otherPlayers[2].isPlayerHere()){
	        for(int i=0; i<otherPlayers[2].getCardsToPaint(); i++) {
	            goodGraphObj.drawImage(revBackCardFace.card, 75, startY+(spaceY*i), null);
	        }
	        goodGraphObj.drawString(otherPlayers[2].getName(), 75, (revBackCardFace.card.getHeight(null)*5) + spaceY);
	        goodGraphObj.drawString("Total Score: "+otherPlayers[2].getTotalScore(), 75, (revBackCardFace.card.getHeight(null)*5) + spaceY + textSpace);
	        goodGraphObj.drawString("Round Score: "+otherPlayers[2].getRoundScore(), 75, (revBackCardFace.card.getHeight(null)*5) + spaceY + (textSpace*2));
        }
        
        /** Play Area - Located in the centre of the screen **/
        goodGraphObj.setColor(Color.LIGHT_GRAY);
        goodGraphObj.drawRect(centre.x-180, centre.y-140, 360, 280);
        goodGraphObj.setColor(Color.BLACK);
        goodGraphObj.drawString(PLAY_AREA_TITLE, centre.x - 25, centre.y - 150);
        deck.repaint();
        if(cutCards.isVisible())
            cutCards.update(cutCards.getGraphics());
        for(int i=3; i>=0; i--) {
            if(this.playedCards[i].isVisible())
                playedCards[i].update(playedCards[i].getGraphics());
        }
        
        /** Robbing area  - Located on the left side of the "Play Area" **/
        goodGraphObj.drawString(ROB_AREA_TITLE, centre.x-179, centre.y+135-turnedUpCard.getHeight()-25);
        goodGraphObj.setColor(Color.LIGHT_GRAY);
        goodGraphObj.drawRect(centre.x-180, centre.y+140-turnedUpCard.getHeight()-20, turnedUpCard.getWidth()+20, turnedUpCard.getHeight()+20);
        if(turnedUpCard.isVisible())
            turnedUpCard.update(turnedUpCard.getGraphics());
    }
    
    /** 
     * Instructs the JPanel to be updated when the Event Dispatching/AWT thread gets a chance 
     */
    public void update(Graphics g){
        paint(g);
        g.dispose();
    }
    
    /**
     * Repaints the screen and determines what actions were just taken on the screen
     */
    public void actionPerformed(ActionEvent evt) {
        //Performs a frame update every ?? milliseconds
        repaint();
    }
    
    /**
     * Sets this players hand to be drawn after a deal
     * 
     * @param cardArr
     */
    public void setThisPlayersHand(String[] cardArr){
        CardInfo[] cards = new CardInfo[cardArr.length];
        
        for(int i=0; i<cards.length; i++)
        	//get the image for this card
            cards[i] = imageHashMap.getCard(cardArr[i]);
        
        myPlayer.setHandArr(cards);
    }
    
    /**
     * Set the 5 cards for one of the other players after a deal
     * 
     * @param playerName
     */
    public void setOtherPlayersHand(String playerName){
        
        for(int index = 0; index<otherPlayers.length; index++){
            if( (otherPlayers[index].isPlayerHere()) && (otherPlayers[index].getName().equals(playerName)) )
                //if there is a player at this position and the names match, then set for 5 cards to be drawn
                otherPlayers[index].setCardsToPaint(5);
        }
    }
    
    /**
     * Replace a card in this players hand when a rob event is successful
     * 
     */
    synchronized public void replaceCard(String cardID, String discardedCardID){
        CardInfo replaceCard = imageHashMap.getCard(cardID);
        int indexOfDiscardedCard = myPlayer.getCardIndex(discardedCardID);
        
        if(indexOfDiscardedCard == -1)
            System.out.println("Error replacing a card"+cardID+"after a Rob, with "+discardedCardID);
        
        myPlayer.setCard(indexOfDiscardedCard, replaceCard, true);
    }
    
    /**
     * Set the turned up card to some new value
     * 
     * @param cardID
     */
    synchronized public void setTurnedUpCard(String cardID){
        CardInfo cardImage = imageHashMap.getCard(cardID);
        
        turnedUpCard.setCardFace(cardImage);
        turnedUpCard.setVisible(true);
    }
    
    /**
     * Draw the pile of cut cards on screen
     *
     */
    synchronized public void drawCutCardsPile(){
        this.cutCards.setVisible(true);
    }
    
    /**
     * Draw a card which was played in the play area
     * 
     * @param cardID
     */
    synchronized public void drawPlayedCard(String cardID){
    	//take out in finished game
        if(playedCardIndex > 3)
            System.out.println("Array error in drawPlayedCard()");
        
    	//take out in finished game
        CardInfo playedCard = imageHashMap.getCard(cardID);
        
        playedCards[playedCardIndex].setCardFace(playedCard);
        playedCards[playedCardIndex].setVisible(true);
        
        playedCardIndex++;
    }
    
    /**
     * Hide one of this players card when they make a move
     * 
     * @param cardID
     */
    synchronized public void hideThisPlayersCard(String cardID){
        int index = myPlayer.getCardIndex(cardID);
        
        if(index == -1)
            System.out.println("Index problem hiding this players card:"+cardID);
        
        myPlayer.setCard(index, imageHashMap.getCard("temp"), false);
    }
    
    /**
     * Hide a one of some other players cards when they make a move
     * 
     * @param playerName
     */
    synchronized public void hideOtherPlayersCard(String playerName){
        for(int i=0; i<otherPlayers.length; i++){
            if( (otherPlayers[i].isPlayerHere()) && (otherPlayers[i].getName().equals(playerName)) )
                otherPlayers[i].decrementCardsToPaint();
        }
    }
    
    /**
     * Updates this players score if and when a score message comes in at the end of a trick
     * 
     * @param score
     */
    synchronized public void updateThisPlayersScore(int score){
        myPlayer.increaseScore(score);
    }
    
    /**
     * Updates a players score if and when a score message comes in at the end of a trick
     * 
     * @param score
     * @param playerName
     */
    synchronized public void updateOtherPlayersScore(int score, String playerName){
        for(int i=0; i<otherPlayers.length; i++){
            if( (otherPlayers[i].isPlayerHere()) && (otherPlayers[i].getName().equals(playerName)) )
                otherPlayers[i].increaseScore(score);
        }
    }
    
    /**
     * Performs a end of trick cleanup
     * 
     * Hides the players cards, the pile of cut cards and the turned up Card at the end of a trick
     *
     */
    synchronized public void endTrick() {
        //hides the cards at the end of a trick after a delay
        //to give people time to see what was played
    	final Timer cardRemovalTimer = new Timer(500, new ActionListener() {
    	    public void actionPerformed(ActionEvent evt) {
    	        //hide this players cards
    	        for(int i=0; i<playedCards.length; i++) {
    	        	//TODO: Will i remove this temp line just below this?
    	            playedCards[i].setCardFace(imageHashMap.getCard("temp"));
    	            playedCards[i].setVisible(false);
    	        }
    	        playedCardIndex=0;
    	        
    	        myPlayer.freezeHand(false);
    	        gameMessages.update("Trick is over, a new one is beginning");
    	    }    
    	});
    	
    	cardRemovalTimer.setRepeats(false);
    	cardRemovalTimer.start();
    	
    	myPlayer.freezeHand(true);
    }
    
    /**
     * Set this players name
     * 
     * @param name
     */
    synchronized public void setThisPlayersName(String name){
        this.myPlayer.setName(name);
    }
    
    /**
     * Sets the game over flag to true
     * 
     * @param game
     */
    synchronized public void setGameOver(boolean game){
    	this.gameOver = game;
    }
    
    /**
     * Performs an end of round cleanup on the game screen
     *
     */
    synchronized public void endRound(){
        //reset the end of round score for this player
    	myPlayer.resetRound();
    	
    	//reset the end of round scores for the other player
    	for(int i=0; i<this.otherPlayers.length; i++) {
    		//if(otherPlayers[i).isPlayerHere())
    		if(otherPlayers[i] != null)
    			otherPlayers[i].resetRound();
    	}
    	
        //hide the cut card pile and the turned up card
        this.turnedUpCard.setVisible(false);
        this.cutCards.setVisible(false);
    }
}