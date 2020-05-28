package de.lncrna.classification.clustering;

import java.util.Collection;
import java.util.List;

import de.lncrna.classification.clustering.algorithms.ClusteringAlgorithm;

/**
 * Context of strategy pattern. Passes all the invocations directly to the 
 * concrete implementation
 * 
 * @author emaeu
 *
 * @param <T> The concrete clustering implementation
 */
public class Cluster<T extends ClusteringAlgorithm> {

	private final T algorithm;
	
	public Cluster(final T algorithm, List<String> sequences) {
		this.algorithm = algorithm;
		this.algorithm.initCluster(sequences);
	}
	
	public void mergeWithOther(Cluster<T> other) {
		this.algorithm.mergeWithOther(other);
	}
	
	public void addLncRNA(String data) {
		this.algorithm.addSequence(data);
	}

	public int getClusterSize() {
		return this.algorithm.getSequences().size();
	}
	
	public boolean containsSequence(String sequence) {
		return this.algorithm.getSequences().contains(sequence);
	}
	
	public double distanceTo(Cluster<T> other) {
		return this.algorithm.distanceTo(other);
	}
	
	public T getAlgorithm() {
		return this.algorithm;
	}
	
	public Collection<String> getSequences() {
		return this.algorithm.getSequences();
	}
	
	public void clear() {
		this.getAlgorithm().getSequences().clear();
	}
	
	public double calcualteAverageClusterDistance() {
		long distanceSum = 0;
		for (int i = 0; i < getClusterSize(); i++) {
			for (int j = i + 1; j < getClusterSize(); j++) {
				
			}		
		}
		return distanceSum / getClusterSize();
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder("Cluster[");
		getAlgorithm().getSequences()
			.forEach(sequence -> builder.append(sequence + ", "));
		builder.replace(builder.length() - 2, builder.length(), "");
		builder.append("]");
		return builder.toString();
	}
	
}
