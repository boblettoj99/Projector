package ed.projector;
import java.awt.FlowLayout;
import java.awt.GraphicsConfiguration;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/*
 * Main frame for pushing buttons: AKA Battle Station
 */
@SuppressWarnings("serial")
public class GUI extends JFrame implements ActionListener, ChangeListener{
	
	//On press, switches the image displayed on the projector.
	public final JButton m_switchBtn = new JButton("Next Move");
	//On slide, changes size of chess board
	public final JSlider m_slider = new JSlider();
	//Parent class Init
	public Init m_parent;
	
	/*
	 * constructor
	 */
	public GUI(Init parent, GraphicsConfiguration gc){
		super(gc);
		this.setTitle("Controls");
		m_switchBtn.addActionListener(this);
		m_slider.addChangeListener(this);
		m_parent = parent;
		this.setLayout(new FlowLayout());
		this.getContentPane().add(m_switchBtn);
		this.getContentPane().add(m_slider);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == m_switchBtn){
			String next = m_parent.getNextHint();
			m_parent.nextHint(next);
		}
	}
	
	@Override
	public void stateChanged(ChangeEvent e){
		if(e.getSource() == m_slider){
				int i = m_slider.getValue();
				m_parent.updateGridSize(i);
		}
	}
}
