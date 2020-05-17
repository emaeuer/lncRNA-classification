package de.lncrna.classification.init.distance;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.ArrayUtils;
import org.biojava.nbio.core.sequence.RNASequence;

import de.lncrna.classification.util.PropertyHandler;
import de.lncrna.classification.util.PropertyKeys;
import me.tongfei.progressbar.ProgressBar;


public class DistanceCalculationCoordinator {
	
	private class ProgressBarHelper implements Iterator<Void> {
		
		private final int listSize;
		private final int updateInterval;
		
		private int currentLine;
		private int numberOfCalculatedElements;
		
		private final ProgressBar bar;
		
		public ProgressBarHelper(int listSize, int updateInterval) {
			this.listSize = listSize;
			this.updateInterval = updateInterval;
			int totalNumberOfCalculations = (listSize * (listSize + 1)) / 2;
			this.bar = new ProgressBar("Distance-Calculation", totalNumberOfCalculations);
		}
		
		public void setCurrentLineNumber(int currentLine) {
			this.currentLine = currentLine;
			
			for (int i = 0; i < this.currentLine; i++) {
				this.numberOfCalculatedElements += this.listSize - this.currentLine;
			}
			this.bar.stepTo(numberOfCalculatedElements);
		}
		
		public void stop() {
			this.bar.close();
		}

		@Override
		public boolean hasNext() {
			return this.bar.getCurrent() < this.bar.getMax();
		}

		@Override
		public Void next() {
			this.bar.step();
			return null;
		}
		
	}
	
	private static final Logger LOG = Logger.getLogger("logger");
	
	private final ProgressBarHelper status;
	
	private final List<RNASequence> sequences;

	public DistanceCalculationCoordinator(List<RNASequence> sequences) {
		this.sequences = sequences;
		this.status = new ProgressBarHelper(this.sequences.size(), 10);
	}

	public void startDistanceCalculation() {
		try (DistanceCSVPrinter printer = new DistanceCSVPrinter(
				PropertyHandler.HANDLER.getPropertyValue(PropertyKeys.DISTANCE_FILE_LOCATION, String.class), this.getHeader())) {
			
			int startIndex = PropertyHandler.HANDLER.getPropertyValue(PropertyKeys.DISTANCE_CALCULATION_NEXT_RECORD, Integer.class);
			if (startIndex != 0) {
				LOG.log(Level.INFO, "Continuing calculation of rna distances");
			} else {
				LOG.log(Level.INFO, "Starting calculation of rna distances");
			}			
			
			this.status.setCurrentLineNumber(startIndex);
			for (int i = startIndex; i < this.sequences.size(); ++i) {
				printer.addRecord(this.createRecord(i));
				PropertyHandler.HANDLER.setPropertieValue(PropertyKeys.DISTANCE_CALCULATION_NEXT_RECORD, i + 1);
			}
			
			this.status.stop();
			LOG.log(Level.INFO, "Finished calculation of rna distances");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private List<Object> createRecord(int i) {
		List<Object> record = new ArrayList<>();
		RNASequence current = this.sequences.get(i);

		// add lnc_rna_name to first column and skip empty columns
		record.add(current.getDescription());		
		for (int j = 0; j < i; j++) {
			record.add("");
		}
		record.add(0);
		this.status.next();
		
		this.sequences.stream()
			.skip(i + 1)
			.map(seq -> calculateDistance(current, seq))
			.forEach(record::add);
		
		return record;
	}

	private Object calculateDistance(RNASequence seq1, RNASequence seq2) {
		this.status.next();
		return DistanceCalculatorUtil.calculateDistance(seq1, seq2);
	}

	private String[] getHeader() {
		String[] header = this.sequences.stream()
			.map(RNASequence::getDescription)
			.toArray(String[]::new);
		
		return ArrayUtils.addFirst(header, "lnc_rna_name");
	}
}