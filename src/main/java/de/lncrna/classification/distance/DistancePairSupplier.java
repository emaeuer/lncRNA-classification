package de.lncrna.classification.distance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections4.SetUtils;

import de.lncrna.classification.cli.ProgressBarHelper;
import de.lncrna.classification.util.PropertyHandler;
import de.lncrna.classification.util.PropertyKeyHelper.PropertyKeys;

public class DistancePairSupplier implements Publisher<DistancePair>, AutoCloseable {
	
	private Map<DistancePair, Integer> blockStarts = new HashMap<>();
	
	private final Map<String, String> sequences;
	
	private final Map<String, Set<Integer>> madeComparisons = new HashMap<>();
	
	private int currentBlockID;
	
	private ExecutorService service;
	
	private BlockingQueue<DistancePair> values = new ArrayBlockingQueue<>(100000);
	
	private final List<DistancePairSubscription> subscriptions = new ArrayList<>();
	
	private ProgressBarHelper status;
	
	private int blockCounter = 1;
	
	private int numberOfBlocks = 1;
	
	public DistancePairSupplier(Map<String, String> sequences) {
		int threadNumber = PropertyHandler.HANDLER.getPropertyValue(PropertyKeys.DISTANCE_CALCULATION_THREAD_COUNT, Integer.class);
		
		this.sequences = sequences;
		this.service = Executors.newFixedThreadPool(threadNumber);
		
		this.status = new ProgressBarHelper();
	}
	
	public void addCompareBlock(List<String> sequenceBlock) {
		int waitingTime = PropertyHandler.HANDLER.getPropertyValue(PropertyKeys.DISTANCE_CALCULATION_WAITING_TIME, Integer.class);
		DistancePair firstPair = null;
		
		for (int i = 0; i < sequenceBlock.size(); i++) {
			String currentSequenceName = sequenceBlock.get(i);
			String currentSequence = this.sequences.get(currentSequenceName);
			
			for (int j = 0; j < i; j++) {
				String compareSequenceName = sequenceBlock.get(j);
				String compareSequence = this.sequences.get(compareSequenceName);
				
				if (checkComparisonAlreadyMade(currentSequence, compareSequence)) {
					continue;
				}
				
				while (values.remainingCapacity() == 0) {
					try {
						Thread.sleep(waitingTime);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				DistancePair currentPair = new DistancePair(currentSequenceName, currentSequence, compareSequenceName, compareSequence);
				if (firstPair == null) {
					firstPair = currentPair;
					this.blockStarts.put(firstPair, sequenceBlock.size());
				}
				this.values.add(currentPair);
			}
		}
		updateMadeComparisons(sequenceBlock);
	}
	
	private boolean checkComparisonAlreadyMade(String currentSequence, String compareSequence) {
		Set<Integer> blocks1 = this.madeComparisons.get(currentSequence);
		Set<Integer> blocks2 = this.madeComparisons.get(compareSequence);
		
		if (blocks1 == null || blocks2 == null) {
			return false;
		}
		
		// check if the sequences were in a previous block together
		return !SetUtils.intersection(blocks1, blocks2).isEmpty();
	}

	private void updateMadeComparisons(List<String> sequenceBlock) {
		for (String seq : sequenceBlock) {
			Set<Integer> blocks = this.madeComparisons.computeIfAbsent(seq, k -> new HashSet<>());
			blocks.add(this.currentBlockID);
		}
		this.currentBlockID++;
	}

	public BlockingQueue<DistancePair> availableValues() {
		return this.values;
	}
	
	public synchronized DistancePair nextSequence() throws InterruptedException {
		DistancePair next = this.values.poll(10, TimeUnit.SECONDS);
		if (next != null) {
			if (this.blockStarts.containsKey(next)) {
				int listSize = this.blockStarts.get(next);
				long totalNumberOfCalculations = (Long.valueOf(listSize) * (listSize + 1)) / 2 - listSize;
				this.status.nextBar(totalNumberOfCalculations, String.format("Block %d of %d", this.blockCounter, this.numberOfBlocks));
				this.blockCounter++;
			}
			this.status.next();
		}
		return next;
	}

	@Override
	public void subscribe(Subscriber<? super DistancePair> subscriber) {
		int threadNumber = PropertyHandler.HANDLER.getPropertyValue(PropertyKeys.DISTANCE_CALCULATION_THREAD_COUNT, Integer.class);
		if (subscriptions.size() < threadNumber) {
			service.submit(() -> {
				DistancePairSubscription subscription = new DistancePairSubscription(this, subscriber);
				this.subscriptions.add(subscription);
				subscriber.onSubscribe(subscription);
			});	
		} else {
			throw new RejectedExecutionException("Reached maximal number of subscriptions");
		}
	}

	@Override
	public void close() throws Exception {
		for (DistancePairSubscription subscribtion : subscriptions) {
			subscribtion.cancel();
		}		
		this.status.stop();
	}

	public void setBlockNumber(int blockNumber) {
		this.numberOfBlocks = blockNumber;		
	}
	
}
