package semantics.typesasclassifiers;

import java.util.TreeSet;

import org.apache.log4j.Logger;

import qmul.ds.formula.Formula;
import qmul.ds.formula.TTRField;
import qmul.ds.formula.TTRLabel;
import qmul.ds.formula.TTRRecordType;
import qmul.ds.type.DSType;
import sium.nlu.stat.DistRow;
import sium.nlu.stat.Distribution;

public class CardinalityClassifier extends IntensionalClassifier {

	public Logger logger;
	private int cardinality;
	
	public CardinalityClassifier(String name, int a_cardinality) {
		super(name);
		this.cardinality = a_cardinality;
		this.logger = Logger.getLogger(CardinalityClassifier.class);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void addDistribution(Distribution<Formula> dist){
		if (this.distributions.size()>0){
			this.logger.debug("Warning- more than one distribution");
			for (int i=0; i<this.distributions.size(); i++){
				this.logger.debug(i);
				this.logger.debug(this.distributions.get(i));
			}
			this.clearDistributions();
		}
		this.distributions.add(dist);
	}

	@Override
	public Distribution<Formula> calculateDistribution() {
		// gets the top N from a flat distribution
		// This will always be a range over record type restrictor
		Distribution<Formula> d = new Distribution<Formula>();
		d.setNormalized(false);
		this.distributions.get(0).normalize();
		TreeSet<DistRow<Formula>> t = null;
		if (this.cardinality==1){
			t = this.distributions.get(0).getTopN(this.distributions.get(0).size()); // get all if just one object
			for (DistRow<Formula> row : t) {
				Double prob = 1.0;
				TTRRecordType finalRecordType = new TTRRecordType();
				int count = 0;
				count+=1;
				//d.addProbability(row.getEntity(), row.getProbability());
				Formula finalF = null;
				DSType finalType = null;
				TTRLabel finalLabel = null;
				// for each record type there should just be one type
				for (TTRField f: ((TTRRecordType) row.getEntity()).getFields()){
					if (finalF==null){
						finalF = f.getType();
						finalType = f.getDSType();
						finalLabel = new TTRLabel(f.getLabel().toString().charAt(0) + String.valueOf(count)) ;
					
					} else if (!f.getType().equals(finalF)){
						this.logger.error("type mismatch!");
						finalF = f.getType();
					}
				}
				finalRecordType.add(new TTRField(finalLabel, finalType, finalF));
				prob = prob * row.getProbability();
				d.addProbability(finalRecordType, prob);
			}
			return d;
		}
		
		
		
		
		t = this.distributions.get(0).getTopN(this.cardinality); // greedy count- can do cartesian product too
		
		
		
		
		Double prob = 1.0;
		TTRRecordType finalRecordType = new TTRRecordType();
		int count = 0;
		for (DistRow<Formula> row : t) {
			count+=1;
			//d.addProbability(row.getEntity(), row.getProbability());
			Formula finalF = null;
			DSType finalType = null;
			TTRLabel finalLabel = null;
			// for each record type there should just be one type
			for (TTRField f: ((TTRRecordType) row.getEntity()).getFields()){
				if (finalF==null){
					finalF = f.getType();
					finalType = f.getDSType();
					finalLabel = new TTRLabel(f.getLabel().toString().charAt(0) + String.valueOf(count)) ;
				
				} else if (!f.getType().equals(finalF)){
					this.logger.error("type mismatch!");
					finalF = f.getType();
				}
			}
			finalRecordType.add(new TTRField(finalLabel, finalType, finalF));
			prob = prob * row.getProbability();
		}
		// just a single record type in the distribution with the right number of elements
		d.addProbability(finalRecordType, prob);
		return d;
	}

}
