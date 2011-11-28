package ed.projector;

import com.googlecode.javacv.CanvasFrame;
import com.googlecode.javacv.OpenCVFrameGrabber;
import com.googlecode.javacv.cpp.opencv_core.IplImage;

public class Camera implements Runnable{
	
	private Init m_parent;
	public CanvasFrame m_cf;
	IplImage m_frame;
	OpenCVFrameGrabber m_webcam;
	private boolean m_keepGoing;
	
	public Camera(Init parent){
		m_parent = parent;
		m_keepGoing = true;
		
        m_webcam = new OpenCVFrameGrabber(1);
        try {
			m_webcam.start();
		} catch (Exception e) {
			System.out.println("Webcam failed to start");
			e.printStackTrace();
		}
        
        try {
			m_frame = m_webcam.grab();
		} catch (Exception e) {
			System.out.println("Failed to grab initial image from webcam");
			e.printStackTrace();
		}
		
        m_cf = new CanvasFrame("");
        m_cf.setCanvasSize(m_frame.width(), m_frame.height());
        
        m_cf.showImage(m_frame);
	}
	
	public void startCamera(){
		this.run();
	}
	
	public void stopCamera(){
		m_keepGoing = false;
	}

	@Override
	public void run() {
        while(m_keepGoing){
        	try {
				m_frame = m_webcam.grab();
			} catch (Exception e1) {
				System.out.println("Failed to grab from webcam");
				e1.printStackTrace();
			}
        	m_cf.showImage(m_frame);
        	//try {
			//	Thread.sleep(1000, 0);
			//} catch (InterruptedException e) {
			//	System.out.println("Failed to sleep");
			//	e.printStackTrace();
			//}	
        }
	}
}