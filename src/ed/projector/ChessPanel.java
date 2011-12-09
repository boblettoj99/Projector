package ed.projector;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;

public class ChessPanel extends JPanel {
	
	private static final long serialVersionUID = 1L;
	
	//contains a 1 in blue hint(move) 
	//2 for white hint(start) 
	//3 for green hint(end)
	//4 for red hint(take)
	//5 for yellow hint(promote)
	private int[][] m_board; 
	
	//length of side of each square in the grid
	//can range from 0-100...should probably change that
	//at some point.
	private int m_gridSize;
	
	private boolean m_drawGrid;
	private boolean m_drawSquare;
	
	//centre of projector at 800x600, hardware specific
	private static final int PROJECTOR_HEIGHT = 600;
	private static final int PROJECTOR_WIDTH = 800;
	
	private int m_centreX;
	private int m_centreY;

	public ChessPanel(){
		m_board = new int[8][8];
		m_centreX = 400;
		m_centreY = 300;
		m_gridSize = 50;
		//TODO: make this changeable
		m_drawGrid = false;
		m_drawSquare = false;
		this.repaint();
	}
	
	public void showLines(boolean grid, boolean square){
		m_drawGrid = grid;
		m_drawSquare = square;
		this.repaint();
	}
	
	/*
	 * translates the grid displayed (used when setting up)
	 * coordinates correspond to centre of grid
	 */
	public void shiftGrid(int x, int y){
		m_centreX = x;
		m_centreY = y;
		this.repaint();
	}
	
	/*
	 * Change grid size
	 * int i corresponds to length of side of each square
	 */
	public void updateGridSize(int i){
		m_gridSize = i;
		this.repaint();
	}
	
	public void calculateSquares(String next){
		/* paints squares based on PGN text
		 * NOT SAN - further eperimentation needed 
		 * here as no doc for chess engine!!!
		 * e.g. move: a1a2
		 * en.wikipedia.org/wiki/Portable_Game_Notation
		 */
		int[] start = new int[2];//start position of move
		int[] end = new int[2];
		
		//reset board
		m_board = new int[8][8];
		
		//normal moves
		//make sure 0 indexed (chess engine is 1 indexed)
		start[0] = next.getBytes()[0] - 97;
		start[1] = next.getBytes()[1] - 49;
		end[0] = next.getBytes()[2] - 97;
		end[1] = next.getBytes()[3] - 49;
		if((start[0] < 0 && start[0] > 7) 
				|| (end[0] < 0 && end[0] > 7)){
			System.err.println("null string");
			//handle this error at some point
		}
		
		boolean isTake = false;
		if(next.length() == 5){//normal
			if(next.toCharArray()[4] == '1'){
				isTake = true;
			}
		}else if(next.length() == 6){//there is promotion piece
			if(next.toCharArray()[5] == 1){
				isTake = true;
			}
			//TODO: Deal with promotion piece
		}
		//TODO: Deal with castling
		//debug
		System.out.println("Square: (" + start[0] + ", " + start[1] + ")" +
				" to square: (" + end[0] + ", " + end[1] + ")."+"\n---------------------------------");
		//set start and end points
		m_board[start[0]][start[1]] = 2;
		m_board[end[0]][end[1]] = 3;
		if(isTake)m_board[end[0]][end[1]] = 4;
		
		calculateInbetweenPoints(start, end);
	}
	
	/*
	 * called by the calculateSquares function to fill in
	 * the squares to move along. 
	 */
	private void calculateInbetweenPoints(int[] start, int[] end){
		//work out inbetween points
		if(Math.abs(end[0]-start[0]) == Math.abs(end[1]-start[1])){
			//ie if diagonal
			int i = Math.abs(end[0]-start[0])-1;
			int j;
			int x = start[0];
			int y = start[1];
			if(end[0] < start[0] && end[1] < start[1]){
				//going southwest
				for(j = 0; j < i; j++){
					x--;
					y--;
					m_board[x][y] = 1;
				}
			}else if(end[0] > start[0] && end[1] < start[1]){
				//going southeast
				for(j = 0; j < i; j++){
					x++;
					y--;
					m_board[x][y] = 1;
				}
			}else if(end[0] > start[0] && end[1] > start[1]){
				//going northeast
				for(j = 0; j < i; j++){
					x++;
					y++;
					m_board[x][y] = 1;
				}
			}else if(end[0] < start[0] && end[1] > start[1]){
				//going northwest
				for(j = 0; j < i; j++){
					x--;
					y++;
					m_board[x][y] = 1;
				}
			}
		}else if(Math.abs(end[0] - start[0]) == 0){
			//vertical
			int i;
			if(start[1] < end[1]){//going up
				for(i = start[1]+1; i < end[1]; i++){
					m_board[start[0]][i] = 1;
				}
			}else{//going down
				for(i = start[1]-1; i > end[1]; i--){
					m_board[start[0]][i] = 1;
				}
			}
		}else if(Math.abs(end[1] - start[1]) == 0){
			//horizontal
			int i;
			if(start[0] < end[0]){//going right
				for(i = start[0]+1; i < end[0]; i++){
					m_board[i][start[1]] = 1;
				}
			}else{//going left
				for(i = start[0]-1; i > end[0]; i--){
					m_board[i][start[1]] = 1;
				}
			}
		}else{
			//must be knight move
			if(end[1] < (start[1]-1)){
				m_board[start[0]][start[1]-1] = 1;
				m_board[start[0]][start[1]-2] = 1;
			} else if(end[1] > (start[1]+1)){
				m_board[start[0]][start[1]+1] = 1;
				m_board[start[0]][start[1]+2] = 1;
			} else if(end[0] < (start[0]-1)){
				m_board[start[0]-1][start[1]] = 1;
				m_board[start[0]-2][start[1]] = 1;
			} else if(end[0] > (start[0]+1)){
				m_board[start[0]+1][start[1]] = 1;
				m_board[start[0]+2][start[1]] = 1;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.JComponent#paint(java.awt.Graphics)
	 */
	@Override
	public void paint(Graphics g){
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		
		//black background
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, PROJECTOR_WIDTH, PROJECTOR_HEIGHT);
		g.setColor(Color.RED);
		
		//init starting coords
		int x = m_centreX - (4*m_gridSize);
		int y = m_centreY - (4*m_gridSize);
		//need these later
		int startx = x;
		int starty = y;
		
		//set the scale
        double scale = 8.0/9.0;
        g2.scale(scale, 1);
        
        //make sure lines are visible
		g2.setStroke(new BasicStroke(15));
		
		//draw square
		if(m_drawSquare){
			g2.drawRect(x, y, 8*m_gridSize, 8*m_gridSize);
		}

		//paint grid
		if(m_drawGrid){			
			int i;
			for(i = 0; i < 9; i++){//need to draw 9 lines not 8
				g.drawLine(x, y, x+(8*m_gridSize), y);
				y = y + m_gridSize;
			}
			//told you
			x = startx;
			y = starty;
			for(i = 0; i < 9; i++){
				g.drawLine(x, y, x, y+(8*m_gridSize));
				x = x + m_gridSize;
			}
		}
		
		//now draw any hint squares
		x = 0;
		y = 7;//annoying reversed coordinate system
		for(int[] col : m_board){
			for(int square : col){
				if(square != 0){
					if(square == 1){//inbetween point
						g.setColor(Color.BLUE);
					}else if(square == 2){//start
						g.setColor(Color.WHITE);
					}else if(square == 3){//end point
						g.setColor(Color.GREEN);
					}else if(square == 4){//end point
						g.setColor(Color.RED);
					}
					g.fillRect(startx+(x*m_gridSize)+1, starty+(y*m_gridSize)+1, 
							m_gridSize-2, m_gridSize-2);
				}
				y--;
			}
			y = 7;
			x++;
		}
	}
}
