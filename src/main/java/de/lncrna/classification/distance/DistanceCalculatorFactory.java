package de.lncrna.classification.distance;

import de.lncrna.classification.distance.calculation.BlastDistanceCalculator;
import de.lncrna.classification.distance.calculation.EditDistanceCalculator;
import de.lncrna.classification.distance.calculation.HammingDistanceCalculator;
import de.lncrna.classification.distance.calculation.NGramDistanceCalculator;
import de.lncrna.classification.distance.calculation.NeedlemanWunschDistanceCalculator;
import de.lncrna.classification.distance.calculation.PropertyDistance;
import de.lncrna.classification.distance.calculation.ShingledNGramDistanceCalculator;
import de.lncrna.classification.distance.calculation.StandardNGramDistanceCalculator;

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
			case Shingled_N_Gram_Distance:
				return new ShingledNGramDistanceCalculator();
			case Standard_N_Gram_Distance:
				return new StandardNGramDistanceCalculator();
			case Hamming_Distance:
				return new HammingDistanceCalculator();
		default:
			break;
		}
		return null;
	}

}
