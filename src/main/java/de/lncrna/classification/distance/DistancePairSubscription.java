package de.lncrna.classification.distance;

import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import java.util.concurrent.ForkJoinPool;

import de.lncrna.classification.util.PropertyHandler;
import de.lncrna.classification.util.PropertyKeyHelper.PropertyKeys;

public class DistancePairSubscription implements Subscription {
	
	private final DistancePairSupplier publisher;
	private final Subscriber<? super DistancePair> subscriber;
	
	private final ForkJoinPool pool = new ForkJoinPool();
	
	public DistancePairSubscription(DistancePairSupplier publisher, Subscriber<? super DistancePair> subscriber) {
		this.publisher = publisher;
		this.subscriber = subscriber;
	}

	@Override
	public void request(long n) {		
		int threadNumber = PropertyHandler.HANDLER.getPropertyValue(PropertyKeys.DISTANCE_CALCULATION_THREAD_COUNT, Integer.class);
		if (this.pool.getRunningThreadCount() > 3 * threadNumber) {
			// TODO let thread wait
			this.subscriber.onNext(null);
			return;
		}
		
		for(int i = 0; i < n; i++) {
			pool.execute(() -> {
				try {
					this.subscriber.onNext(this.publisher.nextSequence());
				} catch (InterruptedException e) {
					// TODO
					e.printStackTrace();
				}
			}); 
		}
	}

	@Override
	public void cancel() {
		int waitingTime = PropertyHandler.HANDLER.getPropertyValue(PropertyKeys.DISTANCE_CALCULATION_WAITING_TIME, Integer.class);
		try {
			// wait until all values are processed
			while (!this.publisher.availableValues().isEmpty()) {
				Thread.sleep(waitingTime);
			}
			
			// wait until all task are finished 
			this.pool.shutdown();
			while (this.pool.isTerminating()) {
				Thread.sleep(waitingTime);
			}
			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		subscriber.onComplete();		
	}
}
