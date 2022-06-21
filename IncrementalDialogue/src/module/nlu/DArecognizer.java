package module.nlu;

import inpro.incremental.unit.WordIU;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import sium.nlu.stat.Distribution;
import util.Stats;
import util.Pair;

public class DArecognizer {
	/***
	 * A simple incremental Dialogue Act classifier for the user's speech.
	 * Decides whether an incoming word continues the last dialogue act,
	 * ends it or starts a new one, and classifies it as a given @MoveType
	 */
	
	//NB silentconfirm is from timeout of given length, rather than a <sil>
	//TODO make language general. Add XML resources.
	public static Set<String> confirmWords;
	public static Set<String> rejectWords;
	public static Set<String> hesitateWords;
	
	
	public DArecognizer(String language) {
		super();
		// TODO should do this from a file
		switch (language){
		case "de":
			confirmWords = new HashSet<String>(Arrays.asList(
					"genau", "ja", "supi", "danke", "gut", "stimmt", "super", "korrekt", "<silentconfirm>", "richtig"));
			rejectWords = new HashSet<String>(Arrays.asList("nein", "nee", "nichts", "falsch", "anderer", "9", "stop", "abrechen"));
			hesitateWords = new HashSet<String>(Arrays.asList("ähm", "äh", "hm", "<sil>"));
			break;
		case "en":
			confirmWords = new HashSet<String>(Arrays.asList(
					"yeah", "yes", "super", "okay", "good", "correct", "confirm", "<silentconfirm>"));
			rejectWords = new HashSet<String>(Arrays.asList("no", "nope", "stop", "wrong", "other", "cancel"));
			hesitateWords = new HashSet<String>(Arrays.asList("um", "uh", "er", "hmm", "<sil>"));
			break;
		default:
			System.err.println("No language for DA recognition:" + language);
			break;
		}
		
		
	}
	
	public DialogueAct recognizeGroundingByKeyWord(String word){
		if (confirmWords.contains(word)) return DialogueAct.CONFIRM;
		if (rejectWords.contains(word)) return DialogueAct.REJECT;
		if (hesitateWords.contains(word))return DialogueAct.HESITATE;
		return null;
	}


		
	public Pair<Boolean, DialogueAct> recognize(
			double timeSinceLastWord,
			ArrayList<Distribution<String>> referentDistributionStack,
			String word,
			List<Pair<WordIU,String>> previousWordIUs,
			UserActionState prevState,
			Set<String> robotFluents,
			ArrayList<String> piecesInPlay, 
			ArrayList<String> destinationsInPlay,
			boolean incremental) {
		/*
		 * Makes a decision whether the incoming word
		 * Extends the current act or starts afresh.
		 * Also classifies it as a move type
		 */
		boolean newact = false; //or, in terms of an incremental user model, whether changing state or not
		DialogueAct move = null;
		//check to see if latest word in key word lists
		boolean confirm = confirmWords.contains(word) ? true : false;
		boolean reject = rejectWords.contains(word) ? true : false;
		boolean hesitate = hesitateWords.contains(word) ? true : false;
	
		boolean unfilledpause = timeSinceLastWord > 1.5 ? true : false;
		
		//TODO eventually get language model in here for syntactic completeness too
		
		//Features to help decision as to whether this
		//Is a WAC (repair) word
		//by looking at semantics in WAC- look at the diff in entropy tiggered by the last word
		Distribution<String> currentReferentDistribution = referentDistributionStack.size()>0 ?
				referentDistributionStack.get(referentDistributionStack.size()-1) : Stats.uniformDistribution(new HashSet<String>(piecesInPlay));
		Distribution<String> previousReferentDistribution = referentDistributionStack.size()>1 ? 
				referentDistributionStack.get(referentDistributionStack.size()-2) : Stats.uniformDistributionFromDist(currentReferentDistribution);
		
		double entropyReduction = previousReferentDistribution.getEntropy() - currentReferentDistribution.getEntropy();
		System.out.print("WAC entropy reduction: ");
		System.out.println(entropyReduction);
		
		if (confirm){
			move = DialogueAct.CONFIRM;
			newact = true; //TODO not if constant 'ja ja ja' with no state change
		} else if (reject) {
			move = DialogueAct.REJECT;
			newact = true; //TODO not if repeated 'nein nein nein'
		} else if (hesitate){
			//move = prevAct.getMoveType();
			move = DialogueAct.HESITATE;
			newact = false;
		} else {
			//not a communication management tool, 
			//just classify whether INSTRUCT or REPAIR (of either OBJECT or DESTINATION)
			//the state model should do the rest
			String pieceunderdiscussion = "-1";
			String destinationunderdiscussion = "-1";
			for (String f : robotFluents){
				if (f.startsWith("ROBOT_OBJECT.")){
					pieceunderdiscussion = f.replaceAll("ROBOT_OBJECT.", "");
				} else if (f.startsWith("ROBOT_DESTINATION.")){
					destinationunderdiscussion = f.replaceAll("ROBOT_DESTINATION.","");
				}
			}
			if ((robotFluents.contains("ROBOT.TAKING")||robotFluents.contains("ROBOT.WAITING_FOR_OBJECT"))
					&&!robotFluents.contains("ROBOT.HOLDING_OBJECT")){
				//blocking the interpretation that 
				if (!pieceunderdiscussion.equals(currentReferentDistribution.getArgMax().getEntity())){
					//new argmax not the same as pento's as he's displayed it
					if (pieceunderdiscussion.equals("-1")){
						//either still trying to convey the piece
						newact = false;
						move = DialogueAct.INSTRUCT_OBJECT;
					} else {
						//or repairing the current one
						newact = true;
						move = DialogueAct.REPAIR_OBJECT;
					}
				} else if (!currentReferentDistribution.getArgMax().getEntity()
					.equals(previousReferentDistribution.getArgMax().getEntity())){
					//WAC has changed its mind! TODO self repair, as hasn't reacted yet
					newact = true;
					move = DialogueAct.INSTRUCT_OBJECT;
				} else {
					//Top rank has stayed the same //assume forward looking destination
					newact = false;
					move = DialogueAct.INSTRUCT_DESTINATION;
				}
			} else if (robotFluents.contains("ROBOT.PLACING")||robotFluents.contains("ROBOT.WAITING_FOR_DESTINATION")){
				//bit simpler for the destination
				boolean relevantupdate = false;
				String relevantPos = null;
				for (String targetpos : destinationsInPlay){
					int targetasnumber = Integer.parseInt(targetpos);
					String targetasword = WordUtil.convertGermanInteger(targetasnumber); 
					if (word.equals(targetpos)|
							word.equals(WordUtil.ordinalFromStringInt(targetpos))|
							word.equals(WordUtil.romanFromStringInt(targetpos))|
							word.equals(targetasword)){
						
						relevantupdate = true;
						relevantPos = targetpos;
						break;
						
					}
				}
				if (!relevantupdate){
					move = DialogueAct.INSTRUCT_DESTINATION;
				} else {
					if (!destinationunderdiscussion.equals(relevantPos)&&!destinationunderdiscussion.equals("-1")){
						move = DialogueAct.REPAIR_DESTINATION;
					} else {
						move = DialogueAct.INSTRUCT_DESTINATION;
					}
				}
			
			}
			
		}

		return new Pair<Boolean,DialogueAct>(newact,move);
	}



}
