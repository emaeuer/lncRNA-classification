package de.lncrna.classification.distance;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.lncrna.classification.clustering.algorithms.ImplementedClusteringAlgorithms;
import de.lncrna.classification.db.Neo4jDatabaseSingleton;
import de.lncrna.classification.util.PropertyHandler;
import de.lncrna.classification.util.PropertyKeyHelper.PropertyKeys;

public class DistanceCalculationInitializer {
	
	private static final Logger LOG = Logger.getLogger("logger");

	private final DistancePairSupplier supplier;
	
	public DistanceCalculationInitializer(Map<String, String> sequences, DistanceType distanceType, boolean useBlocking) {
		this.supplier = new DistancePairSupplier(sequences);
		
		initFlow(distanceType);
		
		if (useBlocking) {
			findAndCalculateBlocks();
		} else {
			calculateAll(sequences, distanceType);
		}
	}

	private void initFlow(DistanceType distanceType) {
		int threadNumber = PropertyHandler.HANDLER.getPropertyValue(PropertyKeys.DISTANCE_CALCULATION_THREAD_COUNT, Integer.class);
				
		for (int i = 0; i < threadNumber; i++) {
			DistanceCalculator calculator = DistanceCalculatorFactory.createDistanceCalculator(distanceType);
			calculator.subscribe(new DistancePairPersister());
			this.supplier.subscribe(calculator);
		}
	}
	
	private void findAndCalculateBlocks() {		
		Map<Long, List<String>> clusters = Neo4jDatabaseSingleton.getQueryHelper().getClusters(DistanceType.Blast_Distance.name(), ImplementedClusteringAlgorithms.Canopy.name());
		LOG.log(Level.INFO, String.format("Found %s clusters for blocking", clusters.size()));
		this.supplier.setBlockNumber(clusters.size());
		clusters.forEach((id, block) -> calculateBlock(id, block));
		terminateFlow();
	}
	


	private void calculateAll(Map<String, String> sequences, DistanceType distanceType) {		
		this.supplier.addCompareBlock(initAllNodes(sequences));
		terminateFlow();
	}
	
	private void terminateFlow() {
		try {
			this.supplier.close();
		} catch (Exception e) {
			LOG.log(Level.WARNING, "Unexpected error permitted closing the flow", e);
		}		
	}


	private List<String> initAllNodes(Map<String, String> sequences) {
		List<String> allSequences = new ArrayList<>(sequences.keySet());
		
		Neo4jDatabaseSingleton.getQueryHelper().insertAllSequences(allSequences);
		
		LOG.log(Level.INFO, "Inserted " + allSequences.size() + " sequence nodes");
		return allSequences;
	}


	private void calculateBlock(long id, List<String> block) {
		this.supplier.addCompareBlock(block);
		// TODO Cluster was just supplied for calculation but is not complete
//		Neo4jDatabaseSingleton.getQueryHelper().setClusterPersisted(id);
	}
	
}
