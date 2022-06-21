package semantics.typesasclassifiers;

import qmul.ds.formula.Formula;
import semantics.GoalActionType;
import sium.nlu.stat.Distribution;
import module.vision.WorldBelief;

public class GoalLocationRelationLoopUpClassifier extends ExtensionalClassifier {

	public GoalLocationRelationLoopUpClassifier(String relation_name) {
		this.setName(relation_name);
	}

	@Override
	public double getUnnormalizedProbability(WorldBelief a_wb,
			Formula object_name) {
		return 1;
	}

	@Override
	public Distribution<Formula> getUnnormalizedProbabilityDistributionOverFormulae(
			WorldBelief a_wb, GoalActionType[] available_actions) {
		Distribution<Formula> dist = new Distribution<Formula>();
		dist.addProbability(Formula.create(this.getName()), 1); // 1 for all
		return dist;
	}

	@Override	
	public Distribution<Formula> getProbabilityDistributionOverFormulae(WorldBelief a_wb, GoalActionType[] available_actions){
		Distribution<Formula> dist = getUnnormalizedProbabilityDistributionOverFormulae(a_wb, available_actions);
		dist.normalize();
		return dist;
	}

}
