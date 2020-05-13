package de.lncrna.classification;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.biojava.nbio.core.sequence.RNASequence;

import de.lncrna.classification.clustering.algorithms.space.MinimalDistanceHierachicalClusteringSpace;
import de.lncrna.classification.importer.FastaImporter;

public class Launcher {
	
	private static final Logger LOG = Logger.getLogger("logger");

	public static void main(String[] args) {
		long startTime = System.currentTimeMillis();
		
		LOG.log(Level.INFO, "Collecting rna sequences");
		List<RNASequence> sequences = null;
		
		try {
			sequences = FastaImporter.requestOrLoadRNASequences();
		} catch (IOException e) {
			LOG.log(Level.WARNING, "Failed to get rna data", e);
			System.exit(0);
		}
		
		sequences = sequences.subList(0, 1000);
		
		MinimalDistanceHierachicalClusteringSpace space = new MinimalDistanceHierachicalClusteringSpace(sequences);
		
		while (space.getClusters().size() > 3) {
			System.out.println(space.nextIteration());
		}
		
		System.out.println((System.currentTimeMillis() - startTime) / 1000.0 / 60);
	}
	
}
