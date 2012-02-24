package ed.projector;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import javax.imageio.ImageIO;

import com.googlecode.javacpp.Pointer;
import com.googlecode.javacv.OpenCVFrameGrabber;
import com.googlecode.javacv.cpp.opencv_core.CvPoint;
import com.googlecode.javacv.cpp.opencv_core.CvPoint2D32f;
import com.googlecode.javacv.cpp.opencv_core.IplImage;

import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;


public class Camera implements Runnable{
	
	//K-MEANS CONSTANTS
	//defines the distance between two means to qualify as local optima problem 
	private final static int NUM_PIXELS_CLOSE = 50;
	//defines distance a mean moves in comparison to last loop 
	//to conclude that means have converged
	private final static int NUM_PIXELS_CONVERGE = 1;
	//defines distance either side of the modal gap between lines
	//in which to check for valid board lines.
	private final static int BOARD_LINE_ERROR = 2;
	
	private Init m_parent;
	IplImage m_frame;
	OpenCVFrameGrabber m_webcam;
	private boolean m_keepGoing;
	private boolean takePicture;
	private Random m_random;
	
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
		//Callibrate camera
		this.calibrate();
        
        //Start continuous feedback
		//this.run();
	}
	
	private void calibrate() {
		//get location of projection
		int[][] pro_corners = findProjection();
		
		//find chessboard
		findBoard();
	}
	
	/*
	 * Projects square on and off.
	 * Finds difference between two images (the square).
	 * Detects edges using Canny.
	 * Detects lines within those edges using Hough.
	 * Finds the corners using k-means on the line end points.
	 * returns corners
	 */
	private int[][] findProjection() {
				
		/*
		 * PART 1
		 *  - GET IMAGE WITH GRID
		 */
		//show grid
		m_parent.showLines(false, true);
		//init webcam
		startWebCam();
		//save last copy of m_frame
		IplImage prev = m_frame.clone();
		//show image
		m_parent.updateCameraImage(prev.getBufferedImage());
		
		
		/*
		 * PART 2
		 *  - GET IMAGE WITHOUT GRID
		 */
		//start again with no gridIplImage.create(m_frame.width(), m_frame.height(), m_frame.depth(), 3);
		m_parent.showLines(false, false);
		//init webcam
		startWebCam();
		//save last copy of m_frame
		IplImage next = m_frame.clone();
		//show image
		m_parent.updateCameraImage(next.getBufferedImage());
		
		
		/*
		 * PART 3
		 *  - FIND THE DIFFERENCE
		 */
		//make new blank image (will be blank soon)
		IplImage difference = m_frame.clone();
		//get difference
		this.getColourDifference(next, prev, difference);		
		//show difference image in box
		m_parent.updateCameraImage(difference.getBufferedImage());
		
		
		/*
		 * PART 4
		 *  - DETECT EDGES 
		 */
		//get image into correct format
		IplImage difference2 = IplImage.create(m_frame.width(), m_frame.height(), IPL_DEPTH_8U, 1);
	    cvCvtColor(difference, difference2, CV_RGB2GRAY);
		//canny edge detector
	    cvCanny(difference2, difference2, 1, 50, 3);
		//transforms on the image to improve line detection
	    cvDilate(difference2, difference2, null, 3);
		//cvErode(difference2, difference2, null, 2);
		//show detected edges
	    m_parent.updateCameraImage(difference2.getBufferedImage());
		
		
		/*
		 * PART 5
		 *  - HOUGH LINES
		 */
		IplImage houghlines = difference.clone();
		CvMemStorage storage = CvMemStorage.create();
		CvPoint pt1, pt2;
		Pointer pointer;
		//hough transform
		CvSeq lines = cvHoughLines2(difference2, storage, CV_HOUGH_PROBABILISTIC, 4, Math.PI / 180, 100, 90, 10);
		//list to store points (start and end points of hough output lines) 
		ArrayList<Point> points = new ArrayList<Point>();
		//draw each line on the image
		for(int i = 0; i < lines.total(); i++ )
		{
			//get point data
            pointer = cvGetSeqElem(lines, i);
            pt1  = new CvPoint(pointer).position(0);
            pt2  = new CvPoint(pointer).position(1);
            //add to list to make it easier to access points
            points.add(new Point(pt1.x(), pt1.y()));
            points.add(new Point(pt2.x(), pt2.y()));
            //draw on image
		    cvCircle(houghlines, pt1, 3, CvScalar.BLUE, -1, 8, 0);
		    cvCircle(houghlines, pt2, 3, CvScalar.BLUE, -1, 8, 0);
			//show image
			m_parent.updateCameraImage(houghlines.getBufferedImage());
		}
		//clear up
		storage.release();
		//show detected lines
		m_parent.updateCameraImage(houghlines.getBufferedImage());
		
		
		/*
		 * PART 6
		 *  - GET CORNERS
		 */
		//get means for corners (average of clustered end/start points)
		int[][] means = kMeans(points, 4, houghlines);
		//draw lines between means
		for(int i = 0; i < 4; i++){
			cvLine(houghlines, new CvPoint(means[i][0], means[i][1]), 
							   new CvPoint(means[(i+1)%4][0], means[(i+1)%4][1]), CvScalar.GREEN, 2, CV_AA, 0);
		}
		//show detected corners
	    m_parent.updateCameraImage(houghlines.getBufferedImage());
	    
	    return means;
	}
	
	private void findBoard() {
		/*
		 * PART 1
		 *  - GET IMAGE
		 */
		//init webcam
		startWebCam();
		//show image
		m_parent.updateCameraImage(m_frame.getBufferedImage());
		
		/*
		 * PART 2
		 *  - EDGE DETECTOR
		 */
		//convert to greyscale for canny
		IplImage edges = IplImage.create(m_frame.width(), m_frame.height(), IPL_DEPTH_8U, 1);
	    cvCvtColor(m_frame, edges, CV_RGB2GRAY);
		//canny edge detector
	    cvCanny(edges, edges, 80, 255, 3);
	    //convert back to colour image for blue hough line drawing
	    cvCvtColor(edges, m_frame, CV_GRAY2RGB);
	    //show detected edges
		m_parent.updateCameraImage(m_frame.getBufferedImage());
		
		/*
		 * PART 3
		 *  - HOUGH LINES
		 */
		IplImage houghlines = m_frame.clone();
		CvMemStorage storage = CvMemStorage.create();
		CvPoint pt1, pt2;
		CvPoint2D32f point, temp_point;
		double rho, theta, temp_rho, temp_theta, a, b, x0, y0;
		int counter;
		ArrayList<CvPoint2D32f> points1 = new ArrayList<CvPoint2D32f>();;
		ArrayList<CvPoint2D32f> points2 = new ArrayList<CvPoint2D32f>();;		
		//hough transform
		CvSeq lines = cvHoughLines2(edges, storage, CV_HOUGH_STANDARD, 1, Math.PI / 180, 50, 0, 0);
		
		/*
		 * PART 4
		 *  - Find the useful lines
		 */
		boolean found = false;
		for(int i = 0; i < lines.total(); i++ )
		{
        	points1 = new ArrayList<CvPoint2D32f>();//for parallel lines
        	points2 = new ArrayList<CvPoint2D32f>();//for perpendicular lines
			point = new CvPoint2D32f(cvGetSeqElem(lines, i));//line data
            rho=point.x();//distance from origin
            theta=point.y();//angle            
            points1.add(point);//clearly parallel to itself
            
            //find parallel/perpendicular lines
            for(int j = 0; j < lines.total(); j++){
            	temp_point = new CvPoint2D32f(cvGetSeqElem(lines, j));
            	temp_rho = temp_point.x();
            	temp_theta = temp_point.y();
            	//if roughly at the same angle - else if roughly perpendicular
            	if(theta < temp_theta + 0.001 
            	&& theta > temp_theta - 0.001){
            		points1.add(temp_point);
            	}else if((theta + Math.PI/2 < temp_theta + 0.001
            	&& theta + Math.PI/2 > temp_theta - 0.001) 
            	|| (theta - Math.PI/2 < temp_theta + 0.001
            	&& theta - Math.PI/2 > temp_theta - 0.001)){
            		points2.add(temp_point);
            	}
            }
            
            //if finds a set of 18 parallel/perpendicular lines
            if(points1.size() >= 9 && points2.size() >= 9){            	
            	//then stop
            	found = true;
            	break;
            }
		}		
		if(!found){
			System.err.println("Could not find a chessboard.");
			System.exit(1);
		}
		
		/*
		 * PART 5
		 *  - Sort the lines into order left to right/ bottom to top
		 */
		CvPoint2D32f[] parallels = sortHoughPoints(points1);
		CvPoint2D32f[] perpendiculars = sortHoughPoints(points2);
		
		/*
		 * PART 6
		 *  - Find the modal distance between lines and remove lines in the gaps
		 */
		CvPoint2D32f[] nine_parallels = removeRedundantLines(parallels);
		CvPoint2D32f[] nine_perpendiculars = removeRedundantLines(perpendiculars);
		
		/*
		 * PART 7
		 *  - find intersection points (individual square corners)
		 */
		ArrayList<CvPoint> intersections = new ArrayList<CvPoint>();
		for(CvPoint2D32f p : nine_parallels){
			for(CvPoint2D32f q : nine_perpendiculars){
				CvPoint int_point = findIntersectionPoint(p, q);
				if(int_point != null){
					intersections.add(int_point);
				}
			}
		}
		//draw parallel board lines
		for(CvPoint2D32f p: nine_parallels){
			drawLineFromHoughPoint(p, houghlines, CvScalar.GREEN);
		}
		//draw perpendicular board lines
		for(CvPoint2D32f p: nine_perpendiculars){
			drawLineFromHoughPoint(p, houghlines, CvScalar.GREEN);
		}
		//draw intersection points
		for(CvPoint p : intersections){
		    cvCircle(houghlines, p, 3, CvScalar.YELLOW, -1, 8, 0);
		}		
		//clear up
		storage.release();
		//show detected lines
		m_parent.updateCameraImage(houghlines.getBufferedImage());
	}
	
	/*
	 * Finds point where these two hough lines intersect.
	 */
	private CvPoint findIntersectionPoint(CvPoint2D32f p, CvPoint2D32f q) {
		double cos_theta1, cos_theta2, sin_theta1, sin_theta2, det;
		int x, y;
		cos_theta1 = Math.cos(p.y());
		sin_theta1 = Math.sin(p.y());
		cos_theta2 = Math.cos(q.y());
		sin_theta2 = Math.sin(q.y());
		det = cos_theta1*sin_theta2 - cos_theta2*sin_theta1;
		if(det == 0){
			System.err.println("Lines are parallel");
			return null;
		}else{
			x = (int)((sin_theta2*p.x() - sin_theta1*q.x())/det);
			y = (int)((cos_theta1*q.x() - cos_theta2*p.x())/det);
			return new CvPoint(x, y);
		}
	}

	private CvPoint2D32f[] removeRedundantLines(CvPoint2D32f[] lines){
		CvPoint2D32f[] nine_lines = new CvPoint2D32f[9];
		//get the gaps between lines
		int[] distances = new int[lines.length-1];
		for(int i = 0; i < lines.length-1; i++){
			distances[i] = (int) (lines[i+1].x()-lines[i].x());
			System.out.println(distances[i]);
		}
		//find regular gap
		int gap = findModalValue(distances);
		if(gap == -1){
			System.err.println("Could not find regular gaps.");
			System.exit(1);
		}
		System.out.println("Modal gap: " + gap);
		
		//find a path of lines which have this same gap between them
		int[] total_distances = new int[distances.length];
		int total_distance, last;
		int i, j;
		boolean next;
		//for each line
		for(i = 0; i < lines.length; i++){
			//set up path array (holds index of each line in the path
			System.out.println("INDEX: " + i);
			ArrayList<Integer> path = new ArrayList<Integer>();
			path.add(i);

			System.out.println("DISTANCES");
			//find the distance to each line in front
			total_distance = 0;
			for(j = i; j < distances.length; j++){
				total_distance += distances[j];
				total_distances[j] = total_distance;
				System.out.println(total_distance);
			}

			System.out.println("NEW PATH");
			//find a path of lines with the given gap
			total_distance = 0;
			next = true;
			int counter = 1;
			int index = -1;
			while(next){
				total_distance += gap;
				index = findDistantLine(total_distance, total_distances, i, counter, BOARD_LINE_ERROR, index);
				if(index == -1){
					next = false;
				}else{
					path.add(index+1);
					System.out.println(index+1);
					counter++;
					if(counter == 9){
						System.out.println("Found path starting at line: " + i);
						return getArraySubset(lines, path);
					}
				}
			}			
		}
		return null;
	}
	
	private CvPoint2D32f[] getArraySubset(CvPoint2D32f[] lines,	ArrayList<Integer> path) {
		CvPoint2D32f[] subset = new CvPoint2D32f[path.size()];
		for(int i = 0; i < subset.length; i++){
			subset[i] = lines[path.remove(0)];
		}
		return subset;
	}

	private int findDistantLine(int distance, int[] distances, int fromIndex, int counter, int error, int lastIndex) {
		int index = -1;
		for(int i = fromIndex; i < distances.length; i++){
			if(distance <= distances[i]+(counter*error) && distance >= distances[i]-(counter*error) && i > lastIndex){
				index = i;
				break;
			}
		}
		return index;
	}

	private int findModalValue(int[] values){
		int[] table = new int[findMax(values)[0] + 1];
		for(int i = 0; i < values.length; i++){
			table[values[i]]++;
		}
		return findMax(table)[1];
	}
	
	private int[] findMax(int[] values){
		int[] data = new int[2];//first for max value, second for index
		data[0] = -1;
		data[1] = -1;
		for(int i = 0; i < values.length; i++){
			if(values[i] > data[0]){
				data[0] = values[i];
				data[1] = i;
			}
		}
		return data;
	}
	
	private CvPoint2D32f[] sortHoughPoints(ArrayList<CvPoint2D32f> unsorted){
		int min;
		CvPoint2D32f[] sorted = new CvPoint2D32f[unsorted.size()];
		for(int i = 0; i < sorted.length; i++){
			min = 0;
			for(int j = 1; j < unsorted.size(); j++){
				if(unsorted.get(min).x() > unsorted.get(j).x()){
					min = j;
				}
			}
			sorted[i] = unsorted.remove(min);
		}
		return sorted;
	}
	
	private void drawLineFromHoughPoint(CvPoint2D32f p, IplImage houghlines, CvScalar color){

        double rho=p.x();//distance from origin
        double theta=p.y();//angle
		//work out end points
        double a = Math.cos(theta);
        double b = Math.sin(theta);            
        double x0 = a * rho;
        double y0 = b * rho;
        CvPoint pt1 = new CvPoint((int) Math.round(x0 + 1000 * (-b)), (int) Math.round(y0 + 1000 * (a)));
        CvPoint pt2 = new CvPoint((int) Math.round(x0 - 1000 * (-b)), (int) Math.round(y0 - 1000 * (a)));
        
		cvLine(houghlines, pt1, pt2, color, 1, CV_AA, 0);
	}
	
	/*
	 * Returns k points which represent k means of an array of points
	 */
	public int[][] kMeans(ArrayList<Point> points, int k, IplImage lines){
		int[][] means = new int[k][2];
		int[][] means_old = new int[k][2];
		int[] assign = new int[points.size()];
		int absX, absY, hypot, min, counter;
		boolean converge = false;
		ArrayList<Point> temp = new ArrayList<Point>();
		for(Point cp : points) temp.add(cp);
		m_random = new Random();
		IplImage lines2;
		
		//initiate means to random point in the array
		int rand;
		for(int i = 0; i < k; i++){
			rand = m_random.nextInt(temp.size());
			means[i][0] = (int) temp.get(rand).getX();
			means[i][1] = (int) temp.get(rand).getY();
			temp.remove(rand);
		}
		
		while(!converge){
			
			means_old[0] = Arrays.copyOf(means[0], means[0].length);
			means_old[1] = Arrays.copyOf(means[1], means[1].length);
			means_old[2] = Arrays.copyOf(means[2], means[2].length);
			means_old[3] = Arrays.copyOf(means[3], means[3].length);
			
			//assign points to 1 of the means
			for(int i = 0; i < points.size(); i++){
				//work out distance from each mean
				min = -1;
				for(int j = 0; j < k; j++){
					absX = (int) Math.abs(points.get(i).getX() - means[j][0]);
					absY = (int) Math.abs(points.get(i).getY() - means[j][1]);
					hypot = (int) Math.sqrt(Math.pow(absX, 2) + Math.pow(absY, 2));
					if(min == -1 || hypot < min){
						min = hypot;
						assign[i] = j;
					}
				}
			}
			
			//recalculate means
			for(int i = 0; i < k; i++){
				means[i][0] = 0;
				means[i][1] = 0;
				counter = 0;
				for(int j = 0; j < points.size(); j++){
					if(assign[j] == i){
						means[i][0] += points.get(j).getX();
						means[i][1] += points.get(j).getY();
						counter++;
					}
				}
				if(counter != 0){
					means[i][0] = means[i][0]/counter;
					means[i][1] = means[i][1]/counter;
				}else{
					//clearly too close to another point, assign new random point
					rand = m_random.nextInt(temp.size());
					means[i][0] = (int) temp.get(rand).getX();
					means[i][1] = (int) temp.get(rand).getY();
				}
			}
			
			//draw lines between means
			lines2 = lines.clone();
			for(int i = 0; i < 4; i++){
				cvLine(lines2, new CvPoint(means[i][0], means[i][1]), 
							new CvPoint(means[(i+1)%4][0], means[(i+1)%4][1]), CvScalar.GREEN, 2, CV_AA, 0);
			}
			//show image
		    m_parent.updateCameraImage(lines2.getBufferedImage());
		    
		    //ERROR CORRECTION
		    for(int i = 0; i < k; i++){
		    	//make sure not too close to other means
		    	for(int j = 0; j < k; j++){
		    		if(i != j){
		    			absX = (int) Math.abs(means[i][0] - means[j][0]);
		    			absY = (int) Math.abs(means[i][1] - means[j][1]);
		    			hypot = (int) Math.sqrt(Math.pow(absX, 2) + Math.pow(absY, 2));
		    			//if too close, assign new random point - solve local optima!
		    			if(hypot <= NUM_PIXELS_CLOSE){
							rand = m_random.nextInt(points.size());
							means[i][0] = (int) points.get(rand).getX();
							means[i][1] = (int) points.get(rand).getY();
							j = -1;
		    			}
		    		}
		    	}
		    }
			
		    
			//check for converge
		    converge = true;
		    for(int i = 0; i < k; i++){
		    	absX = (int) Math.abs(means[i][0] - means_old[i][0]);
		    	absY = (int) Math.abs(means[i][1] - means_old[i][1]);
		    	hypot = (int) Math.sqrt(Math.pow(absX, 2) + Math.pow(absY, 2));
		    	if(hypot > NUM_PIXELS_CONVERGE){
		    		converge = false;
		    	}
		    }
		}
		
		System.out.println("Converged");
		
		return means;
	}

	private void getColourDifference(IplImage next, IplImage prev, IplImage difference) {
		
		//get array of bytes of each image
		ByteBuffer nb = next.getByteBuffer();
		ByteBuffer pb = prev.getByteBuffer();
		ByteBuffer db = difference.getByteBuffer();
		int y, x;//for looping
		int b1, b2, g1, g2, r1, r2, r3;//for colours
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
				if(distance >= 50 && distance <= 200){
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