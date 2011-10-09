package ed.projector;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class Projection extends JFrame {
	
	private static int WIDTH = 400;
	private static int HEIGHT = 400;
	
	//ImageIcon used to display image on projector
	private ImageIcon m_image;
	
	//JLabel that displays image
	private JLabel m_lbl;
	
	//counter, keeps track of current image
	//only useful in the prototype until we can
	//decide image properly!
	private int m_counter;
	
	//ArrayList to hold all the images to switch between
	ArrayList<String> m_list = new ArrayList<String>();
		
	/*
	 * Constructor
	 */
	public Projection(){
		//add images to list
		setupList();
		//initialise image icon to first image
		m_image = createImageIcon(m_list.get(0));
		//initialise counter (to 1 as already have 0 loaded)
		m_counter = 1;
		
		//create label, attach image
		m_lbl = new JLabel(m_image);
		
		//no padding layout
		setLayout(new FlowLayout(FlowLayout.LEFT,0,0));		
		
		//jpanel solves the issue of having a 500x500 contentpane
		//as appose to 500x500 frame
	    JPanel sizePanel = new JPanel();
	    sizePanel.setLayout(new FlowLayout(FlowLayout.LEFT,0,0));
	    sizePanel.setPreferredSize(new Dimension(WIDTH,HEIGHT));
	    sizePanel.add(m_lbl);
		this.getContentPane().add(sizePanel);
	}
	
	/*
	 * Called during initiation,
	 * adds all the images to the arraylist
	 */
	public void setupList() {
		int i;
		for(i = 0; i < 5; i++){
			m_list.add("images/test" + i + ".jpg");
		}
	}

	/*
	 * Called whenever the button on the control frame
	 * is pressed. Switches the image displayed.
	 */
	public void switchImage() {
		//set imageicon to next image in the list
		m_image = createImageIcon(m_list.get(m_counter));
		//if adding one will push beyond array bounds
		if(m_counter == (m_list.size()-1)){
			//reset to 0
			m_counter = 0;
		} else {
			//else add 1
			m_counter++;
		}
		//redraws the image.
		m_lbl.setIcon(m_image);
		this.repaint();
	}
	
	/*
	 *  Returns an ImageIcon, or null if the path was invalid. 
	 */
	protected ImageIcon createImageIcon(String path) {
	    java.net.URL imgURL = getClass().getResource(path);
	    if (imgURL != null) {
	        return new ImageIcon(imgURL);
	    } else {
	        System.err.println("Couldn't find file: " + path);
	        return null;
	    }
	}
}
