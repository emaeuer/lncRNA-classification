package de.lncrna.classification.distance;

import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;

import de.lncrna.classification.db.Neo4jDatabaseSingleton;

public class DistancePairPersister implements Subscriber<DistancePair> {

	private Subscription subscription;
	
	@Override
	public void onSubscribe(Subscription subscription) {
		this.subscription = subscription;
		this.subscription.request(1);
		
	}

	@Override
	public void onNext(DistancePair item) {
		try {			
			Neo4jDatabaseSingleton.getQueryHelper().addDistance(item);
			this.subscription.request(1);
		} catch (Throwable e) {
			onError(e);
		}
	}

	@Override
	public void onError(Throwable throwable) {
		throwable.printStackTrace();
		
	}

	@Override
	public void onComplete() {
		// TODO
	}

}
