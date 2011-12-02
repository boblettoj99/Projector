package ed.projector;

import com.googlecode.javacv.OpenCVFrameGrabber;

import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;


public class Camera implements Runnable{
	
	private Init m_parent;
	private CvMat m_mat;
	IplImage m_frame;
	OpenCVFrameGrabber m_webcam;
	private boolean m_keepGoing;
	
	public Camera(Init parent){
		m_parent = parent;
		m_keepGoing = true;
	}
	
	public void startWebCam(){
		m_webcam = new OpenCVFrameGrabber(1);
        try {
			m_webcam.start();
		} catch (Exception e) {
			System.out.println("Webcam failed to start");
			e.printStackTrace();
			System.exit(1);
		}
        try {
			m_frame = m_webcam.grab();
		} catch (Exception e) {
			System.out.println("Failed to grab initial image from webcam");
			e.printStackTrace();
			System.exit(1);
		}
	}
	public void init(){
		/////////////////////////DETECT DIFFERENCE OF GRID/NO GRID
		
		/////////////////////////////PART 1
		//show grid
		m_parent.showLines(false, true);
		//init webcam
		startWebCam();
		//get greyscale version of first picture
		cvThreshold(m_frame, m_frame, 200, 255, CV_THRESH_BINARY);
        IplImage prev = IplImage.create(m_frame.width(), m_frame.height(), IPL_DEPTH_8U, 1);
        cvCvtColor(m_frame, prev, CV_RGB2GRAY);
        //show in box
        m_parent.updateCameraImage(prev.getBufferedImage());
        
        
        /////////////////////////////PART 2
        //start again with no grid
        m_parent.showLines(false, false);
        try {
			m_webcam.release();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        startWebCam();
        //get greyscale version
    	cvThreshold(m_frame, m_frame, 200, 255, CV_THRESH_BINARY);
        IplImage image = IplImage.create(m_frame.width(), m_frame.height(), IPL_DEPTH_8U, 1);
        cvCvtColor(m_frame, image, CV_RGB2GRAY);
        //show in box
        m_parent.updateCameraImage(image.getBufferedImage());
        try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        //////////////////////////////PART 3 - find the difference
    	IplImage difference = IplImage.create(m_frame.width(), m_frame.height(), IPL_DEPTH_8U, 1);
    	//get difference
        cvAbsDiff(image, prev, difference);
        //threshold out insignificant parts
        cvThreshold(difference, difference, 64, 255, CV_THRESH_BINARY);
        m_parent.updateCameraImage(difference.getBufferedImage());
        try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//test initial stuff before running
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
				System.exit(1);
			}
        	cvThreshold(m_frame, m_frame, 200, 255, CV_THRESH_BINARY);
        	IplImage image = IplImage.create(m_frame.width(), m_frame.height(), IPL_DEPTH_8U, 1);
            cvCvtColor(m_frame, image, CV_RGB2GRAY);
//        	m_mat = new CvMat();
//        	cvGetMat(m_frame, m_mat, null, 0);
//        	//m_mat = filterColour(1);
        	m_parent.updateCameraImage(image.getBufferedImage());
        }
	}
	
	public CvMat filterColour(int colour){
		int i, j;
		double d = 0;
		for(i = 0; i < m_mat.rows(); i++){
			for(j = 0; j < m_mat.cols(); j++){
				System.out.println(d + ", " + i + ", " + j);
				d = m_mat.get(j, i);
			}
		}
		return m_mat;
	}
}