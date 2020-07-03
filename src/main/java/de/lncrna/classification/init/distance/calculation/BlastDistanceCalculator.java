package de.lncrna.classification.init.distance.calculation;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.biojava.nbio.core.sequence.RNASequence;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import de.lncrna.classification.init.distance.DistanceProperties;
import de.lncrna.classification.util.PropertyHandler;
import de.lncrna.classification.util.PropertyKeyHelper.PropertyKeys;

public class BlastDistanceCalculator implements DistanceCalculator {

	private final Table<String, String, Float> distanceMatrix = HashBasedTable.create();
	
	// lazy loading of blast file
	// because Calculators are referenced by DistanceProperties they are always initialized during class loading
	// --> lazy initializing to prevent a slow start up
	private boolean isInitialized = false;
	
	private void readBlastFile() {
		File blastFile = PropertyHandler.HANDLER.getPropertyValue(PropertyKeys.BLAST_RESULT_FILE_LOCATION, File.class);
		
		try (CSVParser reader = new CSVParser(new FileReader(blastFile), CSVFormat.DEFAULT)) {
			Iterator<CSVRecord> lines = reader.iterator();
			
			float minScoreDistance = Integer.MAX_VALUE;
			float maxScoreDistance = Integer.MIN_VALUE;
			float minBitScoreDistance = Integer.MAX_VALUE;
			float maxBitScoreDistance = Integer.MIN_VALUE;
			
			while (lines.hasNext()) {
				CSVRecord line = lines.next();
				String seq1 = line.get(0);
				String seq2 = line.get(1);
				int score = Integer.valueOf(line.get(2));
				float bitScore = Float.valueOf(line.get(3));
				int length = Integer.valueOf(line.get(4));
				
				float distance = 1 - Integer.valueOf(score).floatValue() / length;
				float bitDistance = 1 - bitScore / length;
				
				minScoreDistance = Math.min(minScoreDistance, distance);
				maxScoreDistance = Math.max(maxScoreDistance, distance);
				minBitScoreDistance = Math.min(minBitScoreDistance, bitDistance);
				maxBitScoreDistance = Math.max(maxBitScoreDistance, bitDistance);
			
				this.distanceMatrix.put(seq1, seq2, distance);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	@Override
	public float getDistance(RNASequence seq1, RNASequence seq2) {
		if (!isInitialized) {
			readBlastFile();
			isInitialized = true;
		}
		
		Float distance = this.distanceMatrix.get(seq1.getDescription(), seq2.getDescription());
		
		if (distance == null) {
			distance = this.distanceMatrix.get(seq2.getDescription(), seq1.getDescription());
		}
		
		return distance == null ? -1 : distance;
	}

	@Override
	public DistanceProperties getDistanceProperties() {
		return DistanceProperties.Blast_Distance;
	}
	

}
