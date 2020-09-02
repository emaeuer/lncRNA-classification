package de.lncrna.classification.clustering.algorithms.space;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

import de.lncrna.classification.clustering.Cluster;
import de.lncrna.classification.clustering.algorithms.ClusteringAlgorithm;
import de.lncrna.classification.db.Neo4jDatabaseSingleton;
import de.lncrna.classification.distance.DistanceType;
import de.lncrna.classification.util.stat.StatLogger;

public abstract class AbstractClusteringSpace<T extends ClusteringAlgorithm> {

	protected static final Logger LOG = Logger.getLogger("logger");
	
	private final List<Cluster<T>> clusters = Collections.synchronizedList(new ArrayList<>());
	
	private T algorithm;
	
	private final DistanceType distanceProp;
	
	private int iterationCounter = 0;
	
	private final StatLogger statLogger;
	
	public AbstractClusteringSpace(DistanceType distanceProp, boolean initFromDB) {
		this.distanceProp = distanceProp;
		this.statLogger = new StatLogger();
		
		if (initFromDB) {
			initFromDB();
		} else {
			initSpace();
		}
	}
	
	protected abstract void initFromDB();

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

	public double calculateAverageDistanceWithinCluster() {
		return parallelStreamCalculation(Cluster::getAverageDistanceWithin, true);
	}
	
	public double calculateMaxClusterDiameter() {
		return parallelStreamCalculation(Cluster::getDiameter, false);
	}

	private double parallelStreamCalculation(Function<Cluster<?>, Double> task, boolean calculateAverage) {
		ExecutorService service = Executors.newWorkStealingPool();
		
		List<Future<Double>> tasks = this.clusters.stream()
			.map(c -> service.submit(() -> task.apply(c)))
			.collect(Collectors.toList());
		
		DoubleStream stream = tasks.stream()
				.mapToDouble(value -> {
					try {
						return value.get();
					} catch (InterruptedException | ExecutionException e1) {
						e1.printStackTrace();
						return 1;
					}
				});
		
		if (calculateAverage) {
			return stream.average().orElse(-1);
		} else {
			return stream.max().orElse(-1);
		}
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
	
	protected StatLogger getStatLogger() {
		return this.statLogger;
	}
	
	public Map<Integer, Map<String, Double>> calculateSilhouettes() {
		return getClusters().parallelStream()
			.collect(Collectors.toMap(Cluster::getClusterId, Cluster::calculateSilhouettes));
	}
	
}
