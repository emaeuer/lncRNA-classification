package de.lncrna.classification.init.distance;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.biojava.nbio.core.sequence.RNASequence;

import de.lncrna.classification.cli.ProgressBarHelper;
import de.lncrna.classification.db.Neo4jDatabaseSingleton;
import de.lncrna.classification.init.distance.calculation.BlastDistanceCalculator;
import de.lncrna.classification.init.distance.calculation.DistanceCalculator;
import de.lncrna.classification.util.PropertyHandler;
import de.lncrna.classification.util.PropertyKeyHelper.PropertyKeys;
import de.lncrna.classification.util.data.DistanceDAO;

public class DistanceCalculationCoordinator {
	
	private static final Logger LOG = Logger.getLogger("logger");
	
	private ProgressBarHelper status;
	
	private final List<RNASequence> sequences;
	
	private final ExecutorService executor;
	
	private int numberOfRunningThreads = 0;
	
	private int numberOfFinishedSequences = 0;
	
	private final DistanceCalculator distanceCalculator;	
	
	public DistanceCalculationCoordinator(List<RNASequence> sequences, DistanceCalculator distanceCalculator) {
		int threadNumber = PropertyHandler.HANDLER.getPropertyValue(PropertyKeys.DISTANCE_CALCULATION_THREAD_COUNT, Integer.class);
		
		this.sequences = sequences;
		this.executor = Executors.newFixedThreadPool(threadNumber);
		this.distanceCalculator = distanceCalculator;
	}
	
	public void startDistanceCalculation() {
		int startIndex = PropertyHandler.HANDLER.getPropertyValue(PropertyKeys.NEXT_RECORD, Integer.class);
		this.numberOfFinishedSequences = startIndex;
		
		if (startIndex != 0) {
			LOG.log(Level.INFO, "Continuing calculation of rna distances(" + this.distanceCalculator.getDistanceProperties().name() + ")");
		} else {
			LOG.log(Level.INFO, "Starting calculation of rna distances (" + this.distanceCalculator.getDistanceProperties().name() + ")");
		}	
		
		try {			
			this.status = new ProgressBarHelper(this.sequences.size(), startIndex, "Distance calculation");		
			
			Neo4jDatabaseSingleton.getQueryHelper().insertAllSequences(this.sequences);
			
			if (this.distanceCalculator instanceof BlastDistanceCalculator) {
				((BlastDistanceCalculator) this.distanceCalculator).readBlastFile();
			}
			
			calculateDistances(startIndex);
			
			while(numberOfRunningThreads != 0) {
				Thread.sleep(500);
			}
			
			this.executor.shutdownNow();
			this.status.stop();
			LOG.log(Level.INFO, "Finished calculation of rna distances");
		} catch (Exception e) {
			LOG.log(Level.WARNING, "Failed to calculate distances");
			throw new RuntimeException("An unexpected error occured during distance calculation", e);
		}
	}
	
	private void calculateDistances(int currentLine) throws IOException, InterruptedException, ExecutionException {
		int maxThreadCount = PropertyHandler.HANDLER.getPropertyValue(PropertyKeys.DISTANCE_CALCULATION_THREAD_COUNT, Integer.class);
		int waitingTime = PropertyHandler.HANDLER.getPropertyValue(PropertyKeys.DISTANCE_CALCULATION_WAITING_TIME, Integer.class);
		
		// do until no sequences are remaining
		while (currentLine < this.sequences.size()) {
			if (this.numberOfRunningThreads < maxThreadCount) {
				// only run if threads are available and sequences are remaining
				startThreadForNextSequence(currentLine);
				currentLine++;
			} else {
				// currently no thread is available or all sequences were processed 
				// --> wait refreshTime until next iteration
				Thread.sleep(waitingTime);
			}
		}
	}

	private void startThreadForNextSequence(final int line) {
		this.executor.execute(() -> calculateAllDistancesForSequence(line));
		changeNumberOfRunningThreads(true);
	}
	
	private void calculateAllDistancesForSequence(int i) {
		RNASequence current = this.sequences.get(i);		
		
		this.sequences.stream()
			.limit(i)
			.map(seq -> new DistanceDAO(current.getDescription(), seq.getDescription(), this.distanceCalculator.getDistanceProperties().name(), this.distanceCalculator.getDistance(current, seq)))
			.peek(dao -> this.status.next())
			.filter(dao -> dao.getDistanceValue() != -1)
			.forEach(Neo4jDatabaseSingleton.getQueryHelper()::addDistance);
		
		this.status.next();
		changeNumberOfRunningThreads(false);
		finishSequence(i);
	}

	private synchronized void changeNumberOfRunningThreads(boolean increment) {
		if (increment) {
			this.numberOfRunningThreads++;
		} else {
			this.numberOfRunningThreads--;
		}
	}
	
	private synchronized void finishSequence(int i) {
		if (i > this.numberOfFinishedSequences) {
			this.numberOfFinishedSequences = i;
			PropertyHandler.HANDLER.setPropertieValue(PropertyKeys.NEXT_RECORD, this.numberOfFinishedSequences);
		}
	}

}
