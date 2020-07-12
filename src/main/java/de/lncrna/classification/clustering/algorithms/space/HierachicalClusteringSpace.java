package de.lncrna.classification.clustering.algorithms.space;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

import de.lncrna.classification.clustering.Cluster;
import de.lncrna.classification.clustering.algorithms.implementations.HierarchicalClustering;
import de.lncrna.classification.db.Neo4jDatabaseSingleton;
import de.lncrna.classification.init.distance.DistanceProperties;
import de.lncrna.classification.util.data.DistanceDAO;

public class HierachicalClusteringSpace extends AbstractClusteringSpace<HierarchicalClustering> {
	
	private LinkedList<DistanceDAO> orderedDistances;
	
	public HierachicalClusteringSpace(DistanceProperties distanceProp) {
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
		if (getClusters().size() == 1 || this.orderedDistances.isEmpty()) {
			return false;
		}
		
		DistanceDAO current = this.orderedDistances.poll();
		
		Cluster<HierarchicalClustering> c1 = findContainingCluster(current.getSeq1());
		Cluster<HierarchicalClustering> c2 = findContainingCluster(current.getSeq2());
		
		if (c1 != c2 && c1 != null) {
			System.out.println(String.format("(c1)-%f-(c2) %s %s", current.getDistanceValue(), c1, c2));
			getClusters().remove(c2);
			c1.mergeWithOther(c2);
		} else {
			// nextIteration() until two clusters are merged or singularity is reached
			nextIteration();
		}		
		return true;
	}
	
	private Cluster<HierarchicalClustering> findContainingCluster(String sequence) {
		return getClusters().parallelStream()
			.filter(c -> c.containsSequence(sequence))
			.findFirst()
			.orElse(null);
	}
	
}
