package de.lncrna.classification.clustering;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import de.lncrna.classification.clustering.algorithms.ClusteringAlgorithm;
import de.lncrna.classification.db.Neo4jDatabaseSingleton;

/**
 * Context of strategy pattern. Passes all the invocations directly to the 
 * concrete implementation
 * 
 * @author emaeu
 *
 * @param <T> The concrete clustering implementation
 */
public class Cluster<T extends ClusteringAlgorithm> {

	private static final AtomicInteger NEXT_CLUSTER_ID = new AtomicInteger(0);
	
	private final T algorithm;
	
	private String clustroid = null;
	
	public double averageDistanceWithin = -1;
	private double diameter = -1;
	
	public final int clusterId;
	
	public Cluster(final T algorithm, List<String> sequences, String clustroid) {
		this.clusterId = NEXT_CLUSTER_ID.getAndIncrement();
		this.algorithm = algorithm;
		this.algorithm.initCluster(sequences);
		this.clustroid = clustroid;
	}
	
	public Cluster(final T algorithm, List<String> sequences) {
		this.clusterId = NEXT_CLUSTER_ID.getAndIncrement();
		this.algorithm = algorithm;
		this.algorithm.initCluster(sequences);
	}
	
	public Cluster(final T algorithm, String sequence) {
		this(algorithm, Arrays.asList(sequence));
	}
	
	public void mergeWithOther(Cluster<T> other) {
		this.algorithm.mergeWithOther(other);
		markChanged();
	}
	
	public void addSequence(String data) {
		this.algorithm.addSequence(data);
		markChanged();
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
		markChanged();
	}
	
	private void markChanged() {
		this.averageDistanceWithin = -1;
		this.clustroid = null;
		this.diameter = -1;
	}
	
	public String getClustroid() {
		if (this.getSequences().size() == 1) {
			this.clustroid = this.getSequences().iterator().next();
		} else if (this.clustroid == null) {
			this.clustroid = Neo4jDatabaseSingleton.getQueryHelper().findClustroid(getSequences(), this.algorithm.getDistanceAlgortithm().name());
		} 
		return this.clustroid;
	}
	
	public double getAverageDistanceWithin() {
		if (this.averageDistanceWithin == -1) {
			this.averageDistanceWithin = Neo4jDatabaseSingleton.getQueryHelper().getAverageClusterDistance(getSequences(), this.algorithm.getDistanceAlgortithm().name());
			
		} 
		return this.averageDistanceWithin;
	}
	
	public double getDiameter() {
		if (getSequences().size() == 1) {
			return 0;
		}
		
		if (this.diameter == -1) {
			this.diameter = Neo4jDatabaseSingleton.getQueryHelper().getMaxDistanceWithinCluster(getSequences(), this.algorithm.getDistanceAlgortithm().name());
		} 
		return this.diameter;
	}
	
	public Map<String, Double> calculateSilhouettes() {
		return getSequences()
			.parallelStream()
			.collect(Collectors.toMap(s -> s, s -> calculateSilhoutteForSequence(s)));
	}
	
	private double calculateSilhoutteForSequence(String sequence) {
		if (getClusterSize() <= 1) {
			return 0;
		}
		
		double averageDistanceWithinCluster = Neo4jDatabaseSingleton.getQueryHelper().getAverageDistanceOfSequenceInCluster(sequence, getSequences(), this.algorithm.getDistanceAlgortithm().name());
		double distanceToNextCluster = Neo4jDatabaseSingleton.getQueryHelper().getAverageDistanceToNearestCluster(sequence, this.algorithm.getDistanceAlgortithm().name(), this.algorithm.getName());
		
		double result = (distanceToNextCluster - averageDistanceWithinCluster) / Math.max(averageDistanceWithinCluster, distanceToNextCluster);
		
		if (Double.isNaN(result)) {
			result = 0;
		}
		
		return result;
	}
	
	public int getClusterId() {
		return this.clusterId;
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
