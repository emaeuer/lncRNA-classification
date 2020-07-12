package de.lncrna.classification.clustering.algorithms;

import de.lncrna.classification.clustering.algorithms.implementations.CanopyClustering;
import de.lncrna.classification.clustering.algorithms.implementations.HierarchicalClustering;
import de.lncrna.classification.clustering.algorithms.implementations.KMeansClustering;

public enum ImplementedClusteringAlgorithms {

	Hierarchical(HierarchicalClustering.class),
	K_Means(KMeansClustering.class),
	Canopy(CanopyClustering.class);
	
	private final Class<? extends ClusteringAlgorithm> algorithmType;

	ImplementedClusteringAlgorithms(Class<? extends ClusteringAlgorithm> algorithm) {
		this.algorithmType = algorithm;
	}

	public Class<? extends ClusteringAlgorithm> getImplementationType() {
		return algorithmType;
	}
	
}
