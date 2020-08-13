package de.lncrna.classification.clustering.algorithms.space;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import de.lncrna.classification.clustering.Cluster;
import de.lncrna.classification.clustering.algorithms.ClusteringAlgorithm;
import de.lncrna.classification.db.Neo4jDatabaseSingleton;
import de.lncrna.classification.distance.DistanceType;

public abstract class AbstractClusteringSpace<T extends ClusteringAlgorithm> {

	protected static final Logger LOG = Logger.getLogger("logger");
	
	private final List<Cluster<T>> clusters = Collections.synchronizedList(new ArrayList<>());
	
	private T algorithm;
	
	private final DistanceType distanceProp;
	
	private int iterationCounter = 0;
	
	public AbstractClusteringSpace(DistanceType distanceProp) {
		this.distanceProp = distanceProp;
		initSpace();
	}

	protected abstract void initSpace();
	
	public abstract boolean nextIteration();

	public List<Cluster<T>> getClusters() {
		return clusters;
	}
	
	public void addCluster(Cluster<T> cluster) {
		this.clusters.add(cluster);
	}

	public T getAlgorithm() {
		return algorithm;
	}
	
	protected DistanceType getDistanceProperties() {
		return this.distanceProp;
	}

	public void setAlgorithm(T algorithm) {
		this.algorithm = algorithm;
	}

	public double calculateAverageClusterDistance() {
		return this.clusters.parallelStream()
			.mapToDouble(Cluster::getAverageDistanceWithin)
			.average()
			.orElse(-1);
	}	
	
	public void persistClusterInformation() {
		Neo4jDatabaseSingleton.getQueryHelper().updateClusterInformation(this.clusters);
	}

	public long getNumberOfClusters() {
		return clusters.stream()
				.filter(c -> c.getClusterSize() > 1)
				.count();
	}

	protected int getIterationCounter() {
		return iterationCounter;
	}

	protected void incrementIterationCounter() {
		this.iterationCounter++;
	}
	
}
