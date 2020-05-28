package de.lncrna.classification.init.distance;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;

import org.apache.commons.lang3.ArrayUtils;
import org.biojava.nbio.core.sequence.RNASequence;

import de.lncrna.classification.util.PropertyHandler;
import de.lncrna.classification.util.PropertyKeys;
import de.lncrna.classification.util.csv.DistanceCSVPrinter;
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
	
	private final ExecutorService executor;
	
	private final Map<Integer, List<Object>> finishedUnsafedLines = new ConcurrentHashMap<>();
	
	private volatile int numberOfRunningThreads = 0;

	public DistanceCalculationCoordinator(List<RNASequence> sequences) {
		// add one to the thread number (additional thread is used for printing)
		int threadNumber = PropertyHandler.HANDLER.getPropertyValue(PropertyKeys.DISTANCE_CALCULATION_THREAD_COUNT, Integer.class) + 1;
		
		this.sequences = sequences;
		this.executor = Executors.newFixedThreadPool(threadNumber);
	}

	public void startDistanceCalculation() {
		int startIndex = PropertyHandler.HANDLER.getPropertyValue(PropertyKeys.DISTANCE_CALCULATION_NEXT_RECORD, Integer.class);
		
		try {			
			this.status = new ProgressBarHelper(this.sequences.size(), startIndex);
			
			Future<?> printerFinished = this.executor.submit(() -> printAvailableLines(startIndex));			
			
			calculateDistances(startIndex);
			
			// Wait until the printer thread finishes
			printerFinished.get();
			
			this.executor.shutdownNow();
			this.status.stop();
			LOG.log(Level.INFO, "Finished calculation of rna distances");
		} catch (Exception e) {
			LOG.log(Level.WARNING, "Failed to calculate distances");
			throw new RuntimeException("An unexpected error occured during distance calculation", e);
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
	
	private void calculateDistances(int currentLine) throws IOException, InterruptedException, ExecutionException {
		int maxThreadCount = PropertyHandler.HANDLER.getPropertyValue(PropertyKeys.DISTANCE_CALCULATION_THREAD_COUNT, Integer.class);
		int waitingTime = PropertyHandler.HANDLER.getPropertyValue(PropertyKeys.DISTANCE_CALCULATION_WAITING_TIME, Integer.class);
		
		// do until no sequences are remaining
		while (currentLine < this.sequences.size()) {
			if (this.numberOfRunningThreads < maxThreadCount) {
				// only run if threads are available and sequences are remaining
				startThreadForNextRecord(currentLine);
				currentLine++;
			} else {
				// currently no thread is available or all sequences were processed 
				// --> wait refreshTime until next iteration
				Thread.sleep(waitingTime);
			}
		}
	}

	private void printAvailableLines(int nextLineToPrint) {
		int waitingTime = PropertyHandler.HANDLER.getPropertyValue(PropertyKeys.DISTANCE_CALCULATION_WAITING_TIME, Integer.class);
		
		try (DistanceCSVPrinter printer = initPrinter(nextLineToPrint != 0)) {
			while (nextLineToPrint < this.sequences.size()) {
				nextLineToPrint = checkDataAvailableForPrinting(printer, nextLineToPrint);
				Thread.sleep(waitingTime);
			}
		} catch (Exception e) {
			LOG.log(Level.WARNING, "Unexpected error while trying to print", e);
		}
	}

	private void startThreadForNextRecord(final int line) {
		this.executor.execute(() -> createRecord(line));
		changeNumberOfRunningThreads(true);
	}
	
	private int checkDataAvailableForPrinting(DistanceCSVPrinter printer, int nextLineToPrint) throws IOException, InterruptedException, ExecutionException {
		// print lines in line order
		while (this.finishedUnsafedLines.containsKey(nextLineToPrint)) {
			// the next necessary line is available and will be printed
			List<Object> lineToPrint = this.finishedUnsafedLines.remove(nextLineToPrint);
			printer.addRecord(lineToPrint);
			nextLineToPrint++;
		}
		PropertyHandler.HANDLER.setPropertieValue(PropertyKeys.DISTANCE_CALCULATION_NEXT_RECORD, nextLineToPrint);
		return nextLineToPrint;
	}


	private void createRecord(int i) {
		List<Object> record = new ArrayList<>();
		RNASequence current = this.sequences.get(i);		
		
		record.add(current.getDescription());
		
		this.sequences.stream()
			.limit(i)
			.map(seq -> calculateDistance(current, seq))
			.forEach(record::add);
		
		record.add(0);
		this.status.next();
		
		this.finishedUnsafedLines.put(i, record);
		this.changeNumberOfRunningThreads(false);
	}
	
	private synchronized void changeNumberOfRunningThreads(boolean increment) {
		if (increment) {
			this.numberOfRunningThreads++;
		} else {
			this.numberOfRunningThreads--;
		}
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