package module.nlu;

public enum UserActionState {
	START,
	INSTRUCTING_OBJECT, REPAIRING_OBJECT, REJECTING_OBJECT, CONFIRMING_OBJECT,
	INSTRUCTING_DESTINATION, REPAIRING_DESTINATION, REJECTING_DESTINATION, CONFIRMING_DESTINATION,
	WAITING_FOR_ROBOT_TO_MOVE, WAITING_WHILE_ROBOT_MOVES,
	WAITING_FOR_ROBOT_TO_GRAB, WAITING_WHILE_ROBOT_GRABS,
	WAITING_FOR_ROBOT_TO_DROP, WAITING_WHILE_ROBOT_DROPS,
	END
}
