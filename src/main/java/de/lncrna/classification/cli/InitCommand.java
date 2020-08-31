package de.lncrna.classification.cli;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.biojava.nbio.core.sequence.RNASequence;

import de.lncrna.classification.db.Neo4jDatabaseSingleton;
import de.lncrna.classification.distance.DistanceCalculationInitializer;
import de.lncrna.classification.distance.DistanceType;
import de.lncrna.classification.distance.calculation.PropertyDistance;
import de.lncrna.classification.util.PropertyHandler;
import de.lncrna.classification.util.PropertyKeyHelper;
import de.lncrna.classification.util.PropertyKeyHelper.PropertyKeys;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "init",
	headerHeading = "@|bold,underline Usage|@:%n%n",
	synopsisHeading = "%n",
	descriptionHeading = "%n@|bold,underline Description|@:%n%n",
	optionListHeading = "%n@|bold,underline Options|@:%n",
	header = "Calculate all pairwise distances",
	description = "Calculates all pairwise sequence distances and stores them in the neo4j database",
	separator = " ")
public class InitCommand implements Runnable {
	
	private static class BlastParameter {
		
		@Option(names = {"-bf", "--blastFileLocation"}, description = "File location of the blast file with calculated distances") 
		private File blastFileLocation;
		
	}
	
	private static class NeedlemanWunschParameter {
		
		@Option(names = {"-gop", "--gapOpenPenalty"}, defaultValue = "-1", description = "Penalty for opening a gap in the sequence alignment") 
		private int gapOpenPenalty;
		
		@Option(names = {"-gcp", "--gapClosePenalty"}, defaultValue = "-1", description = "Penalty for closing a gap in the sequence alignment") 
		private int gapClosePenalty;

	}
	
	private static class NGramParameters {
		
		@Option(names = {"-ng", "--nGramLength"}, defaultValue = "-1", description = "Define the length of the n grams/ k mers") 
		private int nGramLength;
		
	}
	
	private static class DistanceSpecificParameters {
		
		@ArgGroup(exclusive = false)
		private BlastParameter blast;
		
		@ArgGroup(exclusive = false)
		private NeedlemanWunschParameter needlemanWunsch;
		
		@ArgGroup(exclusive = false)
		private NGramParameters ngram;
		
	}
	
	@ArgGroup(exclusive = true)
	private DistanceSpecificParameters parameters;
	
	@Option(names = {"-r", "--restart"}, defaultValue = "false", description = "The existing distance matrix will be either overwritten or new distances will be appended") 
	private boolean restartCalculation;
	
	@Option(names = {"-e", "--embedded"}, defaultValue = "false", description = "Use an embedded neo4j instance") 
	private boolean embeddedMode;
	
	@Option(names = {"-d", "--distanceAlgorithm"}, required = true, description = "Choose a distance measure. Possible values: ${COMPLETION-CANDIDATES}") 
	private DistanceType distanceProp;
	
	@Option(names = {"-f", "--fastaFileLocation"}, description = "File location of the fasta file with the sequences") 
	private File fastaFileLocation;
	
	@Option(names = {"-n", "--numberOfSequences"}, defaultValue = "-1", description = "Only calculate distances for the first n sequences of the fasta file") 
	private int sequenceNumber;
	
	@Option(names = {"-t", "--numberOfThreads"}, defaultValue = "-1", description = "Number of threads for distance calculation") 
	private int numberOfThreads;
	
	@Option(names = {"-w", "--distancePrintingDelta"}, defaultValue = "-1", description = "Time interval of the printer to check for new lines which can be appended to the distance matrix") 
	private int printingDelta;
	
	@Option(names = {"-b", "--blocking"}, defaultValue = "false", description = "Use blocking (a block is a canopy cluster calculated with blast distances to reduce number of distance calculations") 
	private boolean blocking;
	
	@Override
	public void run() {		
		PropertyKeyHelper.setGlobalPrefix(distanceProp.name());
		
		boolean hasChanged = CLIHelper.refreshIfNecessary(PropertyKeys.FASTA_FILE_LOCATION, fastaFileLocation);
		CLIHelper.refreshIfNecessary(PropertyKeys.DISTANCE_CALCULATION_THREAD_COUNT, numberOfThreads);
		CLIHelper.refreshIfNecessary(PropertyKeys.DISTANCE_CALCULATION_WAITING_TIME, printingDelta);
		refreshDistanceSpecificParameters();
		
		List<RNASequence> sequences = CommandUtil.getSequences(sequenceNumber);
		
		if (restartCalculation || hasChanged) {
			PropertyHandler.HANDLER.setPropertieValue(PropertyKeys.NEXT_RECORD, 0);
		}
		
		Neo4jDatabaseSingleton.initInstance(embeddedMode);
		
		Map<String, String> seqs = sequences.stream()
				.collect(Collectors.toMap(seq -> seq.getDescription(), seq -> seq.getSequenceAsString()));
		
		if (distanceProp == DistanceType.Property_Distance) {
			PropertyDistance.initPropertiesOfSequences(new ArrayList<>(seqs.keySet()));
		}
		
		new DistanceCalculationInitializer(seqs, distanceProp, blocking);
	}

	private void refreshDistanceSpecificParameters() {
		if (parameters != null && parameters.blast != null) {
			CLIHelper.refreshIfNecessary(PropertyKeys.BLAST_RESULT_FILE_LOCATION, parameters.blast.blastFileLocation);
		}
		
		if (parameters != null && parameters.ngram != null) {
			CLIHelper.refreshIfNecessary(PropertyKeys.N_GRAM_LENGTH, parameters.ngram.nGramLength);
		}
		
		if (parameters != null && parameters.needlemanWunsch != null) {
			CLIHelper.refreshIfNecessary(PropertyKeys.GAP_OPEN_PENALTY, parameters.needlemanWunsch.gapOpenPenalty);
			CLIHelper.refreshIfNecessary(PropertyKeys.GAP_CLOSE_PENALTY, parameters.needlemanWunsch.gapClosePenalty);
		}
	}
	
	

}
