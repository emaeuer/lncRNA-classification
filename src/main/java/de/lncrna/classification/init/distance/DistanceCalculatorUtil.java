package de.lncrna.classification.init.distance;

import org.biojava.nbio.alignment.Alignments;
import org.biojava.nbio.alignment.Alignments.PairwiseSequenceAlignerType;
import org.biojava.nbio.alignment.SimpleGapPenalty;
import org.biojava.nbio.alignment.SubstitutionMatrixHelper;
import org.biojava.nbio.core.sequence.RNASequence;

import de.lncrna.classification.util.PropertyHandler;
import de.lncrna.classification.util.PropertyKeys;

public class DistanceCalculatorUtil {

	private static int gop = PropertyHandler.HANDLER.getPropertyValue(PropertyKeys.GAP_OPEN_PENALTY, Integer.class);
	private static int gep = PropertyHandler.HANDLER.getPropertyValue(PropertyKeys.GAP_CLOSE_PENALTY, Integer.class);
	
	private static String alignerType = PropertyHandler.HANDLER.getPropertyValue(PropertyKeys.PAIRWISE_SEQUENCE_ALIGNER_TYPE, String.class);
	
	public static double calculateDistance(RNASequence seq1, RNASequence seq2) {
		return Alignments.getPairwiseAligner(seq1, seq2, PairwiseSequenceAlignerType.valueOf(alignerType),
				new SimpleGapPenalty(gop, gep), SubstitutionMatrixHelper.getNuc4_2()).getDistance();
	}
	
}
