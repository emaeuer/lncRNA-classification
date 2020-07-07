package de.lncrna.classification.db;

import java.util.function.Function;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;

public class Neo4JHelperServer implements AutoCloseable {
	
	public static final Neo4JHelperServer CONNECTION = new Neo4JHelperServer();
	
	private final Driver driver;
	
	private Neo4JHelperServer() {
		driver = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic("neo4j", "rna"));
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
