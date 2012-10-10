/*
 * Created on 21-Feb-2005
 */
package client.graphics;

import java.awt.Font;

import javax.swing.JTextArea;

/**
 * @author Pj O' Halloran
 */
public class MessageArea extends JTextArea{

    //The font used in the message area
    Font specialText;
    
    /**
     * Creates a area to display messages to the player from the Server or about GamePlay
     * 
     */
    public MessageArea() {
        super();
        specialText = new Font("Serif", Font.BOLD, 16);
        
        super.setEditable(false);
        super.setFont(specialText);
        super.setLineWrap(true);
        super.setWrapStyleWord(true);
    }
    
    /**
     * Updates the Message area with a message about gameplay to provide feedback to the player
     * 
     * @param message The message you wish to display to the Player
     */
    synchronized public void update(String message) {
        super.append("SERVER>" + message + "\n");
    }
    
    /**
     * Clear the textarea. Should be called after every round. After
     * every game at least
     *
     */
    synchronized public void clear(){
        this.replaceRange("", 0, getText().length());
    }
}