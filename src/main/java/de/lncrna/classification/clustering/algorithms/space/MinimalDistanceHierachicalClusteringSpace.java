package de.lncrna.classification.clustering.algorithms.space;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

import de.lncrna.classification.clustering.Cluster;
import de.lncrna.classification.clustering.algorithms.implementations.HierarchicalClusteringMinimalDistance;
import de.lncrna.classification.db.Neo4JCypherQueries;
import de.lncrna.classification.init.distance.DistanceProperties;
import de.lncrna.classification.util.data.DistanceDAO;

public class MinimalDistanceHierachicalClusteringSpace extends AbstractClusteringSpace<HierarchicalClusteringMinimalDistance> {
	
	private LinkedList<DistanceDAO> orderedDistances;
	
	public MinimalDistanceHierachicalClusteringSpace(DistanceProperties distanceProp) {
		super(distanceProp);
	}

	@Override
	protected void initSpace() {
		LOG.log(Level.INFO, "Starting hierarchical clustering with minimal distance");
		LOG.log(Level.INFO, "Initializing clusters");
		
		List<String> sequenceNames = Neo4JCypherQueries.getAllSequenceNames();
		
		sequenceNames.parallelStream()
			.map(sequence -> new Cluster<>(new HierarchicalClusteringMinimalDistance(), Arrays.asList(sequence)))
			.forEach(cluster -> getClusters().add(cluster));		
		
		LOG.log(Level.INFO, "Pairwise comparison of all points");
		// Data is automatically sorted in PriorityQueue
		this.orderedDistances = Neo4JCypherQueries.getDistancesOrdered(getDistanceProperties().name(), 10000);
	}

	@Override
	public void nextIteration() {
		if (getClusters().size() == 1) {
			return;
		}
		
		DistanceDAO current = this.orderedDistances.poll();
		
		Cluster<HierarchicalClusteringMinimalDistance> c1 = findContainingCluster(current.getSeq1());
		Cluster<HierarchicalClusteringMinimalDistance> c2 = findContainingCluster(current.getSeq2());
		
		if (c1 != c2) {
			getClusters().remove(c2);
			c1.mergeWithOther(c2);
		} else {
			// nextIteration() until two clusters are merged or singularity is reached
			nextIteration();
		}		
	}
	
	private Cluster<HierarchicalClusteringMinimalDistance> findContainingCluster(String sequence) {
		return getClusters().parallelStream()
			.filter(c -> c.containsSequence(sequence))
			.findFirst()
			.orElse(null);
	}
	
}
