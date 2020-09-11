package de.lncrna.classification.distance.calculation;

import org.apache.commons.text.similarity.HammingDistance;

import de.lncrna.classification.distance.DistanceCalculator;
import de.lncrna.classification.distance.DistancePair;
import de.lncrna.classification.distance.DistanceType;

public class HammingDistanceCalculator extends DistanceCalculator {

	private final HammingDistance calculator = new HammingDistance();
	
	@Override
	public float getDistance(DistancePair pair) {
		return this.calculator.apply(pair.getSequence1().substring(0, 200), pair.getSequence2().substring(0, 200)) / 200f;
	}

	@Override
	public DistanceType getDistanceProperties() {
		return DistanceType.Hamming_Distance;
	}

}
