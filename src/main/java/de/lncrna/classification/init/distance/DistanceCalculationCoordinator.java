package de.lncrna.classification.init.distance;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;

import org.apache.commons.lang.UnhandledException;
import org.apache.commons.lang3.ArrayUtils;
import org.biojava.nbio.core.sequence.RNASequence;

import de.lncrna.classification.util.PropertyHandler;
import de.lncrna.classification.util.PropertyKeys;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;


public class DistanceCalculationCoordinator {
	
	private class ProgressBarHelper {
		
		private final ProgressBar bar;
		
		public ProgressBarHelper(int listSize, int startIndex) {
			long totalNumberOfCalculations = (Long.valueOf(listSize) * (listSize + 1)) / 2;
			this.bar = new ProgressBarBuilder()
					.setInitialMax(totalNumberOfCalculations)
					.setTaskName("Calculation")
					.setStyle(ProgressBarStyle.ASCII)
					.setUpdateIntervalMillis(2000)
					.showSpeed()
					.build();
			this.bar.stepTo(IntStream.range(0, startIndex).sum());
		}
		
		public void stop() {
			this.bar.close();
		}

		public synchronized void next() {
			this.bar.step();
		}
		
	}
	
	private static final Logger LOG = Logger.getLogger("logger");
	
	private ProgressBarHelper status;
	
	private final List<RNASequence> sequences;
	
	private ExecutorService executor;

	public DistanceCalculationCoordinator(List<RNASequence> sequences) {
		this.sequences = sequences;
		this.executor = Executors.newFixedThreadPool(
				PropertyHandler.HANDLER.getPropertyValue(PropertyKeys.DISTANCE_CALCULATION_THREAD_COUNT, Integer.class));
	}

	public void startDistanceCalculation() {
		int startIndex = PropertyHandler.HANDLER.getPropertyValue(PropertyKeys.DISTANCE_CALCULATION_NEXT_RECORD, Integer.class);
		
		try (DistanceCSVPrinter printer = initPrinter(startIndex != 0)) {			
			this.status = new ProgressBarHelper(this.sequences.size(), startIndex);
						
			calculateDistances(printer, startIndex);
			
			this.executor.shutdownNow();
			this.status.stop();
			LOG.log(Level.INFO, "Finished calculation of rna distances");
		} catch (Exception e) {
			LOG.log(Level.WARNING, "Failed to calculate distances");
			throw new UnhandledException("An unexpected error occured during distance calculation", e);
		}
	}

	private DistanceCSVPrinter initPrinter(boolean append) throws IOException {
		String csvFileLocation = PropertyHandler.HANDLER.getPropertyValue(PropertyKeys.DISTANCE_FILE_LOCATION, String.class);
		DistanceCSVPrinter printer = new DistanceCSVPrinter(csvFileLocation, this.getHeader(), append);
		
		if (append) {
			LOG.log(Level.INFO, "Continuing calculation of rna distances");
		} else {
			LOG.log(Level.INFO, "Starting calculation of rna distances");
		}	
		
		return printer;
	}
	
	private void calculateDistances(DistanceCSVPrinter printer, int currentLine) throws IOException, InterruptedException, ExecutionException {
		int threadCount = PropertyHandler.HANDLER.getPropertyValue(PropertyKeys.DISTANCE_CALCULATION_THREAD_COUNT, Integer.class);
		
		Queue<Future<Entry<Integer, List<Object>>>> awaitingResults = new LinkedList<>();
		
		// do until no sequences are remaining and all threads are finished
		while (currentLine < this.sequences.size() || !awaitingResults.isEmpty()) {
			if (awaitingResults.size() < threadCount && currentLine < this.sequences.size()) {
				// only run if threads are available and sequences are remaining
				startThredForNextRecord(awaitingResults, currentLine);
				currentLine++;
			} else {
				// currently no thread is available or all sequences were processed --> wait for termination of threat(s)
				waitForTerminationOfThread(awaitingResults, printer);
			}
		}
	}

	private void startThredForNextRecord(Queue<Future<Entry<Integer, List<Object>>>> awaitingResults, final int line) {
		awaitingResults.add(this.executor.submit(() -> createRecord(line)));
	}
	
	private void waitForTerminationOfThread(Queue<Future<Entry<Integer, List<Object>>>> awaitingResults, DistanceCSVPrinter printer) throws IOException, InterruptedException, ExecutionException {
		Entry<Integer, List<Object>> result = awaitingResults.poll().get();
		// Waits until result is available
		printer.addRecord(result.getValue());
		PropertyHandler.HANDLER.setPropertieValue(PropertyKeys.DISTANCE_CALCULATION_NEXT_RECORD, result.getKey() + 1);
	}


	private Entry<Integer, List<Object>> createRecord(int i) {
		List<Object> record = new ArrayList<>();
		RNASequence current = this.sequences.get(i);		
		
		record.add(current.getDescription());
		
		this.sequences.stream()
			.limit(i)
			.map(seq -> calculateDistance(current, seq))
			.forEach(record::add);
		
		record.add(0);
		this.status.next();
		
		return new AbstractMap.SimpleEntry<>(i, record);
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