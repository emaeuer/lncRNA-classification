package de.lncrna.classification.cli;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.biojava.nbio.core.sequence.RNASequence;

import de.lncrna.classification.util.PropertyHandler;
import de.lncrna.classification.util.PropertyKeyHelper.PropertyKeys;
import de.lncrna.classification.util.fasta.FastaImporter;

public class CommandUtil {

	private static final Logger LOG = Logger.getLogger("logger");
	
	private CommandUtil() {}
	
	public static List<RNASequence> getSequences(int sequenceNumber, File fastaFile) {
		List<RNASequence> sequences = null;

		try {
			sequences = FastaImporter.requestOrLoadRNASequences(fastaFile);
		} catch (IOException e) {
			LOG.log(Level.WARNING, "Failed to get rna data", e);
			System.exit(0);
		}

		if (sequenceNumber != -1) {
			List<RNASequence> randomSequences = new ArrayList<>();
			Random rand = new Random(49583498340l);
			for (int i = 0; i < sequenceNumber; i++) {
				randomSequences.add(sequences.remove(rand.nextInt(sequences.size())));
			}
			sequences = randomSequences;
		}
		return sequences;
	}
	
	public static List<RNASequence> getSequences(int sequenceNumber) {
		return getSequences(sequenceNumber, 
				PropertyHandler.HANDLER.getPropertyValue(PropertyKeys.FASTA_FILE_LOCATION, File.class));
	}
	
}
