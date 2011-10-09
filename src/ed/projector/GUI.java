package ed.projector;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JButton;

/*
 * Main frame for pushing buttons: AKA Battle Station
 */
@SuppressWarnings("serial")
public class GUI extends JFrame implements ActionListener{
	
	//On press, switches the image displayed on the projector.
	public final JButton m_switchBtn = new JButton("Switch Image");
	//Parent class Init
	public Init m_parent;
	
	/*
	 * constructor
	 */
	public GUI(Init parent){
		super("Controls");
		m_switchBtn.addActionListener(this);
		m_parent = parent;
		this.setLayout(new FlowLayout());
		this.getContentPane().add(m_switchBtn);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == m_switchBtn){
			m_parent.switchImage();
		}
	}
}
