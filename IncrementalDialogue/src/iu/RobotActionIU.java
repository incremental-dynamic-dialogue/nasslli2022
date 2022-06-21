package iu;

import inpro.incremental.unit.IU;

public class RobotActionIU extends IU {
	
	/** types of action possible for pentorob */ 
	public enum Action { MOVE, DROP, GRAB, GRAB_LOW, DROP_LOW, ROTATE, ABORT }
	
	private Action actionType; //the action of the arm
	private String actionParam; //params of the action, can be a coordinate (and speed) or degrees
	private String instruction; //the string that is actually sent to PentoRob
	private Progress progress; //overriding normal IU getprogress methods
	private int startTime; //in ms
	private int endTime; //in ms
	private Boolean aborted; //whether the action was aborted before finishing
	private Boolean interruptive; //whether the action is meant to be interruptive/end the previous action before schedule or not
	
	@Override
	public Progress getProgress(){
		return progress;
	}
	
	public void makeUpcoming() {
		setProgress(Progress.UPCOMING);
	}
	
	public void makeOngoing(){
		setProgress(Progress.ONGOING);
	}
	
	public void makeCompleted(){
		setProgress(Progress.COMPLETED);
	}
	
	public void setProgress(Progress current_prog){
		progress = current_prog;
	}


	public String getInstruction() {
		return instruction;
	}

	public void setInstruction(String instruction) {
		this.instruction = instruction;
	}

	public Action getActionType() {
		return actionType;
	}

	public void setActionType(Action actiontype) {
		this.actionType = actiontype;
	}

	public String getActionParam() {
		return actionParam;
	}

	public void setActionParam(String actionparam) {
		this.actionParam = actionparam;
	}
	
	public Boolean isAborted() {
		return aborted;
	}

	public void setAborted(Boolean aborted) {
		this.aborted = aborted;
	}
	
	public Boolean isInterruptive() {
		
		return interruptive;
	}

	public void setInterruptive(Boolean new_interruptive) {
		if (new_interruptive){
			this.setInstruction(instruction.replace("command,","command,INTERRUPT_"));
		}
		this.interruptive = new_interruptive;
	}

	public int getStartTime() {
		return startTime;
	}

	public void setStartTime(int starttime) {
		this.startTime = starttime;
	}

	public int getEndTime() {
		return endTime;
	}

	public void setEndTime(int endtime) {
		this.endTime = endtime;
	}
		
	public RobotActionIU(Action action, String params) {
		setInstruction("command,"+ action + ":" + params);
		setProgress(Progress.UPCOMING);
		setActionType(action);
		setActionParam(params);
		setAborted(false);
		setInterruptive(false);
		setStartTime(0); //instantiated properly by the callback
		setEndTime(0); //instantiated properly by the callback
	}

	@Override
	public String toPayLoad() {
		return String.valueOf(getID()) + "," + getInstruction();
	}


}
