package module.nlu;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.cmu.sphinx.util.props.Configurable;
import edu.cmu.sphinx.util.props.PropertyException;
import edu.cmu.sphinx.util.props.PropertySheet;
import edu.cmu.sphinx.util.props.S4Boolean;
import edu.cmu.sphinx.util.props.S4Double;
import edu.cmu.sphinx.util.props.S4String;
import inpro.incremental.unit.EditMessage;
import inpro.incremental.unit.EditType;
import inpro.incremental.unit.IU;
import inpro.incremental.unit.WordIU;
import iu.RobotIntentionFrameIU;
import iu.SceneIU;
import module.dm.Game;
import module.dm.Robot;
import module.vision.WorldBelief;
import module.vision.WorldBeliefParser;

public class UserDev extends User {
	
	//Test to switch between different languages and grammars in the NLU module
	//Need DE + EN to work
	//Need different DS-TTR grammars to work

	@S4String(defaultValue = "")
	public final static String GRAMMAR = "grammar";
	
	@S4Boolean(defaultValue = true)
	public final static String INCREMENTAL = "incremental";
	private boolean incremental; //whether we wait for all slots to be filled PLACE(X,Y) or move after each part (i.e. TAKE(X) then PLACE(X,Y)).
	
	@S4Double(defaultValue = 0.03)
	public final static String THRESHOLD = "threshold";
	private double threshold; //the margin over the second rank above which a selection decision is made (for pieces and locations)
	
	
	private RobotActionRequestFactory requestFactory;
	
	boolean TEST = true;
	
	@Override
	public void newProperties(PropertySheet ps) throws PropertyException {
		super.newProperties(ps);
		logger.info("Setting up NLU");
		this.requestFactory = new RobotActionRequestFactory(game, robot, "resources/classifiers/" + ps.getString(CLASSIFIERS), "lookup");
		this.incremental = ps.getBoolean(INCREMENTAL);
		this.threshold = ps.getDouble(THRESHOLD);
		
		if (TEST) {
			WorldBeliefParser parser = new WorldBeliefParser();
			try {
				BufferedReader br = new BufferedReader(new FileReader("resources/sample-images/famula/XMLWorldBelief_2017-07-25_15-23-38.xml"));
				List<String> objectTexts = new ArrayList<String>();

					String input = "";
					String line = br.readLine();
					while (line != null) {
						// System.out.println(line);
						input += line;
						input += System.lineSeparator();
						line = br.readLine();
					}
					String xmlInput = input.toString();
					// Parse the xmlInput
					WorldBelief wb = parser.parseWorldBeliefFromXML(xmlInput);
					
					//SIMULATE new vision
					this.requestFactory.updateVision(wb);
					//factory.updateActionRequestDistribution();

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
		}
		
		
		
	}
	
	@Override
	public void setGame(Robot robot) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setRobot(String robot) {
		// TODO Auto-generated method stub
		logger.info("robot model: " + robot);
		
	}
	
	@Override
	public Game getGame() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Robot getRobot() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void processAddedWordIU(WordIU iu){
		super.processAddedWordIU(iu);
		this.requestFactory.updateParseSemantics(this.getParser().getMaxSemantics());
		this.currentDialogueAct = this.processedDialogueActs.get(iu.getID());
		logger.info(this.currentDialogueAct);
		// assume the error is in the classifier rather than ASR
		
	}
	
	@Override
	public void processRevokedWordIU(WordIU iu){
		super.processRevokedWordIU(iu);
		
	}
	
	@Override
	protected void leftBufferUpdate(Collection<? extends IU> ius,
			List<? extends EditMessage<? extends IU>> edits) {
		
		
		ArrayList<EditMessage<RobotIntentionFrameIU>> newEdits = new ArrayList<EditMessage<RobotIntentionFrameIU>>();
		for (EditMessage<? extends IU> edit: edits) {
			IU iu = edit.getIU();
			if (iu instanceof WordIU){
				String word = iu.toPayLoad();
				switch (edit.getType()) {
					case ADD:
						logger.debug(System.currentTimeMillis() + "," + word +","+edit.getType().toString()+","+iu.getID());
						logger.info("adding " + edit.getIU().toPayLoad());
						this.processAddedWordIU((WordIU) iu); 
						break;
					case REVOKE:
						logger.debug(System.currentTimeMillis() + "," + word +","+edit.getType().toString()+","+iu.getID());
						logger.info("revoking " + edit.getIU().toPayLoad());
						//TODO this.parser.rollback.. TODO and merge??
						this.processRevokedWordIU((WordIU) iu);
						break;
					case COMMIT:
						break;
					default:
						break;
				}

			} else if (iu instanceof SceneIU){
				switch (edit.getType()) {
					case ADD:
						logger.debug("getting SceneIU");
						logger.info("adding " + edit.getIU().toPayLoad());
						this.requestFactory.updateVision(((SceneIU) iu).getWorldBelief());
						break;
					case REVOKE:
						break;
					case COMMIT:
						break;
					default:
						break;
				}
			}
		} 
		// with updates complete, we now have a new action request to send on to the robot state (possibly)
		logger.info(this.requestFactory.getCurrentActionRequest());
		if (this.requestFactory.getConfidenceInArgMax()>this.threshold){
			List<RobotIntentionFrameIU> newFrames = this.requestFactory.getCurrentActionRequest();
			if (newFrames!=null){
				for (RobotIntentionFrameIU newFrame : newFrames){
					if (this.currentFrame==null||this.currentFrame.getObjectID().equals("-1")){
						this.currentFrame = newFrame;
						newEdits.add(new EditMessage<RobotIntentionFrameIU>(EditType.ADD, newFrame));
						
					} else {
						if (this.currentFrame.getAction().equals(newFrame.getAction())&&
								this.currentFrame.getObjectID().equals(newFrame.getObjectID())&&
								this.currentFrame.getDestinationIDs().equals(newFrame.getDestinationIDs())){
							continue;
						}
						newEdits.add(new EditMessage<RobotIntentionFrameIU>(EditType.ADD, newFrame));
					}
				}
			}
		}
		
		if (!TEST){
			rightBuffer.setBuffer(newEdits);	
		}

		
	}

}
