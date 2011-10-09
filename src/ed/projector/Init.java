package ed.projector;
import javax.swing.JFrame;

public class Init {
	
	//width/height of controls
	public static int CONTROL_WIDTH = 400;
	public static int CONTROL_HEIGHT = 300;
	
	//The two frames
	public GUI m_gui;
	public Projection m_projection;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//Make instance of this class, acts as parent for frames
		Init init = new Init();		
		/*
		 * The two frames (gui displayed on screen 1, 
		 * projection is displayed projector)
		 */
		init.m_gui = new GUI(init);
		init.m_projection = new Projection();
		
		//set size of control box
		init.m_gui.setSize(CONTROL_WIDTH, CONTROL_HEIGHT);
		
		//set size of projection 
		//(temporary until sort out fullscreen on projector)
		//init.m_projection.setSize(400, 400);
		init.m_projection.setLocation(500, 0);
		init.m_projection.pack();
		
		//close everything on any frame's closing
		init.m_gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		init.m_projection.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		//show frames
		init.m_gui.setVisible(true);
		init.m_projection.setVisible(true);	
	}

	public void switchImage() {
		m_projection.switchImage();		
	}

}
