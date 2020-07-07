package de.lncrna.classification.clustering.algorithms;

import de.lncrna.classification.clustering.algorithms.implementations.CanopyClustering;
import de.lncrna.classification.clustering.algorithms.implementations.HierarchicalClustering;
import de.lncrna.classification.clustering.algorithms.implementations.KMeansClustering;

public enum ImplementedClusteringAlgorithms {

	HIERARCHICAL_CLUSTERING(HierarchicalClustering.class),
	K_MEANS(KMeansClustering.class),
	CANOPY(CanopyClustering.class);
	
	private final Class<? extends ClusteringAlgorithm> algorithmType;

	ImplementedClusteringAlgorithms(Class<? extends ClusteringAlgorithm> algorithm) {
		this.algorithmType = algorithm;
	}

	public Class<? extends ClusteringAlgorithm> getImplementationType() {
		return algorithmType;
	}
	
}
