package ed.projector;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import com.googlecode.javacv.CanvasFrame;
import com.googlecode.javacv.OpenCVFrameGrabber;
import com.googlecode.javacv.cpp.opencv_core.IplImage;

import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;


public class Camera implements Runnable{
	
	private Init m_parent;
	private CvMat m_mat;
	IplImage m_frame;
	OpenCVFrameGrabber m_webcam;
	private boolean m_keepGoing;
	private boolean takePicture;
	
	public Camera(Init parent){
		m_parent = parent;
		m_keepGoing = true;
		takePicture = false;
	}
	
	public void startWebCam(){
		try {
			m_webcam.release();
		} catch (Exception e1) {
			System.err.println("No webcam made yet.");
		}
		m_webcam = new OpenCVFrameGrabber(1);
        try {
			m_webcam.start();
	        System.out.println("Webcam started.");
		} catch (Exception e) {
			System.out.println("Webcam failed to start");
			e.printStackTrace();
			System.exit(1);
		}
        try {
			m_frame = m_webcam.grab(); //first image always looks bad for some reason..
			m_frame = m_webcam.grab(); //so take two!
		} catch (Exception e) {
			System.out.println("Failed to grab initial image from webcam");
			e.printStackTrace();
			System.exit(1);
		}
	}
	public void init(){
		//Get difference image
		this.callibrate();
        
        //Start continuous feedback
		//this.run();
	}
	
	private void callibrate() {
		/////////////////////////DETECT DIFFERENCE OF GRID/NO GRID
				
		/////////////////////////////PART 1 - GET IMAGE WITH GRID
		//show grid
		m_parent.showLines(false, true);
		//init webcam
		startWebCam();
		//save last copy of m_frame
		IplImage prev = m_frame.clone();
		//show in box
		m_parent.updateCameraImage(prev.getBufferedImage());
		
		
		/////////////////////////////PART 2 - GET IMAGE WITHOUT GRID
		//start again with no grid
		m_parent.showLines(false, false);
		//init webcam
		startWebCam();
		//save last copy of m_frame
		IplImage next = m_frame.clone();
		//show in box
		m_parent.updateCameraImage(next.getBufferedImage());
		
		
		//////////////////////////////PART 3 - FIND THE DIFFERENCE
		//make new blank image (will be blank soon)
		IplImage difference = m_frame.clone();
		//get difference
		this.getColourDifference(next, prev, difference);
		
		//show difference image in box
		m_parent.updateCameraImage(difference.getBufferedImage());
		
		//canny edge detector
		IplImage difference2 = IplImage.create(m_frame.width(), m_frame.height(), IPL_DEPTH_8U, 1);
		cvCvtColor(difference, difference2, CV_RGB2GRAY);
		cvCanny(difference2, difference2, 0, 0, 3);
		cvDilate(difference2, difference2, null, 3);
		//m_parent.updateCameraImage(difference2.getBufferedImage());
		
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
	}

	private void getColourDifference(IplImage next, IplImage prev, IplImage difference) {
		
		//get array of bytes of each image
		ByteBuffer nb = next.getByteBuffer();
		ByteBuffer pb = prev.getByteBuffer();
		ByteBuffer db = difference.getByteBuffer();
		int y, x;//for looping
		int b1, b2, b3, g1, g2, g3, r1, r2, r3;//for colours
		int width = next.width();
		int height = next.height();
		double distance;
		for(y = 0; y < height; y++){
			for(x = 0; x < width*3; x+=3){
				//get pixel colours from both images
				b1 = nb.get(y*width*3 + x);
				g1 = nb.get(y*width*3 + x+1);
				r1 = nb.get(y*width*3 + x+2);
				b2 = pb.get(y*width*3 + x);
				g2 = pb.get(y*width*3 + x+1);
				r2 = pb.get(y*width*3 + x+2);
				
				//euclidean distance between pixels in rgb space
				distance = Math.sqrt(Math.pow((b2-b1), 2) + Math.pow((g2-g1), 2) + Math.pow((r2-r1), 2));
				if(distance >= 20 && distance <= 200){
					r3 = 255;
				}else{
					r3 = 0;
				}
				
				//fill difference image
				db.put(y*width*3 + x, (byte)0);
				db.put(y*width*3 + x+1, (byte)0);
				db.put(y*width*3 + x+2, (byte)r3);
			}
		}
		//System.out.println("Number of 0s: " + counter + ", Number of pixels: " + (640*480));
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
        	if(takePicture){
            	m_parent.showLines(false, true);
                try {
        			m_webcam.release();
        		} catch (Exception e1) {
        			// TODO Auto-generated catch block
        			e1.printStackTrace();
        		}
                startWebCam();
	        	try {
	        	    BufferedImage bi = m_frame.getBufferedImage(); // retrieve image
	        	    File outputfile = new File("saved.png");
	        	    ImageIO.write(bi, "png", outputfile);
	        	    takePicture = false;
	        	} catch (IOException e) {
	        	    System.out.println("Messed up");
	        	    System.exit(0);
	        	}
        	}
        	m_parent.updateCameraImage(m_frame.getBufferedImage());
        }
	}
}