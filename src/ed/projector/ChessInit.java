package ed.projector;

import org.forritan.talvmenni.ChessEngine;
import org.forritan.talvmenni.TalvMenni;
import org.forritan.talvmenni.knowledge.Move;
import org.forritan.talvmenni.knowledge.MoveHistory;
import org.forritan.talvmenni.knowledge.Position;
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
	
	//constructor
	public ChessInit(){
		//set up new game just to get default position
		//use default values from talvmenni project
		PrincipalVariation pv = Factory.create(TalvMenni.PLY);
		TheoryBook tb = new TheoryBook(TalvMenni.MAX_THEORY_BOOK_ENTRIES);
		m_ce = ChessEngine.create(new AlphaBetaStrategy(TalvMenni.PLY, tb, pv));
		m_ce.run();
		m_ce.getProtocol().newGame();
		
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
		//debug
		System.out.println(s);
		int isTake = updatePosition(m);
		return s+isTake;
	}
	
	private int updatePosition(Position.Move move){
		if(m_whiteMove){
			m_whiteMove = false;
		}else{
			m_whiteMove = true;
		}
		if(move != null){
			Move m = new Move(m_ce.getProtocol().getCurrentPosition(),
					move.from, move.to, move.promotionPiece);
			MoveHistory.getInstance().add(m);
			m_ce.getProtocol().setCurrentPosition(m.toPosition);
		}else{
			System.err.println("Null move!");
		}
		//Always returning 0 (no take)
		//TODO: deal with taking pieces.
		return 0;
	}
}
