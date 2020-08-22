package de.lncrna.classification.clustering.algorithms.space;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Collectors;
import de.lncrna.classification.clustering.Cluster;
import de.lncrna.classification.clustering.algorithms.implementations.KMeansClustering;
import de.lncrna.classification.db.Neo4jDatabaseSingleton;
import de.lncrna.classification.distance.DistanceType;
import de.lncrna.classification.util.PropertyHandler;
import de.lncrna.classification.util.PropertyKeyHelper.PropertyKeys;

public class KMeansClusteringSpace extends AbstractClusteringSpace<KMeansClustering> {
        private List<String> currClusterCentroids;
        private List<String> prevClusterCentroids;
        private Map<String, List<String>> clusterAssignments;


        public KMeansClusteringSpace(DistanceType distanceProp) {
                super(distanceProp);

        }

        @Override
        protected void initSpace() {
                LOG.log(Level.INFO, "Starting K_Means clustering");
                LOG.log(Level.INFO, "Initializing clusters");

                this.currClusterCentroids = new ArrayList<>(Neo4jDatabaseSingleton.getQueryHelper()
                                .getRandomSequenceNames(PropertyHandler.HANDLER.getPropertyValue(
                                                PropertyKeys.MIN_AMOUNT_SEQUENCES_IN_CLUSTER,
                                                Integer.class),
                                                PropertyHandler.HANDLER.getPropertyValue(
                                                                PropertyKeys.CLUSTER_COUNT,
                                                                Integer.class)));
                this.prevClusterCentroids = new ArrayList<>();

                LOG.log(Level.INFO, "Finished initializing clustering space");

        }

        @Override
        public boolean nextIteration() {
                if (this.prevClusterCentroids.containsAll(this.currClusterCentroids)
                                || isMaxIteration()) {
                        this.clusterAssignments.entrySet().stream().forEach(entry -> {
                                addCluster(new Cluster<>(
                                                new KMeansClustering(getDistanceProperties()),
                                                entry.getValue(), entry.getKey()));
                        });
                        return false;
                }

                incrementIterationCounter();
                if (isTimeToRefresh()) {
                        double maxAverageClusterDistance = PropertyHandler.HANDLER.getPropertyValue(
                                        PropertyKeys.AVERAGE_CLUSTER_DISTANCE_THRESHOLD,
                                        Double.class);
                        double averageClusterDistance = calculateAverageClusterDistance();

                        if (averageClusterDistance >= maxAverageClusterDistance) {
                                return false;
                        }

                        LOG.log(Level.INFO, String.format("KMeans clustering: Iteration %d",
                                        getIterationCounter()));
                }

                this.clusterAssignments = Neo4jDatabaseSingleton.getQueryHelper()
                                .getSequencesWithinCluster(this.currClusterCentroids,
                                                this.getDistanceProperties().name());

                this.prevClusterCentroids = new ArrayList<>(this.currClusterCentroids);
                this.currClusterCentroids = this.currClusterCentroids.stream()
                                .map(centroid -> Neo4jDatabaseSingleton.getQueryHelper()
                                                .findClustroid(clusterAssignments.get(centroid),
                                                                getDistanceProperties().name()))
                                .collect(Collectors.toCollection(ArrayList::new));
                return true;
        }

        private boolean isMaxIteration() {
                return PropertyHandler.HANDLER.getPropertyValue(PropertyKeys.MAX_ITERATION,
                                Integer.class) <= getIterationCounter();
        }

        private boolean isTimeToRefresh() {
                return getIterationCounter() % PropertyHandler.HANDLER.getPropertyValue(
                                PropertyKeys.STAT_REFRESH_INTERVAL, Integer.class) == 0;
        }
}
