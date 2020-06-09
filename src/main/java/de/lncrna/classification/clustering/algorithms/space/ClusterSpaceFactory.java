package de.lncrna.classification.clustering.algorithms.space;

import de.lncrna.classification.clustering.algorithms.ClusteringAlgorithm;
import de.lncrna.classification.clustering.algorithms.implementations.HierarchicalClusteringAverageDistance;
import de.lncrna.classification.clustering.algorithms.implementations.HierarchicalClusteringMaximalDistance;
import de.lncrna.classification.clustering.algorithms.implementations.HierarchicalClusteringMinimalDistance;
import de.lncrna.classification.clustering.algorithms.implementations.KMeansClustering;
import de.lncrna.classification.util.data.DistanceMatrix;

public class ClusterSpaceFactory {

	public static  AbstractClusteringSpace<? extends ClusteringAlgorithm> createClusterSpace(Class<? extends ClusteringAlgorithm> clusterType, DistanceMatrix matrix) {
		if (HierarchicalClusteringMinimalDistance.class == clusterType) {
			return new MinimalDistanceHierachicalClusteringSpace(matrix);
		} else if (HierarchicalClusteringMaximalDistance.class == clusterType) {
			return null;
		} else if (HierarchicalClusteringAverageDistance.class == clusterType) {
			return null;
		} else if (KMeansClustering.class == clusterType) {
			return null;
		}
		return null;
	}
	
}
