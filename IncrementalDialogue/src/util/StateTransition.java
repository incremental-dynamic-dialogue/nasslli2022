package util;

import java.util.ArrayList;

public class StateTransition {
	
	private String head;
	private ArrayList<String> conditions;
	
	public String getHead() {
		return head;
	}
	public void setHead(String head) {
		if (head.equals("NULL")){
			this.head = null;
			return;
		}
		this.head = head;
	}
	public ArrayList<String> getConditions() {
		return conditions;
	}
	public void setConditions(ArrayList<String> conditions) {
		this.conditions = conditions;
	}
	
	public StateTransition(String new_head){
		setHead(new_head);
	}
	
	public StateTransition(String new_head, ArrayList<String> newConditions){
		setHead(new_head);
		setConditions(newConditions);
	}
	
	
}
