package de.lncrna.classification.clustering.algorithms.space;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

import de.lncrna.classification.clustering.Cluster;
import de.lncrna.classification.clustering.algorithms.implementations.HierarchicalClustering;
import de.lncrna.classification.db.Neo4jDatabaseSingleton;
import de.lncrna.classification.distance.DistanceType;
import de.lncrna.classification.util.PropertyHandler;
import de.lncrna.classification.util.PropertyKeyHelper.PropertyKeys;
import de.lncrna.classification.util.data.DistanceDAO;

public class HierachicalClusteringSpace extends AbstractClusteringSpace<HierarchicalClustering> {
	
	private LinkedList<DistanceDAO> orderedDistances;
	
	public HierachicalClusteringSpace(DistanceType distanceProp) {
		super(distanceProp);
	}

	@Override
	protected void initSpace() {
		LOG.log(Level.INFO, "Starting hierarchical clustering with minimal distance");
		LOG.log(Level.INFO, "Initializing clusters");
		
		List<String> sequenceNames = Neo4jDatabaseSingleton.getQueryHelper().getAllSequenceNames();
		
		sequenceNames.parallelStream()
			.map(sequence -> new Cluster<>(new HierarchicalClustering(getDistanceProperties()), sequence))
			.forEach(cluster -> getClusters().add(cluster));		
		
		LOG.log(Level.INFO, "Pairwise comparison of all points");
		// Data is automatically sorted in PriorityQueue
		this.orderedDistances = Neo4jDatabaseSingleton.getQueryHelper().getDistancesOrdered(getDistanceProperties().name(), 20000);
	}

	@Override
	public boolean nextIteration() {
		if (breakCondition()) {
			return false;
		}
		
		DistanceDAO current = this.orderedDistances.poll();
		
		Cluster<HierarchicalClustering> c1 = findContainingCluster(current.getSeq1());
		Cluster<HierarchicalClustering> c2 = findContainingCluster(current.getSeq2());
		
		if (c1 != c2 && c1 != null) {
			incrementIterationCounter();
			
			int refreshInterval = PropertyHandler.HANDLER.getPropertyValue(PropertyKeys.STAT_REFRESH_INTERVAL, Integer.class);
			if (getIterationCounter() % refreshInterval == 0) {
				double maxAverageClusterDistance = PropertyHandler.HANDLER.getPropertyValue(PropertyKeys.AVERAGE_CLUSTER_DISTANCE_THRESHOLD, Double.class);
				double averageClusterDistance = calculateAverageClusterDistance();
				
				if (averageClusterDistance >= maxAverageClusterDistance) {
					return false;
				}
				
				LOG.log(Level.INFO, String.format("Hierarchical clustering: Iteration %d; current number of clusters %d with average cluster distance of %d", getIterationCounter(), getClusters().size(), averageClusterDistance));
			}
			
			getClusters().remove(c2);
			c1.mergeWithOther(c2);
		} else {
			// nextIteration() until two clusters are merged or singularity is reached
			return nextIteration();
		}		
		
		return true;
	}
	
	private boolean breakCondition() {
		int maxClusterCount = PropertyHandler.HANDLER.getPropertyValue(PropertyKeys.CLUSTER_COUNT, Integer.class);
		
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
	
}
