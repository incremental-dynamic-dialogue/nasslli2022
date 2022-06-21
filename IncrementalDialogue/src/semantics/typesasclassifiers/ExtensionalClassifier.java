package semantics.typesasclassifiers;

import qmul.ds.formula.Formula;
import semantics.GoalActionType;
import sium.nlu.stat.Distribution;
import module.vision.WorldBelief;

public abstract class ExtensionalClassifier extends Classifier {

	/**
	 * Given a world belief and an object how strongly the classifier applies
	 * between [0, 1]
	 * 
	 * @param a_wb
	 * @param object_name
	 * @return
	 */
	public abstract double getUnnormalizedProbability(WorldBelief a_wb,
			 Formula object_name);

	/**
	 * Unnormalized probability distribution of a given formulae being the case (true) when the classifier is applied to a situation (world belief).
	 * The formulae could be simple types, predicate types, or record types.
	 * 
	 * @param a_wb
	 * @param available_actions
	 * @return
	 */
	public abstract Distribution<Formula> getUnnormalizedProbabilityDistributionOverFormulae(
			WorldBelief a_wb, GoalActionType[] available_actions);

	
	/**
	 * The probability distribution of a given formulae being the case (true) when the classifier is applied to a situation (world belief).
	 * The formulae could be simple types, predicate types, or record types.
	 * 
	 * @param a_wb
	 * @param available_actions
	 * @return
	 */
	public abstract Distribution<Formula> getProbabilityDistributionOverFormulae(
			WorldBelief a_wb, GoalActionType[] available_actions);

}
