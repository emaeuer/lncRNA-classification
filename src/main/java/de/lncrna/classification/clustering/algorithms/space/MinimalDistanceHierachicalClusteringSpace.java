package de.lncrna.classification.clustering.algorithms.space;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.logging.Level;

import com.google.common.collect.Table.Cell;

import de.lncrna.classification.clustering.Cluster;
import de.lncrna.classification.clustering.algorithms.implementations.HierarchicalClusteringMinimalDistance;
import de.lncrna.classification.util.data.DistanceMatrix;

public class MinimalDistanceHierachicalClusteringSpace extends AbstractClusteringSpace<HierarchicalClusteringMinimalDistance> {

	private static final Comparator<Cell<String, String, Float>> CELL_COMPARATOR = 
			(Cell<String, String, Float> c1, Cell<String, String, Float> c2) -> Float.compare(c1.getValue(), c2.getValue());
	
	private PriorityQueue<Cell<String, String, Float>> orderedDistances;
	
	public MinimalDistanceHierachicalClusteringSpace(DistanceMatrix distances) {
		super(distances);
	}

	@Override
	protected void initSpace(DistanceMatrix distances) {
		LOG.log(Level.INFO, "Starting hierarchical clustering with minimal distance");
		LOG.log(Level.INFO, "Initializing clusters");
		
		List<String> sequenceNames = distances.getSequenceNames();
		
		sequenceNames.parallelStream()
			.map(sequence -> new Cluster<>(new HierarchicalClusteringMinimalDistance(), Arrays.asList(sequence)))
			.forEach(cluster -> getClusters().add(cluster));		
		
		LOG.log(Level.INFO, "Pairwise comparison of all points");
		// Data is automatically sorted in PriorityQueue
		this.orderedDistances = new PriorityQueue<>(CELL_COMPARATOR);
		this.orderedDistances.addAll(distances.getAllCells());
	}

	@Override
	public void nextIteration() {
		if (getClusters().size() == 1) {
			return;
		}
		
		Cell<String, String, Float> current = this.orderedDistances.poll();
		
		Cluster<HierarchicalClusteringMinimalDistance> c1 = findContainingCluster(current.getRowKey());
		Cluster<HierarchicalClusteringMinimalDistance> c2 = findContainingCluster(current.getColumnKey());
		
		if (c1 != c2) {
			System.out.println(c1 + " " + c2);
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
