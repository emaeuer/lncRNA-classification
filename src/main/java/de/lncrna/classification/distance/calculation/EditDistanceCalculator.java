package de.lncrna.classification.distance.calculation;

import org.apache.lucene.search.spell.LevenshteinDistance;

import de.lncrna.classification.distance.DistanceCalculator;
import de.lncrna.classification.distance.DistancePair;
import de.lncrna.classification.distance.DistanceType;

public class EditDistanceCalculator extends DistanceCalculator {
	
	private final LevenshteinDistance calculator = new LevenshteinDistance();
	
	@Override
	public float getDistance(DistancePair pair) {
		return 1 - calculator.getDistance(pair.getSequence1(), pair.getSequence2());
	}

	@Override
	public DistanceType getDistanceProperties() {
		return DistanceType.Edit_Distance;
	}

}
