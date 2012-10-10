 /*
 * Created on 23-Feb-2005
 */
package common;

import java.awt.Container;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 * @author Pj O' Halloran
 */
public abstract class Menu {

    protected JFrame menu;
    protected Container cPane;
    
    /**
     * Creates a frame
     * 
     * @param title The title of the frame
     */
    public Menu(String title){
        menu = new JFrame(title);
        cPane = menu.getContentPane();
    }
    
    /**
     * Sets up the frame
     * @param width Width of the frame
     * @param height Height of the frame
     * @param x_loc Starting x location of the frame
     * @param y_loc Starting y location of the frame
     */
    public void setUp(int width, int height, int x_loc, int y_loc){
        //Close initial menu options and listener
		menu.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//draws the gui roughly in the middle of the screen
		menu.setLocation(x_loc, y_loc);
		cPane.setLayout(new BoxLayout(cPane, BoxLayout.Y_AXIS) );
		//Cant resize the menu
		menu.setResizable(false);
		menu.setSize(width, height);
		//The labels of the fields
    }
    
    /**
     * Displays the frame
     *
     */
    public void display(){
        menu.setVisible(true);
    }
    
    /**
     * Hides the frame
     *
     */
    public void hide(){
        menu.setVisible(false);
    }
    
    /**
     * Displays a pop up error message
     * 
     * @param errorName The feedback you wish to display to the user
     */
    public void displayError(String errorName){
	    JOptionPane.showMessageDialog( null, errorName, "Message Input Error", 
	      		JOptionPane.ERROR_MESSAGE);
    }
    
    public abstract void closeUp();
}