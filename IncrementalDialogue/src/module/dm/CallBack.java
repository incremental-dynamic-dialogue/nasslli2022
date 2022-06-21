package module.dm;

import iu.RobotActionIU.Action;

public class CallBack {
	/*
	 * Simple class to parse callback messages from PentoRob
	 */
	private int actionIU_ID; //the unique ID for the action
	private int timeReceived; //the time this comes into the system
	private float actionDuration; // predicted duration for the action
	private float strokeDuration;//when an action is complex with a stroke + (hold) + retract this is differen to duration
	private Action actionType;
	private boolean successful;
	private String message;
	
	public CallBack(String callbackmessage, int time) {
		//callback message string as it comes back from PentoRobAPI of form
		//ActionIU_id:action_type:duration:success
	
		String[] parts = callbackmessage.split(":");
		this.actionIU_ID = Integer.parseInt(parts[0]);
		this.actionType = null;
		for (Action a : Action.values()){
			if (a.name().equals(parts[1])){
				this.actionType = a;
				break;
			}
		}
		if (this.actionType==null){
			System.out.print("PENTOROB.CALLBACK WARNING action " + parts[1] + " not part of action ontology");
		}
		this.actionDuration = Float.parseFloat(parts[2]);
		this.successful = Boolean.parseBoolean(parts[3]);
		message = callbackmessage;
		this.setTimeReceived(time);
		this.setStrokeDuration(this.actionDuration);
		
		//extra for grabs and drops (stroke times)
		if (parts.length>4){
			this.setStrokeDuration(Float.parseFloat(parts[4]));
		}
	}
	
	@Override
	public String toString(){
		return message;
	}

	public int getAction_IU_id() {
		return actionIU_ID;
	}

	public float getActionDuration() {
		return actionDuration;
	}

	public Action getActionType() {
		return actionType;
	}

	public boolean isSuccessful() {
		return successful;
	}

	public int getTimeReceived() {
		return timeReceived;
	}

	public void setTimeReceived(int timeRecieved) {
		this.timeReceived = timeRecieved;
	}

	public float getStrokeDuration() {
		return strokeDuration;
	}

	public void setStrokeDuration(float duration_of_stroke) {
		this.strokeDuration = duration_of_stroke;
	}

}
