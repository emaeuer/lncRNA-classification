package de.lncrna.classification.util.fasta;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import org.biojava.nbio.core.sequence.RNASequence;
import org.biojava.nbio.core.sequence.io.FastaWriter;
import org.biojava.nbio.core.sequence.io.template.FastaHeaderFormatInterface;
import org.biojava.nbio.core.sequence.template.Compound;

public class FastaExporter {

	public static void saveFasta(File file, List<RNASequence> sequences) throws Exception {
		FastaHeaderFormatInterface<RNASequence, Compound> headerFormat = new FastaHeaderFormatInterface<RNASequence, Compound>() {
			
			@Override
			public String getHeader(RNASequence sequence) {
				return sequence.getDescription();
			}
		};
		
		try (FileOutputStream output = new FileOutputStream(file)) {
			FastaWriter<RNASequence, Compound> writer = new FastaWriter<RNASequence, Compound>(output, sequences, headerFormat);
			writer.process();
		}
	}
	
}
