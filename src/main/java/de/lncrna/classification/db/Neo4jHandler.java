package de.lncrna.classification.db;

import java.util.function.Function;

public interface Neo4jHandler<T> extends AutoCloseable {

	public abstract void commitQuery(String query);
	
	public abstract <R> R executeQuery(String query, Function<T, R> mapper);
	
}
