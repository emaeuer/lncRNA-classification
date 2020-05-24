package de.lncrna.classification;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.biojava.nbio.core.sequence.RNASequence;

import de.lncrna.classification.init.distance.DistanceCalculationCoordinator;
import de.lncrna.classification.init.importer.FastaImporter;


public class Launcher {
	private static final Logger LOG = Logger.getLogger("logger");

	public static void main(String[] args) {
		LOG.log(Level.INFO, "Starting RNA classification");
		List<RNASequence> sequences = null;

		try {
			sequences = FastaImporter.requestOrLoadRNASequences();
		} catch (IOException e) {
			LOG.log(Level.WARNING, "Failed to get rna data", e);
			System.exit(0);
		}

		sequences = sequences.subList(0, 20);
		
		DistanceCalculationCoordinator coordinator = new DistanceCalculationCoordinator(sequences);
		coordinator.startDistanceCalculation();
		
		LOG.log(Level.INFO, "Finished RNA classification");
	}
}