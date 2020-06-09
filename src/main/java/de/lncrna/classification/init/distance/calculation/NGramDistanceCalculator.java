package de.lncrna.classification.init.distance.calculation;

import org.apache.lucene.search.spell.NGramDistance;
import org.biojava.nbio.core.sequence.RNASequence;

import de.lncrna.classification.init.distance.DistanceProperties;
import de.lncrna.classification.util.PropertyHandler;
import de.lncrna.classification.util.PropertyKeyHelper.PropertyKeys;

public class NGramDistanceCalculator implements DistanceCalculator {

	private final NGramDistance calculator;
	
	public NGramDistanceCalculator() {
		int nGramLength = PropertyHandler.HANDLER.getPropertyValue(PropertyKeys.N_GRAM_LENGTH, Integer.class);
		this.calculator = new NGramDistance(nGramLength);
	}
	
	@Override
	public float getDistance(RNASequence seq1, RNASequence seq2) {
		return Float.valueOf(this.calculator.getDistance(seq1.getSequenceAsString(), seq2.getSequenceAsString()));
	}

	@Override
	public DistanceProperties getDistanceProperties() {
		return DistanceProperties.N_Gram_Distance;
	}
	
}
