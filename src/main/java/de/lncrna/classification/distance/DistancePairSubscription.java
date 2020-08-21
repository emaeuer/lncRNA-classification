package de.lncrna.classification.distance;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import de.lncrna.classification.util.PropertyHandler;
import de.lncrna.classification.util.PropertyKeyHelper.PropertyKeys;

public class DistancePairSubscription implements Subscription {
	
	private final DistancePairSupplier publisher;
	
	private final Subscriber<? super DistancePair> subscriber;
	
	private final BlockingQueue<DistancePair> values = new LinkedBlockingQueue<>(20000);
	
	private Executor executor = Executors.newFixedThreadPool(2);
	
	public DistancePairSubscription(DistancePairSupplier distancePairSupplier, Subscriber<? super DistancePair> subscriber) {
		this.subscriber = subscriber;
		this.publisher = distancePairSupplier;
	}

	@Override
	public void request(long n) {	
		try {
			DistancePair next = this.values.poll(10, TimeUnit.SECONDS);
			this.publisher.nextSequence(next);
			executor.execute(() -> this.subscriber.onNext(next));
		} catch (InterruptedException e) {
			this.subscriber.onError(e);
		}
		
	}

	@Override
	public void cancel() {
		int waitingTime = PropertyHandler.HANDLER.getPropertyValue(PropertyKeys.DISTANCE_CALCULATION_WAITING_TIME, Integer.class);
		try {
			// wait until all values are processed
			while (!this.values.isEmpty()) {
				Thread.sleep(waitingTime);
			}
			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		subscriber.onComplete();		
	}
	
	public synchronized int getRemainingQueueCapacity() {
		return this.values.remainingCapacity();
	}
	
	public void supplyValues(List<DistancePair> items) {
		this.values.addAll(items);
	}

	public void supplyValue(DistancePair item) {
		this.values.add(item);
	}
	
}
