package de.lncrna.classification.cli;

import java.io.File;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.biojava.nbio.core.sequence.RNASequence;

import de.lncrna.classification.filter.NumberOfIntronsFilter;
import de.lncrna.classification.util.fasta.FastaExporter;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "filter",
	headerHeading = "@|bold,underline Usage|@:%n%n",
	synopsisHeading = "%n",
	descriptionHeading = "%n@|bold,underline Description|@:%n%n",
	optionListHeading = "%n@|bold,underline Options|@:%n",
	header = "Filter the fasta file for sequences with at least on intron",
	description = "Creates a new Fasta file containing only sequences with atleast one intron",
	separator = " ")
public class FilterCommand implements Runnable {

	private static final Logger LOG = Logger.getLogger("logger");
	
	private ProgressBarHelper status;
	
	@Option(names = {"-i", "--inputFastaFile"}, description = "File location of the input Fasta file", required = true) 
	private File inputFastaFile;
	
	@Option(names = {"-o", "--outputFastaFile"}, description = "File location of the output Fasta file with filtered sequences", required = true) 
	private File outputFastaFile;

	@Override
	public void run() {
		List<RNASequence> sequences = CommandUtil.getSequences(-1, inputFastaFile);
		
		this.status = new ProgressBarHelper();
		this.status.nextBlock(sequences.size(), "Filtering Sequences");
		
		List<RNASequence> filteredSequences = sequences.parallelStream()
			.filter(s -> NumberOfIntronsFilter.hasAtLeastOneIntron(s.getDescription()))
			.peek(e -> this.status.next())
			.collect(Collectors.toList());
		
		this.status.stop();
		
		try {
			FastaExporter.saveFasta(outputFastaFile, filteredSequences);
		} catch (Exception e) {
			LOG.log(Level.WARNING, "Failed to save filtered fasta file to \"" + outputFastaFile.getAbsolutePath() + "\"", e);
		}
		
	}
	
}
