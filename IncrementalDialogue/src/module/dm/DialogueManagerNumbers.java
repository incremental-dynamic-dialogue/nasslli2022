package module.dm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.cmu.sphinx.util.props.PropertyException;
import edu.cmu.sphinx.util.props.PropertySheet;
import inpro.incremental.IUModule;
import inpro.incremental.unit.EditMessage;
import inpro.incremental.unit.EditType;
import inpro.incremental.unit.IU;

import iu.DialogueActIU;
import iu.NumberIU;

public class DialogueManagerNumbers extends IUModule {
	
	public static int NUMBER_LENGTH = 10; // US style number 001-541-754-3010 (or 10-number option without 001 country code)
	
	public ArrayList<ArrayList<NumberIU>> recordedNumbers; // all previous numbers recorded by the app
	
	// three different variables in terms of grounding status
	public ArrayList<NumberIU> currentNumberExtensionPending;  // the part of the number understood but yet to be confirmed
	public ArrayList<NumberIU> currentNumberExtensionVocalized;
	public ArrayList<NumberIU> currentNumberConfirmed;  // assume phone/banking type number. Given one at a time.
	
	
	
	public void updateGUI() {
		
	}
	

	private void processRevokedNumberIU(NumberIU iu) {
		logger.debug("removing iu " + iu.toPayLoad());
		if (this.currentNumberExtensionVocalized.contains(iu)) {
			logger.debug("already vocalized!");
			return;
		}
		if(this.currentNumberExtensionPending.contains(iu)) {
			boolean removed = this.currentNumberExtensionPending.remove(iu);
			logger.debug("removing iu " + iu.toString() + "?: " + removed);
		} else {
			logger.debug(iu.toString() + " for revoking not in pending " + this.currentNumberExtensionPending);
		}
		
	}


	private void processAddedNumberIU(NumberIU iu) {
		
		ArrayList<EditMessage<DialogueActIU>> newDialogueActIUEdits = new ArrayList<EditMessage<DialogueActIU>>();
		
		// check to see whether extension is possible or not- i.e. where the previous digits vocalized but not confirmed
		if (!this.currentNumberExtensionVocalized.isEmpty()) {
			logger.debug("Current number extension pending");
			logger.debug(currentNumberExtensionPending);
			logger.debug("Current number confirmed");
			logger.debug(currentNumberConfirmed);
			logger.debug("currentNumberExtensionVocalized");
			logger.debug(currentNumberExtensionVocalized);
			String content = "Please first confirm the last digits ";
			for (NumberIU number : this.currentNumberExtensionVocalized) {
				content+= number.toPayLoad() + " ";
			}
			
			newDialogueActIUEdits.add(new EditMessage<DialogueActIU>(EditType.ADD,new DialogueActIU("re-verify", content)));
			
	
			rightBuffer.setBuffer(newDialogueActIUEdits);
			return;
			
		}
		
		// we get to here, it's a valid extension
			
		currentNumberExtensionPending.add(iu);
		logger.debug("Current number extension pending");
		logger.debug(currentNumberExtensionPending);
		logger.debug("Current number confirmed");
		logger.debug(currentNumberConfirmed);
		if (this.currentNumberExtensionPending.size()==3 & this.currentNumberConfirmed.size()<this.NUMBER_LENGTH-4) {
			// after 3, 6 and 9 etc. digits send the digits for confirmation
			String content = "";
			for (NumberIU number : this.currentNumberExtensionPending) {
				content+= number.toPayLoad() + " ";
				this.currentNumberExtensionVocalized.add(number);
			}
			newDialogueActIUEdits.add(new EditMessage<DialogueActIU>(EditType.ADD,new DialogueActIU("verify-repeat", content)));
			rightBuffer.setBuffer(newDialogueActIUEdits);
			
		} else if (this.currentNumberExtensionPending.size()==4 &this.currentNumberConfirmed.size()==this.NUMBER_LENGTH-4){
			// when 4 digits remain
			String content = "";
			for (NumberIU number : this.currentNumberExtensionPending) {
				content+= number.toPayLoad() + " ";
				this.currentNumberExtensionVocalized.add(number);
			}
			newDialogueActIUEdits.add(new EditMessage<DialogueActIU>(EditType.ADD,new DialogueActIU("verify-repeat", content)));
			rightBuffer.setBuffer(newDialogueActIUEdits);
			
		}
		
	}
	

	private void processDialogueActIU(DialogueActIU iu) {
		logger.debug("Processing dialogue act " + iu.toString());
		ArrayList<EditMessage<DialogueActIU>> newDialogueActIUEdits = new ArrayList<EditMessage<DialogueActIU>>();
		
		if (((DialogueActIU) iu).getDialogueActType().equals("reject")){
			logger.debug("Dialogue maanger getting reject");
			if (this.currentNumberExtensionVocalized.isEmpty()) {
				// self repair, we assume of last number (TODO or of last word)
				this.currentNumberExtensionPending.remove(this.currentNumberExtensionPending.size()-1);
			} else {
				// number has been vocalized, so other repair of machine
				this.currentNumberExtensionPending.clear();
				this.currentNumberExtensionVocalized.clear();
			}
			
			newDialogueActIUEdits.add(new EditMessage<DialogueActIU>(EditType.ADD,new DialogueActIU("reuqest-repeat", "okay what did you say?")));
			rightBuffer.setBuffer(newDialogueActIUEdits);
			
		} else if (((DialogueActIU) iu).getDialogueActType().equals("confirm")){
			logger.debug("Dialogue maanger getting confirm");
			logger.debug(this.currentNumberExtensionPending);
			logger.debug(this.currentNumberExtensionVocalized);
			logger.debug(this.currentNumberConfirmed);
			
			ArrayList<NumberIU> toRemove = new ArrayList<NumberIU>();
			for (NumberIU number : this.currentNumberExtensionPending) {
				this.currentNumberConfirmed.add(number);
				toRemove.add(number);
			}
			for (NumberIU number : toRemove) {
				this.currentNumberExtensionVocalized.remove(number);
				this.currentNumberExtensionPending.remove(number);
			}
			// if it's a confirmation, go ahead
			if (this.currentNumberConfirmed.size()==this.NUMBER_LENGTH) {
				String content = "okay, so that's ";
				for (NumberIU number : this.currentNumberConfirmed) {
					content+= number.toPayLoad() + " ";
				}
				
				this.currentNumberConfirmed.clear();
				this.currentNumberExtensionPending.clear();
				this.currentNumberExtensionVocalized.clear();
				
				newDialogueActIUEdits.add(new EditMessage<DialogueActIU>(EditType.ADD,new DialogueActIU("verify", content)));
				newDialogueActIUEdits.add(new EditMessage<DialogueActIU>(EditType.ADD,new DialogueActIU("request", ". Give me another number to call")));
				rightBuffer.setBuffer(newDialogueActIUEdits);
			}
			
			
		}
		
	}
	
	
	public void newProperties(PropertySheet ps) throws PropertyException {
		super.newProperties(ps);
		logger.info("Started dialogue manager");
		currentNumberConfirmed = new ArrayList<NumberIU>();  // assume phone/banking type number. Given one at a time.
		currentNumberExtensionPending = new ArrayList<NumberIU>();
		currentNumberExtensionVocalized = new ArrayList<NumberIU>();
			
         
	}
	

	@Override
	protected void leftBufferUpdate(Collection<? extends IU> ius, List<? extends EditMessage<? extends IU>> edits) {
		logger.debug("Dialogue manager getting updates");
		logger.debug(edits);
		for (EditMessage<? extends IU> edit: edits) {
			IU iu = edit.getIU();
			if (iu instanceof NumberIU){
				String number = iu.toPayLoad();
				switch (edit.getType()) {
					case ADD:
						logger.debug(System.currentTimeMillis() + "," + number +","+edit.getType().toString()+","+iu.getID());
						logger.info("adding " + edit.getIU().toPayLoad());
						this.processAddedNumberIU((NumberIU) iu); 
						break;
					case REVOKE:
						logger.debug(System.currentTimeMillis() + "," + number +","+edit.getType().toString()+","+iu.getID());
						logger.info("revoking " + edit.getIU().toPayLoad());
						//TODO this.parser.rollback.. TODO and merge??
						this.processRevokedNumberIU((NumberIU) iu);
						break;
					case COMMIT:
						break;
					default:
						break;
				}
			} else if (iu instanceof DialogueActIU) {
				this.processDialogueActIU((DialogueActIU) iu);
			}
		}
		
		this.updateGUI();
	}


	
}