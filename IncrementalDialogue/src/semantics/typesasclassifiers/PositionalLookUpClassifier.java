package semantics.typesasclassifiers;

import qmul.ds.formula.Formula;
import semantics.GoalActionType;
import sium.nlu.stat.Distribution;
import module.vision.WorldBelief;
import java.util.Collections;
import java.util.HashMap;


public class PositionalLookUpClassifier extends ExtensionalClassifier {

	public PositionalLookUpClassifier(String line) {
		// TODO Auto-generated constructor stub
		this.setName(line);
	}

	@Override
	public double getUnnormalizedProbability(WorldBelief a_wb,
			Formula object_name) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Distribution<Formula> getUnnormalizedProbabilityDistributionOverFormulae(
			WorldBelief a_wb, GoalActionType[] available_actions) {
		// TODO the below could be embedded in the world belief for speed, or not, words are less frequent than visual updates?
		float bottomEdge_y = Collections.min(a_wb.getObjectScoresForProperty("position_y").values());
		float topEdge_y = Collections.max(a_wb.getObjectScoresForProperty("position_y").values()); // TODO Famula-specific top pos, bottom neg
		float leftEdge_x = Collections.max(a_wb.getObjectScoresForProperty("position_x").values()); // TODO Famula-specific right negative, left negative
		float rightEdge_x = Collections.min(a_wb.getObjectScoresForProperty("position_x").values());
		float fullY_length = topEdge_y - bottomEdge_y;
		float fullX_width = leftEdge_x - rightEdge_x; // TODO Famula-specific left positive, right negative
		HashMap<String, Float> x_scores = a_wb.getObjectScoresForProperty("position_x");
		HashMap<String, Float> y_scores = a_wb.getObjectScoresForProperty("position_y");
		Distribution<Formula> dist = new Distribution<Formula>();
		for (String obj_name : x_scores.keySet()){
			float score = 0;
			switch (this.getName()) {
				case "position_top":
					score = 1 - ((topEdge_y - y_scores.get(obj_name)) / fullY_length); // TODO Famula-specific top positive, bottom negative
					break;
				case "position_bottom":
					score = 1 - ((bottomEdge_y - y_scores.get(obj_name)) / - fullY_length); // TODO Famula-specific top positive, bottom negative
					break;
				case "position_left":
					// inverse of closeness to left edge 1 = fully
					score = 1 - ((leftEdge_x - x_scores.get(obj_name)) / fullX_width); // TODO Famula-specific left positive, right negative
					break;
				case "position_right":
					// inverse of closeness to right edge 1 = fully
					score = 1 - ((rightEdge_x - x_scores.get(obj_name)) / - fullX_width);  // TODO Famula-specific left positive, right negative
					break;
				default:
					break;
			
			}
			dist.addProbability(Formula.create(obj_name), score);
			
		}
		//System.out.println(this.getName());
		//System.out.println(dist);
		return dist;
	}

	@Override
	public Distribution<Formula> getProbabilityDistributionOverFormulae(
			WorldBelief a_wb, GoalActionType[] available_actions) {
		// TODO Auto-generated method stub
		Distribution<Formula> d = this.getUnnormalizedProbabilityDistributionOverFormulae(a_wb, available_actions);
		d.normalize();
		
		
		return null;
	}

}
