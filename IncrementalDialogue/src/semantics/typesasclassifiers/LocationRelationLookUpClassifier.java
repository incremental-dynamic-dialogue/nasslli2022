package semantics.typesasclassifiers;

import qmul.ds.formula.Formula;
import semantics.GoalActionType;
import sium.nlu.stat.Distribution;
import module.vision.WorldBelief;

public class LocationRelationLookUpClassifier extends ExtensionalClassifier {

	@Override
	public double getUnnormalizedProbability(WorldBelief a_wb,
			Formula object_name) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Distribution<Formula> getUnnormalizedProbabilityDistributionOverFormulae(
			WorldBelief a_wb, GoalActionType[] available_actions) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Distribution<Formula> getProbabilityDistributionOverFormulae(
			WorldBelief a_wb, GoalActionType[] available_actions) {
		// TODO Auto-generated method stub
		return null;
	}

}
