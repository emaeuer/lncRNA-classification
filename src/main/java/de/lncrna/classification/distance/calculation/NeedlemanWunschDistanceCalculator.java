package de.lncrna.classification.distance.calculation;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.biojava.nbio.alignment.Alignments;
import org.biojava.nbio.alignment.Alignments.PairwiseSequenceAlignerType;
import org.biojava.nbio.alignment.SimpleGapPenalty;
import org.biojava.nbio.alignment.SubstitutionMatrixHelper;
import org.biojava.nbio.core.exceptions.CompoundNotFoundException;
import org.biojava.nbio.core.sequence.RNASequence;

import de.lncrna.classification.distance.DistanceCalculator;
import de.lncrna.classification.distance.DistancePair;
import de.lncrna.classification.distance.DistanceType;
import de.lncrna.classification.util.PropertyHandler;
import de.lncrna.classification.util.PropertyKeyHelper.PropertyKeys;

public class NeedlemanWunschDistanceCalculator extends DistanceCalculator {

	protected static final Logger LOG = Logger.getLogger("logger");
	
	private static final int GOP = PropertyHandler.HANDLER.getPropertyValue(PropertyKeys.GAP_OPEN_PENALTY, Integer.class);
	private static final int GEP = PropertyHandler.HANDLER.getPropertyValue(PropertyKeys.GAP_CLOSE_PENALTY, Integer.class);
	
	private static final String ALIGNER_TYPE = PropertyHandler.HANDLER.getPropertyValue(PropertyKeys.PAIRWISE_SEQUENCE_ALIGNER_TYPE, String.class);
	
	@Override
	public float getDistance(DistancePair pair) {
		try {
			RNASequence seq1 = new RNASequence(pair.getSequence1());
			RNASequence seq2 = new RNASequence(pair.getSequence2());
			
			return Double.valueOf(Alignments.getPairwiseAligner(seq1, seq2, PairwiseSequenceAlignerType.valueOf(ALIGNER_TYPE),
					new SimpleGapPenalty(GOP, GEP), SubstitutionMatrixHelper.getNuc4_4()).getDistance()).floatValue();
		} catch (CompoundNotFoundException e) {
			LOG.log(Level.WARNING, String.format("Failed to calculate distance between %s and %s", pair.getSequenceName1(), pair.getSequenceName2()), e);
			return -1;
		}
	}

	@Override
	public DistanceType getDistanceProperties() {
		return DistanceType.Needleman_Wunsch_Distance;
	}

}
