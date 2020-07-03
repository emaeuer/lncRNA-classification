package de.lncrna.classification.init.distance;

import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.lncrna.classification.db.Neo4JCypherQueries;
import de.lncrna.classification.util.data.DistanceDAO;

public class DistancePersister implements Subscriber<DistanceDAO> {

	private static final Logger LOG = Logger.getLogger("logger");
	
	private Subscription subscription;
	
	@Override
	public void onSubscribe(Subscription subscription) {
		LOG.log(Level.WARNING, "Subscription opened");		
		this.subscription = subscription;
		this.subscription.request(1);		
	}

	@Override
	public void onNext(DistanceDAO dao) {
		Neo4JCypherQueries.addDistance(dao);
		this.subscription.request(1);
	}

	@Override
	public void onError(Throwable e) {
		LOG.log(Level.WARNING, "Subscription closed exceptionally", e);		
	}

	@Override
	public void onComplete() {
		LOG.log(Level.WARNING, "Subscription closed normally");	
	}

}
