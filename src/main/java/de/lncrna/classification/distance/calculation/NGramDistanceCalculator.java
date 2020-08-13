package de.lncrna.classification.distance.calculation;

import org.apache.lucene.search.spell.NGramDistance;

import de.lncrna.classification.distance.DistanceCalculator;
import de.lncrna.classification.distance.DistancePair;
import de.lncrna.classification.distance.DistanceType;
import de.lncrna.classification.util.PropertyHandler;
import de.lncrna.classification.util.PropertyKeyHelper.PropertyKeys;

public class NGramDistanceCalculator extends DistanceCalculator {

	private final NGramDistance calculator;
	
	public NGramDistanceCalculator() {
		int nGramLength = PropertyHandler.HANDLER.getPropertyValue(PropertyKeys.N_GRAM_LENGTH, Integer.class);
		this.calculator = new NGramDistance(nGramLength);
	}
	
	@Override
	public float getDistance(DistancePair pair) {
		return Float.valueOf(this.calculator.getDistance(pair.getSequence1(), pair.getSequence2()));
	}

	@Override
	public DistanceType getDistanceProperties() {
		return DistanceType.N_Gram_Distance;
	}
	
}
