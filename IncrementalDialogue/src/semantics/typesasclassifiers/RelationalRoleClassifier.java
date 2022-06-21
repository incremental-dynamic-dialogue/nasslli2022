package semantics.typesasclassifiers;

import java.util.HashMap;
import java.util.List;

import qmul.ds.formula.Formula;
import sium.nlu.stat.Distribution;

public class RelationalRoleClassifier extends IntensionalClassifier {

	public String predicateName;
	public List<String> arity; // list of argument types in right order
	public HashMap<Integer,Distribution<Formula>> argumentDistributions;
	
	public RelationalRoleClassifier(String predicateName, List<String> args){
		this.setName(predicateName);
		this.predicateName = predicateName;
		this.arity = args;
		this.argumentDistributions = new HashMap<Integer,Distribution<Formula>>();
		
	}

	/**
	 * For each argument type add a distribution over formula values.
	 * 
	 * @param arityIndex
	 * @param distribution
	 */
	public void addArgumentDistribution(Integer arityIndex,
			Distribution<Formula> distribution) {
		this.argumentDistributions.put(arityIndex, distribution);
	}
	
	public Distribution<Formula> calculateDistribution() {
		// a joint distribution over all possible formula values for each argument
		// For now just return first distribution, should be ordered values over formulae
		
		return this.argumentDistributions.get(0);
	}

}
