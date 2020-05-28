package de.lncrna.classification;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.biojava.nbio.core.sequence.RNASequence;

import de.lncrna.classification.clustering.algorithms.implementations.HierarchicalClusteringMinimalDistance;
import de.lncrna.classification.clustering.algorithms.space.AbstractClusteringSpace;
import de.lncrna.classification.clustering.algorithms.space.MinimalDistanceHierachicalClusteringSpace;
import de.lncrna.classification.data.DistanceMatrix;
import de.lncrna.classification.init.distance.DistanceCalculationCoordinator;
import de.lncrna.classification.init.importer.FastaImporter;
import de.lncrna.classification.util.PropertyHandler;
import de.lncrna.classification.util.PropertyKeys;


public class Launcher {
	private static final Logger LOG = Logger.getLogger("logger");

	public static void main(String[] args) {
		LOG.log(Level.INFO, "Starting RNA classification");
		
		initData();
		
		String csvFileLocation = PropertyHandler.HANDLER.getPropertyValue(PropertyKeys.DISTANCE_FILE_LOCATION, String.class);
		
		try {
			DistanceMatrix matrix = DistanceMatrix.initFromCSV(csvFileLocation);
			AbstractClusteringSpace<HierarchicalClusteringMinimalDistance> space = new MinimalDistanceHierachicalClusteringSpace(matrix);
			while (space.getClusters().size() > 1) {
				space.nextIteration();
				System.out.println(space.calculateAverageClusterDistance());
			}
		} catch (IOException e) {
			LOG.log(Level.WARNING, "Failed to load distance matrix", e);		
		}
		
		LOG.log(Level.INFO, "Finished RNA classification");
		
	}

	private static void initData() {
		List<RNASequence> sequences = null;

		try {
			sequences = FastaImporter.requestOrLoadRNASequences();
		} catch (IOException e) {
			LOG.log(Level.WARNING, "Failed to get rna data", e);
			System.exit(0);
		}

		sequences = sequences.subList(0, 30);
		
		DistanceCalculationCoordinator coordinator = new DistanceCalculationCoordinator(sequences);
		coordinator.startDistanceCalculation();
	}
}