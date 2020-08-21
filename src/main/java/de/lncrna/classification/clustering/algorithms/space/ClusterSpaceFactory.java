package de.lncrna.classification.clustering.algorithms.space;

import de.lncrna.classification.clustering.algorithms.ClusteringAlgorithm;
import de.lncrna.classification.clustering.algorithms.implementations.CanopyClustering;
import de.lncrna.classification.clustering.algorithms.implementations.HierarchicalClustering;
import de.lncrna.classification.clustering.algorithms.implementations.KMeansClustering;
import de.lncrna.classification.distance.DistanceType;
import de.lncrna.classification.util.PropertyHandler;
import de.lncrna.classification.util.PropertyKeyHelper.PropertyKeys;

public class ClusterSpaceFactory {

	public static  AbstractClusteringSpace<? extends ClusteringAlgorithm> createClusterSpace(Class<? extends ClusteringAlgorithm> clusterType, DistanceType distanceProp) {
		if (HierarchicalClustering.class == clusterType) {
			return new HierachicalClusteringSpace(distanceProp);
		} else if (KMeansClustering.class == clusterType) {
			return new KMeansClusteringSpace(distanceProp);
		} else if (CanopyClustering.class == clusterType) {
			float loose = PropertyHandler.HANDLER.getPropertyValue(PropertyKeys.LOOSE_THRESHOLD, Float.class);
			float tight = PropertyHandler.HANDLER.getPropertyValue(PropertyKeys.TIGHT_THRESHOLD, Float.class);
			return new CanopyClusteringSpace(distanceProp, loose, tight);
		}
		return null;
	}
	
}
