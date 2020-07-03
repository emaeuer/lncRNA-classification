package de.lncrna.classification.util.data;

import org.neo4j.graphdb.RelationshipType;

public class DistanceDAO {

	public enum RelTypes implements RelationshipType {
		DISTANCE_TO;
	}
	
	private String seq1;
	private String seq2;
	private String distanceName;
	private float distanceValue;
	
	public DistanceDAO(String seq1, String seq2, String distanceName, float distanceValue) {
		setSeq1(seq1);
		setSeq2(seq2);
		setDistanceName(distanceName);
		setDistanceValue(distanceValue);
	}
	
	public DistanceDAO(String seq1, String seq2, String distanceName) {
		setSeq1(seq1);
		setSeq2(seq2);
		setDistanceName(distanceName);
	}

	public String getSeq1() {
		return seq1;
	}
	
	public void setSeq1(String seq1) {
		this.seq1 = seq1;
	}
	
	public String getSeq2() {
		return seq2;
	}
	
	public void setSeq2(String seq2) {
		this.seq2 = seq2;
	}
	
	public String getDistanceName() {
		return distanceName;
	}
	
	public void setDistanceName(String distanceName) {
		this.distanceName = distanceName;
	}
	
	public float getDistanceValue() {
		return distanceValue;
	}
	
	public void setDistanceValue(float distanceValue) {
		this.distanceValue = distanceValue;
	}
	
}
