package de.lncrna.classification.clustering.algorithms.implementations;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.lncrna.classification.clustering.Cluster;
import de.lncrna.classification.clustering.algorithms.ClusteringAlgorithm;

/**
 * Concrete Implementation of the strategy pattern. Implements an hierarchical clustering 
 * algorithm that takes the minimal distance between two points from different clusters as 
 * the distance between them
 * 
 * @author emaeu
 *
 */
public class HierarchicalClusteringMinimalDistance implements ClusteringAlgorithm {

	private final Set<String> sequences = new HashSet<>();
	
	@Override
	public double distanceTo(Cluster<?> cluster) {
		// not implemented --> handled by MinimalDistanceHierachicalClusteringSpace
		return Double.NaN;
	}

	@Override
	public void mergeWithOther(Cluster<?> other) {
		this.sequences.addAll(other.getAlgorithm().getSequences());
		other.clear();
	}

	@Override
	public void addSequence(String data) {
		this.sequences.add(data);		
	}

	@Override
	public Collection<String> getSequences() {
		return this.sequences;
	}

	@Override
	public void initCluster(List<String> sequences) {
		this.sequences.addAll(sequences);		
	}
	
}
