package iu;

import inpro.incremental.unit.IU;

public class MessageIU extends IU {

	private long time; //simply a system time at which it was sent in millis and the message
	public String message;
	
	public MessageIU(long l, String message){
		this.setTime(l);
		this.setMessage(message);
	}
	
	private void setMessage(String new_message) {
		// TODO Auto-generated method stub
		this.message = new_message;
	}

	@Override
	public String toPayLoad() {
		// TODO Auto-generated method stub
		return message + ":" + String.valueOf(time);
	}

	public long getTime() {
		return time;
	}

	public void setTime(long l) {
		this.time = l;
	}
	
}
