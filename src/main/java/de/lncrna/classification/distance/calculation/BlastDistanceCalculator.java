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

	private static final Table<String, String, Float> DISTANCE_MATRIX = HashBasedTable.create();
	
	// lazy loading of blast file
	// because Calculators are referenced by DistanceProperties they are always initialized during class loading
	// --> lazy initializing to prevent a slow start up
	private static boolean isInitialized = false;
	
	public synchronized static void readBlastFile() {
		if (isInitialized) {
			return;
		}
		
		File blastFile = PropertyHandler.HANDLER.getPropertyValue(PropertyKeys.BLAST_RESULT_FILE_LOCATION, File.class);
		
		try (CSVParser reader = new CSVParser(new FileReader(blastFile), CSVFormat.DEFAULT)) {
			Iterator<CSVRecord> lines = reader.iterator();
			
			while (lines.hasNext()) {
				CSVRecord line = lines.next();
				String seq1 = line.get(0);
				String seq2 = line.get(1);
				float bitScore = Float.valueOf(line.get(3));
				int length = Integer.valueOf(line.get(4));
				
				// normalization of bitscore 
				// 0 means no distance --> sequences are equal
				// 1 means maximal distance --> sequences are maximal different
				float distance = 1 - bitScore / (2.4f * length);
				DISTANCE_MATRIX.put(seq1, seq2, distance);
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		isInitialized = true;
		
	}

	@Override
	public float getDistance(DistancePair pair) {
		Float distance = DISTANCE_MATRIX.get(pair.getSequenceName1(), pair.getSequenceName2());
		
		if (distance == null) {
			distance = DISTANCE_MATRIX.get(pair.getSequenceName2(), pair.getSequenceName1());
		}
		
		return distance == null ? -1 : distance;
	}

	@Override
	public DistanceType getDistanceProperties() {
		return DistanceType.Blast_Distance;
	}	

}
