package de.lncrna.classification.init.distance;

import de.lncrna.classification.init.distance.calculation.BlastDistanceCalculator;
import de.lncrna.classification.init.distance.calculation.DistanceCalculator;
import de.lncrna.classification.init.distance.calculation.EditDistanceCalculator;
import de.lncrna.classification.init.distance.calculation.NGramDistanceCalculator;
import de.lncrna.classification.init.distance.calculation.NeedlemanWunschDistanceCalculator;

public enum DistanceProperties {
		
	Edit_Distance(new EditDistanceCalculator()),
	Needleman_Wunsch_Distance(new NeedlemanWunschDistanceCalculator()),
	N_Gram_Distance(new NGramDistanceCalculator()),
	Blast_Distance(new BlastDistanceCalculator());
	
	private final DistanceCalculator calculator;
	
	DistanceProperties(DistanceCalculator calculator) {
		this.calculator = calculator;
	}
	
	public DistanceCalculator getCalculator() {
		return this.calculator;
	}

}
