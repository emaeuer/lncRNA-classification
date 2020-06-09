package de.lncrna.classification.init.distance.calculation;

import org.biojava.nbio.alignment.Alignments;
import org.biojava.nbio.alignment.Alignments.PairwiseSequenceAlignerType;
import org.biojava.nbio.alignment.SimpleGapPenalty;
import org.biojava.nbio.alignment.SubstitutionMatrixHelper;
import org.biojava.nbio.core.sequence.RNASequence;

import de.lncrna.classification.init.distance.DistanceProperties;
import de.lncrna.classification.util.PropertyHandler;
import de.lncrna.classification.util.PropertyKeyHelper.PropertyKeys;

public class NeedlemanWunschDistanceCalculator implements DistanceCalculator {

	private static final int GOP = PropertyHandler.HANDLER.getPropertyValue(PropertyKeys.GAP_OPEN_PENALTY, Integer.class);
	private static final int GEP = PropertyHandler.HANDLER.getPropertyValue(PropertyKeys.GAP_CLOSE_PENALTY, Integer.class);
	
	private static final String ALIGNER_TYPE = PropertyHandler.HANDLER.getPropertyValue(PropertyKeys.PAIRWISE_SEQUENCE_ALIGNER_TYPE, String.class);
	
	@Override
	public float getDistance(RNASequence seq1, RNASequence seq2) {
		return Double.valueOf(Alignments.getPairwiseAligner(seq1, seq2, PairwiseSequenceAlignerType.valueOf(ALIGNER_TYPE),
				new SimpleGapPenalty(GOP, GEP), SubstitutionMatrixHelper.getNuc4_4()).getDistance()).floatValue();
	}

	@Override
	public DistanceProperties getDistanceProperties() {
		return DistanceProperties.Needleman_Wunsch_Distance;
	}

}
