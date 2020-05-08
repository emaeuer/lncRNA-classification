package de.lncrna.classification.clustering.algorithms;

import de.lncrna.classification.clustering.algorithms.implementations.HierarchicalClusteringAverageDistance;
import de.lncrna.classification.clustering.algorithms.implementations.HierarchicalClusteringMaximalDistance;
import de.lncrna.classification.clustering.algorithms.implementations.HierarchicalClusteringMinimalDistance;
import de.lncrna.classification.clustering.algorithms.implementations.KMeansClustering;

public enum ImplementedClusteringAlgorithms {

	HIERARCHICAL_CLUSTERING_AVERAGE_DISTANCE(new HierarchicalClusteringAverageDistance()),
	HIERARCHICAL_CLUSTERING_MINIMAL_DISTANCE(new HierarchicalClusteringMinimalDistance()),
	HIERARCHICAL_CLUSTERING_MAXIMAL_DISTANCE(new HierarchicalClusteringMaximalDistance()),
	K_MEANS(new KMeansClustering());
	
	private final ClusteringAlgorithm algorithm;

	ImplementedClusteringAlgorithms(ClusteringAlgorithm algorithm) {
		this.algorithm = algorithm;
	}

	public ClusteringAlgorithm getImplementation() {
		return algorithm;
	}
	
}
