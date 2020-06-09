package de.lncrna.classification.cli;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.lncrna.classification.clustering.algorithms.ImplementedClusteringAlgorithms;
import de.lncrna.classification.clustering.algorithms.space.AbstractClusteringSpace;
import de.lncrna.classification.clustering.algorithms.space.ClusterSpaceFactory;
import de.lncrna.classification.init.distance.DistanceProperties;
import de.lncrna.classification.util.PropertyKeyHelper;
import de.lncrna.classification.util.data.DistanceMatrix;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "cluster",
	headerHeading = "@|bold,underline Usage|@:%n%n",
	synopsisHeading = "%n",
	descriptionHeading = "%n@|bold,underline Description|@:%n%n",
	optionListHeading = "%n@|bold,underline Options|@:%n",
	header = "CLuster lnc rna sequences",
	description = "Clusters all sequences contained in the distance matrix",
	separator = " ")
public class ClusterCommand implements Runnable {

	private static final Logger LOG = Logger.getLogger("logger");
	
	@Option(names = {"-a", "--algorithm"}, required = true, description = "Choose a clustering algorithm. Possible values: ${COMPLETION-CANDIDATES}") 
	private ImplementedClusteringAlgorithms algorithm;
	
	@Option(names = {"-d", "--distanceAlgorithm"}, required = true, description = "Choose a distance measure. Possible values: ${COMPLETION-CANDIDATES}") 
	private DistanceProperties distanceProp;

	@Option(names = {"-n", "--clusterCount"}, defaultValue = "1", description = "Number of clusters for k-means. Hierarchical clustering stops when this number is reached.") 
	private int maxClusterCount;
	
	@Option(names = {"-c", "--maxAverageClusterDistance"}, defaultValue = "1", description = "Clustering stops when maximal average cluster distance is reached") 
	private double maxAverageClusterDistance;
	
	@Override
	public void run() {
		PropertyKeyHelper.setGlobalPrefix(distanceProp.name());
		
		DistanceMatrix matrix = null;
		double averageClusterDistance = 0;
		
		try {
			matrix = DistanceMatrix.initFromCSV();
		} catch (IOException e) {
			LOG.log(Level.WARNING, "Failed to load distance matrix", e);
			System.exit(0);
		}
		
		AbstractClusteringSpace<?> space = ClusterSpaceFactory.createClusterSpace(algorithm.getImplementationType(), matrix);
		
		
		while (space.getClusters().size() > maxClusterCount && averageClusterDistance < maxAverageClusterDistance) {
			space.nextIteration();
			averageClusterDistance = space.calculateAverageClusterDistance();
		}
		
	}

}