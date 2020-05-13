package de.lncrna.classification.clustering.algorithms.implementations;

import java.util.Collection;
import java.util.List;

import org.biojava.nbio.core.sequence.RNASequence;

import de.lncrna.classification.clustering.Cluster;
import de.lncrna.classification.clustering.algorithms.ClusteringAlgorithm;

/**
 * Concrete Implementation of the strategy pattern. Implements the k-means algorithm
 * 
 * @author emaeu
 *
 */
public class KMeansClustering implements ClusteringAlgorithm {

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
	public void addSequence(RNASequence data) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Collection<RNASequence> getSequences() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void initCluster(List<RNASequence> sequences) {
		// TODO Auto-generated method stub
		
	}

}