package ed.projector;

import java.util.ArrayList;

import org.forritan.talvmenni.ChessEngine;
import org.forritan.talvmenni.TalvMenni;
import org.forritan.talvmenni.bitboard.Square;
import org.forritan.talvmenni.bitboard.Squares;
import org.forritan.talvmenni.knowledge.Move;
import org.forritan.talvmenni.knowledge.MoveHistory;
import org.forritan.talvmenni.knowledge.Position;
import org.forritan.talvmenni.knowledge.Position.PromotionPiece;
import org.forritan.talvmenni.knowledge.TheoryBook;
import org.forritan.talvmenni.search.PrincipalVariation;
import org.forritan.talvmenni.search.PrincipalVariation.Factory;
import org.forritan.talvmenni.strategy.AlphaBetaStrategy;

/*
 * Main class to interface with the talvmenni chess engine
 */
public class ChessInit {
	
	//the chess engine
	private ChessEngine m_ce;
	//is it white's go?
	private boolean m_whiteMove;
	//next move to do (if user chooses to do hint)
	private Position.Move m_nextMove;
	//useful object, used for comparisons, square names etc
	public Square m_sq = Squares.create();
	
	//constructor
	public ChessInit(){
		//set up new game just to get default position
		//use default values from talvmenni project
		PrincipalVariation pv = Factory.create(TalvMenni.PLY);
		TheoryBook tb = new TheoryBook(TalvMenni.MAX_THEORY_BOOK_ENTRIES);
		m_ce = ChessEngine.create(new AlphaBetaStrategy(TalvMenni.PLY, tb, pv));
		m_ce.run();
		m_ce.getProtocol().newGame();
		
		//no move at the start
		m_nextMove = null;
		//start on white
		m_whiteMove = true;
	}
	
	/*
	 * uses the getNextMove() function of the chess engine to find next hint
	 */
	public String getNextHint(){
		Position.Move m = m_ce.getProtocol().getStrategy()
				.getNextMove(m_ce.getProtocol().getCurrentPosition(), m_whiteMove);
		//get string representation of move
		//fromsquare(2)-tosquare(2)-promotionpieceifapplicable(1)
		String s = m.toString();
		int isTake = isTakeMove(m);
		System.out.println(s+isTake);
		m_nextMove = m;
		return s+isTake;
	}
	
	/*
	 * Works out whether or not a move takes a piece.
	 * Looks at all the opposition pieces and sees if they are
	 * on the square the user is going to.
	 */
	private int isTakeMove(Position.Move m){
		long allPieces;
		int isTake = 0;
		Position p = m_ce.getProtocol().getCurrentPosition();
		if(m_whiteMove){
			allPieces = p.getBlack().allPieces;
			if((m.to & allPieces) != Square._EMPTY_BOARD){//piece is taken
				isTake = 1;
			}
		}else{
			allPieces = p.getWhite().allPieces;
			if((m.to & allPieces) != Square._EMPTY_BOARD){//piece is taken
				isTake = 1;
			}
		}
		return isTake;
	}
	
	/*
	 * Plays the given move on the actual chess game.
	 */
	private void updatePosition(Position.Move move){
		if(move != null){
			Move m = new Move(m_ce.getProtocol().getCurrentPosition(),
					move.from, move.to, move.promotionPiece);
			MoveHistory.getInstance().add(m);
			m_ce.getProtocol().setCurrentPosition(m.toPosition);
		}else{
			System.err.println("Null move!");
		}
		//update whose go it is
		if(m_whiteMove){
			m_whiteMove = false;
		}else{
			m_whiteMove = true;
		}
	}

	/*
	 * Checks each of the 64 squares, adds any to the list which 
	 * contain pieces of the team whose go it is.
	 */
	public ArrayList<String> getFromSquares() {
		ArrayList<String> list = new ArrayList<String>();
		long allPieces;
		if(m_whiteMove){
			allPieces = m_ce.getProtocol().getCurrentPosition().getWhite().allPieces;
		}else{
			allPieces = m_ce.getProtocol().getCurrentPosition().getBlack().allPieces;
		}
		int i;
		long l;
		for(i = 0; i < 64; i++){
			l = m_sq.getSquare(i);
			if((allPieces & l) != Square._EMPTY_BOARD){
				list.add(m_sq.getSquareName(l));
			}
		}
		return list;
	}

	/*
	 * Checks every square, sees if it is legal move for
	 * piece from 'from' square.
	 */
	public ArrayList<String> getToSquares(String f) {
		ArrayList<String> list = new ArrayList<String>();
		int i;
		long to;
		long from = m_sq.getSquare(f.toUpperCase());
		for(i=0;i<64;i++){
			to = m_sq.getSquare(i);
			if(m_ce.getProtocol().getCurrentPosition().isLegalMove(from, to)){
				list.add(m_sq.getSquareName(to));
			}
		}
		return list;
	}

	/*
	 * plays the hint move which was stored earlier
	 * when getting the hint for projection in getNextHint()
	 */
	public void executeHint() {
		updatePosition(m_nextMove);
	}

	/*
	 * plays the custom move the user selected from the drop
	 * down boxes.
	 */
	public void executeMove(String move) {
		System.out.println(move.substring(0, 2).toUpperCase());
		System.out.println(move.substring(2, 4).toUpperCase());
		long from = m_sq.getSquare(move.substring(0, 2).toUpperCase());
		long to = m_sq.getSquare(move.substring(2, 4).toUpperCase());
		Position.Move m = new Position.Move(from, to, PromotionPiece.NONE);
		updatePosition(m);
	}
}
