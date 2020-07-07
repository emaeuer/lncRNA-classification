package de.lncrna.classification.clustering.algorithms.space;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import de.lncrna.classification.clustering.Cluster;
import de.lncrna.classification.clustering.algorithms.ClusteringAlgorithm;
import de.lncrna.classification.db.Neo4JCypherQueriesServer;
import de.lncrna.classification.init.distance.DistanceProperties;

public abstract class AbstractClusteringSpace<T extends ClusteringAlgorithm> {

	protected static final Logger LOG = Logger.getLogger("logger");
	
	private final List<Cluster<T>> clusters = Collections.synchronizedList(new ArrayList<>());
	
	private T algorithm;
	
	private final DistanceProperties distanceProp;
	
	public AbstractClusteringSpace(DistanceProperties distanceProp) {
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
	
	protected DistanceProperties getDistanceProperties() {
		return this.distanceProp;
	}

	public void setAlgorithm(T algorithm) {
		this.algorithm = algorithm;
	}

	public double calculateAverageClusterDistance() {
		return this.clusters.parallelStream()
			.mapToDouble(c -> Neo4JCypherQueriesServer.getAverageClusterDistance(c.getSequences(), this.distanceProp.name()))
			.average()
			.orElse(-1);
	}	
}
