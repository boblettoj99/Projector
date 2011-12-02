package ed.projector;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/*
 * Main frame for pushing buttons: AKA Battle Station
 */
@SuppressWarnings("serial")
public class GUI extends JFrame implements ActionListener, ChangeListener{
	
	//On press, game begins
	public final JButton m_beginBtn = new JButton("Begin game!");
	//On press, plays the next go (same as projected)
	public final JButton m_hintBtn = new JButton("Play hint");
	//on press, plays the move entered in the box
	public final JButton m_moveBtn = new JButton("Play custom move");
	//drop down list, select from and to move
	public final JComboBox m_selectFrom = new JComboBox();
	public final JComboBox m_selectTo = new JComboBox();
	//On slide, changes size of chess board
	public final JSlider m_sizeSlider = new JSlider();
	//On slide, changes x and y coordinates of grid
	public final JSlider m_xSlider = new JSlider(0, 800);
	public final JSlider m_ySlider = new JSlider(0, 600);
	//checkboxes for drawing grid/square
	public final JCheckBox m_setGrid = new JCheckBox();
	public final JCheckBox m_setSquare = new JCheckBox();
	//JPanel and icon for drawing camera image
	public final JPanel m_cameraPanel = new JPanel();
	public final JLabel m_cameraLabel = new JLabel("[none]");
	//Parent class Init
	public Init m_parent;
	
	//initial sizes of grids (for different chess boards)
	public final static int SMALL_BOARD = 40;
	public final static int SALLY_ARMY_BOARD = 69;
	
	
	/*
	 * constructor
	 */
	public GUI(Init parent, GraphicsConfiguration gc){
		super(gc);
		this.setTitle("Controls");
		//assign listeners
		m_beginBtn.addActionListener(this);
		m_hintBtn.addActionListener(this);
		m_moveBtn.addActionListener(this);
		m_sizeSlider.addChangeListener(this);
		m_xSlider.addChangeListener(this);
		m_ySlider.addChangeListener(this);
		m_setGrid.addActionListener(this);
		m_setSquare.addActionListener(this);
		m_selectFrom.addActionListener(this);
		m_selectTo.addActionListener(this);
		m_parent = parent;
		//initiated for salvation army chess set
		m_sizeSlider.setValue(SMALL_BOARD);
		m_xSlider.setValue(400);
		m_ySlider.setValue(300);
		//set to move to disabled until from is chosen
		m_selectTo.setEnabled(false);
		//set others to disabled until begin game is pressed
		m_hintBtn.setEnabled(false);
		m_selectFrom.setEnabled(false);
		m_moveBtn.setEnabled(false);
		//labels
		JLabel sizeLabel = new JLabel("Grid size: ");
		JLabel xLabel = new JLabel("Grid x position: ");
		JLabel yLabel = new JLabel("Grid y position: ");
		JLabel fromLabel = new JLabel("From: ");
		JLabel toLabel = new JLabel("To: ");
		JLabel gridLabel = new JLabel("Show grid: ");
		JLabel squareLabel = new JLabel("Show square: ");
		m_cameraPanel.add(m_cameraLabel);
		m_cameraPanel.setPreferredSize(new Dimension(480, 640));
		//add it all
		this.setLayout(new FlowLayout());
		this.getContentPane().add(xLabel);
		this.getContentPane().add(m_xSlider);
		this.getContentPane().add(yLabel);
		this.getContentPane().add(m_ySlider);	
		this.getContentPane().add(sizeLabel);
		this.getContentPane().add(m_sizeSlider);	
		this.getContentPane().add(gridLabel);
		this.getContentPane().add(m_setGrid);
		this.getContentPane().add(squareLabel);
		this.getContentPane().add(m_setSquare);
		this.getContentPane().add(m_beginBtn);
		this.getContentPane().add(m_hintBtn);
		this.getContentPane().add(fromLabel);
		this.getContentPane().add(m_selectFrom);
		this.getContentPane().add(toLabel);
		this.getContentPane().add(m_selectTo);
		this.getContentPane().add(m_moveBtn);
		this.getContentPane().add(m_cameraPanel);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == m_hintBtn){
			//play hint previously suggested
			m_parent.executeHint();
			//find and show next hint
			String next = m_parent.getNextHint();
			m_parent.nextHint(next);
			//disable move buttons until from has been chosen
			m_selectTo.setEnabled(false);
			m_moveBtn.setEnabled(false);
			//update the from options
			updateComboBox();
		}else if(e.getSource() == m_moveBtn){
			//plays the move selected
			m_parent.executeMove((String)m_selectFrom.getSelectedItem()
					+ (String)m_selectTo.getSelectedItem());
			//System.out.println("Moved");
			//same as above
			String next = m_parent.getNextHint();
			m_parent.nextHint(next);
			m_selectTo.setEnabled(false);
			m_moveBtn.setEnabled(false);
			updateComboBox();
		}else if(e.getSource() == m_selectFrom && m_selectFrom.isEnabled()){
			if(m_selectFrom.getSelectedItem() != null){
				//must protect against null as event always
				//fired when updating options.
				//update to options.
				updateComboBox2();
				m_moveBtn.setEnabled(true);
			}
		}else if(e.getSource() == m_setGrid){
			//show grid
			m_parent.showLines(m_setGrid.isSelected(), m_setSquare.isSelected());
		}else if(e.getSource() == m_setSquare){
			//show board outline
			m_parent.showLines(m_setGrid.isSelected(), m_setSquare.isSelected());
		}else if(e.getSource() == m_beginBtn){
			//begin the game
			//populate from options
			updateComboBox();
			//enable buttons
			m_hintBtn.setEnabled(true);
			m_selectFrom.setEnabled(true);
			//show first hint
			String next = m_parent.getNextHint();
			m_parent.nextHint(next);
		}
	}
	
	@Override
	public void stateChanged(ChangeEvent e){
		if(e.getSource() == m_sizeSlider){
				int i = m_sizeSlider.getValue();
				m_parent.updateGridSize(i);
		}else if(e.getSource() == m_xSlider || e.getSource() == m_ySlider){
				int x = m_xSlider.getValue();
				int y = m_ySlider.getValue();
				m_parent.shiftGrid(x, y);
		}
	}
	
	public void updateCameraImage(Image im){
		ImageIcon icon = new ImageIcon(im);
		m_cameraLabel.setIcon(icon);
		this.repaint();
	}
	
	public void updateComboBox(){
		//finds squares with pieces on it and adds to options
		ArrayList<String> squares = m_parent.getFromSquares();
		m_selectFrom.removeAllItems();
		for(String s: squares){
			m_selectFrom.addItem(s);
		}
	}
	
	public void updateComboBox2(){
		//finds legal moves for given piece and adds to list
		ArrayList<String> squares = m_parent.getToSquares((String)m_selectFrom.getSelectedItem());
		m_selectTo.removeAllItems();
		for(String s: squares){
			m_selectTo.addItem(s);
		}
		m_selectTo.setEnabled(true);
	}
}
