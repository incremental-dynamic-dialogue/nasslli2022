package util;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import qmul.ds.formula.TTRField;
import qmul.ds.formula.TTRRecordType;

public class TTRutil {
	
	private static String FIELD_SEPARATOR = "@";
	public static Logger logger;
	
	public TTRutil(){
		this.logger = Logger.getLogger(TTRutil.class);
	}
	
	
	
	/**
	 * Convert a TTR record type to a directed graph where nodes
	 * are the fields. Recursive method.
	 */
	public Digraph<String> convertRecordTypeToDigraph(TTRRecordType ttr,
													  Digraph<String> digraph,
													  TTRField head,
													  List<Integer> index_orig){
		logger.debug("Call to method with head:");
		logger.debug(head);
		logger.debug("with ttr record type:");
		logger.debug(ttr);
		
		String headFormula = "";
		
		if (head.getType()!=null){
			if (head.getType() instanceof TTRRecordType){
				TTRRecordType internalttr = ((TTRRecordType) head.getType());
				internalttr = internalttr.removeHead();
				headFormula = head.getLabel().toString() + FIELD_SEPARATOR + internalttr.toString();;
			} else {
				headFormula = head.getLabel().toString() + FIELD_SEPARATOR + head.getType().toString();
			}
		} else {
			headFormula = head.getLabel().toString() + FIELD_SEPARATOR + head.getDSType().toString();
		}
		
		
		if (headFormula.equals("")){
			logger.debug("No head formula");
			logger.debug(head);
		}
		// add the dependency in
		List<Integer> index = index_orig.subList(0, index_orig.size());
		index.add(1);
	
		
		List<TTRField> dependents = new ArrayList<TTRField>();
		if (head.hasManifestContent()&&head.getType() instanceof TTRRecordType){
			// if it's a record type, then
			logger.debug("Internal record type");
			TTRRecordType subTTR = (TTRRecordType) head.getType();
			TTRField subhead = subTTR.getHeadField();
			subTTR = subTTR.removeHead();
			digraph = convertRecordTypeToDigraph(subTTR, digraph, subhead, index);
			for (TTRField d: subTTR.getFields()){
				logger.debug("Dependent top:");
				logger.debug(d);
				String dependentFieldFormula = "";
				if (d.getType()!=null){
					if (d.getType() instanceof TTRRecordType){
						TTRRecordType internalttr = ((TTRRecordType) d.getType());
						internalttr = internalttr.removeHead();
						dependentFieldFormula = d.getLabel().toString() + FIELD_SEPARATOR + internalttr.toString();;
					} else {
						dependentFieldFormula = d.getLabel().toString() + FIELD_SEPARATOR + d.getType().toString();
					}
				} else {
					dependentFieldFormula = d.getLabel().toString() + FIELD_SEPARATOR + d.getDSType().toString();
				}
				
				digraph.add(head.getLabel().toString() + FIELD_SEPARATOR + subTTR.toString(), dependentFieldFormula);
			}
		} 
		for (TTRField f : ttr.getFields()){
			//logger.debug(f);
			//logger.debug(ttr.getDependents(f));
			if (f.dependsOn(head)&&!f.isHead()){
				dependents.add(f);
			}
		}
		
		logger.debug("Dependents:");
		logger.debug(dependents);
		TTRRecordType test_ttr = ttr.clone();
		
		// needs to be recursive
		int count = 1;
		for (TTRField d : dependents){
			logger.debug("Dependent:");
			logger.debug(d);
			String dependentFieldFormula = "";
			if (d.getType()!=null){
				if (d.getType() instanceof TTRRecordType){
					TTRRecordType internalttr = ((TTRRecordType) d.getType());
					internalttr = internalttr.removeHead();
					dependentFieldFormula = d.getLabel().toString() + FIELD_SEPARATOR + internalttr.toString();
				} else {
					dependentFieldFormula = d.getLabel().toString() + FIELD_SEPARATOR + d.getType().toString();
				}
			} else {
				dependentFieldFormula = d.getLabel().toString() + FIELD_SEPARATOR + d.getDSType().toString();
			}
			digraph.add(dependentFieldFormula, headFormula);
			index.add(count);
			count+=1;
			logger.debug("adding link " + dependentFieldFormula + " -> " + headFormula);
			logger.debug(index);
			// check if you've got dependents
			List<TTRField> subdependents = new ArrayList<TTRField>();
			List<TTRField> fields = test_ttr.getFields();
			for (int i =0; i<fields.size(); i++){
				TTRField f = fields.get(i);
				logger.debug("Subdependent candidate for:" + d);
				logger.debug(f);
				if (d.dependsOn(f)&&!f.isHead()){
					logger.debug("SUBDEPENDENT");
					subdependents.add(f);
					digraph = convertRecordTypeToDigraph(test_ttr, digraph, d, index);
				}
				//fields.remove(0);
			}
					
		}
		
		return digraph;
	}
	
	public Digraph<String> convertRecordTypeToDigraph(TTRRecordType ttr){
		TTRField head = ttr.getHeadField(); // NOTE ONLY WORKS FOR THE HEAD FIELD BEING REFERRED TO
		logger.debug(head);
		TTRRecordType ttr_copy = ttr.clone();
		List<TTRField> fields = new ArrayList<TTRField>();
		if (head!=null){
			fields.add(head);
			ttr_copy = ttr_copy.removeHead();
			ttr_copy = ttr_copy.removeField(head);
			//fields = ttr_copy.getDependents(head);

		} 
			//just a flat structure
			fields.addAll(ttr_copy.getFields());
			//for (TTRField f : fields){
			//	
			//}
		logger.debug(fields);
		Digraph<String> digraph = new Digraph<String>();
		List<Integer> index = new ArrayList<Integer>();
		int count = 1;
		while(fields.size()>0){
			TTRField d = fields.get(0);
			String headFormula = "";
			if (d.getType()!=null){
				if (d.getType() instanceof TTRRecordType){
					TTRRecordType internalttr = ((TTRRecordType) d.getType());
					internalttr = internalttr.removeHead();
					headFormula = internalttr.toString();
				} else {
					headFormula = d.getType().toString();
				}
			} else {
				headFormula = d.getDSType().toString();
			}
			index = new ArrayList<Integer>(count);
			digraph = convertRecordTypeToDigraph(ttr, digraph, d, index);
			logger.debug("adding link top: " + headFormula + " -> " + ttr.toString());
			logger.debug(index);
			digraph.add("TOP" + FIELD_SEPARATOR + ttr.toString(), d.getLabel().toString() + FIELD_SEPARATOR + headFormula);
			fields.remove(0);
			count++;
		}
		
		return digraph;
	}
	
	public static void main(String[] args) {
		//PropertyConfigurator.configure("log4j.properties");
		//String ttrString = "[x1==addressee : e|e3==take : es|r2 : [x : e|head==x : e|p10==apple(x) : t|p11==quant_plural(x) : t|p9==red(x) : t|p8==big(x) : t|p7==quant_three(x) : t]|head==e3 : es|p6==imperative(e3) : t|x5==(iota, r2.head, r2) : e|p4==obj(e3, x5) : t|p5==subj(e3, x1) : t]";
		//String ttrString = "[r2 : [x : e|head==x : e|p10==apple(x) : t|p11==quant_plural(x) : t|p9==red(x) : t|p8==big(x) : t|p7==quant_three(x) : t]|x5==(iota, r2.head, r2) : e]";
		//String ttrString = "[x : e|head==x : e|p10==apple(x) : t|p11==quant_plural(x) : t|p9==red(x) : t|p8==big(x) : t|p7==quant_three(x) : t]";
		String ttrString = "[r2 : [x13 : e|head==x13 : e]|e4==goal_into : es|e3==action_place : es|x1==addressee : e|r1 : [x9 : e|p7==color_red(x9) : t|p9==quant_1(x9) : t|p8==label_apple(x9) : t|head==x9 : e]|x12==(iota, r2.head, r2) : e|head==e3 : es|x4==(iota, r1.head, r1) : e|p4==obj(e3, x4) : t|p11==obj(e4, x12) : t|p6==subj(e3, x1) : t|p5==indobj(e3, e4) : t]";
		TTRRecordType ttr = TTRRecordType.parse(ttrString);
		Digraph<String> DAG = new TTRutil().convertRecordTypeToDigraph(ttr);
		logger.info("Sort");
		for (String s : DAG.topSort()){
			logger.info("@" + s);
		}
		logger.info(DAG);
	}
	

}
