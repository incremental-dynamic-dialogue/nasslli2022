package semantics.typesasclassifiers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import qmul.ds.formula.Formula;
import qmul.ds.formula.PredicateArgumentFormula;
import qmul.ds.formula.TTRField;
import qmul.ds.formula.TTRLabel;
import qmul.ds.formula.TTRRecordType;
import sium.nlu.stat.DistRow;
import sium.nlu.stat.Distribution;
import util.Digraph;
import util.Pair;
import util.TTRutil;

public class RecordTypeClassifier extends IntensionalClassifier {

	public Logger logger;
	public TTRRecordType ttr;
	public Digraph<String> classifierGraph;
	public HashMap<String,Distribution<Formula>> fieldDistributions;
	
	/**
	 * Construct from a record type and the classifier graph.
	 * 
	 * @param a_ttr
	 * @param a_classifierGraph 
	 */
	public RecordTypeClassifier(TTRRecordType a_ttr, Digraph<String> a_classifierGraph) {
		super(a_ttr.toString());
		this.logger = Logger.getLogger(RecordTypeClassifier.class);
		this.ttr = a_ttr;
		this.classifierGraph = a_classifierGraph;
		this.fieldDistributions = new HashMap<String,Distribution<Formula>>();
	}
	
	/**
	 * For each field name add a distribution over formula values.
	 * 
	 * @param field_name
	 * @param distribution
	 */
	public void addFieldDistribution(String field_label, Distribution<Formula> distribution) {
		this.fieldDistributions.put(field_label, distribution);
	}
	
	public TTRRecordType getTTR(){
		return this.ttr;
	}

	/**
	 * Principal method for calculating the distribution over record types
	 * given the distributions of the fields.
	 * 
	 */
	public Distribution<Formula> calculateFrameDistribution(){
		this.logger.debug("calculating frame distribution");
		this.logger.debug("field distributions");
		for (String key : this.fieldDistributions.keySet()){
			this.logger.debug(key + " " + this.fieldDistributions.get(key));
		}
		
		Set<Pair<TTRRecordType,Double>> ttrSet = new HashSet<Pair<TTRRecordType,Double>>();
		// initialize
		ttrSet.add(new Pair<TTRRecordType,Double>(TTRRecordType.parse("[]"),1.0));
		// should preserve the dependency order of the record type
		List<String> ttrGraphOrder = this.classifierGraph.topSort();
		this.logger.debug("ttrGraphOrder: " + ttrGraphOrder);
		
		// iterate over the graph order from bottom up
		for (int i=ttrGraphOrder.size()-1; i>=0; i=i-1){
			Set<Pair<TTRRecordType,Double>> newTTRSet = new HashSet<Pair<TTRRecordType,Double>>();
			
			String l_string = ttrGraphOrder.get(i);
			String l = l_string.split("@")[0];
			this.logger.debug(l_string);
			// get the distribution for this field
			Distribution<Formula> fieldDistribution = this.fieldDistributions.get(l);
			
			if (fieldDistribution==null){
				this.logger.debug("no field distribution for " + l);
				if (l.equals("TOP")){
					
					
				} else {
					/*
					 
					// trivial field, add
					if (ttrSet.size()>0){
						 for (Pair<TTRRecordType,Double> pair : ttrSet){
							TTRRecordType newTTR = pair.getLeft().clone();
							logger.debug(this.ttr);
							if (this.ttr.hasLabel(new TTRLabel(l))){
								newTTR.add(this.ttr.getField(new TTRLabel(l)));
							}
							this.logger.debug(newTTR.clone());
							newTTRSet.add(new Pair<TTRRecordType, Double>(newTTR.clone(),pair.getRight()));
						}
					} else {
						TTRRecordType newTTR = new TTRRecordType();
						logger.debug(this.ttr);
						if (this.ttr.hasLabel(new TTRLabel(l))){
							newTTR.add(this.ttr.getField(new TTRLabel(l)));
						}
						this.logger.debug(newTTR.clone());
						newTTRSet.add(new Pair<TTRRecordType, Double>(newTTR.clone(),1.0));
					}
					ttrSet = newTTRSet;
					* 
					*/
				}
				continue;
			}
			
			this.logger.debug(fieldDistribution);
			List<String> parents = this.classifierGraph.getDaughters(l_string);
			this.logger.debug("parents:");
			this.logger.debug(parents);
			// get the type for this field		

			for (int j=0; j<fieldDistribution.size(); j++){
				DistRow<Formula> r = fieldDistribution.getItem(j);
				Formula field_formula = r.getEntity();  //entity or event, will replace the e supertype
				Double p = r.getProbability();
				// for each record type, 
				// add a field and delete the old one
				for (Pair<TTRRecordType,Double> pair : ttrSet){
					TTRRecordType newTTR = pair.getLeft().clone();
					List<String> internalParents = new ArrayList<String>();
					if (!parents.isEmpty()){
						for (String parent : parents){
							String parent_label = parent.split("@")[0];
							TTRField parentField = newTTR.getField(new TTRLabel(parent_label));
							if (parentField==null){
								this.logger.debug("No field " + parent_label);
								continue;
							}
							internalParents.add(parent);
						
						}
					}
					
					if (!internalParents.isEmpty()) {
						// if there are dependent values, then make sure this probability value
						// matches up with the appropriate entity or event
						boolean match = false;
						for (String parent : internalParents){
							
							String parent_label = parent.split("@")[0];
							logger.debug(parent_label);
							TTRField parentField = newTTR.getField(new TTRLabel(parent_label));
							if (parentField==null){
								this.logger.error("No field " + parent);
								continue;

							}
							else if (parentField.getType()==null){
								this.logger.error("No manifest type for " + parent);
								continue;
							}
							TTRLabel innerLabel = parentField.getLabel();
							Formula f = parentField.getType();
							if (f.equals(field_formula)){
								match = true;
								break;
							} else {
								if (field_formula instanceof PredicateArgumentFormula){
									for (Formula arg : ((PredicateArgumentFormula) field_formula).getArguments()){
										if (arg.toString().equals(arg.toString())){
											match = true;
											break;
										}
									}
									if (match){
										break;
									}
									
								}
								this.logger.debug(f + " not equal to field_formula " + field_formula);
							}
							
						}
						
						if (!match){
							// don't add a new record type
							logger.debug("not adding field to : " + newTTR.toString() + " the field formula " + field_formula.toString());
							continue;
						}
					}
					
					// make sure that no two entities are the same- only need one
					Boolean entityCheck = true;
					if (this.ttr.getField(new TTRLabel(l)).getDSType().toString().equals("e")){
						logger.debug("checking entities");
						// ensure there's only one entity of this type
						for (TTRField f : newTTR.getFields()){
							if (f.getDSType().toString().equals("e")){
								if (f.getType()==null){
									continue;
								}
								if (field_formula.equals(f.getType())){
									entityCheck = false;
									logger.debug(field_formula.toString() + " entity already in record type");
									break;
								} else {
									Set<Formula> thisFieldFormulae = new HashSet<Formula>();
									if (field_formula instanceof TTRRecordType){
										for (TTRField internalF : ((TTRRecordType) field_formula).getFields()){
										thisFieldFormulae.add(internalF.getType());
										}
									} else {
										thisFieldFormulae.add(field_formula);
									}
									
									Set<Formula> newFieldFormula = new HashSet<Formula>();
									if (f.getType() instanceof TTRRecordType){
										for (TTRField otherInternalF : ((TTRRecordType) f.getType()).getFields()){
											newFieldFormula.add(otherInternalF.getType());
										}
									} else {
										newFieldFormula.add(f.getType());
									}
									if (newFieldFormula.equals(thisFieldFormulae)){
										logger.debug(field_formula.toString() + " entity (object set) already in record type");
										entityCheck = false;
										break;
									}	
								}
								
							}
						}
					}
					
					if (!entityCheck){
						logger.debug("an entity copy in record!");
						continue;
					}
					
					double newProb = p * pair.getRight();
					newTTR.add(new TTRLabel(l), field_formula, this.ttr.getField(new TTRLabel(l)).getDSType());
					this.logger.debug("adding new record type");
					this.logger.debug(l);
					this.logger.debug(newProb);
					this.logger.debug(newTTR.clone());
					newTTRSet.add(new Pair<TTRRecordType,Double>(newTTR.clone(),newProb));
				}
			}
			
			// after each field is processed, the new set becomes the main set of RTs
			ttrSet = newTTRSet;
		}
		
		// add all the record types to the distribution
		Distribution<Formula> ttrDistribution = new Distribution<Formula>();
		for (Pair<TTRRecordType,Double> pair : ttrSet){
			ttrDistribution.addProbability(pair.getLeft(), pair.getRight());
		}
		
		ttrDistribution.normalize();
		this.logger.debug(ttrDistribution);
		return ttrDistribution;
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

	public HashMap<String,Distribution<Formula>> getFieldDistributions() {
		return fieldDistributions;
	}

	public void setFieldDistributions(HashMap<String,Distribution<Formula>> fieldDistributions) {
		this.fieldDistributions = fieldDistributions;
	}
	
	public static void main(String[] args) throws InterruptedException {
		PropertyConfigurator.configure("log4j.properties");
		String ttrString = "[x2 : e|e3==action_place : es|x1==addressee : e|x3 : e|head==e3 : es|p4==obj(e3, x3) : t|p6==subj(e3, x1) : t|p5==indobj(e3, x2) : t]";
		Digraph<String> a_classifierGraph = new TTRutil().convertRecordTypeToDigraph(TTRRecordType.parse(ttrString));
		
		RecordTypeClassifier c  = new RecordTypeClassifier(TTRRecordType.parse(ttrString), a_classifierGraph);
		
		Distribution<Formula> x2_distribution = new Distribution<Formula>();
		x2_distribution.addProbability(Formula.create("obj1"), 0.3);
		x2_distribution.addProbability(Formula.create("obj2"), 0.4);
		x2_distribution.addProbability(Formula.create("obj3"), 0.4);
		x2_distribution.normalize();
		c.addFieldDistribution("x2", x2_distribution);
		
		Distribution<Formula> e3_distribution = new Distribution<Formula>();
		e3_distribution.addProbability(Formula.create("action_place"), 1.0);
		e3_distribution.normalize();
		c.addFieldDistribution("e3", e3_distribution);
		
		Distribution<Formula> x1_distribution = new Distribution<Formula>();
		x1_distribution.addProbability(Formula.create("addressee"), 1.0);
		x1_distribution.normalize();
		c.addFieldDistribution("x1", x1_distribution);
		
		Distribution<Formula> x3_distribution = new Distribution<Formula>();
		x3_distribution.addProbability(Formula.create("obj1"), 0.25);
		x3_distribution.addProbability(Formula.create("obj2"), 0.25);
		x3_distribution.addProbability(Formula.create("obj3"), 0.25);
		x3_distribution.addProbability(Formula.create("obj4"), 0.25);
		x3_distribution.normalize();
		c.addFieldDistribution("x3", x3_distribution);
		
		//c.addFieldDistribution("p4", p4_distribution);
		//c.addFieldDistribution("p6", p6_distribution);
		//c.addFieldDistribution("p5", p5_distribution);
		// distribution for each field where needed, sometimes fields are left unmapped
		// c.addFieldDistribution(field_label, distribution);
		
		// calculate distribution over possible RT values
		Distribution<Formula> frames = c.calculateFrameDistribution();
		double total = 0.0;
		for (int i =0; i<frames.size(); i++){
			System.out.println(frames.getItem(i));
			//System.out.println(frames.getItem(i).getEntity() instanceof TTRRecordType);
			total+=frames.getItem(i).getProbability();
		}
		System.out.println(total);

		
		
		
	}
	
	

}
