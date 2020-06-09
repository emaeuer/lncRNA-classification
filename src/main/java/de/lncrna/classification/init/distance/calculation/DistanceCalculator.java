package de.lncrna.classification.init.distance.calculation;

import org.biojava.nbio.core.sequence.RNASequence;

import de.lncrna.classification.init.distance.DistanceProperties;

public interface DistanceCalculator {

	public float getDistance(RNASequence seq1, RNASequence seq2);
	
	public DistanceProperties getDistanceProperties();
	
}
