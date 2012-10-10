/*
 * Created on 21-Feb-2005
 */
package client.graphics;

import java.awt.Container;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JScrollPane;

import client.network.Client;

/**
 * @author Pj O' Halloran
 */
public class PlayScreen {
    
    //The main frame of the game screen
    private JFrame frame;
    
    //A reference to the client object which contains the important output stream
    private Client myClient;
    //The game screen where card game play takes place
    private GameArea gameScreen;
    //The message area for communicating messages to the Player from the server
    private MessageArea gameMessages;
    //This menu is displayed so that the player can enter the number of cards they wish
    //	to cut when its their turn to do so
    private CutMenu cutGUI;
    //The Hash map containing all the Cards Images and their id's
    private CardGameImages imageHashMap;
    
//    //true if i am debugging
//    private boolean debug;
    
    /**
     * Constructs the play screen object setting up both the Message area underneath and the 
     * game panel on top taking up roughly 75 % of the total panel
     *
     */
    public PlayScreen(Client c, boolean debug){
    	frame = new JFrame("Game Screen");
    	
        Container cPane = frame.getContentPane();
        
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 750);
        cPane.setLayout(new BoxLayout(cPane, BoxLayout.Y_AXIS));
        frame.setResizable(false);
        
//        this.debug = debug;
        imageHashMap = new CardGameImages();
        myClient = c;
        
        gameMessages = new MessageArea();
        
        gameScreen = new GameArea("", 1000, 600, myClient, debug, gameMessages, imageHashMap);
        
        cutGUI = new CutMenu(myClient, debug);
        
        JScrollPane scroll = new JScrollPane(gameMessages);
        scroll.setBorder(BorderFactory.createTitledBorder("Message display area"));
        scroll.setMaximumSize(new Dimension(900, 100));
        scroll.setPreferredSize(new Dimension(850, 130));
        
        cPane.add(gameScreen);
        cPane.add(scroll);
    }
    
    /**
     * Sets the order string which is used to lay out the players on screen
     * @param order
     */
    public void setOrderInfo(String order){
        gameScreen.setOrderInfo(order);
    }
    
    /**
     * First must be called in the ServerWatcher when the server is ready to begin play
     *
     */
    public void display(){
        myClient.sendMessage("cReady");
        frame.setVisible(true);
    }
    
    /**
     * Hides the frame
     *
     */
    public void hide(){
        frame.setVisible(false);
    }
    
    /**
     * Adds a message onto the message area
     * 
     * @param message The message you wish the player to recieve
     */
    public void updateMessageArea(String message){
        gameMessages.update(message);
    }
    
    /**
     * Called when the Server sends this player over their cards. It sets this players
     * cards with the right image and card id and makes the card visible on the screen
     * 
     * @param cardArr This contains: { "ah", "tc", "3s", "6h", "jd" }
     *
     */
    public void setThisPlayersHand(String[] cardArr){
        gameScreen.setThisPlayersHand(cardArr);
    }
    
    /**
     * Called when the Server tells this player that another player has recieved
     * their cards. When it is called it finds the player whos name matches the name
     * passed in and it paints 5 cards for that player
     *
     */
    public void setOtherPlayersHand(String playerName){
        gameScreen.setOtherPlayersHand(playerName);
    }
    
    /**
     * When a message is recieved from the Server for this client to cut cards
     * a pop up box is dispalyed
     *
     */
    public void cutCardEvent(){
        cutGUI.display();
    }
    
    /**
     * This method carries out a cut operation on the client side when instructed by the Server
     * 
     * @param number The number of cards to cut
     * @param nameOfCutter The name of the person who just cut
     */
    public void cutCards(String number, String nameOfCutter){
        int num = 0;
        
        try{
            num = Integer.parseInt(number);
        }
        catch(NumberFormatException nfe){
            System.err.println("The cutting string passed in is not a valid number: "+num);
        }
        
        gameMessages.update(nameOfCutter+" has cut "+num+" cards from the deck");
        gameScreen.drawCutCardsPile();
    }
    
    /**
     * Draws the turned up card in the game screen and writes out the trump suit in the message panel
     *
     */
    public void setTurnedUpCard(String cardID){
    	char suit = cardID.charAt(1);
    	
    	//write out the trump suit on the message panel
    	switch(suit){
    		case 'd':{
    			gameMessages.update("The trump suit for the round is DIAMONDS");
    		}break;
    		case 'h':{
    			gameMessages.update("The trump suit for the round is HEARTS");
    		}break;
    		case 's':{
    			gameMessages.update("The trump suit for the round is SPADES");
    		}break;
    		case 'c':{
    			gameMessages.update("The trump suit for the round is CLUBS");
    		}break;
    	}
    	
    	//set the card to be drawn
        gameScreen.setTurnedUpCard(cardID);
    }
    
    /**
     * This is called when the Server notifies a client that a rob event has
     * taken place
     * 
     * @param thisPlayer True if its this client who's robbing, false if its some other client
     * @param cardReplaceID The id of the turnedUpCard which will be placed into this players hand (if its this player who is robbing)
     * @param buffer Is eithir the card to be discarded (or) the name of the player who is robbing
     */
    public void robCardEvent(boolean thisPlayer, String turnedUpCardID, String buffer){
        
        if(thisPlayer == true){
            String discardedCardID = buffer;
            
            //If this player robbed a card
            gameScreen.replaceCard(turnedUpCardID, discardedCardID);
            gameMessages.update("You have successfully robbed the "+getCardName(turnedUpCardID)+" and replaced the "+getCardName(discardedCardID)+" in your hand.");
        }
        else{
            String playerName = buffer;
            
            //else some other player robbed a card and tell this client they did
            gameMessages.update(playerName+" has robbed the turned up card.");
        }
        //Set the turned up card (under the deck) to have a picture of the back of a card
        gameScreen.setTurnedUpCard("cardV");
    }
    
    /**
     * When a play card message is recieved, it will be drawn in the game screen
     * and a message will be written out in the text area
     *
     */
    public void playCardEvent(boolean isThisPlayer, String playerName, String cardMove){
        
        //Draw the card just played in the play area
        gameScreen.drawPlayedCard(cardMove);
        
        if(isThisPlayer){
            gameScreen.hideThisPlayersCard(cardMove);
            gameMessages.update("You have successfully played the "+getCardName(cardMove));
        }
        else{
            gameMessages.update(playerName+" has played their card");
            gameScreen.hideOtherPlayersCard(playerName);
        }
    }
    
    /**
     * updates this players name on the game screen
     * 
     * @param name
     */
    public void setThisPlayersName(String name){
        gameScreen.setThisPlayersName(name);
    }
    
    /**
     * Updates the score on both panels when a score mesage is recieved
     *
     */
    public void updateScore(boolean thisPlayer, String scoreIncrease, String playerName, boolean bonus){
        int score = 0;
        
        try{
            score = Integer.parseInt(scoreIncrease.trim());
        }
        catch(NumberFormatException nfe){
            System.err.println("Error trying to parse the score increase during the score update at the end of a trick: "+nfe.getMessage());
        }
        
        if(thisPlayer){
            if(bonus){
                gameScreen.updateThisPlayersScore(score);
                gameMessages.update("You got the best Trump for the round! Your score was increased by "+score);
                gameScreen.endRound();
                //end round
            }
            else{
                gameScreen.updateThisPlayersScore(score);
                gameMessages.update("You won the Trick! Your score was increased by "+score);
                gameScreen.endTrick();
            }
        }
        else{
            if(bonus){
                gameScreen.updateOtherPlayersScore(score, playerName);
                gameMessages.update(playerName+" got the best Trump for the round. Their score was increased by "+score);
                gameScreen.endRound();
                //end round
            }
            else{
                gameScreen.updateOtherPlayersScore(score, playerName);
                gameMessages.update(playerName+" won the Trick. Their score was increased by "+score);
                gameScreen.endTrick();               
            }
        }
    }
    
    /**
     * Updates the game screen to show a game over message
     *
     */
    public void gameOver(){
        this.gameScreen.setGameOver(true);
    }
    
    /**
     * Returns the full name of a card
     * 
     * @param cardID The string used to identify the card by the client and the server
     * @return The full name of the card
     */
    private String getCardName(String cardID) {
    	char card = cardID.charAt(0);
    	char suit = cardID.charAt(1);
    	String cardName="";
    	
    	if(cardID.equals("jok"))
    		return "Joker";
    	
    	switch(card){
    		case '2':{
    			cardName = "\"2 of";
    		}break;
    		case '3':{
    			cardName = "\"3 of";    			
    		}break;
    		case '4':{
    			cardName = "\"4 of";
    		}break;
    		case '5':{
    			cardName = "\"5 of";
    		}break;
    		case '6':{
    			cardName = "\"6 of";
    		}break;
    		case '7':{
    			cardName = "\"7 of";
    		}break;
    		case '8':{
    			cardName = "\"8 of";
    		}break;
    		case '9':{
    			cardName = "\"9 of";
    		}break;
    		case 't':{
    			cardName = "\"10 of";
    		}break;
    		case 'a':{
    			cardName = "\"Ace of";
    		}break;
    		case 'j':{
    			cardName = "\"Jack of";
    		}break;
    		case 'q':{
    			cardName = "\"Queen of";
    		}break;
    		case 'k':{
    			cardName = "\"King of";
    		}break;
    	}
    	
    	switch(suit){
    		case 'h':{
    			cardName += " Hearts\"";
    		}break;
    		case 'd':{
    			cardName += " Diamonds\"";    			
    		}break;
    		case 'c':{
    			cardName += " Clubs\"";
    		}break;
    		case 's':{
    			cardName += " Spades\"";
    		}break;
    	}
    	
    	return cardName;
    }
}