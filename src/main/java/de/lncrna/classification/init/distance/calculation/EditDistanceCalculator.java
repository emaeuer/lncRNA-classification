package de.lncrna.classification.init.distance.calculation;

import org.apache.lucene.search.spell.LevenshteinDistance;
import org.biojava.nbio.core.sequence.RNASequence;

import de.lncrna.classification.init.distance.DistanceProperties;

public class EditDistanceCalculator implements DistanceCalculator {
	
	private final LevenshteinDistance calculator = new LevenshteinDistance();
	
	@Override
	public float getDistance(RNASequence seq1, RNASequence seq2) {
		return calculator.getDistance(seq1.getSequenceAsString(), seq2.getSequenceAsString());
	}

	@Override
	public DistanceProperties getDistanceProperties() {
		return DistanceProperties.Edit_Distance;
	}

}
