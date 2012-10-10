/*
 * Created on Nov 7, 2004
 */
package client.graphics;

import java.awt.Graphics;
import java.awt.Image;

import javax.swing.JPanel;

/**
 * @author Edward Dixon (CS4815 Computer Graphics, Semester 1, 2004/2005)
 * 
 * (with a few edits made by @author Pj O' Halloran)
 *  Edit 1 - made DoubleBuffer extend off of a JPanel instead of a JFrame
 *  Edit 2 - made DoubleBuffer an abstract class
 */
public abstract class DoubleBuffer extends JPanel {

	//Width of JPanel
	private int bufferWidth;
	//Height of JPanel
	private int bufferHeight;
	//Image of JPanel, to be cleared before each frame 
	private Image bufferImage;
	//JPanel Graphics object
	private Graphics bufferGraphics;
	
	/**
	 * Creates a doublebuffered JPanel
	 *
	 */
	public DoubleBuffer(){
		super(null, true);
	}	

	/**
	 * Updates the JPanel.
	 * 
	 */
	public void update(Graphics g){
		paint(g);
		//dispose of Geaphics object for performance.
		g.dispose();
	}

	/**
	 * JPanels paint method
	 * 
	 */
	public void paintComponent(Graphics g){
		resetBuffer();
		
		if(bufferGraphics != null){
			bufferGraphics.clearRect(0, 0, bufferWidth, bufferHeight);
			paintBuffer(bufferGraphics);
			g.drawImage(bufferImage, 0, 0, this);	
		}
	}
	
	/**
	 * 
	 * @param g Graphics object
	 */
	public void paintBuffer(Graphics g){
		//will be overridden in subclasses
	}
	
	/**
	 * Clears the JPanel for faster painting
	 *
	 */
	private void resetBuffer(){
		bufferWidth = getSize().width;
		bufferHeight = getSize().height;
		
		if(bufferGraphics!=null){
			bufferGraphics.dispose();
			bufferGraphics = null;	
		}
		if(bufferImage != null){
			bufferImage.flush();
			bufferImage = null;	
		}
		// Create the new image with the our size
		bufferImage=createImage(bufferWidth, bufferHeight);
		bufferGraphics=bufferImage.getGraphics();
	}
}