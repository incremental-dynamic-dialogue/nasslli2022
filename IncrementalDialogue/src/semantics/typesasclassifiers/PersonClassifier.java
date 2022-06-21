package semantics.typesasclassifiers;

import qmul.ds.formula.Formula;
import semantics.GoalActionType;
import sium.nlu.stat.Distribution;
import module.vision.WorldBelief;

public class PersonClassifier extends ExtensionalClassifier {

	public PersonClassifier(String a_name) {
		this.setName(a_name);
	}

	@Override
	public double getUnnormalizedProbability(WorldBelief a_wb,
			Formula object_name) {
		return 1.0;
	}

	@Override
	public Distribution<Formula> getUnnormalizedProbabilityDistributionOverFormulae(
			WorldBelief a_wb, GoalActionType[] available_actions) {
		Distribution<Formula> dist = new Distribution<Formula>();
		dist.addProbability(Formula.create(this.getName()), 1);  // trivial classifier
		return dist;
	}

	@Override
	public Distribution<Formula> getProbabilityDistributionOverFormulae(
			WorldBelief a_wb, GoalActionType[] available_actions) {
		return this.getUnnormalizedProbabilityDistributionOverFormulae(a_wb, available_actions);
	}

}
