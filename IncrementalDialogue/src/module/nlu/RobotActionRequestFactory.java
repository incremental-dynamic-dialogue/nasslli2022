package module.nlu;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import iu.RobotIntentionFrameIU;
import util.Pair;
import util.Stats;
import util.XmlIO;
import module.vision.WorldBelief;
import module.dm.Game;
import module.dm.Robot;
import module.dm.RobotState;

import org.apache.log4j.Logger;

import qmul.ds.formula.Formula;
import qmul.ds.formula.PredicateArgumentFormula;
import qmul.ds.formula.TTRField;
import qmul.ds.formula.TTRLabel;
import qmul.ds.formula.TTRPath;
import qmul.ds.formula.TTRRecordType;
import semantics.GoalActionType;
import semantics.GoalLocationRelation;
import sium.nlu.stat.DistRow;
import sium.nlu.stat.Distribution;
import semantics.typesasclassifiers.AndClassifier;
import semantics.typesasclassifiers.CardinalityClassifier;
import semantics.typesasclassifiers.Classifier;
import semantics.typesasclassifiers.ClassifierGraph;
import semantics.typesasclassifiers.EntityClassifier;
import semantics.typesasclassifiers.EntityLookUpClassifier;
import semantics.typesasclassifiers.EventClassifier;
import semantics.typesasclassifiers.ExtensionalClassifier;
import semantics.typesasclassifiers.GoalLocationRelationLoopUpClassifier;
import semantics.typesasclassifiers.IntensionalClassifier;
import semantics.typesasclassifiers.PersonClassifier;
import semantics.typesasclassifiers.PositionalLookUpClassifier;
import semantics.typesasclassifiers.RecordTypeClassifier;
import semantics.typesasclassifiers.RelationalRoleClassifier;


/**
 * Class to generate the appropriate robotic action request (possibly none given updates to various sources of input) 
 * 
 * Given we have:
 * - the maximal semantics from the parse, which includes objects in the scene resolved
 * - the scene information or world belief
 * - the game state
 * - the robot state
 * - the user state
 * output the instruction to be sent to the robot's API or central state machine.
 *
 * @author jhough
 */
public class RobotActionRequestFactory {

	public final int MAX_OBJECT_CARDINALITY = 3; //The number of objects that can be ordered to be picked up in a row- lowers complexity
	
	public static Logger logger;
	
	public Game game; // accessible for its state
	public Robot robot; // accessible for its state
	
	public ClassifierGraph classifierGraph;
	public TTRRecordType parse; // the semantic representation from the parser so far
	public TTRRecordType groundedSem; // TODO a linked list between grounded preds/constants in the parse and the classifiers in the WB and/or the actions. the parts 
	public WorldBelief wb;
	public RobotState robotState;
	public UserActionState userActionState;
	
	public List<RobotIntentionFrameIU> actionRequest; // the arg max action request that is updated
	public Distribution<List<RobotIntentionFrameIU>> actionRequestDistribution;
	public Distribution<Formula> formulaDistribution;
	public Distribution<String> actionDistribution;
	public Distribution<String> objectSetDistribution;
	public Set<SortedSet<String>> objectSets; //the powerset of the objects in the scene for plural/multiple objects
	public Set<Classifier> classifiers;
	public Set<Integer> objectQuantifiers;
	public Distribution<String> goalLandmarkObjectDistribution;
	public Distribution<String> relationDistribution;
	public Distribution<String> goalDistribution;
	
	public RobotActionRequestFactory(Game a_game, Robot a_robot, String classifier_directory, String method){
		logger = Logger.getLogger(RobotActionRequestFactory.class);
		this.game = a_game; //get the game type globally
		this.robot = a_robot;
		this.classifiers = new HashSet<Classifier>();
		this.loadClassifiers(classifier_directory, method);
		this.init();

	}
	
	public void init(){
		logger.info("Initializing robot action request factory");
		this.actionRequest = null; //null initial frame
		this.actionRequestDistribution = new Distribution<List<RobotIntentionFrameIU>>();
		Set<String> relations = new HashSet<String>();
		for (semantics.GoalLocationRelation relation : semantics.GoalLocationRelation.values()){
			relations.add(relation.toString());
		}
		Set<String> actions = new HashSet<String>();
		for (semantics.GoalActionType relation : semantics.GoalActionType.values()){
			actions.add(relation.toString());
		}
		this.actionDistribution = Stats.uniformDistribution(actions);
		this.relationDistribution = Stats.uniformDistribution(relations);
		this.objectSetDistribution = new Distribution<String>(); // don't have the world belief yet
		this.objectSets = new HashSet<SortedSet<String>>();
		this.objectQuantifiers = new HashSet<Integer>();
		this.goalLandmarkObjectDistribution = new Distribution<String>();
		this.goalDistribution = new Distribution<String>();
		
		this.classifierGraph = new ClassifierGraph(this.classifiers);
		this.parse = null;
		this.groundedSem = null;
		this.wb = null;
	}
	
	public void loadClassifiers(String classifier_directory, String method){
		if (method.equals("lookup")){
			Path file = Paths.get(classifier_directory + "/lookup_classifiers.txt");
			try (InputStream in = Files.newInputStream(file);
			    BufferedReader reader =
			      new BufferedReader(new InputStreamReader(in))) {
			    String line = null;
			    while ((line = reader.readLine()) != null) {
			        logger.info(line);
			        if (line.contains("position_")){
			        	this.classifiers.add(new PositionalLookUpClassifier(line));
			        } else {
				        this.classifiers.add(new EntityLookUpClassifier(line));
			        }

			    }
			} catch (IOException x) {
			    logger.error(x);
			    logger.error("Need entity classifier file");
			    System.exit(0);
			}
		}
		// load the standard intensional classifiers
	
		for (GoalActionType action : semantics.GoalActionType.values()){
			String action_name = "action_" + action.toString();
			logger.info(action_name.toLowerCase());
			this.classifiers.add(new EventClassifier(action_name.toLowerCase()));
		}
		
		for (GoalLocationRelation relation: semantics.GoalLocationRelation.values()){
			String relation_name = "goal_" + relation.toString();
			logger.info(relation_name.toLowerCase());
			this.classifiers.add(new GoalLocationRelationLoopUpClassifier(relation_name.toLowerCase()));
		}
	
		for (int i=1; i<=this.MAX_OBJECT_CARDINALITY; i++){
			logger.info(String.valueOf(i));
			this.classifiers.add(new CardinalityClassifier("quant_" + String.valueOf(i), i));
		}
		
		this.classifiers.add(new AndClassifier("and"));
		logger.info("and");
		this.classifiers.add(new EntityClassifier("e")); //generic entity
		logger.info("e");
		
		// relational role classifiers like subj(e, x)
		List<String> arity = new ArrayList<String>();
		arity.add("es");
		arity.add("e");
		this.classifiers.add(new RelationalRoleClassifier("subj", arity));
		this.classifiers.add(new RelationalRoleClassifier("obj", arity));
		this.classifiers.add(new RelationalRoleClassifier("indobj", arity));
		this.classifiers.add(new PersonClassifier("addressee"));
		
		logger.info(this.classifiers.size() + " classifiers loaded");
	
	}
	
	public boolean hasWorldBelief(){
		return wb.isEmpty() ? false : true;
	}
	
	/**
	 * Main method for calculating the distribution over frames
	 * 
	 */
	public void applyCurrentClassifierGraphToWorldBelief(){
		if (!hasWorldBelief()){
			logger.debug("No objects in world belief, not applying graph classifier.");
			return;
		}
		logger.debug("Applying classifier graph on current world belief for " + this.parse);
		
		HashMap<String,Distribution<Formula>> classificationApplication = new HashMap<String,Distribution<Formula>>();
		for (Pair<String,Classifier> pair : this.classifierGraph.getClassifierOrder()){
			String[] handle = pair.getLeft().split("@");
			String label = handle[0];
			String formula = handle[1];
			logger.debug(label);
			logger.debug(formula);
			Classifier classifier = pair.getRight();
			//Distribution<Formula> currentDistribution = new Distribution<Formula>();
			if (classifier instanceof ExtensionalClassifier){
				// simple classifier, apply it on the world belief of objects and available actions
				logger.debug(classifier.getName());
				//logger.debug(this.wb);
				Distribution<Formula> formulaDistribution = null;
				//if (classifier instanceof EntityClassifier){
					formulaDistribution = ((ExtensionalClassifier) classifier)
								.getUnnormalizedProbabilityDistributionOverFormulae(
								this.wb,
								semantics.GoalActionType.values()
								);
				//}
				logger.debug(formulaDistribution);
				logger.debug("adding classification " + label);
				classificationApplication.put(label, formulaDistribution);
			} else if (classifier instanceof IntensionalClassifier){
				// conjunctive classifier or quantification classifier, could be:
				// i. record type classifier where fields are all calculated as intersection of distributions
				// ii. conjunctive classifier
				// iii. quantification classifier
				// Set up the argument structure depending on the RT
				logger.debug("Intensional classifier:" + classifier.getName());
				// restart the record type
				if (classifier instanceof RecordTypeClassifier){
					logger.debug("Record type classifier");
					((RecordTypeClassifier) classifier).clearDistributions(); //TODO need to add all distributions again
					// Each field is a conjunct, as all dependencies have already been calculated in the distributions
					for (TTRField f : ((RecordTypeClassifier) classifier).getTTR().getFields()){
						logger.debug("trying to add field " + f + " to possibly internal record type");
						if (f.isHead()){
							logger.debug("head");
							continue;
						}
						if (f.getDSType()!=null){
							String DStypeString = f.getDSType().toString();
							String labelString = f.getLabel().toString();
							if (DStypeString.equals("t")){
								logger.debug("role classifier");
								Distribution<Formula>  dist = classificationApplication.get(labelString);
								logger.debug(dist);
								if (dist!=null){
									// only for those applied, i.e. Extensional
									((RecordTypeClassifier) classifier).addFieldDistribution(labelString, dist);
								} else {
									logger.debug("no distribution for " + labelString);
								}
							
							} else if (f.getType()!=null&&f.getType().toString().contains(", r")) {
								logger.debug("quantification or cardinality term");
								//List<TTRPath> paths = f.getTTRPaths();
								//logger.debug(paths.get(0).getParentRecType());
								//TTRLabel rpath = paths.get(0).getFirstLabel();
								/// for now just replace
								//logger.debug(rpath);
								//Distribution<Formula> dist = classificationApplication.get(rpath);
								Distribution<Formula>  dist = classificationApplication.get(labelString);
								logger.debug(dist);
								if (dist!=null){
									// only for those applied, i.e. Extensional
									((RecordTypeClassifier) classifier).addFieldDistribution(labelString, dist);
								} else {
									logger.debug("no distribution for " + labelString);
								}
							} else {
								Distribution<Formula>  dist = classificationApplication.get(labelString);
								logger.debug(labelString);
								logger.debug(DStypeString);
								logger.debug(dist);
								if (DStypeString.equals("e")){
									((RecordTypeClassifier) classifier).addFieldDistribution(labelString, dist);
								} else if (DStypeString.equals("es")){
									((RecordTypeClassifier) classifier).addFieldDistribution(labelString, dist);
								}
							}
							
							
						} else {
							logger.debug("no ds type in field");
							continue;
						}
					}

					Distribution<Formula> frameDistribution = ((RecordTypeClassifier) classifier).calculateFrameDistribution();
					int top = 3;
					for (int t=0; t<frameDistribution.size(); t++){
						logger.debug(frameDistribution.getItem(t));
						top = top -1;
						if (top<0) break;
					}
					logger.debug("adding classification " + label);
					classificationApplication.put(label, frameDistribution);
					
				} else if (classifier instanceof AndClassifier){
					// conjunction of events or entities
					logger.debug("and classifier");
					
				} else if (classifier instanceof CardinalityClassifier) {
					// less specific than the above
					logger.debug("cardinality classifier");
					logger.debug(this.parse);
					TTRField f = this.parse.getField(new TTRLabel(label));
					logger.debug(f);
					List<TTRPath> paths = f.getTTRPaths();
					logger.debug(paths.get(0).getParentRecType());
					TTRLabel rpath = paths.get(0).getFirstLabel();
					// for now just replace
					logger.debug(rpath);
					Distribution<Formula> internal_dist = classificationApplication.get(rpath.toString());
					logger.debug(internal_dist);
					((CardinalityClassifier) classifier).addDistribution(internal_dist);
					Distribution<Formula> dist = ((CardinalityClassifier) classifier).calculateDistribution();
					logger.debug(dist);
					logger.debug("adding classification " + label);
					classificationApplication.put(label, dist);
					
				} else if (classifier instanceof RelationalRoleClassifier){
					logger.debug("Relational role classifier");
					int arity_index = 0;
					for (Formula relational_role : ((PredicateArgumentFormula) this.parse.getField(new TTRLabel(label)).getType()).getArguments()){
						((RelationalRoleClassifier) classifier).addArgumentDistribution(arity_index, classificationApplication.get(relational_role.toString()));
						arity_index+=1;
					}
					Distribution<Formula> dist = new Distribution<Formula>();
					dist.addProbability(this.parse.getField(new TTRLabel(label)).getType(), 1.0);
							//((RelationalRoleClassifier) classifier).calculateDistribution();
					logger.debug(dist);
					logger.debug("adding classification " + label);
					classificationApplication.put(label, dist);
					
				}
				
			}
			
		}
		
		int topN = 5;
		this.formulaDistribution = new Distribution<Formula>();
		Distribution<Formula> finalFrameDistribution = classificationApplication.get("TOP");
		for (DistRow<Formula> d : finalFrameDistribution.getTopN(topN)){
			this.formulaDistribution.addProbability(d.getEntity(), d.getProbability());
			logger.debug("prob " + d.getProbability());
			for (TTRField f : ((TTRRecordType) d.getEntity()).getFields()){
				logger.debug(f);
			}
			topN-=1;
			if (topN==0) break;
		}
	}
	
	/**
	 * New input from the parser, may have changed the relevant fields in the record type.
	 * @param sem
	 */
	public void updateParseSemantics(TTRRecordType sem){
		logger.debug("recieving new ttr sem");
		// 1. create/update the graph based on the new parse
		this.parse = sem;
		this.classifierGraph.createGraph(this.parse);
		// 2. apply the classifiers on the world belief in the dependency order
		this.applyCurrentClassifierGraphToWorldBelief();
		this.updateActionRequestDistribution();
	}
	
	/**
	 * Updates world belief in its entirety.
	 * Runs the compiled record type classifier graph on it if there is one, assuming the graph is unchanged
	 * 
	 */
	public void updateVision(WorldBelief new_wb){
		logger.info("Receiving new world belief.");
		this.wb = new_wb;
		if (this.classifierGraph.ttr==null) return;
		this.applyCurrentClassifierGraphToWorldBelief();
		this.updateActionRequestDistribution();
	}
	
	public void updateRobotState(RobotState new_robot_state){
		this.robotState = new_robot_state;
		this.updateActionRequestDistribution();
		
	}
	
	public void updateUserActionState(UserActionState new_user_state){
		this.userActionState = new_user_state;
		this.updateActionRequestDistribution();
	}
	
	/**
	 * Do the calculation of the new distribution of action request,
	 * assigning the arg max to {@link actionRequest}.
	 * 
	 * TODO for now do all the calculations in one go.
	 */
	public void updateActionRequestDistribution(){
		this.actionRequestDistribution = new Distribution<List<RobotIntentionFrameIU>>();

		for (int i=0; i<this.formulaDistribution.size(); i++){
			TTRRecordType ttr = (TTRRecordType) this.formulaDistribution.getItem(i).getEntity();
			double prob = this.formulaDistribution.getItem(i).getProbability();
			logger.debug(ttr);
			logger.debug(prob);
			TTRField headField = null;
			if (ttr.getHeadField()==null){
				for (TTRField field : ttr.getFields()){
					if (field.getType()!=null&&field.getType().toString().contains("action_")){
						headField = field;
						break;
					}
				}
			} else {
				headField = ttr.getHeadField();
			}
			GoalActionType goal = this.getHeadAction(ttr);
			//logger.debug(goal);
			String relation = null;
			ArrayList<String> objectObjects = new ArrayList<String>();
			ArrayList<String> indObjObjects = new ArrayList<String>();
			for (TTRField f : ttr.getFields()){
				// just look for relational fields which result in finding other fields
				logger.debug(f);
				if (f.getType()!=null && f.getDSType().toString().equals("t")){
					if (f.getType() instanceof PredicateArgumentFormula){
						String firstArg = ((PredicateArgumentFormula) f.getType()).getArguments().get(0).toString();
						
						
						if (headField!=null&&!headField.getLabel().toString().equals(firstArg)){
							continue;
						}
						String argFieldName = ((PredicateArgumentFormula) f.getType()).getArguments().get(1).toString();
						logger.debug(argFieldName);
						TTRField argField = ttr.getField(new TTRLabel(argFieldName));
						
						
						
						switch (((PredicateArgumentFormula) f.getType()).getPredicate().toString()) {
							case "indobj":
								logger.debug("indobj");
								if ((!goal.toString().equals("GIVE"))&&(argField!=null&&argField.getType()!=null)){
									relation = argField.getType().toString();
								} else {
									relation = null;
									break;
								}
								if (argField.getDSType().toString().equals("es")){
									logger.debug("es type arg");
									
									TTRField embeddedObj = ttr.getDependents(argField).get(0);
									logger.debug("dependent: " + embeddedObj);
								
									if (embeddedObj.getType() instanceof TTRRecordType){
										for (TTRField internalF : ((TTRRecordType) embeddedObj.getType()).getFields()){
											indObjObjects.add(internalF.getType().toString());
										}
									} else {
										indObjObjects.add(embeddedObj.getType().toString());
									}
									
									
								} else if (argField.getDSType().toString().equals("e")){
									logger.warn("no event type relation only e type");
									objectObjects.add(argField.getType().toString());
								}
								
								//if ()
								break;
							case "obj":
								// could be several objects
								if (argField.getType() instanceof TTRRecordType){
									for (TTRField internalF : ((TTRRecordType) argField.getType()).getFields()){
										objectObjects.add(internalF.getType().toString());
									}
								} else {
									objectObjects.add(argField.getType().toString());
								}
								break;
							default:
								break;
						}
						
						
					}
				}
			}
			
			// for each object to be manipulated create a list of actions to be executed
			List<RobotIntentionFrameIU> frameIUList = new ArrayList<RobotIntentionFrameIU>();
			for (String obj : objectObjects){
				logger.debug("adding " + obj);
				// change to PICK if no target destination yet
				if (goal.toString().equals("PLACE")&&indObjObjects.size()==0){
					goal = semantics.GoalActionType.PICK;
				}
				RobotIntentionFrameIU frame = null;
				if (relation==null){
					frame = new RobotIntentionFrameIU(obj, goal.toString(), indObjObjects, prob);
				} else {
					semantics.GoalLocationRelation finalRelation = semantics.GoalLocationRelation.valueOf(relation.split("_")[1].toUpperCase());
					logger.debug(finalRelation);
					frame = new RobotIntentionFrameIU(obj, goal.toString(),
								finalRelation, indObjObjects, prob);
				}
				logger.debug(frame);
				frameIUList.add(frame);
			}
			this.actionRequestDistribution.addProbability(frameIUList, prob);
		}
		
		this.actionRequest = this.actionRequestDistribution.getArgMax().getEntity();
		logger.debug(this.actionRequest);
		
	}
	
	/**
	 * Given the current parse only the probability of the
	 * actionName being the desired action
	 * 
	 * @param actionName
	 * @return
	 */
	private GoalActionType getHeadAction(TTRRecordType a_parse) {
		TTRField headField = null;
		if (a_parse==null||(a_parse.getHeadField()==null||a_parse.getHeadField().getType()==null)){
			// assume uniform distribution if no parse

			if (a_parse.getHeadField()==null){
				for (TTRField field : a_parse.getFields()){
					if (field.getType()!=null&&field.getType().toString().contains("action_")){
						headField = field;
						break;
					}
				}
			} else {
				headField = a_parse.getHeadField();
			}
		} else {
			headField = a_parse.getHeadField();
		}
		
		if (headField==null){
			logger.error("no headField in " + a_parse);
			return null;
		}
		// for now a simple assumption that if there's a match it's 1 else 0
		semantics.GoalActionType goal = null;
		String actionString = headField.getType().toString();
		//logger.debug("Action String = " + actionString);
		// Normalize the values of the parse to the available actions
		if (actionString.equals("action_place")) {
			goal = semantics.GoalActionType.PLACE;
		} else if (actionString.equals("action_pick")) {
			goal = semantics.GoalActionType.PICK;
		} else if (actionString.equals("action_give")) {
			goal = semantics.GoalActionType.GIVE;
		} else {
			logger.error("no action match for " + actionString);
		}
		return goal;
	}
	
	public List<RobotIntentionFrameIU> getCurrentActionRequest() {
		return this.actionRequest;
	}
	
	public double getConfidenceInArgMax(){
		if (this.actionRequestDistribution==null||this.actionRequestDistribution.isEmpty()){
			return 0;
		}
		return this.actionRequestDistribution.getArgMax().getProbability();
	}

	public List<String> getCurrentActionRequestXMLStrings(){
		ArrayList<String> instructions = new ArrayList<String>();
		for (RobotIntentionFrameIU frameIU : this.getCurrentActionRequest()){
			String objID = frameIU.getObjectID();
			logger.info(objID);
			String wbString = wb.getXMLStringForObj(objID);
			
			String instruction = "";
			
			String action = frameIU.getAction();
			logger.info(action);
			
			
			instruction += String.format("<pre><%s><STATUS origin=\"Submitter\" value=\"initiated\"/>%s</%s></pre>", action, wbString, action);
			
			instruction = new XmlIO().prettyFormat(instruction);
			
			//actionRequest = new RobotActionIU(action, pick_instruction);
			//actionRequest.setInstruction(pick_instruction);
			instructions.add(instruction);
		}
		
		return instructions;
	}
	
	public static void main(String[] args) {
		//Game g = null;
		//Robot r = null;
	}
	
	
}