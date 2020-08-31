package de.lncrna.classification.cli;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.lncrna.classification.clustering.algorithms.ImplementedClusteringAlgorithms;
import de.lncrna.classification.clustering.algorithms.space.AbstractClusteringSpace;
import de.lncrna.classification.clustering.algorithms.space.ClusterSpaceFactory;
import de.lncrna.classification.db.Neo4jDatabaseSingleton;
import de.lncrna.classification.distance.DistanceType;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "score",
		headerHeading = "@|bold,underline Usage|@:%n%n",
		synopsisHeading = "%n",
		descriptionHeading = "%n@|bold,underline Description|@:%n%n",
		optionListHeading = "%n@|bold,underline Options|@:%n",
		header = "Calculate scores for existing clusters",
		description = "Calculates scores for the current state of the clusters",
		separator = " ")
public class ScoreCommand implements Runnable {

	private static final Logger LOG = Logger.getLogger("logger");
	
	@Option(names = {"-a", "--algorithm"}, required = true, description = "Choose a clustering algorithm. Possible values: ${COMPLETION-CANDIDATES}") 
	private ImplementedClusteringAlgorithms algorithm;
	
	@Option(names = {"-d", "--distanceAlgorithm"}, required = true, description = "Choose a distance measure. Possible values: ${COMPLETION-CANDIDATES}") 
	private DistanceType distanceType;
	
	@Option(names = {"-f", "--outputFile"}, required = true, description = "Destination file for silhouette scores") 
	private File outputFile;
	
	@Override
	public void run() {	
		
		LOG.log(Level.INFO, "Loading clusters from db");
		Neo4jDatabaseSingleton.initInstance(true);
		
		AbstractClusteringSpace<?> space = ClusterSpaceFactory.loadClusterSpaceFromDB(this.algorithm, this.distanceType);
		LOG.log(Level.INFO, "Succesfully loaded clusters from db");
		
		LOG.log(Level.INFO, "Starting silhouette score calculation");
		Map<Integer, Map<String, Double>> silhouettes = space.calculateSilhouettes();
		LOG.log(Level.INFO, "Finished silhouette score calculation");
		
		LOG.log(Level.INFO, "Persisting scores");
		printSilhouettesToFile(silhouettes);
		LOG.log(Level.INFO, "Successfully persisted scores");
		
	}

	private void printSilhouettesToFile(Map<Integer, Map<String, Double>> silhouettes) {
		try (PrintWriter printer = new PrintWriter(this.outputFile)) {
			silhouettes.entrySet()
				.stream()
				.peek(c -> System.out.println(c.getValue()))
				.peek(c -> printer.append("Cluster-" + c.getKey()))
				.map(e -> e.getValue())
				.peek(m -> m.values()
					.stream()
					.sorted()
					.forEach(sil -> printer.append(", " + sil)))
				.peek(c -> printer.append("\n"))
				.forEach(c -> printer.flush());
		} catch (FileNotFoundException e) {
			LOG.log(Level.WARNING, "Failed to write silhouette scores to file", e);
		}
		
	}
	
	
}
