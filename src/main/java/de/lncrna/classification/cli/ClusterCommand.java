package de.lncrna.classification.cli;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.lncrna.classification.clustering.algorithms.ImplementedClusteringAlgorithms;
import de.lncrna.classification.clustering.algorithms.space.AbstractClusteringSpace;
import de.lncrna.classification.clustering.algorithms.space.ClusterSpaceFactory;
import de.lncrna.classification.db.Neo4jDatabaseSingleton;
import de.lncrna.classification.distance.DistanceType;
import de.lncrna.classification.util.PropertyKeyHelper;
import de.lncrna.classification.util.PropertyKeyHelper.PropertyKeys;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "cluster",
	headerHeading = "@|bold,underline Usage|@:%n%n",
	synopsisHeading = "%n",
	descriptionHeading = "%n@|bold,underline Description|@:%n%n",
	optionListHeading = "%n@|bold,underline Options|@:%n",
	header = "Cluster lnc rna sequences",
	description = "Clusters all sequences contained in the distance matrix",
	separator = " ")
public class ClusterCommand implements Runnable {

	private static final Logger LOG = Logger.getLogger("logger");
	
	private static class CanopyClusteringParameters {
		
		@Option(names = {"-l", "--looseBound"}, defaultValue = "-1", description = "Sequences within the loose bound are added to the cluster without removing it from the candidate list") 
		private float looseThreshold;
		
		@Option(names = {"-t", "--tightBound"}, defaultValue = "-1", description = "Sequences within the thight bound are added to the cluster and will be removed from the candidate list") 
		private float tightThreshold;
		
	}
	
	private static class HierarchicalClusteringParameters {
		
		@Option(names = {"-n", "--clusterCount"}, defaultValue = "1", description = "Hierarchical clustering stops when this number is reached.") 
		private int maxClusterCount;
		
		@Option(names = {"-ha", "--hMaxAverageClusterDistance"}, defaultValue = "1", description = "Clustering stops when maximal average cluster distance is reached") 
		private double maxAverageClusterDistance;
		
	}
	
	private static class KMeansClusteringParameters {
		
		@Option(names = {"-k", "--kClusters"}, defaultValue = "1", description = "Number of clusters for k-means.") 
		private int clusterCount;
		
		@Option(names = {"-ka", "--kMaxAverageClusterDistance"}, defaultValue = "1", description = "Clustering stops when maximal average cluster distance is reached") 
		private double maxAverageClusterDistance;
		
	}
	
	private static class ClusteringSpecificParameters {
		
		@ArgGroup(exclusive = false)
		private CanopyClusteringParameters canopy;
		
		@ArgGroup(exclusive = false)
		private HierarchicalClusteringParameters hierarchical;
		
		@ArgGroup(exclusive = false)
		private KMeansClusteringParameters kmeans;
		
	}
	
	@ArgGroup(exclusive = true)
	private ClusteringSpecificParameters parameters;
	
	@Option(names = {"-a", "--algorithm"}, required = true, description = "Choose a clustering algorithm. Possible values: ${COMPLETION-CANDIDATES}") 
	private ImplementedClusteringAlgorithms algorithm;
	
	@Option(names = {"-d", "--distanceAlgorithm"}, required = true, description = "Choose a distance measure. Possible values: ${COMPLETION-CANDIDATES}") 
	private DistanceType distanceProp;
	
	@Option(names = {"-e", "--embedded"}, defaultValue = "false", description = "Use an embedded neo4j instance") 
	private boolean embeddedMode;
	
	@Option(names = {"-s", "--statRefreshInterval"}, defaultValue = "-1", description = "Number of iterations between refreshs of the cluster statistics") 
	private int statRefreshInterval;
	
	@Option(names = {"-sl", "--statisticsLogFile"}, description = "Destination file for statistic log") 
	private File statisticsLogFile;
	
	@Override
	public void run() {		
		PropertyKeyHelper.setGlobalPrefix(distanceProp.name());
		
		refreshAlgorithmSpecificParameters();
		
		CLIHelper.refreshIfNecessary(PropertyKeys.STAT_REFRESH_INTERVAL, statRefreshInterval);
		CLIHelper.refreshIfNecessary(PropertyKeys.STAT_LOG_FILE, statisticsLogFile);
		
		Neo4jDatabaseSingleton.initInstance(embeddedMode);
		
		AbstractClusteringSpace<?> space = ClusterSpaceFactory.createClusterSpace(algorithm.getImplementationType(), distanceProp);
		
		while (space.nextIteration());
		
		LOG.log(Level.INFO, String.format("%s clustering completed created %d clusters", algorithm.name(), space.getNumberOfClusters()));
		
		LOG.log(Level.INFO, "Started persiting clusters");
		space.persistClusterInformation();
		LOG.log(Level.INFO, "Finished persiting clusters");
		
	}

	private void refreshAlgorithmSpecificParameters() {
		if (parameters != null && parameters.canopy != null) {
			CLIHelper.refreshIfNecessary(PropertyKeys.LOOSE_THRESHOLD, parameters.canopy.looseThreshold);
			CLIHelper.refreshIfNecessary(PropertyKeys.TIGHT_THRESHOLD, parameters.canopy.tightThreshold);
		}
		
		if (parameters != null && parameters.kmeans != null) {
			CLIHelper.refreshIfNecessary(PropertyKeys.KMEANS_AVERAGE_CLUSTER_DISTANCE_THRESHOLD, parameters.kmeans.maxAverageClusterDistance);
			CLIHelper.refreshIfNecessary(PropertyKeys.KMEANS_CLUSTER_COUNT, parameters.kmeans.clusterCount);
		}
		
		if (parameters != null && parameters.hierarchical != null) {
			CLIHelper.refreshIfNecessary(PropertyKeys.HIERARCHICAL_AVERAGE_CLUSTER_DISTANCE_THRESHOLD, parameters.hierarchical.maxAverageClusterDistance);
			CLIHelper.refreshIfNecessary(PropertyKeys.HIERARCHICAL_CLUSTER_COUNT, parameters.hierarchical.maxClusterCount);
		}
	}

}
