package de.lncrna.classification.clustering.algorithms.space;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;

import de.lncrna.classification.clustering.Cluster;
import de.lncrna.classification.clustering.algorithms.ImplementedClusteringAlgorithms;
import de.lncrna.classification.clustering.algorithms.implementations.KMeansClustering;
import de.lncrna.classification.db.Neo4jDatabaseSingleton;
import de.lncrna.classification.distance.DistanceType;
import de.lncrna.classification.util.PropertyHandler;
import de.lncrna.classification.util.PropertyKeyHelper.PropertyKeys;

public class KMeansClusteringSpace extends AbstractClusteringSpace<KMeansClustering> {
	
	private Set<String> prevClusterCentroids;
	
	private List<String> currClusterCentroids;
	
	private int retryCounter;
	
	private List<Cluster<KMeansClustering>> bestClusterConfiguration;
	
	private double bestSilhouetteScore = Double.MIN_VALUE;

	public KMeansClusteringSpace(DistanceType distanceProp, Boolean initFromDB) {
		super(distanceProp, initFromDB);
	}

	@Override
    protected void initSpace() {
		if (this.retryCounter == 0) {
			this.retryCounter = 1;
		} else {
			this.retryCounter++;
		}

		
        LOG.log(Level.INFO, "Starting K_Means clustering. Retry number: " + this.retryCounter);
        LOG.log(Level.INFO, "Initializing clusters");

        this.currClusterCentroids = new ArrayList<>(Neo4jDatabaseSingleton.getQueryHelper()
                        .getRandomSequenceNames(PropertyHandler.HANDLER.getPropertyValue(
                                        PropertyKeys.KMEANS_MIN_NUMBER_OF_DISTANCES,
                                        Integer.class),
                                        PropertyHandler.HANDLER.getPropertyValue(
                                                        PropertyKeys.KMEANS_CLUSTER_COUNT,
                                                        Integer.class)));

        
        resetIterationCounter();
        getClusters().clear();
        this.prevClusterCentroids = new HashSet<>();
        
        LOG.log(Level.INFO, "Finished initializing clustering space");

    }

	@Override
	public boolean nextIteration() {		
		String currentClustroidsString = createCurrentClustroidsString();
		
		if (this.prevClusterCentroids.contains(currentClustroidsString) || isMaxIteration()) {
			int maxRetryNumber = PropertyHandler.HANDLER.getPropertyValue(PropertyKeys.KMEANS_NUMBER_OF_RETRIES, Integer.class);
			
			if (this.retryCounter > maxRetryNumber) {
				getClusters().clear();
				getClusters().addAll(this.bestClusterConfiguration);
				return false;
			}
			
			checkIfCurrentClusterIsBetter();
			initSpace();
			currentClustroidsString = createCurrentClustroidsString();
		}
		
		refreshClusters();
		
		this.prevClusterCentroids.add(currentClustroidsString);
		this.currClusterCentroids = getClusters().parallelStream()
				.map(Cluster::getClustroid)
				.collect(Collectors.toList());
		
		incrementIterationCounter();
		if (isTimeToRefresh()) {
			double maxAverageClusterDistance = PropertyHandler.HANDLER
					.getPropertyValue(PropertyKeys.KMEANS_AVERAGE_CLUSTER_DISTANCE_THRESHOLD, Double.class);
			double averageClusterDistance = calculateAverageDistanceWithinCluster();

			double maxClusterDiameter = calculateMaxClusterDiameter();
			
			if (averageClusterDistance >= maxAverageClusterDistance) {
				return false;
			}
			
			getStatLogger().logAverageDistanceWithinCluster(averageClusterDistance, getIterationCounter(), this.retryCounter);
			getStatLogger().logMaxClusterDiameter(maxClusterDiameter, getIterationCounter(), this.retryCounter);

			LOG.log(Level.INFO, String.format("K_Means clustering: Iteration %d (Retry %d); current number of clusters %d with average cluster distance of %f and a maximal diameter of %f", getIterationCounter(), this.retryCounter, getClusters().size(), averageClusterDistance, maxClusterDiameter));
		}

		return true;
	}
	
	private void checkIfCurrentClusterIsBetter() {
		persistClusterInformation();
		
		double currentSilhouetteScore = calculateSilhouettes().values()
			.stream()
			.flatMapToDouble(m -> m.values().stream().mapToDouble(d -> d))
			.average()
			.orElse(Double.MIN_VALUE);
		
		if (currentSilhouetteScore > this.bestSilhouetteScore) {
			LOG.log(Level.INFO, String.format("Found new best cluster configurtation in retry %d with silhouette coefficient of %f", this.retryCounter, currentSilhouetteScore));
			getStatLogger().logCurrentBestRetry(this.retryCounter, currentSilhouetteScore);
			this.bestClusterConfiguration = getClusters();
			this.bestSilhouetteScore = currentSilhouetteScore;
		}
			
	}

	private void refreshClusters() {
		Map<String, List<String>> clusterAssignments = Neo4jDatabaseSingleton.getQueryHelper()
				.getSequencesWithinCluster(this.currClusterCentroids, this.getDistanceProperties().name());
		
		// Clustroid is missing if it is the only sequence of this cluster
		this.currClusterCentroids.forEach(s -> clusterAssignments.putIfAbsent(s, new ArrayList<>()));
		
		// Add the clustroid to the cluster
		clusterAssignments.entrySet()
			.forEach(e -> e.getValue().add(e.getKey()));
		
		getClusters().clear();
		clusterAssignments.values()
			.forEach(c -> getClusters().add(new Cluster<>(new KMeansClustering(getDistanceProperties()), c)));
	}

	private String createCurrentClustroidsString() {
		StringBuilder builder = new StringBuilder();
		
		this.currClusterCentroids.stream()
			.sorted()
			.forEach(builder::append);
		
		return builder.toString();
	}

	private boolean isMaxIteration() {
		return PropertyHandler.HANDLER.getPropertyValue(PropertyKeys.KMEANS_MAX_ITERATION,
				Integer.class) <= getIterationCounter();
	}

	private boolean isTimeToRefresh() {
		return getIterationCounter() % PropertyHandler.HANDLER.getPropertyValue(PropertyKeys.STAT_REFRESH_INTERVAL, Integer.class) == 0;
	}

	@Override
	protected void initFromDB() {
		Map<Long, List<String>> clusters = Neo4jDatabaseSingleton.getQueryHelper()
				.getClusters(getDistanceProperties().name(), ImplementedClusteringAlgorithms.K_Means.name());

		clusters.entrySet().stream()
				.map(c -> new Cluster<KMeansClustering>(new KMeansClustering(getDistanceProperties()), c.getValue()))
				.forEach(this::addCluster);
	}
}
