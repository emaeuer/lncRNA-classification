package de.lncrna.classification.importer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.biojava.nbio.core.sequence.DNASequence;
import org.biojava.nbio.core.sequence.RNASequence;
import org.biojava.nbio.core.sequence.io.FastaReaderHelper;

public class FastaImporter {

	private static final Logger LOG = Logger.getLogger("logger");
	
	private FastaImporter() {
	}
	
	public static List<RNASequence> requestOrLoadRNASequences() throws IOException {
		if (!new File("data/sequences.fasta").isFile()) {
			downloadFastaFromURL("https://lncipedia.org/downloads/lncipedia_5_2/full-database/lncipedia_5_2.fasta");
		}
					
		return readFastaFromFile("data/sequences.fasta")
				.entrySet()
				.parallelStream()
				.map(entry -> {
					RNASequence sequence = entry.getValue().getRNASequence();
					sequence.setDescription(entry.getKey());
					return sequence;
				})
				.collect(Collectors.toList());
	}

	private static LinkedHashMap<String, DNASequence> readFastaFromFile(String path) throws IOException {
		LOG.log(Level.INFO, "Reading FASTA-file");
		try (InputStream input = new FileInputStream(path)) {
			return FastaReaderHelper.readFastaDNASequence(input);
		}
	}

	private static void downloadFastaFromURL(String url) throws IOException {
		LOG.log(Level.INFO, "Downloading FASTA-file");
		try (InputStream input = new URL(url).openStream()) {
			saveFastaFile(input);
		}
	}

	private static void saveFastaFile(InputStream input) {
		LOG.log(Level.INFO, "Saving FASTA-file");
		try (FileOutputStream output = new FileOutputStream("data/sequences.fasta")) {
			input.transferTo(output);
		}  catch (IOException e) {
			LOG.log(Level.WARNING, "An exception occured while saving fasta file", e);
		}
	}
	
}
