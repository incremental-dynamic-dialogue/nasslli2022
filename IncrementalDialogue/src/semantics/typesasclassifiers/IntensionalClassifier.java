package semantics.typesasclassifiers;

import java.util.ArrayList;
import java.util.List;

import qmul.ds.formula.Formula;
import sium.nlu.stat.Distribution;

/**
 * Classifier that takes the output of other classifiers
 * to combine their output distributions or single probabilities.
 *
 */
public abstract class IntensionalClassifier extends Classifier {

	public List<Distribution<Formula>> distributions;
	
	/**
	 * The main method for calculating the resulting distribution
	 * over formulae (type judgements).
	 * 
	 * @return
	 */
	public abstract Distribution<Formula> calculateDistribution();
	
	public void clearDistributions(){
		this.distributions.clear();
	}
	
	public IntensionalClassifier(){
		
	}
	
	public IntensionalClassifier(String name){
		setName(name);
		this.distributions = new ArrayList<Distribution<Formula>>();
	}
	
	public void addDistribution(Distribution<Formula> dist){
		this.distributions.add(dist);
	}

}
