package ed.projector;
import java.awt.FlowLayout;
import java.awt.GraphicsConfiguration;
import javax.swing.JFrame;

@SuppressWarnings("serial")
public class Projection extends JFrame {
	
	//JLabel that displays image
	private ChessPanel m_panel;
		
	/*
	 * Constructor
	 */
	public Projection(GraphicsConfiguration gc){
		super(gc);
		
		//remove window borders
		setUndecorated(true);
		
		setAlwaysOnTop(true);
		
		//create label, attach image
		m_panel = new ChessPanel();
		
		//no padding layout
		m_panel.setLayout(new FlowLayout(FlowLayout.LEFT,0,0));
		
		//add the image label
		this.getContentPane().add(m_panel);
	}

	/*
	 * Called whenever the button on the control frame
	 * is pressed. Switches the image displayed.
	 */
	public void showNextMove(String next) {
		m_panel.calculateSquares(next);
		m_panel.repaint();
	}
	
	public void updateGridSize(int i){
		m_panel.updateGridSize(i);
	}	
	
	public void showLines(boolean grid, boolean square){
		m_panel.showLines(grid, square);
	}
	
	public void shiftGrid(int x, int y){
		m_panel.shiftGrid(x, y);
	}
}
