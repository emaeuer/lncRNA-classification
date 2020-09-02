package de.lncrna.classification.util.stat;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.lncrna.classification.clustering.Cluster;
import de.lncrna.classification.util.PropertyHandler;
import de.lncrna.classification.util.PropertyKeyHelper.PropertyKeys;

public class StatLogger implements Closeable {

	private static final Logger LOG = Logger.getLogger("logger");
	
	// Merge: <Cluster-ID-1>-<Cluster-ID-2>-<Distance>-<Iteration-Counter>
	private static final String MERGE_ENTRY_PATTERN = "Merge: %s-%s-%f-%d";
	
	// Average_Distance: <Average-Cluster-Distance>-<Iteration-Counter>
	private static final String AVERAGE_CLUSTER_DISTANCE_ENTRY_PATTERN = "Average_Distance: %f-%d";

	private static final String MAX_CLUSTER_DIAMETER_ENTRY_PATTERN = "Max_Diameter: %f-%d";
	
	private static final String CLUSTER_ID_MAPPING_ENTRY_PATTERN = "Mapping: %d-%s";
	
	private PrintWriter printer;

	public StatLogger() {
		File logFile = PropertyHandler.HANDLER.getPropertyValue(PropertyKeys.STAT_LOG_FILE, File.class);
		try {
			this.printer = new PrintWriter(logFile);
		} catch (IOException e) {
			LOG.log(Level.WARNING, "Failed to create statistic logger", e);
		}
	}
	
	public void logMerge(Cluster<?> c1, Cluster<?> c2, float distance, int iterationCounter) {
		log(String.format(MERGE_ENTRY_PATTERN, c1.getClusterId(), c2.getClusterId(), distance, iterationCounter));
	}
	
	public void logAverageDistanceWithinCluster(double averageDistance, int iterationCounter) {
		log(String.format(AVERAGE_CLUSTER_DISTANCE_ENTRY_PATTERN, averageDistance, iterationCounter));
	}
	
	public void logMaxClusterDiameter(double averageClusterDiameter, int iterationCounter) {
		log(String.format(MAX_CLUSTER_DIAMETER_ENTRY_PATTERN, averageClusterDiameter, iterationCounter));
	}
	
	public void logClusterIdMapping(Cluster<?> cluster) {
		if (!cluster.getSequences().isEmpty()) {
			log(String.format(CLUSTER_ID_MAPPING_ENTRY_PATTERN, cluster.getClusterId(), cluster.getSequences().iterator().next()));
		}
	}
	
	private void log(String logMessage) {
		if (this.printer != null) {
			printer.println(logMessage);
			printer.flush();
		} else {
			LOG.log(Level.WARNING, "Failed to log statistic: " + logMessage);
		}
	}
	
	@Override
	public void close() throws IOException {
		this.printer.close();	
	}

	
}
