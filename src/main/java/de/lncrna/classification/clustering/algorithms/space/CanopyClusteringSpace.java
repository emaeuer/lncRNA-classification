package de.lncrna.classification.clustering.algorithms.space;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;

import de.lncrna.classification.clustering.Cluster;
import de.lncrna.classification.clustering.algorithms.implementations.CanopyClustering;
import de.lncrna.classification.db.Neo4jDatabaseSingleton;
import de.lncrna.classification.distance.DistanceType;
import de.lncrna.classification.util.PropertyHandler;
import de.lncrna.classification.util.PropertyKeyHelper.PropertyKeys;

public class CanopyClusteringSpace extends AbstractClusteringSpace<CanopyClustering> {

	private List<String> candidates;
	
	private final float looseTreshold;
	private final float tightTreshold;
	
	public CanopyClusteringSpace(DistanceType distanceProp, float looseTreshold, float tightTreshold, boolean initFromDB) {
		super(distanceProp, initFromDB);
		this.looseTreshold = looseTreshold;
		this.tightTreshold = tightTreshold;
	}

	@Override
	protected void initSpace() {
		LOG.log(Level.INFO, "Starting canopy clustering ");
		LOG.log(Level.INFO, "Initializing clustering space (loading all seqeunces)");
		
		this.candidates = new ArrayList<>(Neo4jDatabaseSingleton.getQueryHelper().getAllSequenceNames());
		
		LOG.log(Level.INFO, "Finished initializing clustering space");
	}
	
	@Override
	protected void initFromDB() {
		Map<Long, List<String>> clusters = Neo4jDatabaseSingleton.getQueryHelper().getClusters(getAlgorithm().getDistanceAlgortithm().name(), getAlgorithm().getName());
		
		clusters.entrySet()
			.stream()
			.map(c -> new Cluster<CanopyClustering>(getAlgorithm(), c.getValue()))
			.forEach(this::addCluster);;
		
	}

	@Override
	public boolean nextIteration() {
		if (this.candidates.isEmpty()) {
			return false;
		}
		
		incrementIterationCounter();
		
		int refreshInterval = PropertyHandler.HANDLER.getPropertyValue(PropertyKeys.STAT_REFRESH_INTERVAL, Integer.class);
		if (getIterationCounter() % refreshInterval == 0) {
			LOG.log(Level.INFO, String.format("Canopy clustering: Iteration %d; %d candidates remaining", getIterationCounter(), this.candidates.size()));
		}
		
		// randomly choose a sequence from the 
		String center = this.candidates.remove(new Random().nextInt(this.candidates.size()));
		
		Set<String> sequencesOfCluster = new HashSet<>();
		sequencesOfCluster.add(center);
		
		Map<Boolean, List<String>> sequencesToAdd = 
				Neo4jDatabaseSingleton.getQueryHelper().getSequencesWithinTresholds(center, this.tightTreshold, this.looseTreshold, getDistanceProperties().name());
		
		filterSequencesToAddForCandidates(sequencesToAdd);
		
		sequencesOfCluster.addAll(sequencesToAdd.getOrDefault(true, Collections.emptyList()));
		sequencesOfCluster.addAll(sequencesToAdd.getOrDefault(false, Collections.emptyList()));
		
		candidates.removeAll(sequencesToAdd.getOrDefault(true, Collections.emptyList()));
		
		if (center == null) {
			System.out.println("hier");
		}
		
		addCluster(new Cluster<>(new CanopyClustering(getDistanceProperties()), new ArrayList<>(sequencesOfCluster), center));
		return true;
	}

	private void filterSequencesToAddForCandidates(Map<Boolean, List<String>> sequencesToAdd) {
		sequencesToAdd.get(true).removeIf(s -> !this.candidates.contains(s));		
		sequencesToAdd.get(false).removeIf(s -> !this.candidates.contains(s));		
	}

	@Override
	public void addCluster(Cluster<CanopyClustering> newCluster) {		
		for (Cluster<CanopyClustering> cluster : getClusters()) {
			if (cluster.getClusterSize() > newCluster.getClusterSize()) {
				if (cluster.getSequences().containsAll(newCluster.getSequences())) {
					return; // cluster with all those sequences already exists
				}
			} else {
				if (newCluster.getSequences().containsAll(cluster.getSequences())) {
					getClusters().remove(cluster); // new cluster contains all elements of the current one
					break;
				}
			}
		}
		
		super.addCluster(newCluster);
	}

}
