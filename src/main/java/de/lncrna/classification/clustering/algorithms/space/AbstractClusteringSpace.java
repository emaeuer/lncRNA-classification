package de.lncrna.classification.clustering.algorithms.space;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import de.lncrna.classification.clustering.Cluster;
import de.lncrna.classification.clustering.algorithms.ClusteringAlgorithm;
import de.lncrna.classification.util.data.DistanceMatrix;

public abstract class AbstractClusteringSpace<T extends ClusteringAlgorithm> {

	protected static final Logger LOG = Logger.getLogger("logger");
	
	private final List<Cluster<T>> clusters = Collections.synchronizedList(new ArrayList<>());
	
	private T algorithm;
	
	private final DistanceMatrix matrix;
	
	public AbstractClusteringSpace(DistanceMatrix distances) {
		initSpace(distances);
		this.matrix = distances;
	}

	protected abstract void initSpace(DistanceMatrix distances);
	
	public abstract void nextIteration();

	public List<Cluster<T>> getClusters() {
		return clusters;
	}

	public T getAlgorithm() {
		return algorithm;
	}

	public void setAlgorithm(T algorithm) {
		this.algorithm = algorithm;
	}

	public double calculateAverageClusterDistance() {
		return this.clusters.parallelStream()
			.mapToDouble(this::calculateAverageDistanceOfCluster)
			.average()
			.orElse(-1);
	}
	
	public double calculateAverageDistanceOfCluster(Cluster<T> cluster) {
		double distanceSum = 0;
		int cnt = 1;
		String[] sequences = cluster.getSequences().toArray(new String[cluster.getClusterSize()]);
		for (int i = 0; i < cluster.getClusterSize(); i++) {
			for (int j = i + 1; j < cluster.getClusterSize(); j++) {
				distanceSum += this.matrix.getDistance(sequences[i], sequences[j]).getValue();
				cnt++;
			}
		}
		return distanceSum / cnt;
	}
	
}
