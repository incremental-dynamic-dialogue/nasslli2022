package iu;

import inpro.incremental.unit.IU;
import qmul.ds.formula.TTRRecordType;

public class DialogueActIU extends IU {
	
	String dialogueActType;
	TTRRecordType ttr;
	String words;

	public DialogueActIU(String dialogueAct) {
		super();
		this.dialogueActType = dialogueAct;
		this.ttr = new TTRRecordType();
		this.words = "";
	}
	
	public DialogueActIU(String dialogueAct, TTRRecordType content) {
		super();
		this.dialogueActType = dialogueAct;
		this.ttr = content;
		this.words = "";
	}
	
	public DialogueActIU(String dialogueAct, TTRRecordType content, String words) {
		super();
		this.dialogueActType = dialogueAct;
		this.ttr = content;
		this.words = words;
	}
	
	public DialogueActIU(String dialogueAct, String words) {
		super();
		this.dialogueActType = dialogueAct;
		this.ttr = new TTRRecordType();
		this.words = words;
	}

	public TTRRecordType getTTR() {
		return ttr;
	}
	
	public String getWords() {
		return words;
	}
	
	public String getDialogueActType() {
		return dialogueActType;
	}
	
	@Override
	public String toPayLoad() {
		return dialogueActType + ":" + words +  ":" + ttr.toString();
	}

}
