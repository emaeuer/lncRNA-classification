package de.lncrna.classification.clustering.algorithms.implementations;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.lncrna.classification.clustering.Cluster;
import de.lncrna.classification.clustering.algorithms.ClusteringAlgorithm;
import de.lncrna.classification.clustering.algorithms.ImplementedClusteringAlgorithms;
import de.lncrna.classification.init.distance.DistanceProperties;

/**
 * Concrete Implementation of the strategy pattern. Implements an hierarchical clustering 
 * algorithm that takes the minimal distance between two points from different clusters as 
 * the distance between them
 * 
 * @author emaeu
 *
 */
public class HierarchicalClustering implements ClusteringAlgorithm {

	private final Set<String> sequences = new HashSet<>();

	private final DistanceProperties distanceProperty;

	public HierarchicalClustering(DistanceProperties distanceProperty) {
		this.distanceProperty = distanceProperty;
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
	
	@Override
	public DistanceProperties getDistanceAlgortithm() {
		return this.distanceProperty;
	}
	
	@Override
	public String getName() {
		return ImplementedClusteringAlgorithms.Hierarchical.name();
	}
	
}
