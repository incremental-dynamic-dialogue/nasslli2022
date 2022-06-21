package module.dm;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import module.dm.Game;
import iu.RobotActionIU;
import inpro.incremental.IUModule;
import inpro.incremental.unit.EditMessage;
import inpro.incremental.unit.IU;

public abstract class Robot extends IUModule {

	@Override
	protected abstract void leftBufferUpdate(Collection<? extends IU> ius, List<? extends EditMessage<? extends IU>> edits);

	/**
	 * The time in ms the robot has not moved for.
	 */
	public abstract int getTimeSinceLastGoalMovement();
	
	public abstract Game getGame();

	public abstract Set<String> getStateFluents();
	
	public abstract boolean updateStateFromCallBackMessage(CallBack callback);
	
	public abstract boolean updateState(boolean displayUpdate);
	
	/**
	 * Boolean of whether the robot's current goal is legible or not.
	 */
	public abstract boolean currentGoalActionLegible();
	
	public abstract void startGame();
	
	public abstract void endGameShutDown();

	public abstract IU getCurrentAction();

	public abstract List<RobotActionIU> getCompletedActions();

	public abstract List<RobotActionIU> getPendingActions();

	public abstract int getEndTimeOfCurrentAction();

}
