package de.lncrna.classification.clustering.algorithms.implementations;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.NotImplementedException;
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
	private final Set<String> sequences = new HashSet<>();
	private final DistanceType distanceProperty;

	public KMeansClustering(final DistanceType distanceProperty) {
		this.distanceProperty = distanceProperty;
	}

	@Override
	public void mergeWithOther(final Cluster<?> cluster) {
		throw new NotImplementedException();
	}

	@Override
	public void addSequence(final String data) {
		throw new NotImplementedException();

	}

	@Override
	public Collection<String> getSequences() {
		return this.sequences;
	}

	@Override
	public void initCluster(final List<String> sequences) {
		this.sequences.addAll(sequences);
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
