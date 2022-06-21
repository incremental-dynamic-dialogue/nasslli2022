package semantics.typesasclassifiers;

import org.apache.log4j.Logger;

import qmul.ds.formula.Formula;
import semantics.GoalActionType;
import sium.nlu.stat.Distribution;
import module.vision.WorldBelief;

public class EntityClassifier extends ExtensionalClassifier {
	
	public Logger logger;
	
	public EntityClassifier(String a_name){
		logger = Logger.getLogger(EntityClassifier.class);
		this.setName(a_name);
		
	}

	@Override
	public double getUnnormalizedProbability(WorldBelief a_wb,
			Formula object_name) {
		if (a_wb.getObjectScoresForProperty(this.getName()).keySet().contains(object_name.toString())){
			return 1;
		}
		return 0;
	}

	@Override
	public Distribution<Formula> getUnnormalizedProbabilityDistributionOverFormulae(
			WorldBelief a_wb, GoalActionType[] available_actions) {
		Distribution<Formula> dist = new Distribution<Formula>();
		logger.debug(this.getName());
		logger.debug(a_wb.keySet());
		if (this.getName().equals("e")){
			for (String obj_name : a_wb.keySet()){
				dist.addProbability(Formula.create(obj_name), 1); // 1 for all
			}
			return dist;
		}
		// TODO should only be for the generic E classifier
		logger.debug("no world belief for entity classifier!");
		return null;
	}

	@Override
	public Distribution<Formula> getProbabilityDistributionOverFormulae(
			WorldBelief a_wb, GoalActionType[] available_actions) {
		Distribution<Formula> dist = getUnnormalizedProbabilityDistributionOverFormulae(a_wb, available_actions);
		dist.normalize();
		return dist;
	}

}
