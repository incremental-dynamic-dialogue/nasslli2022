package module.nlu;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.cmu.sphinx.util.props.PropertyException;
import edu.cmu.sphinx.util.props.PropertySheet;
import edu.cmu.sphinx.util.props.S4String;
import inpro.incremental.IUModule;

import inpro.incremental.unit.EditMessage;
import inpro.incremental.unit.EditType;
import inpro.incremental.unit.IU;
import inpro.incremental.unit.WordIU;
import iu.DialogueActIU;
import iu.NumberIU;
import util.Pair;
import util.WordUtil;

public class NLUnumbers extends IUModule {
	
	@S4String(defaultValue = "")
	public final static String LANGUAGE = "language";
	
	public WordUtil wordUtil;
	
	private List<Pair<Integer, NumberIU>> wordIUtoNumberIUMap;  //records which word IU triggered which number IU
	private ArrayList<WordIU> revokeStack; //the words that may be revoked but not until computing the diff
	
	
	/**
	 * Do the desired processing for ADDed WordIU. 
	 * 
	 * @param iu
	 */
	protected synchronized void processAddedWordIU(WordIU iu) {
		logger.debug("Processing added word " + iu.toPayLoad());
		if (this.wordUtil.isConfirmWord(iu.toPayLoad())) {
			ArrayList<EditMessage<DialogueActIU>> actEdits = new ArrayList<EditMessage<DialogueActIU>>();
			actEdits.add(new EditMessage<DialogueActIU>(EditType.ADD,new DialogueActIU("confirm")));
			rightBuffer.setBuffer(actEdits);
			return;
		} else if(this.wordUtil.isRejectWord(iu.toPayLoad())) {
			ArrayList<EditMessage<DialogueActIU>> actEdits = new ArrayList<EditMessage<DialogueActIU>>();
			actEdits.add(new EditMessage<DialogueActIU>(EditType.ADD,new DialogueActIU("reject")));
			rightBuffer.setBuffer(actEdits);
			return;
		}
		
		ArrayList<EditMessage<NumberIU>> newNumberIUEdits = new ArrayList<EditMessage<NumberIU>>();
		ArrayList<String> digits = wordUtil.digitsFromASR(iu.toPayLoad().toLowerCase());
		logger.debug("Digits extracted from ASR:");
		logger.debug(digits);
		int diffposition = 0;
		
		
		/*
		// first revoke those number IUs that have genuinely changed
		if ((!revokeStack.isEmpty()) & (!digits.isEmpty())) {
			// a word has been revoked, work out the difference
			WordIU revokedIU = revokeStack.get(revokeStack.size()-1);
			revokeStack.remove(revokeStack.size()-1);
			ArrayList<NumberIU> potentialRevokeIUs = new ArrayList<NumberIU>();
			boolean diff = false;
			
			for (int i=0; i<wordIUtoNumberIUMap.size(); i++) {
				Pair<Integer, NumberIU> pair = wordIUtoNumberIUMap.get(i);
				if (pair.getLeft().equals(revokedIU.getID())){
					if (!pair.getRight().toPayLoad().equals(digits.get(i))||diff ==true) {
						newNumberIUEdits.add(new EditMessage<NumberIU>(EditType.REVOKE,pair.getRight()));
						diff = true;
						if (diffposition==0) {
							diffposition = i;
						}
					}
				}
			}
			
		}
		*/
		
		for (int j=diffposition; j<digits.size();  j++) {
			NumberIU num = new NumberIU(digits.get(j));
			newNumberIUEdits.add(new EditMessage<NumberIU>(EditType.ADD,num));
			this.wordIUtoNumberIUMap.add(new Pair<Integer, NumberIU>(iu.getID(),num));
		}
		logger.debug("Number edits:");
		logger.debug(newNumberIUEdits);
		
		rightBuffer.setBuffer(newNumberIUEdits);
		
		logger.debug("NLU Numbers AFTER ADD: WordIUToNumberMap");
		logger.debug(wordIUtoNumberIUMap);
		logger.debug("current right buffer");
		logger.debug(newNumberIUEdits);
		
	}
	
	/**
	 * Do the desired processing for REVOKEd WordIU. 
	 * 
	 * @param iu
	 */
	protected synchronized void processRevokedWordIU(WordIU iu) {
		// may revoke already confirmed digits as some ASRs like google try to update the
		// only revoke numberIUs linked to this wordIU where they differ in terms of position
		logger.debug("processing revoked word " + iu.toPayLoad());
		ArrayList<EditMessage<NumberIU>> newNumberIUEdits = new ArrayList<EditMessage<NumberIU>>();
		// revoke all numbers associated with that word
		ArrayList<Pair<Integer, NumberIU>> pairsToRemove = new ArrayList<Pair<Integer, NumberIU>>();
		for (int i=0; i<wordIUtoNumberIUMap.size(); i++) {
			Pair<Integer, NumberIU> pair = wordIUtoNumberIUMap.get(i);
			if (pair.getLeft().equals(iu.getID())){
				newNumberIUEdits.add(new EditMessage<NumberIU>(EditType.REVOKE,pair.getRight()));
				pairsToRemove.add(pair);
			}
		}
		for (Pair<Integer, NumberIU> pair : pairsToRemove) {
			wordIUtoNumberIUMap.remove(pair);
		}
		logger.debug("Number IUs being revoked to DM:");
		logger.debug(newNumberIUEdits);
		
		rightBuffer.setBuffer(newNumberIUEdits);
		revokeStack.add(iu);
		
		logger.debug("NLU Numbers AFTER REVOKE: WordIUToNumberMap");
		logger.debug(wordIUtoNumberIUMap);
		logger.debug("current right buffer");
		logger.debug(newNumberIUEdits);
	}
	
	public void newProperties(PropertySheet ps) throws PropertyException {
		super.newProperties(ps);
		logger.info("Started NLU in " + ps.getString(LANGUAGE));
		wordUtil = new WordUtil(ps.getString(LANGUAGE).split("-")[0]);
		wordIUtoNumberIUMap = new ArrayList<Pair<Integer, NumberIU>>();  //records which word IU triggered which number IU
		revokeStack = new ArrayList<WordIU>(); 
		
	}

	@Override
	protected void leftBufferUpdate(Collection<? extends IU> ius, List<? extends EditMessage<? extends IU>> edits) {
		for (EditMessage<? extends IU> edit: edits) {
			IU iu = edit.getIU();
			if (iu instanceof WordIU){
				String word = iu.toPayLoad();
				switch (edit.getType()) {
					case ADD:
						//logger.debug(System.currentTimeMillis() + "," + word +","+edit.getType().toString()+","+iu.getID());
						//logger.info("adding " + edit.getIU().toPayLoad());
						//this.processAddedWordIU((WordIU) iu); 
						break;
					case REVOKE:
						logger.debug(System.currentTimeMillis() + "," + word +","+edit.getType().toString()+","+iu.getID());
						logger.info("revoking " + edit.getIU().toPayLoad());
						this.processRevokedWordIU((WordIU) iu);
						break;
					case COMMIT:
						// for now only taking commits, rather than adds
						logger.debug(System.currentTimeMillis() + "," + word +","+edit.getType().toString()+","+iu.getID());
						logger.info("adding " + edit.getIU().toPayLoad());
						this.processAddedWordIU((WordIU) iu); 
						break;
					default:
						break;
				}
			}
		}
		
	}
	
	
}