package de.lncrna.classification.clustering.algorithms.space;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import de.lncrna.classification.clustering.Cluster;
import de.lncrna.classification.clustering.algorithms.ImplementedClusteringAlgorithms;
import de.lncrna.classification.clustering.algorithms.implementations.HierarchicalClustering;
import de.lncrna.classification.db.Neo4jDatabaseSingleton;
import de.lncrna.classification.distance.DistancePair;
import de.lncrna.classification.distance.DistanceType;
import de.lncrna.classification.util.PropertyHandler;
import de.lncrna.classification.util.PropertyKeyHelper.PropertyKeys;

public class HierachicalClusteringSpace extends AbstractClusteringSpace<HierarchicalClustering> {
	
	private LinkedList<DistancePair> orderedDistances;
	
	private int requestNumber = 1;
	
	public HierachicalClusteringSpace(DistanceType distanceProp, boolean initFromDB) {
		super(distanceProp, initFromDB);
	}

	@Override
	protected void initSpace() {
		LOG.log(Level.INFO, "Starting hierarchical clustering with minimal distance");
		LOG.log(Level.INFO, "Initializing clusters");
		
		List<String> sequenceNames = Neo4jDatabaseSingleton.getQueryHelper().getAllSequenceNames();
		
		sequenceNames.parallelStream()
			.map(sequence -> new Cluster<>(new HierarchicalClustering(getDistanceProperties()), sequence))
			.forEach(getClusters()::add);		
		
		LOG.log(Level.INFO, String.format("Initialized %d clusters", getClusters().size()));
		// Data is automatically sorted in PriorityQueue
		this.orderedDistances = Neo4jDatabaseSingleton.getQueryHelper().getDistancesOrdered(getDistanceProperties().name(), 100000, this.requestNumber);
	}

	@Override
	public boolean nextIteration() {
		Cluster<HierarchicalClustering> c1 = null;
		Cluster<HierarchicalClustering> c2 = null;
		
		DistancePair current = null;
		
		do {
			if (this.orderedDistances.isEmpty()) {
				this.requestNumber++;
				this.orderedDistances = Neo4jDatabaseSingleton.getQueryHelper().getDistancesOrdered(getDistanceProperties().name(), 100000, this.requestNumber);
			}
			
			if (breakCondition()) {
				return false;
			}
			
			current = this.orderedDistances.poll();
			
			c1 = findContainingCluster(current.getSequenceName1());
			c2 = findContainingCluster(current.getSequenceName2());
		} while (c1 == c2 || c1 == null || c2 == null);
		
		incrementIterationCounter();
		
		getStatLogger().logMerge(c1, c2, current.getDistance(), getIterationCounter());
		
		int refreshInterval = PropertyHandler.HANDLER.getPropertyValue(PropertyKeys.STAT_REFRESH_INTERVAL, Integer.class);
		if (getIterationCounter() % refreshInterval == 0) {
			double maxAverageClusterDistance = PropertyHandler.HANDLER.getPropertyValue(PropertyKeys.HIERARCHICAL_AVERAGE_CLUSTER_DISTANCE_THRESHOLD, Double.class);
			double averageClusterDistance = handleAverageDistanceWithinCluster();
			double averageDiameter = handleAverageClusterDiameter();
			
			if (averageClusterDistance > maxAverageClusterDistance) {
				return false;
			}
			
			LOG.log(Level.INFO, String.format("Hierarchical clustering: Iteration %d; current number of clusters %d with average cluster distance of %f and diameter of %f", getIterationCounter(), getClusters().size(), averageClusterDistance, averageDiameter));
		}
		
		getClusters().remove(c2);
		c1.mergeWithOther(c2);		
		
		return true;
	}
	
	private double handleAverageDistanceWithinCluster() {
		double averageClusterDistance = calculateAverageDistanceWithinCluster();
		
		getStatLogger().logAverageDistanceWithinCluster(averageClusterDistance, getIterationCounter());
		
		return averageClusterDistance;
	}
	
	private double handleAverageClusterDiameter() {
		double averageClusterDiameter = calculateAverageClusterDiameter();
		
		getStatLogger().logAverageClusterDiameter(averageClusterDiameter, getIterationCounter());
		
		return averageClusterDiameter;
	}
	
	private boolean breakCondition() {
		int maxClusterCount = PropertyHandler.HANDLER.getPropertyValue(PropertyKeys.HIERARCHICAL_CLUSTER_COUNT, Integer.class);
		
		return getClusters().size() == 1 
			|| this.orderedDistances.isEmpty() 
			|| getClusters().size() <= maxClusterCount;
	}

	private Cluster<HierarchicalClustering> findContainingCluster(String sequence) {
		return getClusters().parallelStream()
			.filter(c -> c.containsSequence(sequence))
			.findFirst()
			.orElse(null);
	}

	@Override
	protected void initFromDB() {
		Map<Long, List<String>> clusters = Neo4jDatabaseSingleton.getQueryHelper().getClusters(getDistanceProperties().name(), ImplementedClusteringAlgorithms.Hierarchical.name());
		
		clusters.entrySet()
			.stream()
			.map(c -> new Cluster<>(new HierarchicalClustering(getDistanceProperties()), c.getValue()))
			.forEach(this::addCluster);;
		
	}
	
}
