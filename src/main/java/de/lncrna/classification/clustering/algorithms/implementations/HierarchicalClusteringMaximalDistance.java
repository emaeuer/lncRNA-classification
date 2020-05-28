package de.lncrna.classification.clustering.algorithms.implementations;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.lncrna.classification.clustering.Cluster;
import de.lncrna.classification.clustering.algorithms.ClusteringAlgorithm;

/**
 * Concrete Implementation of the strategy pattern. Implements an hierarchical clustering 
 * algorithm that takes the maximal distance between two points from different clusters as 
 * the distance between them
 * 
 * @author emaeu
 *
 */
public class HierarchicalClusteringMaximalDistance implements ClusteringAlgorithm {

	private final List<String> sequences = new ArrayList<>();
	
	@Override
	public double distanceTo(Cluster<?> cluster) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void mergeWithOther(Cluster<?> cluster) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addSequence(String data) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Collection<String> getSequences() {
		return this.sequences;
	}

	@Override
	public void initCluster(List<String> sequences) {
		// TODO Auto-generated method stub
		
	}
	
}
