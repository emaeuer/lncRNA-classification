package de.lncrna.classification.clustering;

import java.util.Arrays;
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
	
	public Cluster(final T algorithm, String sequence) {
		this(algorithm, Arrays.asList(sequence));
	}
	
	public void mergeWithOther(Cluster<T> other) {
		this.algorithm.mergeWithOther(other);
	}
	
	public void addSequence(String data) {
		this.algorithm.addSequence(data);
	}

	public int getClusterSize() {
		return this.algorithm.getSequences().size();
	}
	
	public boolean containsSequence(String sequence) {
		return this.algorithm.getSequences().contains(sequence);
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
