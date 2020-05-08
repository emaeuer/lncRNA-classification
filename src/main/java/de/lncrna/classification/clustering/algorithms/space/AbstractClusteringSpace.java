package de.lncrna.classification.clustering.algorithms.space;

import java.util.ArrayList;
import java.util.List;

import org.biojava.nbio.core.sequence.RNASequence;

import de.lncrna.classification.clustering.Cluster;
import de.lncrna.classification.clustering.algorithms.ClusteringAlgorithm;

public abstract class AbstractClusteringSpace<T extends ClusteringAlgorithm> {

	private final List<Cluster<T>> clusters = new ArrayList<>();
	
	private T algorithm;
	
	public AbstractClusteringSpace(List<RNASequence> data) {
		initSpace(data);
	}

	protected abstract void initSpace(List<RNASequence> data);
	
	public abstract void nextIteration();

	public List<Cluster<T>> getClusters() {
		return clusters;
	}

	public T getAlgorithm() {
		return algorithm;
	}

	public void setAlgorithm(T algorithm) {
		this.algorithm = algorithm;
	}
	
}
