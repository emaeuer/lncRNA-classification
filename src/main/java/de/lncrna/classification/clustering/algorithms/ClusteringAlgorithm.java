package de.lncrna.classification.clustering.algorithms;

import java.util.Collection;
import java.util.List;

import de.lncrna.classification.clustering.Cluster;
import de.lncrna.classification.init.distance.DistanceProperties;

/**
 * Strategy of the strategy pattern
 * 
 * @author emaeu
 *
 */
public interface ClusteringAlgorithm {
	
	public void initCluster(List<String> sequences);
	
	public void mergeWithOther(Cluster<?> cluster);
	
	public void addSequence(String data);
	
	public Collection<String> getSequences(); 
	
	public DistanceProperties getDistanceAlgortithm();
	
	public String getName();
	
}
