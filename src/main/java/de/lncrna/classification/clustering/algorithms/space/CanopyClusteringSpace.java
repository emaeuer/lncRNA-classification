package de.lncrna.classification.clustering.algorithms.space;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;

import de.lncrna.classification.clustering.Cluster;
import de.lncrna.classification.clustering.algorithms.implementations.CanopyClustering;
import de.lncrna.classification.db.Neo4jDatabaseSingleton;
import de.lncrna.classification.init.distance.DistanceProperties;

public class CanopyClusteringSpace extends AbstractClusteringSpace<CanopyClustering> {

	private List<String> candidates;
	
	private final float looseTreshold;
	private final float tightTreshold;
	
	public CanopyClusteringSpace(DistanceProperties distanceProp, float looseTreshold, float tightTreshold) {
		super(distanceProp);
		this.looseTreshold = looseTreshold;
		this.tightTreshold = tightTreshold;
	}

	@Override
	protected void initSpace() {
		LOG.log(Level.INFO, "Starting canopy clustering ");
		LOG.log(Level.INFO, "Initializing clusters");
		
		this.candidates = new ArrayList<>(Neo4jDatabaseSingleton.getQueryHelper().getAllSequenceNames());
	}

	@Override
	public boolean nextIteration() {
		if (this.candidates.isEmpty()) {
			return false;
		}
		
		// randomly choose a sequence from the 
		String center = this.candidates.remove(new Random().nextInt(this.candidates.size()));
		
		List<String> sequencesOfCluster = new ArrayList<>();
		sequencesOfCluster.add(center);
		
		Map<Boolean, List<String>> sequencesToAdd = 
				Neo4jDatabaseSingleton.getQueryHelper().getSequencesWithinTresholds(center, this.tightTreshold, this.looseTreshold, getDistanceProperties().name());
		sequencesOfCluster.addAll(sequencesToAdd.getOrDefault(true, Collections.emptyList()));
		sequencesOfCluster.addAll(sequencesToAdd.getOrDefault(false, Collections.emptyList()));
		
		candidates.removeAll(sequencesToAdd.getOrDefault(true, Collections.emptyList()));
		
		addCluster(new Cluster<>(new CanopyClustering(getDistanceProperties()), sequencesOfCluster));
		return true;
	}

}
