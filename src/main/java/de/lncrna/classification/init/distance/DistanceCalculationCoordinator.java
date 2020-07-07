package de.lncrna.classification.init.distance;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.SubmissionPublisher;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.biojava.nbio.core.sequence.RNASequence;

import de.lncrna.classification.cli.ProgressBarHelper;
import de.lncrna.classification.db.Neo4JCypherQueriesServer;
import de.lncrna.classification.init.distance.calculation.DistanceCalculator;
import de.lncrna.classification.util.PropertyHandler;
import de.lncrna.classification.util.PropertyKeyHelper.PropertyKeys;
import de.lncrna.classification.util.data.DistanceDAO;

public class DistanceCalculationCoordinator implements Publisher<DistanceDAO> {
	
	private static final Logger LOG = Logger.getLogger("logger");
	
	private ProgressBarHelper status;
	
	private final List<RNASequence> sequences;
	
	private final ExecutorService executor;
	
	private volatile int numberOfRunningThreads = 0;
	
	private final DistanceCalculator distanceCalculator;

	private final SubmissionPublisher<DistanceDAO> publisher = new SubmissionPublisher<>();
	
	
	public DistanceCalculationCoordinator(List<RNASequence> sequences, DistanceCalculator distanceCalculator) {
		int threadNumber = PropertyHandler.HANDLER.getPropertyValue(PropertyKeys.DISTANCE_CALCULATION_THREAD_COUNT, Integer.class);
		
		this.sequences = sequences;
		this.executor = Executors.newFixedThreadPool(threadNumber);
		this.distanceCalculator = distanceCalculator;
	}
	
	public void startDistanceCalculation() {
		int startIndex = PropertyHandler.HANDLER.getPropertyValue(PropertyKeys.NEXT_RECORD, Integer.class);
		
		if (startIndex != 0) {
			LOG.log(Level.INFO, "Continuing calculation of rna distances(" + this.distanceCalculator.getDistanceProperties().name() + ")");
		} else {
			LOG.log(Level.INFO, "Starting calculation of rna distances (" + this.distanceCalculator.getDistanceProperties().name() + ")");
		}	
		
		try {			
			this.status = new ProgressBarHelper(this.sequences.size(), startIndex, "Distance calculation");		
			
			calculateDistances(startIndex);
			
			while(numberOfRunningThreads != 0) {
				Thread.sleep(500);
			}
			
			this.publisher.close();
			this.executor.shutdownNow();
			this.status.stop();
			LOG.log(Level.INFO, "Finished calculation of rna distances");
		} catch (Exception e) {
			LOG.log(Level.WARNING, "Failed to calculate distances");
			this.publisher.closeExceptionally(e);
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
		this.executor.execute(() -> createRecord(line));
		changeNumberOfRunningThreads(true);
	}
	
	private void createRecord(int i) {
		RNASequence current = this.sequences.get(i);		
		
		this.sequences.stream()
			.limit(i)
			.map(seq -> new DistanceDAO(current.getDescription(), seq.getDescription(), this.distanceCalculator.getDistanceProperties().name(), this.distanceCalculator.getDistance(current, seq)))
			.peek(dao -> this.status.next())
			.filter(dao -> dao.getDistanceValue() != -1)
			.forEach(Neo4JCypherQueriesServer::addDistance);
		
		this.status.next();
		this.changeNumberOfRunningThreads(false);
	}
	
	private synchronized void changeNumberOfRunningThreads(boolean increment) {
		if (increment) {
			this.numberOfRunningThreads++;
		} else {
			this.numberOfRunningThreads--;
		}
	}
	
	@Override
	public void subscribe(Subscriber<? super DistanceDAO> subscriber) {
		publisher.subscribe(subscriber);
	}

}
