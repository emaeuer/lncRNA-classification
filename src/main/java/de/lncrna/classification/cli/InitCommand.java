package de.lncrna.classification.cli;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.biojava.nbio.core.sequence.RNASequence;

import de.lncrna.classification.init.distance.DistanceCalculationCoordinator;
import de.lncrna.classification.init.distance.DistanceProperties;
import de.lncrna.classification.init.importer.FastaImporter;
import de.lncrna.classification.util.PropertyHandler;
import de.lncrna.classification.util.PropertyKeyHelper;
import de.lncrna.classification.util.PropertyKeyHelper.PropertyKeys;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "init",
	headerHeading = "@|bold,underline Usage|@:%n%n",
	synopsisHeading = "%n",
	descriptionHeading = "%n@|bold,underline Description|@:%n%n",
	optionListHeading = "%n@|bold,underline Options|@:%n",
	header = "Initialize distance matrix for clustering",
	description = "Calculates all pairwise sequence distances and stores them in a distance matrix",
	separator = " ")
public class InitCommand implements Runnable {

	private static final Logger LOG = Logger.getLogger("logger");
	
	@Option(names = {"-r", "--restart"}, defaultValue = "false", description = "The existing distance matrix will be either overwritten or new distances will be appended") 
	private boolean restartCalculation;
	
	@Option(names = {"-d", "--distanceAlgorithm"}, required = true, description = "Choose a distance measure. Possible values: ${COMPLETION-CANDIDATES}") 
	private DistanceProperties distanceProp;
	
	@Option(names = {"-f", "--fastaFileLocation"}, description = "File location of the fasta file with the sequences") 
	private File fastaFileLocation;
	
	@Option(names = {"-n", "--numberOfSequences"}, defaultValue = "-1", description = "Only calculate distances for the first n sequences of the fasta file") 
	private int sequenceNumber;
	
	@Option(names = {"-t", "--numberOfThreads"}, defaultValue = "-1", description = "Number of threads for distance calculation") 
	private int numberOfThreads;
	
	@Option(names = {"-w", "--distancePrintingDelta"}, defaultValue = "-1", description = "Time interval of the printer to check for new lines which can be appended to the distance matrix") 
	private int printingDelta;
	
	@Override
	public void run() {
		PropertyKeyHelper.setGlobalPrefix(distanceProp.name());
		
		boolean hasChanged = refreshIfNecessary(PropertyKeys.FASTA_FILE_LOCATION, fastaFileLocation);
		refreshIfNecessary(PropertyKeys.DISTANCE_CALCULATION_THREAD_COUNT, numberOfThreads);
		refreshIfNecessary(PropertyKeys.DISTANCE_CALCULATION_WAITING_TIME, printingDelta);
		
		List<RNASequence> sequences = getSequences(sequenceNumber);
		
		if (restartCalculation || hasChanged) {
			PropertyHandler.HANDLER.setPropertieValue(PropertyKeys.NEXT_RECORD, 0);
		}
		
		new DistanceCalculationCoordinator(sequences, distanceProp.getCalculator())
			.startDistanceCalculation();

	}
	
	private List<RNASequence> getSequences(int sequenceNumber) {
		List<RNASequence> sequences = null;

		try {
			sequences = FastaImporter.requestOrLoadRNASequences();
		} catch (IOException e) {
			LOG.log(Level.WARNING, "Failed to get rna data", e);
			System.exit(0);
		}

		if (sequenceNumber != -1) {
			sequences = sequences.subList(0, sequenceNumber);
		}
		return sequences;
	}
	
	private boolean refreshIfNecessary(PropertyKeys key, int value) {
		if (value != -1) {
			PropertyHandler.HANDLER.setPropertieValue(key, value);
			return true;
		}	
		return false;
	}
	
	private boolean refreshIfNecessary(PropertyKeys key, Object value) {
		if (value != null) {
			Object old = PropertyHandler.HANDLER.getPropertyValue(key, value.getClass());
			if (old != null && !old.equals(value)) {
				PropertyHandler.HANDLER.setPropertieValue(key, value);
				return true;
			}
		}	
		return false;
	}

}
