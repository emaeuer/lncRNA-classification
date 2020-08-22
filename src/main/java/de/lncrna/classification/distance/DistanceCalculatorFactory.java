package de.lncrna.classification.distance;

import de.lncrna.classification.distance.calculation.BlastDistanceCalculator;
import de.lncrna.classification.distance.calculation.EditDistanceCalculator;
import de.lncrna.classification.distance.calculation.NGramDistanceCalculator;
import de.lncrna.classification.distance.calculation.NeedlemanWunschDistanceCalculator;
import de.lncrna.classification.distance.calculation.PropertyDistance;

public class DistanceCalculatorFactory {

	private DistanceCalculatorFactory() {
	}

	public static DistanceCalculator createDistanceCalculator(DistanceType type) {
		switch (type) {
			case Blast_Distance:
				BlastDistanceCalculator.readBlastFile();
				return new BlastDistanceCalculator();
			case Edit_Distance:
				return new EditDistanceCalculator();
			case N_Gram_Distance:
				return new NGramDistanceCalculator();
			case Needleman_Wunsch_Distance:
				return new NeedlemanWunschDistanceCalculator();
			case Property_Distance:
				return new PropertyDistance();
		}
		return null;
	}

}
