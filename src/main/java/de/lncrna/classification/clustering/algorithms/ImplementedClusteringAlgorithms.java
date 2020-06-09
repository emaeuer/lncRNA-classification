package de.lncrna.classification.clustering.algorithms;

import de.lncrna.classification.clustering.algorithms.implementations.HierarchicalClusteringAverageDistance;
import de.lncrna.classification.clustering.algorithms.implementations.HierarchicalClusteringMaximalDistance;
import de.lncrna.classification.clustering.algorithms.implementations.HierarchicalClusteringMinimalDistance;
import de.lncrna.classification.clustering.algorithms.implementations.KMeansClustering;

public enum ImplementedClusteringAlgorithms {

	HIERARCHICAL_CLUSTERING_AVERAGE_DISTANCE(HierarchicalClusteringAverageDistance.class),
	HIERARCHICAL_CLUSTERING_MINIMAL_DISTANCE(HierarchicalClusteringMinimalDistance.class),
	HIERARCHICAL_CLUSTERING_MAXIMAL_DISTANCE(HierarchicalClusteringMaximalDistance.class),
	K_MEANS(KMeansClustering.class);
	
	private final Class<? extends ClusteringAlgorithm> algorithmType;

	ImplementedClusteringAlgorithms(Class<? extends ClusteringAlgorithm> algorithm) {
		this.algorithmType = algorithm;
	}

	public Class<? extends ClusteringAlgorithm> getImplementationType() {
		return algorithmType;
	}
	
}
