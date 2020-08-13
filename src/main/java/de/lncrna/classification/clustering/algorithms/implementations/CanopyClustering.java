package de.lncrna.classification.clustering.algorithms.implementations;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.lncrna.classification.clustering.Cluster;
import de.lncrna.classification.clustering.algorithms.ClusteringAlgorithm;
import de.lncrna.classification.clustering.algorithms.ImplementedClusteringAlgorithms;
import de.lncrna.classification.distance.DistanceType;

public class CanopyClustering implements ClusteringAlgorithm  {

	private final Set<String> sequences = new HashSet<>();
	
	private final DistanceType distanceProperty;

	public CanopyClustering(DistanceType distanceProperty) {
		this.distanceProperty = distanceProperty;
	}

	@Override
	public void initCluster(List<String> sequences) {
		this.sequences.addAll(sequences);
	}

	@Override
	public void mergeWithOther(Cluster<?> cluster) {
		this.sequences.addAll(cluster.getSequences());
	}

	@Override
	public void addSequence(String sequence) {
		this.sequences.add(sequence);
	}

	@Override
	public Collection<String> getSequences() {
		return this.sequences;
	}

	@Override
	public DistanceType getDistanceAlgortithm() {
		return this.distanceProperty;
	}

	@Override
	public String getName() {
		return ImplementedClusteringAlgorithms.Canopy.name();
	}

}
