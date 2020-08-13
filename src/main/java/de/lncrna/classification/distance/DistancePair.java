package de.lncrna.classification.distance;

import java.util.AbstractMap.SimpleEntry;

public class DistancePair {
	
	private DistanceType distanceType;
	
	private SimpleEntry<String, String> sequence1;
	private SimpleEntry<String, String> sequence2;
	
	private float distance;
	
	public DistancePair(String sequenceName1, String sequence1, String sequenceName2, String sequence2) {
		if (sequenceName1.compareTo(sequenceName2) == -1) {
			this.sequence1 = new SimpleEntry<>(sequenceName1, sequence1);
			this.sequence2 = new SimpleEntry<>(sequenceName2, sequence2);
		} else {
			this.sequence2 = new SimpleEntry<>(sequenceName1, sequence1);
			this.sequence1 = new SimpleEntry<>(sequenceName2, sequence2);
		}
	}
	
	public String getSequenceName1() {
		return sequence1.getKey();
	}

	public String getSequenceName2() {
		return sequence2.getKey();
	}
	
	public String getSequence1() {
		return sequence1.getValue();
	}

	public String getSequence2() {
		return sequence2.getValue();
	}
	
	public float getDistance() {
		return distance;
	}
	
	public void setDistance(float distance) {
		this.distance = distance;
	}
	
	public DistanceType getDistanceType() {
		return this.distanceType;		
	}

	public void setDistanceType(DistanceType distanceType) {
		this.distanceType = distanceType;
	}
	
}