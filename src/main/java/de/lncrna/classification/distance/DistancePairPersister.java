package de.lncrna.classification.distance;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import de.lncrna.classification.db.Neo4jDatabaseSingleton;
import de.lncrna.classification.util.PropertyHandler;
import de.lncrna.classification.util.PropertyKeyHelper.PropertyKeys;

public class DistancePairPersister implements Subscriber<DistancePair> {

	private static final Logger LOG = Logger.getLogger("logger");
	
	private Subscription subscription;
	
	private ExecutorService executor = Executors.newFixedThreadPool(5, new ThreadFactoryBuilder().setNameFormat("persist-helper-thread-%d").build());
	
	private final Queue<Future<?>> runningTasks = new LinkedList<>();
	
	@Override
	public void onSubscribe(Subscription subscription) {
		this.subscription = subscription;
		this.subscription.request(1);
		
	}

	@Override
	public void onNext(DistancePair item) {
		while (runningTasks.peek() != null && runningTasks.peek().isDone()) {
			runningTasks.poll();
		}
		try {		
//			executor.submit(() -> Neo4jDatabaseSingleton.getQueryHelper().addDistance(item));
			runningTasks.add(executor.submit(() -> Neo4jDatabaseSingleton.getQueryHelper().addDistance(item)));
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
		int sleepTime = PropertyHandler.HANDLER.getPropertyValue(PropertyKeys.DISTANCE_CALCULATION_WAITING_TIME, Integer.class);
		
		// Wait until all threads finished
		while (!runningTasks.isEmpty()) {
			while (runningTasks.peek() != null && runningTasks.peek().isDone()) {
				runningTasks.poll();
			}
			try {
				Thread.sleep(sleepTime);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		this.executor.shutdownNow();
		
		LOG.log(Level.INFO, "Closing this branch of the flow");
	}

}
