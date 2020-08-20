package de.lncrna.classification.distance.calculation;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import de.lncrna.classification.distance.DistanceCalculator;
import de.lncrna.classification.distance.DistancePair;
import de.lncrna.classification.distance.DistanceType;
import de.lncrna.classification.util.PropertyHandler;
import de.lncrna.classification.util.PropertyKeyHelper.PropertyKeys;

public class BlastDistanceCalculator extends DistanceCalculator {

	public static class BlastDistance {
		
		private float bitScore; // 2
		
		private int alignmentLength; //3
		
		public BlastDistance(CSVRecord record) {			
			this.bitScore = Float.valueOf(record.get(2));
			this.alignmentLength = Integer.valueOf(record.get(3));
		}
		
		/** 
 		* Normalized bit score Distance = 1 - BitScore / (2.4 * AlignmentLength)
		* 0 means no distance --> sequences are equal
		* 1 means maximal distance --> sequences are maximal different
		*/
		public float getDistance() {
			return 1 - getBitScore() / (2.4f * getAlignmentLength());
		}
		
		public void mergeAlignments(BlastDistance other) {
			if (other.getAlignmentLength() > getAlignmentLength()) {
				setAlignmentLength(other.getAlignmentLength());
				setBitScore(other.getBitScore());
			} else if (other.getAlignmentLength() == getAlignmentLength()) {
				setBitScore((getBitScore() + other.getBitScore()) / 2);
			}
		}
	
		public float getBitScore() {
			return bitScore;
		}
		
		public void setBitScore(float bitScore) {
			this.bitScore = bitScore;
		}
		
		public int getAlignmentLength() {
			return alignmentLength;
		}
		
		public void setAlignmentLength(int alignmentLength) {
			this.alignmentLength = alignmentLength;
		}		
	}
	
	private static final Table<String, String, BlastDistance> DISTANCE_MATRIX = HashBasedTable.create();
	
	// lazy loading of blast file
	// because Calculators are referenced by DistanceProperties they are always initialized during class loading
	// --> lazy initializing to prevent a slow start up
	private static boolean isInitialized = false;
	
	/**
	 * Columns in blast file: Query_Sequence, Subject_Sequence, Bitscore, Alignment_Length, Query_Length, Subject_Length
	 */
	public synchronized static void readBlastFile() {
		if (isInitialized) {
			return;
		}
		
		File blastFile = PropertyHandler.HANDLER.getPropertyValue(PropertyKeys.BLAST_RESULT_FILE_LOCATION, File.class);
		
		try (CSVParser reader = new CSVParser(new FileReader(blastFile), CSVFormat.DEFAULT)) {
			Iterator<CSVRecord> lines = reader.iterator();
			
			float max = Float.MIN_VALUE;
			float min = Float.MAX_VALUE;
			
			while (lines.hasNext()) {
				CSVRecord line = lines.next();
				String seq1 = line.get(0);
				String seq2 = line.get(1);
				
				if (seq1.equals(seq2)) {
					continue;
				}
				
				BlastDistance distance;
				
				if (DISTANCE_MATRIX.contains(seq1, seq2)) {
					distance = DISTANCE_MATRIX.get(seq1, seq2);
					distance.mergeAlignments(new BlastDistance(line));
				} else {
					distance = new BlastDistance(line);
					DISTANCE_MATRIX.put(seq1, seq2, distance);
				}
				
				max = Math.max(max, distance.getDistance());
				min = Math.min(min, distance.getDistance());
			}
			
			System.out.println(String.format("[%s, %s]", min, max));
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		isInitialized = true;
		
	}

	@Override
	public float getDistance(DistancePair pair) {
		BlastDistance distance = DISTANCE_MATRIX.get(pair.getSequenceName1(), pair.getSequenceName2());
		
		if (distance == null) {
			distance = DISTANCE_MATRIX.get(pair.getSequenceName2(), pair.getSequenceName1());
		}
		
		return distance == null ? -1 : distance.getDistance();
	}

	@Override
	public DistanceType getDistanceProperties() {
		return DistanceType.Blast_Distance;
	}	

}
