package util;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import module.nlu.UserActionState;

/**
 * A simple Finite State Machine which gives the allowable transitions from one state to the next.
 * Can function as a Harel State Machine as transitions can be disjunctive and reference hierarchical states. 
 * 
 * @author jhough 
 */
public class StateMachine<StateType extends java.lang.Enum<StateType>> {
	private static Logger logger;
	private StateType[] states; //the possible state values
	private Map<Enum<StateType>, Integer> stateToIndexMap; //map from state to index in states
	private StateTransition[][] transitions; //a matrix of StateTransitions where the first dimension is domain state and second is the range
	private Set<String> conditions; //set of all the possible holding conditions in Harel terms for this state chart
	private String transitionRegex = "^([A-Z_]*)\\(([A-Z_\\.\\:\\|]*)\\)"; //for parsing pred(event)-arg(condition) structures for transition labels
	Pattern transition_reg = Pattern.compile(transitionRegex);
	
	public StateType getResultingState(StateType state, String event, Set<String> state_values){
		StateLoop : for (int i=0; i<states.length; i++){
			StateTransition test = transitions[stateToIndexMap.get(state)][i];
			if (test==null){
				continue;
			}
			String test_event = test.getHead();
			ArrayList<String> test_conditions = test.getConditions();
			if (test_event==null||test_event.equals(event)){
				if (test_conditions==null||test_conditions.isEmpty()){ //nothing specific needs to hold
					return states[i];
				}
				if (state_values==null||state_values.isEmpty()){
					continue;
				}
				//to check that all the conditions hold, simply ensure that every condition in the transition is met
				for (String c : test_conditions){
					if (!state_values.contains(c)){
						continue StateLoop;
					}
				}
				return states[i];
				
			}
		}
		return null;
	}
	
	public StateType getResultingState(StateType state, String event) {
		return this.getResultingState(state, event, null);
	}
	
	public StateType getResultingStateBackwards(StateType state, String event, Set<String> state_values){
		//get the previous state from the event being applied
		StateLoop : for (int i=0; i<states.length; i++){
			StateTransition test = transitions[i][stateToIndexMap.get(state)];
			if (test==null){
				continue;
			}
			String test_event = test.getHead();
			ArrayList<String> test_conditions = test.getConditions();
			if (test_event==null||test_event.equals(event)){
				if (test_conditions==null||test_conditions.isEmpty()){ //nothing specific needs to hold
					return states[i];
				}
				if (state_values==null||state_values.isEmpty()){
					continue;
				}
				//to check that all the conditions hold, simply ensure that every condition in the transition is met
				for (String c : test_conditions){
					if (!state_values.contains(c)){
						continue StateLoop;
					}
				}
				return states[i];
				
			}
		}
		return null;
	}
	
	public StateType getResultingStateBackwards(StateType state, String event){
		return this.getResultingStateBackwards(state, event, null);
	}
	
	public StateTransition getTransitions(StateType domain, StateType range){
		return transitions[stateToIndexMap.get(domain)][stateToIndexMap.get(range)];
	}
	
	public void addTransition(Enum<StateType> domain, Enum<StateType> range, StateTransition trans){
		transitions[stateToIndexMap.get(domain)][stateToIndexMap.get(range)] = trans;
	}
	
	public void loadStateMachineFromCSVFile(String filename) throws FileNotFoundException{
		logger.info("loading state machine from file.");
		Map<String,Enum<StateType>> statenamemap = new HashMap<String,Enum<StateType>>();
		for (Enum<StateType> state : states){
			statenamemap.put(state.name(), state);
		}
		Scanner inputStream = new Scanner(new File(filename));
		Map<Integer,Enum<StateType>> columnnumbertostatemap = new HashMap<Integer,Enum<StateType>>();
	
		
		
		boolean hasheader = false;
		while (inputStream.hasNextLine())
		{
			String line = inputStream.nextLine();
			if (hasheader == false) {
				hasheader=true;
				String[] header = line.split(",");
				for (int r=0; r<header.length; r++){
					columnnumbertostatemap.put(r, statenamemap.get(header[r]));
				}
				continue;
			}
		    String[] fields = line.split(",");
		    if (fields.length == states.length+1)
		    {	
		    	Enum<StateType> domain = statenamemap.get(fields[0]);
		    	//logger.debug(domain + ":");
		        for (int f=1; f<fields.length; f++){
		        	String field = fields[f];
		        	//logger.debug("cell=" + field);
		        	Enum<StateType> range = columnnumbertostatemap.get(f);
		        	if (field.equals("0")){
		        		this.addTransition(domain, range, null);
		        	} else {
		        		//we need to get the conditions as separate
		        		Matcher m = transition_reg.matcher(field);
		        		StateTransition trans = null;
		        		if (m.find()){
		        			String event = m.group(1);
		        			//logger.debug("(event=" + event + ")");
		        			ArrayList<String> myconditions = new ArrayList<String>( Arrays.asList( m.group(2).split(":")));
		        			for (String condition : myconditions){
		        				this.conditions.add(condition);
		        			}
		        			trans = new StateTransition(event,myconditions);
		        			if (trans.getHead()==null){
		        				logger.debug("warning null head in reading in cell!");
		        				//logger.debug(domain + ":");
		        				logger.debug("cell=" + field);
		        				//logger.debug("(event=" + event + ")");
		        			}
		        		} else {
		        			trans = new StateTransition(field);
		        			//logger.debug("(event=" + field + ")");
		        		}
		        		this.addTransition(domain, range, trans);
		        	}	
		        }
		    }
		    else
		    {
		        logger.error("Invalid line: " + line);
		    }
		}
		inputStream.close();
		logger.info("Loaded state machine from file: " + filename);
	}
	
	/**
	 * Constructor for state machine from the file specifying transitions.
	 *  
	 * @param filename
	 * @param newstates
	 */
	public StateMachine(String filename, StateType[] newstates){
		PropertyConfigurator.configure("log4j.properties");
		logger = Logger.getLogger(StateMachine.class);
		
		stateToIndexMap = new HashMap<Enum<StateType>,Integer>();
		conditions = new HashSet<String>();
		states = newstates;

		for (int i=0; i<states.length; i++){
			Enum<StateType> state = states[i];
			stateToIndexMap.put(state, i);
		}
		
		transitions = new StateTransition[states.length][states.length];
		
		//read in the state transitions in the csv file
		try {
			loadStateMachineFromCSVFile(filename);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			logger.error("State machine file not found: " + filename);
			e.printStackTrace();
		}
		
	}
	
	public static void main(String[] args) {

		StateMachine<UserActionState> s = new StateMachine<UserActionState>("/home/dsg-labuser/git/015_pentoland/code/java/Pentoland/resources/PentoRobModels/UserHarelSM.csv",UserActionState.values());
		
		/*
		for (StateTransition[] t : s.transitions){
			for (StateTransition t1 : t){
				if (t1==null){
					logger.info("null");
					continue;
				}
				if (t1.getHead()==null){
					logger.info("null head");
				}
				System.out.println("trans: " + t1.getHead());
				logger.info(t1.getConditions());
			}
		}
		*/
		System.out.println("conds");
		for (Object c : s.conditions.toArray()){
			logger.info((String) c);
		}
		HashSet<String> myconditions = new HashSet<String>(Arrays.asList("ROBOT.WAITING_FOR_PIECE"));
		UserActionState mystate = s.getResultingState(UserActionState.CONFIRMING_DESTINATION, "INSTRUCT_PIECE",myconditions);
		logger.info(mystate);
		
	}

	
	

}
