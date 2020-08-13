package de.lncrna.classification.distance;

import java.util.concurrent.Flow.Processor;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import java.util.concurrent.SubmissionPublisher;

public abstract class DistanceCalculator implements Processor<DistancePair, DistancePair> {

	private final SubmissionPublisher<DistancePair> publisher = new SubmissionPublisher<>();
	private Subscription subscription;
	
	public abstract float getDistance(DistancePair pair);
	
	public abstract DistanceType getDistanceProperties();
	
	@Override
	public void onSubscribe(Subscription subscription) {
		this.subscription = subscription;
		this.subscription.request(1);
	}

	@Override
	public void onNext(DistancePair item) {
		try  {
			if (item != null) {
				float distance = getDistance(item);
				
				if (distance != -1) {
					item.setDistance(distance);
					item.setDistanceType(getDistanceProperties());
					this.publisher.submit(item);
				}
			}
			this.subscription.request(1);
		} catch (Throwable e) {
			onError(e);
		}
	}

	@Override
	public void onError(Throwable throwable) {
		this.publisher.closeExceptionally(throwable);
		
	}

	@Override
	public void onComplete() {
		this.publisher.close();
	}

	@Override
	public void subscribe(Subscriber<? super DistancePair> subscriber) {
		this.publisher.subscribe(subscriber);		
	}
	
}
