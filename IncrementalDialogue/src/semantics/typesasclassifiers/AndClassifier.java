package semantics.typesasclassifiers;

import qmul.ds.formula.Formula;
import sium.nlu.stat.Distribution;
import module.vision.WorldBelief;

public class AndClassifier extends IntensionalClassifier {

	public AndClassifier(String name) {
		super(name);
	}

	@Override
	public Distribution<Formula> calculateDistribution() {
		//simple combination
		Distribution<Formula> d = distributions.get(0);
		for (int i=1; i<distributions.size(); i++){
			d.combineDistribution(distributions.get(i));
		}
		return d;
	}


}
