package de.lncrna.classification.util.data;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.common.collect.Table.Cell;
import com.google.common.collect.Tables;

import de.lncrna.classification.util.PropertyHandler;
import de.lncrna.classification.util.PropertyKeyHelper.PropertyKeys;

public class DistanceMatrix {

	private final Table<String, String, Float> distanceMatrix = HashBasedTable.create();
	
	private List<String> header = new ArrayList<>();
	
	public static DistanceMatrix initFromCSV() throws IOException {
		File csvFile = PropertyHandler.HANDLER.getPropertyValue(PropertyKeys.FILE_LOCATION, File.class);
		
		DistanceMatrix matrix = new DistanceMatrix();
		CSVParser reader = new CSVParser(new FileReader(csvFile), CSVFormat.EXCEL);
		
		Iterator<CSVRecord> lines = reader.iterator();
		if (lines.hasNext()) {
			lines.next().forEach(matrix.header::add);
		}
		while (lines.hasNext()) {
			CSVRecord line = lines.next();
			String lineName = line.get(0);
			for (int i = 1; i < line.getRecordNumber(); i++) {
				matrix.distanceMatrix.put(lineName, matrix.header.get(i), Float.valueOf(line.get(i)));
			}	
		}
		
		reader.close();
		return matrix;
	}
	
	public List<String> getSequenceNames() {
		return this.header.subList(1, this.header.size());
	}
	
	public Set<Cell<String, String, Float>> getAllCells() {
		return this.distanceMatrix.cellSet();
	}
	
	public Cell<String, String, Float> getDistance(String seq1, String seq2) {				
		Float result = this.distanceMatrix.get(seq1, seq2);
		result = result != null ? result : this.distanceMatrix.get(seq2, seq1);
		return Tables.immutableCell(seq1, seq2, result);
	}
	
}
