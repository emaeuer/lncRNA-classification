package de.lncrna.classification.clustering.algorithms.implementations;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.NotImplementedException;

import de.lncrna.classification.clustering.Cluster;
import de.lncrna.classification.clustering.algorithms.ClusteringAlgorithm;

public class CanopyClustering implements ClusteringAlgorithm  {

	private final List<String> sequences = new ArrayList<>();

	@Override
	public void initCluster(List<String> sequences) {
		this.sequences.addAll(sequences);
	}

	@Override
	public void mergeWithOther(Cluster<?> cluster) {
		throw new NotImplementedException("This clustering algorithm doesn't support merging");		
	}

	@Override
	public void addSequence(String sequence) {
		this.sequences.add(sequence);
	}

	@Override
	public Collection<String> getSequences() {
		return this.sequences;
	}

}
