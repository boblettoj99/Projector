package ed.projector;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.util.ArrayList;

import javax.swing.JFrame;

public class Init {
	
	//width/height of controls
	public static int CONTROL_WIDTH = 650;
	public static int CONTROL_HEIGHT = 650;
	
	//The two frames
	public GUI m_gui;
	public Projection m_projection;
	
	//Webcam input
	public Camera m_camera;
	
	//the chess game interface
	public ChessInit m_chess;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//Make instance of this class, acts as parent for frames
		Init init = new Init();
		
		//get the graphics environment
		GraphicsEnvironment ge = GraphicsEnvironment.
			getLocalGraphicsEnvironment();
		GraphicsDevice[] gs = ge.getScreenDevices();
		//check there are more than one devices
		//this is actually broken (has 2 devices whether or not projector is connected)
		//odd
		if(gs.length < 1){
			System.err.println("You haven't connected the projector!");
			System.exit(1);
		}
		//get the projector and screen
		GraphicsDevice gd0 = gs[1];//projector
		GraphicsDevice gd1 = gs[0];//screen
		GraphicsConfiguration gc = gd0.getDefaultConfiguration();
		//set projection's graphics configuration to device 0 (projector)
		init.m_projection = new Projection(gc);
		//create the gui frame on device 1 (screen)
		init.m_gui = new GUI(init, gd1.getDefaultConfiguration());
		
		//set size of control box
		init.m_gui.setSize(CONTROL_WIDTH, CONTROL_HEIGHT);
		
		//make projection fullscreen
		gd0.setFullScreenWindow(init.m_projection);
		
		//close everything on any frame's closing
		init.m_gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		init.m_projection.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		//show frames
		init.m_gui.setVisible(true);
		init.m_projection.setVisible(true);
		
		//Create chess interface
		init.m_chess = new ChessInit();

		//Make webcam
		init.m_camera = new Camera(init);
		//Start camera going
		init.m_camera.init();
	}
	
	//uses the current position and finds the next best move
	public String getNextHint(){
		return m_chess.getNextHint();
	}
	
	/*
	 * This class acts as middle man between the 
	 * various parts of program, so a few methods go through here.
	 */
	public void nextHint(String next) {
		m_projection.showNextMove(next);
	}
	
	public void updateGridSize(int i) {
		m_projection.updateGridSize(i);
	}
	
	public void showLines(boolean grid, boolean square){
		m_projection.showLines(grid, square);
	}
	
	public void shiftGrid(int x, int y){
		m_projection.shiftGrid(x, y);
	}

	public ArrayList<String> getFromSquares() {
		return m_chess.getFromSquares();
	}

	public ArrayList<String> getToSquares(String from) {
		return m_chess.getToSquares(from);
	}

	public void executeHint() {
		m_chess.executeHint();
	}

	public void executeMove(String move) {
		m_chess.executeMove(move);
	}
	
	public void updateCameraImage(Image im){
		m_gui.updateCameraImage(im);
	}
}
