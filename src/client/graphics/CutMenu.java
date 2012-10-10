/*
 * Created on 21-Feb-2005
 */
package client.graphics;

import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

import client.network.Client;

import common.Menu;

/**
 * @author Pj O' Halloran
 */
public class CutMenu extends Menu {

    //The textfield which takes in the number of cards the player wishes to cut
    private JTextField cutField;
    //A reference to the client which contains the outputstream object
    private Client myClient;
    //True if i am debugging the program
    boolean debug;
    
    /**
     * Constructs and sets up a Cutmenu
     *
     */
    public CutMenu(Client c, boolean d) {
        super("Enter the number of cards you wish to cut");
        myClient = c;
        debug = d;
        setUp();
    }
    
    /**
     * Sets up the cut menu
     *
     */
    public void setUp() {
        super.setUp(330, 100, 350, 300);
        Dimension butPanelDim = new Dimension(330, 50);
        
        JPanel textPanel = new JPanel();
        JPanel buttonPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        
        cutField = new JTextField();
        cutField.setToolTipText("Enter the number of cards to cut between 0 and 30");
        cutField.setText("0");
        
        JButton cut = new JButton("Cut");
        cut.setToolTipText("Click to cut how ever many cards you specify in the text area above");
        cut.addMouseListener(new MouseAdapter(){
			
			public void mouseClicked(MouseEvent e){
			    checkValidInput();
			    
			}
		});
        JButton noCut = new JButton("Cancel");
        noCut.setToolTipText("Click to cut no cards");
        noCut.addMouseListener(new MouseAdapter(){
			
			public void mouseClicked(MouseEvent e){
			    makeCutMessage("0");
			}
		});
        buttonPanel.add(cut);
        buttonPanel.add(noCut);
        buttonPanel.setMinimumSize(butPanelDim);
        
        textPanel.add(cutField);
        super.cPane.add(textPanel);
        super.cPane.add(buttonPanel);
    }
    
    /**
     * Displays a cut menu
     * 
     */
    public void display(){
        super.display();
    }
    
    /**
     * Hides the cut menu and resets the text on it to "0"
     * 
     */
    public void hide() {
        cutField.setText("0");
        super.hide();
    }
    
    /**
     * Display a pop up error message
     * 
     */
    public void displayError(String errorMessage) {
        super.displayError(errorMessage);
    }
    
    /**
     * When a user enters something onto the text field, this function is called and
     * checks to see if it is a number and if it is a valid number, else it will display
     * a pop up error message.
     *
     */
    private void checkValidInput() {
        String cutStr = cutField.getText().trim();
        int cutValue = 0;
        
        try {
            cutValue = Integer.parseInt(cutStr);
        }
        catch(NumberFormatException nfe) {
            System.err.println("Couldnt parse the cut value entered");
            displayError("The string you entered wasn't a valid number, please enter a number between 0 and 30.");
            return;
        }
        
        if( (cutValue<0) || (cutValue>30) ) {
            displayError("Please enter a number between 0 and 30");
            return;
        }
        
        makeCutMessage(cutStr);
    }
    
    /**
     * Makes a cut message and sends it to the Server
     * 
     * @param cutStr The string of the number of cards to be cut
     */
    private void makeCutMessage(String cutStr) {
        String message = "cCut" + "," + cutStr;
        
        if(debug)
            System.out.println(message);
        else
            myClient.sendMessage(message);
        hide();
    }
    
    /**
     * Close up the cut menu
     */
    public void closeUp() {
        super.menu.dispose();
    }
}