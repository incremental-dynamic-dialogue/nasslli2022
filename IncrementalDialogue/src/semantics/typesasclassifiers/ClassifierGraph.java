package semantics.typesasclassifiers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import org.apache.log4j.Logger;

import qmul.ds.formula.TTRField;
import qmul.ds.formula.TTRRecordType;
import util.Digraph;
import util.Pair;
import util.TTRutil;
import module.vision.WorldBelief;

public class ClassifierGraph {
	
	public TTRRecordType ttr;  // the current parse
	public Logger logger;
	public Set<Classifier> classifiers;  // all the available classifiers which the graph can be built from
	public HashMap<String, Classifier> classifierNameMap;
	public Digraph<String> classifierGraph;
	
	public List<Pair<String,Classifier>> classifierOrder; // the ordering of the application
	
	public ClassifierGraph(Set<Classifier> a_classifiers) {
		this.logger = Logger.getLogger(ClassifierGraph.class);
		this.classifiers = a_classifiers;
		this.setUpClassifierMap();
		this.ttr = null;
		this.classifierGraph = null;
	}
	
	public ClassifierGraph(TTRRecordType a_parse, Set<Classifier> a_classifiers) {
		this(a_classifiers);
		this.ttr = a_parse;
		this.createGraph();
	}
	
	public void setUpClassifierMap(){
		this.classifierNameMap = new HashMap<String, Classifier>();
		for (Classifier c : classifiers){
			String name = c.getName();
			// logger.info(name);
			this.classifierNameMap.put(name, c);
		}
	}
	
	/**
	 * Principal method for classification graph.
	 */
	public void createGraph(){
		logger.debug("creating classifier graph for " + this.ttr.toString());
		// Creates a graph of classifiers based on the record type paths
		// When applied to a world belief
		this.classifierGraph = new TTRutil().convertRecordTypeToDigraph(this.ttr);
		logger.debug("digraph=");
		logger.debug(this.classifierGraph);
		// Now do name matching of those in the classifier names
		setClassifierOrder(new ArrayList<Pair<String,Classifier>>());
		List<String> ttrGraphOrder = this.classifierGraph.topSort();
		for (int i=ttrGraphOrder.size()-1; i>=0; i=i-1){
			
			String[] label_value = ttrGraphOrder.get(i).split("@");
			String label = label_value[0];
			String formula = label_value[1];
			String classifier_name = null;
			Classifier classifier = null;
			logger.debug(label);
			logger.debug(formula);
			
			try {
				
	
			if (formula.startsWith("[")){
				logger.debug("embedded or TOP record type field");
				// TTRRecordType- add the classifiers it depends on which have already been added
				classifier_name = formula;
				Digraph<String> subgraph = new Digraph<String>();
				subgraph = new TTRutil().convertRecordTypeToDigraph(TTRRecordType.parse(formula));
				
				// TODO a bit slow to recompute attempt below to speed up, not that essential for small RTs
				/*List<String> parents = this.classifierGraph.getParents(ttrGraphOrder.get(i));
				logger.debug(parents);
				for (String v : this.classifierGraph.getNeighbours().keySet()){
					if (parents.contains(v)){
						// if a parent add it
						subgraph.add(v.split("@")[0]);
						subgraph.add(v.split("@")[0],formula); // add relation to ttr
						for (String v_parent : this.classifierGraph.getParents(v)){
							// add internal structure if needs be
							if (parents.contains(v)){
								subgraph.add(v.split("@")[0], v_parent.split("@")[0]);
							}
						}
					}
				}*/
				
				classifier = new RecordTypeClassifier(TTRRecordType.parse(formula), subgraph);
				
			} else if (formula.contains(", r")){
				logger.debug("restrictor field");
				logger.debug(this.classifierGraph.getDaughters(ttrGraphOrder.get(i)));
				TTRRecordType restrictorRT = TTRRecordType.parse(this.classifierGraph.getDaughters(ttrGraphOrder.get(i)).get(0).split("@")[1]);
				logger.debug(restrictorRT);
				// find any numeric/cardinality quantifier in the restrictor (slight hack) and use that
				// if no cardinality established and just iota/eps/tau, then do something else
				List<String> quantifiers = new ArrayList<String>();
				for (TTRField f : restrictorRT.getFields()){
					if (f.getType()!=null){
						logger.debug(f.getType().toString());
						if (f.getType().toString().contains("quant_")){
							String quant = f.getType().toString().split("\\(")[0];
							quantifiers.add(quant);
							logger.debug("found quant " + quant);
						}
					}
				}
				classifier_name = "";
				int[] numbers = new int[]{1,2,3,4,5,6,7,8,9,10};
				for (String quant : quantifiers){
					boolean found = false;
					String number = quant.split("_")[1];
					// see if it matches a number, if so stop
					for (int n : numbers){
						if (quant.equals("quant_" + String.valueOf(n))){
							classifier_name = quant;
							found = true;
							break;
						}
					}
					if (found) break;
					// if no cardinal found
					if (classifier_name!=""){
						classifier_name = "quant_" + number;
					}
				}
				// assume iota single for now
				if (classifier_name.equals("")){
					classifier_name = "quant_1";
				}
				logger.debug("classifier to be retrieved: " + classifier_name);
				classifier = this.classifierNameMap.get(classifier_name);
				
				
			} else {
				if (formula.contains("(")){
					// PType or straight look up
					classifier_name = formula.split("\\(")[0];
				} else {
					classifier_name = formula;
				}
				logger.debug("classifier to be retrieved: " + classifier_name);
				classifier = this.classifierNameMap.get(classifier_name);
				if (classifier==null){
					logger.error("no classifier for " + classifier_name);
					continue;
				}
				if (classifier instanceof CardinalityClassifier){
					logger.debug("ignoring internal cardinality classifier " + classifier_name);
					continue;
				}
				
			}
			} catch (Exception e) {
				logger.error("problem adding classifier for " + label + " " + formula);
				continue;
			}
		
			// add the classifier, linked to its formula
			logger.debug(ttrGraphOrder.get(i));
			logger.debug(classifier.getName());
			this.classifierOrder.add(new Pair<String,Classifier>(ttrGraphOrder.get(i),classifier));
		}
		
		//logger.debug(classifierNameMap);
	}
	
	public void createGraph(TTRRecordType sem){
		this.setTTR(sem);
		this.createGraph();
	}

	private void setTTR(TTRRecordType sem) {
		this.ttr = sem;	
	}

	public double getUnNormalizedProbabilityOfObjectSetGivenWorldBelief(
			SortedSet<String> objectSet,
			WorldBelief wb){
		double prob = 0;
		
		
		return prob;
	}
	
	public double getProbabilityOfObjectSetGivenWorldBelief(
			SortedSet<String> objectSet,
			WorldBelief wb){
		double prob = 0;
		
		
		return prob;
	}

	public List<Pair<String,Classifier>> getClassifierOrder() {
		return this.classifierOrder;
	}

	public void setClassifierOrder(List<Pair<String,Classifier>> classifierOrder) {
		this.classifierOrder = classifierOrder;
	}
	

}
