package de.lncrna.classification.distance.calculation;

import de.lncrna.classification.distance.DistanceCalculator;
import de.lncrna.classification.distance.DistancePair;
import de.lncrna.classification.distance.DistanceType;
import de.lncrna.classification.util.PropertyHandler;
import de.lncrna.classification.util.PropertyKeyHelper.PropertyKeys;
import info.debatty.java.stringsimilarity.NGram;

public class StandardNGramDistanceCalculator extends DistanceCalculator {

	private final NGram calculator;
	
	public StandardNGramDistanceCalculator() {	
		int nGramLength = PropertyHandler.HANDLER.getPropertyValue(PropertyKeys.N_GRAM_LENGTH, Integer.class);
		this.calculator = new NGram(nGramLength);
	}
	
	@Override
	public float getDistance(DistancePair pair) {
		return Double.valueOf(this.calculator.distance(pair.getSequence1(), pair.getSequence2())).floatValue();
	}

	@Override
	public DistanceType getDistanceProperties() {
		return DistanceType.Standard_N_Gram_Distance;
	}
	
}
