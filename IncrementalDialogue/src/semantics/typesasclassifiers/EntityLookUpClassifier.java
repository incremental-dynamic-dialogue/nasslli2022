package semantics.typesasclassifiers;

import java.util.HashMap;

import org.apache.log4j.Logger;

import qmul.ds.formula.Formula;
import semantics.GoalActionType;
import sium.nlu.stat.Distribution;
import module.vision.WorldBelief;

/**
 * Simple classifier which operates as a simple look-up function on a 
 * world belief and gets the probability value from it directly.
 * 
 */
public class EntityLookUpClassifier extends ExtensionalClassifier {

	public Logger logger;
	public Float LOWER_BOUND_CONSTANT = 0.0001f;
	
	public EntityLookUpClassifier(String id){
		this.setName(id);
		this.logger = Logger.getLogger(ExtensionalClassifier.class);
	}
	
	public double getUnnormalizedProbabilityOverFormulae(WorldBelief a_wb, Formula object_name){
		//System.out.println(wb.getObjectScoresForProperty("color_red_prob"));
		//System.out.println(wb.getObjectScoresForProperty("label_apple_prob"));
		//System.out.println(wb.getObjectScoresForProperty("elongated_short_prob").get("object0"));
		
		return a_wb.getObjectScoresForProperty(this.getName()).get(object_name.toString());
	}
	
	public Distribution<Formula> getUnnormalizedProbabilityDistributionOverFormulae(WorldBelief a_wb,
			GoalActionType[] available_actions){
		HashMap<String,Float> scores = a_wb.getObjectScoresForProperty(this.getName() + "_prob");
		logger.debug(this.getName());
		logger.debug(scores);
		Distribution<Formula> dist = new Distribution<Formula>();
		for (String obj_name : scores.keySet()){
			Float prob = scores.get(obj_name);
			prob =  prob == 0f ? this.LOWER_BOUND_CONSTANT : prob;
			dist.addProbability(Formula.create(obj_name), prob);
		}
		return dist;
	}
	
	public Distribution<Formula> getProbabilityDistributionOverFormulae(WorldBelief a_wb, GoalActionType[] available_actions){
		Distribution<Formula> dist = getUnnormalizedProbabilityDistributionOverFormulae(a_wb, available_actions);
		dist.normalize();
		return dist;
	}

	@Override
	public double getUnnormalizedProbability(WorldBelief a_wb,
			Formula object_name) {
		// TODO Auto-generated method stub
		return 0;
	}
	
}
