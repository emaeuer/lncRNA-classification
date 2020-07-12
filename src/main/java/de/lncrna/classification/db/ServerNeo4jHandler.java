package de.lncrna.classification.db;

import java.util.function.Function;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;

public class ServerNeo4jHandler implements Neo4jHandler<Result> {
	
	private final Driver driver;
	
	public ServerNeo4jHandler() {
		driver = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic("neo4j", "test"));
		
		// Register a shutdown hook 
		Runtime.getRuntime().addShutdownHook(new Thread(() -> close())); 
	}
	
	public void commitQuery(String query) {
		try (Session session = driver.session()) {
			try (Transaction tx = session.beginTransaction()) {		
				tx.run(query);
				tx.commit();
			}
		}
	}
	
	public <R> R executeQuery(String query, Function<Result, R> mapper) {
		try (Session session = driver.session()) {
			try (Transaction tx = session.beginTransaction()) {				
				return mapper.apply(tx.run(query));
			}
		}
	}

	@Override
	public void close() {
		this.driver.close();
	}

}
