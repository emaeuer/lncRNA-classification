package de.lncrna.classification.clustering.algorithms.implementations;

import java.util.Collection;
import java.util.List;

import de.lncrna.classification.clustering.Cluster;
import de.lncrna.classification.clustering.algorithms.ClusteringAlgorithm;
import de.lncrna.classification.clustering.algorithms.ImplementedClusteringAlgorithms;
import de.lncrna.classification.distance.DistanceType;

/**
 * Concrete Implementation of the strategy pattern. Implements the k-means algorithm
 * 
 * @author emaeu
 *
 */
public class KMeansClustering implements ClusteringAlgorithm {

	private final DistanceType distanceProperty;

	public KMeansClustering(DistanceType distanceProperty) {
		this.distanceProperty = distanceProperty;
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void initCluster(List<String> sequences) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public DistanceType getDistanceAlgortithm() {
		return this.distanceProperty;
	}
	
	@Override
	public String getName() {
		return ImplementedClusteringAlgorithms.K_Means.name();
	}

}
