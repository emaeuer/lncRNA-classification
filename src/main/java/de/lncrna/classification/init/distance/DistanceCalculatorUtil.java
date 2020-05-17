package de.lncrna.classification.init.distance;

import org.biojava.nbio.alignment.Alignments;
import org.biojava.nbio.core.sequence.RNASequence;

import de.lncrna.classification.clustering.algorithms.AlignmentConstants;

public class DistanceCalculatorUtil {

	public static double calculateDistance(RNASequence seq1, RNASequence seq2) {
		return Alignments.getPairwiseAligner(seq1, seq2, AlignmentConstants.ALIGNER_TYPE,
				AlignmentConstants.GAP_PENALTY, AlignmentConstants.SUBSTITUTION_MATRIX).getDistance();
	}
	
}
