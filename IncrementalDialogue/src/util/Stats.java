package util;

import java.util.ArrayList;

//import math.geom2d.Point2D;




import java.util.HashSet;
import java.util.Set;

import sium.nlu.stat.DistRow;
import sium.nlu.stat.Distribution;

public class Stats {
	
	public static Distribution<String> adjustedDistributionForSubset(Distribution<String> rawdist, ArrayList<String> subset){
		Distribution<String> dist = new Distribution<String>();
		for (DistRow<String> d : rawdist.getDistribution()) {
			String ID = d.getEntity();
			if (subset.contains(ID)){ //only add those of interest
				dist.addProbability(ID, d.getProbability());
			}
		}
		dist.normalize();
		return dist;
	}

	public static Distribution<String> uniformDistribution(
			Set categories) {
		double prob = 1.0/categories.size(); //assuming uniform
		Distribution<String> dist = new Distribution<String>();
		for (Object c : categories){
			dist.addProbability(c.toString(), prob);
		}
		return dist;
	}

	public static Distribution<String> uniformDistributionFromDist(
			Distribution<String> rawdist) {
		Set<String> cats = new HashSet<String>();
		for (DistRow<String> d : rawdist.getDistribution()) {
			String ID = d.getEntity();
			cats.add(ID);
		}
		return uniformDistribution(cats);
	}
	
	public double mean(double[] m) {
	    double sum = 0;
	    for (int i = 0; i < m.length; i++) {
	        sum += m[i];
	    }
	    return sum / m.length;
	}
	
	/*
	public double[] computeWeightedCentroid(double[][] rawpoints, double[] weights){
		*//***
		 * Given the weights (a probability distribution), return the coordinates which 
		 * reflect the weights, where objects with higher probabilities will be closer than
		 * those with low ones.
		 *//*
		Point2D[] points = new Point2D[rawpoints.length];
		for (int i=0; i<rawpoints.length; i++){
			points[i] = new Point2D(rawpoints[i][0],rawpoints[i][1]);
		}
		Point2D point = Point2D.centroid(points, weights);
		return new double[]{ point.getX(), point.getY() };
	}
	*/
}
