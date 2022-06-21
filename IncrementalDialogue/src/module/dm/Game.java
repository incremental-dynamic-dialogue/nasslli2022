package module.dm;

import org.apache.log4j.Logger;

/**
 * An abstract class for a collaborative game played between robot and human.
 * 
 * @author jhough
 */
public abstract class Game {
	
	private boolean inProgress;
	private long startTime;
	protected String NAME;
    protected Logger logger;
	
	public Game(){
		logger = Logger.getLogger(this.getClass());
		setName(this.getClass().getSimpleName());
		setInProgress(false);	
	}
	
	public void start(){
		java.awt.Toolkit.getDefaultToolkit().beep();
		this.startTime = System.currentTimeMillis();
		logger.info("GAME STARTED AT " + this.startTime);
		logger.info("GAME TYPE:" + this.getName());
		setInProgress(true);
	}
	
	public void end(){
		long duration =  System.currentTimeMillis() - this.startTime;
		logger.info("GAME FINISHED AT " + System.currentTimeMillis());
		logger.info("Duration: ");
		logger.info(duration);
		java.awt.Toolkit.getDefaultToolkit().beep();
		setInProgress(false);
	}

	public boolean inProgress() {
		return inProgress;
	}

	public void setInProgress(boolean playing) {
		this.inProgress = playing;
	}
	
	public void setName(String name){
		this.NAME = name;
	}
	
	public String getName(){
		return this.NAME;
	}

	
	public abstract Integer[] getCentroidOfCentroidsOfAvailableTargetDestinations();

	public abstract Integer[] geCenroidOfCurrentTargetLocation();
	
	/**
	 * Criterion has been met for the game to be over.
	 */
	public abstract boolean completed();


}
