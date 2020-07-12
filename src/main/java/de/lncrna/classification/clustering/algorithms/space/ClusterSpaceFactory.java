package de.lncrna.classification.clustering.algorithms.space;

import de.lncrna.classification.clustering.algorithms.ClusteringAlgorithm;
import de.lncrna.classification.clustering.algorithms.implementations.CanopyClustering;
import de.lncrna.classification.clustering.algorithms.implementations.HierarchicalClustering;
import de.lncrna.classification.clustering.algorithms.implementations.KMeansClustering;
import de.lncrna.classification.init.distance.DistanceProperties;

public class ClusterSpaceFactory {

	public static  AbstractClusteringSpace<? extends ClusteringAlgorithm> createClusterSpace(Class<? extends ClusteringAlgorithm> clusterType, DistanceProperties distanceProp) {
		if (HierarchicalClustering.class == clusterType) {
			return new HierachicalClusteringSpace(distanceProp);
		} else if (KMeansClustering.class == clusterType) {
			return null;
		} else if (CanopyClustering.class == clusterType) {
			return new CanopyClusteringSpace(distanceProp, 0.3f, 0.25f);
		}
		return null;
	}
	
}
