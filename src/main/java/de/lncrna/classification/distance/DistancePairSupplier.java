package de.lncrna.classification.distance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.collections4.SetUtils;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import de.lncrna.classification.cli.ProgressBarHelper;
import de.lncrna.classification.util.PropertyHandler;
import de.lncrna.classification.util.PropertyKeyHelper.PropertyKeys;

public class DistancePairSupplier implements Publisher<DistancePair>, AutoCloseable {
	
	private Map<DistancePair, Integer> blockStarts = new HashMap<>();
	
	private final Map<String, String> sequences;
	
	private final Map<String, Set<Integer>> madeComparisons = new HashMap<>();
	
	private int currentBlockID;
	
	private ExecutorService subscriberThreadPool;
	
	private ExecutorService publishThreadPool = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("publish-thread-%d").build());
	
	private final List<DistancePairSubscription> subscriptions = new ArrayList<>();
	
	private ProgressBarHelper status;
	
	private int blockCounter = 1;
	
	private int numberOfBlocks = 1;
	
	private AtomicInteger lastPublishedSubscription = new AtomicInteger(0);
	
	private Queue<DistancePair> availableValues = new ConcurrentLinkedQueue<>();
	
	public DistancePairSupplier(Map<String, String> sequences) {
		int threadNumber = PropertyHandler.HANDLER.getPropertyValue(PropertyKeys.DISTANCE_CALCULATION_THREAD_COUNT, Integer.class);
		
		this.sequences = sequences;
		this.subscriberThreadPool = Executors.newFixedThreadPool(threadNumber, new ThreadFactoryBuilder().setNameFormat("subscription-thread-%d").build());
		
		this.status = new ProgressBarHelper();
		
		this.publishThreadPool.execute(this::publishItems);
	}
	
	public void addCompareBlock(List<String> sequenceBlock) {
		int sleepTime = PropertyHandler.HANDLER.getPropertyValue(PropertyKeys.DISTANCE_CALCULATION_WAITING_TIME, Integer.class);
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
				
				DistancePair currentPair = new DistancePair(currentSequenceName, currentSequence, compareSequenceName, compareSequence);
				if (firstPair == null) {
					firstPair = currentPair;
					this.blockStarts.put(firstPair, sequenceBlock.size());
				}
				
				this.availableValues.add(currentPair);
			}
			
			if (this.availableValues.size() > 1000000) {
				while (this.availableValues.size() > 500000) {
					try {
						Thread.sleep(sleepTime);
					} catch (InterruptedException e) {
						continue;
					}
				}
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
	
	private void publishItems() {		
		// stopped in close
		while (true) {
			if (this.subscriptions.isEmpty()) {
				System.out.println("empty");
				continue;
			}
			
			if (Thread.interrupted()) {
				break;
			}
			
			DistancePairSubscription subscription = this.subscriptions.get(this.lastPublishedSubscription.get());
			int remainingCapacity = subscription.getRemainingQueueCapacity();
			
			subscription.supplyValues(pollAvailableValues(remainingCapacity));
			
			this.lastPublishedSubscription.incrementAndGet();
			
			if (this.lastPublishedSubscription.get() >= this.subscriptions.size()) {
				this.lastPublishedSubscription.set(0);
			}
		}
	}

	private List<DistancePair> pollAvailableValues(int remainingCapacity) {
		List<DistancePair> availableValues = new ArrayList<>();
		
		for (int i = 0; i < remainingCapacity && !this.availableValues.isEmpty(); i++) {
			availableValues.add(this.availableValues.poll());
		}
		return availableValues;
	}

	private void updateMadeComparisons(List<String> sequenceBlock) {
		for (String seq : sequenceBlock) {
			Set<Integer> blocks = this.madeComparisons.computeIfAbsent(seq, k -> new HashSet<>());
			blocks.add(this.currentBlockID);
		}
		this.currentBlockID++;
	}
	
	public synchronized void nextSequence(DistancePair next) {
		if (next != null) {
			if (this.blockStarts.containsKey(next)) {
				int listSize = this.blockStarts.remove(next);
				long totalNumberOfCalculations = (Long.valueOf(listSize) * (listSize + 1)) / 2 - listSize;
				this.status.nextBlock(totalNumberOfCalculations, String.format("Block %d of %d", this.blockCounter, this.numberOfBlocks));
				this.blockCounter++;
			}
			this.status.next();
		}
	}

	@Override
	public void subscribe(Subscriber<? super DistancePair> subscriber) {
		int threadNumber = PropertyHandler.HANDLER.getPropertyValue(PropertyKeys.DISTANCE_CALCULATION_THREAD_COUNT, Integer.class);
		if (subscriptions.size() < threadNumber) {
			subscriberThreadPool.submit(() -> {
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
		int sleepTime = PropertyHandler.HANDLER.getPropertyValue(PropertyKeys.DISTANCE_CALCULATION_WAITING_TIME, Integer.class);
		
		while (!this.availableValues.isEmpty()) {
			Thread.sleep(sleepTime);
		}
		
		this.publishThreadPool.shutdownNow();
		this.subscriberThreadPool.shutdownNow();
		
		for (DistancePairSubscription subscribtion : subscriptions) {
			subscribtion.cancel();
		}		
		
		this.status.stop();
	}

	public void setBlockNumber(int blockNumber) {
		this.numberOfBlocks = blockNumber;		
	}
	
}
