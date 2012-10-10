/*
 * Created on 21-Feb-2005
 */
package client.network;

import java.io.BufferedReader;
import java.io.IOException;

import javax.swing.SwingUtilities;

import client.graphics.PlayScreen;

/**
 * @author Pj O' Halloran
 */
public class ServerWatcher extends Thread {

    //The inputstream for recieving messages from the Server
    private BufferedReader in;
    //The reference to the client object containing the important outputstream
    private Client myClient;
    //Gives this thread control of the waitScreen to update it
    private GameWaitScreen waitScreen;
    //Gives this thread control of the PlayScreen to update it
    private PlayScreen mainGame;
    
    /**
     * Creates a thread to handle messages recieved from the server
     * 
     * @param in The inputstream which messages from the server are passed along
     * @param c A refernece to the Client object
     */
    public ServerWatcher(BufferedReader in, Client c, PlayScreen p, GameWaitScreen w){
        this.in = in;
        waitScreen = w;
        mainGame = p;
        myClient = c;
    }
    
    /**
     * Called when the thread is started
     */
    public void run() {
        String buffer;
        
        try{
	        while( (buffer = in.readLine() ) != null) {
	            System.out.println("line recieved from server: " + buffer);
	            doRequest(buffer);
	        }
        }
        catch(IOException ioe) {
            System.out.println("IO Exception occured in ServerWatcher: "+ioe.getMessage());
            System.exit(-1);
        }
    }

    /**
     * Takes a message from the server and acts on it manipulating the Clients menus and screens to provide
     * game feedback to the player
     * 
     * Messages from the Server to all Clients
     * 
     * 1  sGRules						A request to pick the game and its rules
     * 2  sDealer,NAME					Letting a client know who the current dealer is
     * 3  sCut							A request for a client to cut some cards
     * 4  sCut,NUM,NAME					Letting a client know who cut some cards and how many they cut from the deck
     * 5  sDeal,_,_,_,_,_				**Lets a client know what cards they got in a deal and who got their cards first
     * 6  sTrump,_						Lets a client know the turned up card on the deck
     * 7  sNoRob						Tells a client that they are ineligible to rob
     * 8  sRobTrump,_,_					Tells a client they did rob and passes back the robbed card and the card to discard out of this players hand
     * 9  sRobTrump,_,NAME				Tells all clients who robbed the card
     * 10 sCardRej,(Reneg or Ruff)		Tells a clients their move was rejected and why?
     * 11 sCard,NAME,_					Tells a client who made a move
     * 12 sScore,INCREASE,NAME			Tells a client to update the score for a particular player
     * 13 sScore,INCREASE,NAME,BTBonus	Tells a client to award a best trump bonus to a player
     * 14 sAutoWin,NAME					Tells a client who autowon the game
     * 15 sWin,NAME						Tells a client who won the game
     * 16 sTurn,NAME					Tells a client whos turn it is
     * 17 sBegin						(Look in Server)
     * 18 sInGame						Lets a client know they have been added to the Server for the game
     * 19 sNotInGame					Lets a client know they have NOT been added to the Server for the game
     * 20 sOrder,NAME,NAME,NAME			Lets the client know how to lay out their play screens
     * 21 sInvalid						Lets the client know that the move they just made was not acceptable at this point
     */
    public void doRequest(String command){
        
        if(command.equals("sGRules")) {
            Runnable displayGMenu = new Runnable() {
                public void run() {
                    //Client gets displayed the game menu and must pick the rules
                    GameMenu game = new GameMenu(myClient, false);
                }
            };
            SwingUtilities.invokeLater(displayGMenu);
        }
        else if(command.equals("sWait")) {
            Runnable displayWaitScreen = new Runnable(){
                public void run(){
                    waitScreen.display();
                }
            };
            SwingUtilities.invokeLater(displayWaitScreen);
        }
        else if(command.startsWith("sDealer,")){
            final String[] temp = command.split("\\,");
            
            Runnable stateTheDealer = new Runnable() {
                public void run() {
                    mainGame.updateMessageArea(temp[1].toUpperCase()+", Its your turn to Deal out the cards");
                }
            };
            SwingUtilities.invokeLater(stateTheDealer);
        }
        else if(command.equals("sCut")){
            Runnable startCutProcess = new Runnable() {
                public void run(){
                    mainGame.cutCardEvent();
                }
            };
            SwingUtilities.invokeLater(startCutProcess);
        }
        else if(command.startsWith("sCut,")){
            final String[] temp = command.split("\\,");
            
            Runnable cutCards = new Runnable() {
                public void run(){
                    mainGame.cutCards(temp[1].trim(), temp[2].trim());
                }
            };
            SwingUtilities.invokeLater(cutCards);
        }
        else if(command.startsWith("sDeal,")){
            final String[] temp = command.split("\\,");
            
	            Runnable dealCards = new Runnable() {
	                public void run() {
	                    if(temp.length == 2)
	                        //If another player recieved their cards { "sDeal", "NAME" }
	                        mainGame.setOtherPlayersHand(temp[1].trim());
	                    else{
	                        //else if this player has recieved their cards { "sDeal", "ah", "td", "3c", "js", "5h" }
	        	            String[] tempCards = new String[5]; 
	        	            System.arraycopy(temp, 1, tempCards, 0, temp.length-1);
		                    mainGame.setThisPlayersHand(tempCards);
	                    }
	                }
	            };
	            SwingUtilities.invokeLater(dealCards);
        }
        else if(command.startsWith("sTrump,")){
            final String[] temp = command.split("\\,");
            
            Runnable setTrump = new Runnable() {
                public void run(){
                    mainGame.setTurnedUpCard(temp[1]);
                }
            };
            SwingUtilities.invokeLater(setTrump);
        }
        else if(command.equals("sNoRob")){
            Runnable updateMessage = new Runnable() {
                public void run(){
                    mainGame.updateMessageArea("You are ineligible to rob the Trump Card");
                }
            };
            SwingUtilities.invokeLater(updateMessage);
        }
        else if(command.startsWith("sRobTrump,")){
            final String[] temp = command.split("\\,");
            
            Runnable robMove = new Runnable() {
                public void run(){
                    //if the length of the third part is 2, then it is a card, not a name of a player
                    if(temp[2].length() == 2)
                        //Your rob was successful ie temp = {"sRobTrump", "ah", "3c"}
                        mainGame.robCardEvent(true, temp[1], temp[2]);
                    else
                        //Someone elses rob was successful ie temp = {"sRobTrump", "ah", "NAME_OF_ROBBER"}
                        //mainGame.robCardEvent(false, temp[1], myClient.getNickName());
                    	mainGame.robCardEvent(false, temp[1], temp[2]);
                }
            };
            SwingUtilities.invokeLater(robMove);
        }
        else if(command.equals("sRobJoker")){
            Runnable robJok = new Runnable() {
                public void run(){
                	mainGame.updateMessageArea("You must rob the Joker before the game can continue!");
                }
            };
            SwingUtilities.invokeLater(robJok);
        }
        else if(command.startsWith("sCardRej,")){
            final String temp[] = command.split("\\,");
            
            Runnable rejectMove = new Runnable() {
                public void run(){
                    if(temp[1].equals("Reneg")){
                        //The players move was rejected for Reneging 
                        mainGame.updateMessageArea("Move was rejected as you are withholding a trump card");
                    }
                    else{
                        //The players move was rejected for Ruffing
                        mainGame.updateMessageArea("Move was rejected as you must eithir a) follow suit OR b) play any trump card when a card of non trump was led");
                    }
                }
            };
            SwingUtilities.invokeLater(rejectMove);
        }
        else if(command.startsWith("sCard,")){
            final String temp[] = command.split("\\,");
            
            Runnable playMove = new Runnable() {
                public void run(){
                    boolean thisPlayer = (myClient.getNickName().equals(temp[1])) ? true : false; 
                    
                    mainGame.playCardEvent(thisPlayer, temp[1], temp[2]);
                }
            };
            SwingUtilities.invokeLater(playMove);
        }
        else if(command.startsWith("sScore,")){
            //NB This is an indicator that the end of a trick has been reached
            	//There are 5 tricks in a round
            final String[] temp = command.split("\\,");
            
            Runnable endTrick = new Runnable() {
                public void run(){
                    boolean thisPlayer = (myClient.getNickName().equals(temp[2])) ? true : false;
                    boolean bonus = (temp.length == 4) ? true : false ;
                    
                    mainGame.updateScore(thisPlayer, temp[1], temp[2], bonus);
                }
            };
            SwingUtilities.invokeLater(endTrick);
        }
        else if(command.startsWith("sAutoWin,")){
            final String[] temp = command.split("\\,");
            
            Runnable autoWin = new Runnable() {
                public void run(){
                    mainGame.updateMessageArea(temp[1]+" won the game because he/she won all 5 tricks in the last round");
                    mainGame.gameOver();
                }
            };
            SwingUtilities.invokeLater(autoWin);
        }
        else if(command.startsWith("sWin,")){
            final String[] temp = command.split("\\,");
            
            Runnable win = new Runnable() {
                public void run(){
                    mainGame.updateMessageArea(temp[1]+" won the game because he/she reached the target score first");
                    mainGame.gameOver();
                }
            };
            SwingUtilities.invokeLater(win);
        }
        else if(command.startsWith("sTurn,")){
            final String[] temp = command.split("\\,");
            
            Runnable indicateTurn = new Runnable() {
                public void run(){
                    mainGame.updateMessageArea(temp[1].toUpperCase()+", It's your turn to play a card!");
                }
            };
            SwingUtilities.invokeLater(indicateTurn);
        }
        else if(command.equals("sBegin")){
            Runnable startGame = new Runnable() {
                public void run(){
                    waitScreen.hide();
                    System.out.println("Hiding the Wait screen, and displaying the game");
                    mainGame.display();
                }
            };
            SwingUtilities.invokeLater(startGame);
        }
        else if(command.equals("sInGame")){
            System.out.println("Player has been added to the game");
            //get this when a player is added to the group, after sending their name over
        }
        else if(command.equals("sNotInGame")) {
            System.out.println("Player has not been added to the game");
        }
        else if( (command.startsWith("sGame")) || (command.startsWith("sORDER,")) ){
            final String message = command;
            
            Runnable updateStatus = new Runnable() {
                public void run(){
                    waitScreen.update(message);
                }
            };
            SwingUtilities.invokeLater(updateStatus);
        }
        else if(command.startsWith("sInvalid,")){
            final String[] temp = command.split("\\,");
        	
            //If you make an invalid move for this stage in the game
            Runnable error = new Runnable() {
                public void run(){
                	if(temp[1].equals("Deal")){
                		mainGame.updateMessageArea("Its not your turn to deal out the cards, Please wait!");
                	}
                	else if(temp[1].equals("NotTime")){
                		//mainGame.updateMessageArea("Its not time to make that move yet.");
                	}
                	else if(temp[1].equals("Card")){
                		mainGame.updateMessageArea("Its not your turn to play a card, Please wait!");                		
                	}
                	else if(temp[1].equals("End")){
                		mainGame.updateMessageArea("The game is over, please restart.");
                	}
                	else{
                		System.out.println("Invalid message recieved");
                	}
                }
            };
            SwingUtilities.invokeLater(error);
        }
        else{
            System.out.println("Unknown command recieved from Server: " + command);
        }
    }
}