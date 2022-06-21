package module.nlu;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import util.Pair;
import util.StateMachine;
import edu.cmu.sphinx.util.props.Configurable;
import edu.cmu.sphinx.util.props.PropertyException;
import edu.cmu.sphinx.util.props.PropertySheet;
import edu.cmu.sphinx.util.props.S4Component;
import edu.cmu.sphinx.util.props.S4String;
import module.dm.Game;
import iu.RobotIntentionFrameIU;
import module.dm.Robot;
import module.vision.SceneUpdater;
import inpro.incremental.IUModule;
import inpro.incremental.unit.EditMessage;
import inpro.incremental.unit.IU;
import inpro.incremental.unit.WordIU;

/**
 * Abstract class for a generic user in a situated scenario
 * with a robot.
 * 
 * This is a subclass of an incremental module in InproTk.
 * 
 * @author jhough
 */
public abstract class User extends IUModule {
	
	@S4String(defaultValue = "")
	public final static String STATE_MACHINE_FILE = "state_machine_file";
	protected StateMachine<UserActionState> actionStateMachine; //Harel state machine that loads from file showing what the user's doing
	
	@S4String(defaultValue = "")
	public final static String GRAMMAR = "grammar";
	
	@S4String(defaultValue = "")
	public final static String LANGUAGE = "language";
	public DArecognizer DA;
	public WordUtil wordUtil;
	
	@S4String(defaultValue = "")
	public final static String CLASSIFIERS = "classifiers";
	
	private SemanticParser parser; //Semantic Parser
	
	@S4Component(type = SceneUpdater.class)
	public final static String SCENE = "scene";
	private SceneUpdater scene;
	
	//@S4Component(type = Robot.class)
	@S4String(defaultValue = "")
	public final static String ROBOT = "robot";
	public Robot robot; //Has access by reference to robot state
	
	public Game game; //Has access by reference to game state
	
	
	
	protected RobotIntentionFrameIU currentFrame; //the current frame being sent to the robot, initialized to null
	protected DialogueAct currentDialogueAct; //the current dialogue act, initialized to null
	protected List<Pair<WordIU, String>> currentDialogueActWordStack; //the words in the current dialogue act //TODO change to all words so far?
	protected int lastWordEndTime; //millis

	private List<Pair<Integer, Integer>> wordIUtoParserStateMap;
	private List<String> parsedWords; // a list of the words parsed successfully
	public HashMap<Integer, DialogueAct> processedDialogueActs; // a list of the dialogue acts with one to one mapping to parsed words
	
	public double getTimeSinceLastWordEnd(){
		return this.getTime() - lastWordEndTime;
	}
	
	public void setLastWordEndTime(int time){
		lastWordEndTime = time;
	}
	
	abstract public void setGame(Robot robot);
	
	/**
	 * Gets game from the robot component.
	 * 
	 * @param robot
	 */
	abstract public void setRobot(String robot);
	
	public abstract Game getGame();
	
	public abstract Robot getRobot();
	
	public synchronized void groundWordIUinParserStateIdx(IU iu, int state_idx){
		logger.debug("grounding " + iu.getID() + " " + iu.toPayLoad());
		Pair<Integer, Integer> pair = new Pair<Integer, Integer>(iu.getID(), state_idx);
		wordIUtoParserStateMap.add(pair);
		logger.debug(this.wordIUtoParserStateMap);
		parsedWords.add(iu.getID() + " " + iu.toPayLoad());
	}
	
	public synchronized void rollBackParserStateFromWordIU(IU iu){
		logger.debug("rolling back to before IU with id " + iu.getID());
		if (this.wordIUtoParserStateMap.isEmpty()){
		//if (this.parsedWords.isEmpty()){
			logger.debug("Empty state, no need to roll back");
			return;
		}
		this.processedDialogueActs.remove(iu.getID());
		int rollback = 0;
		int current_idx = wordIUtoParserStateMap.size()-1;
		logger.debug("Current right frontier index: " + current_idx);
		for (int i=current_idx; i >= 0; i=i-1){
			int id = wordIUtoParserStateMap.get(i).getLeft();
			rollback++;
			if (iu.getID()==id){
				logger.debug("matched ID " + id);
				logger.debug("rolling back " + rollback);
				this.parser.rollBack(rollback);
				this.wordIUtoParserStateMap = this.wordIUtoParserStateMap.subList(0, (current_idx-rollback));
				this.parsedWords = this.parsedWords.subList(0, (current_idx-rollback));
				return;
			}

		}
		logger.debug("No matching parser state found");
		
	}
	
	public SemanticParser getParser(){
		return this.parser;
	}
	
	/**
	 * Do the desired processing for ADDed WordIU. 
	 * 
	 * @param iu
	 */
	protected synchronized void processAddedWordIU(WordIU iu) {
		String word = wordUtil.normalizeFromASR(iu.toPayLoad().toLowerCase());
		DialogueAct dact = DA.recognizeGroundingByKeyWord(word);
		boolean successfulParse = this.parser.parseWord(word);
		if (successfulParse){
			this.groundWordIUinParserStateIdx(iu, this.parser.state_history.size()-1);
		}
		this.processedDialogueActs.put(iu.getID(), dact);
		
	}
	
	/**
	 * Do the desired processing for REVOKEd WordIU. 
	 * 
	 * @param iu
	 */
	protected synchronized void processRevokedWordIU(WordIU iu) {
		this.rollBackParserStateFromWordIU(iu);
	}
	
	@Override
	public void newProperties(PropertySheet ps) throws PropertyException {
		super.newProperties(ps);
		scene = (SceneUpdater) ps.getComponent(SCENE);
		System.out.println("loaded scene updater");
		parser = new SemanticParser(ps.getString(GRAMMAR),true);
		logger.info("loaded parser");
		DA = new DArecognizer(ps.getString(LANGUAGE).split("-")[0]);
		logger.info("loaded DA");
		wordUtil = new WordUtil(ps.getString(LANGUAGE));
		setRobot(ps.getString(ROBOT));
		setGame(this.getRobot());
		actionStateMachine = new StateMachine<UserActionState>(ps.getString(STATE_MACHINE_FILE),UserActionState.values());
		lastWordEndTime = this.getTime();
		currentFrame = new RobotIntentionFrameIU("-1", "-1", 1.0);
		currentDialogueAct = null;
		currentDialogueActWordStack = new ArrayList<Pair<WordIU, String>>();
		wordIUtoParserStateMap = new ArrayList<Pair<Integer, Integer>>();
		parsedWords = new ArrayList<String>();
		processedDialogueActs = new HashMap<Integer, DialogueAct>();
		
	}
	
	@Override
	protected void leftBufferUpdate(Collection<? extends IU> ius,
			List<? extends EditMessage<? extends IU>> edits) {
		// TODO Auto-generated method stub
		
	}

}
