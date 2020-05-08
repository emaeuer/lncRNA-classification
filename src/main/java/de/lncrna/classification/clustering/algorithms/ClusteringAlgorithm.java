package de.lncrna.classification.clustering.algorithms;

import java.util.Collection;
import java.util.List;

import org.biojava.nbio.core.sequence.RNASequence;

import de.lncrna.classification.clustering.Cluster;

/**
 * Strategy of the strategy pattern
 * 
 * @author emaeu
 *
 */
public interface ClusteringAlgorithm {
	
	public double distanceTo(Cluster<?> cluster);
	
	public void initCluster(List<RNASequence> sequences);
	
	public void mergeWithOther(Cluster<?> cluster);
	
	public void addSequence(RNASequence data);
	
	public Collection<RNASequence> getSequences(); 
	
}
