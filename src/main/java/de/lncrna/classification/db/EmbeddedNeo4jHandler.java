package de.lncrna.classification.db;

import java.io.File;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.neo4j.configuration.connectors.BoltConnector;
import org.neo4j.configuration.helpers.SocketAddress;
import org.neo4j.dbms.api.DatabaseManagementService;
import org.neo4j.dbms.api.DatabaseManagementServiceBuilder;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.schema.IndexDefinition;
import org.neo4j.kernel.DeadlockDetectedException;

import de.lncrna.classification.util.PropertyHandler;
import de.lncrna.classification.util.PropertyKeyHelper.PropertyKeys;

public class EmbeddedNeo4jHandler implements Neo4jHandler<Result> {
	
	private static final Logger LOG = Logger.getLogger("logger");
	
	private final DatabaseManagementService managementService;
	private final GraphDatabaseService graphDB;
	
	public EmbeddedNeo4jHandler() {
		this.managementService = initManagementService();
		this.graphDB = this.managementService.database(PropertyHandler.HANDLER.getPropertyValue(PropertyKeys.NEO4J_DATABASE_NAME, String.class));
				
		initSchemeIfNecessary();
	}

	private DatabaseManagementService initManagementService() {
		DatabaseManagementService manager = new DatabaseManagementServiceBuilder(PropertyHandler.HANDLER.getPropertyValue(PropertyKeys.NEO4J_LOCATION, File.class))
				.loadPropertiesFromFile(PropertyHandler.HANDLER.getPropertyValue(PropertyKeys.NEO4J_CONFIG, String.class))
				.setConfig(BoltConnector.enabled, true)
				.setConfig(BoltConnector.listen_address, new SocketAddress("localhost", 7687))
				.build();
		
		// Register a shutdown hook 
		Runtime.getRuntime().addShutdownHook(new Thread(() -> close())); 
		
		return manager;
	}
	
	private void initSchemeIfNecessary() {
		if (!schemaAlreadyCreated()) {
			try (Transaction tx = this.graphDB.beginTx()) {
				// Create index on sequence name
	        	tx.schema()
	            	.indexFor(Label.label("Sequence"))  
	                .on("seqName")                                  
	                .withName("seqNameIndex")                           
	                .create();     
	        
	            tx.commit();                                               
	        }
        }

	}

	private boolean schemaAlreadyCreated() {
		try (Transaction tx = graphDB.beginTx()) {
	        Iterable<IndexDefinition> definitions =  tx.schema().getIndexes(Label.label("Sequence"));
	        for (IndexDefinition definition : definitions) {
				if ("seqNameIndex".equals(definition.getName())) {
					return true;
				}
			}
	    }
		return false;
	}
	
	@Override
	public void commitQuery(String query) {
		try (Transaction tx = graphDB.beginTx()) {			
			tx.execute(query);
			tx.commit();
		} catch (DeadlockDetectedException e) {
			LOG.log(Level.INFO, "Neo4j detected a deadlock and aborted the transaction. Restarting the transaction and trying again");
			commitQuery(query);
		}
	}
	
	@Override
	public <R> R executeQuery(String query, Function<Result, R> mapper) {
		try (Transaction tx = graphDB.beginTx()) {	
			return mapper.apply(tx.execute(query));
		} catch (DeadlockDetectedException e) {
			LOG.log(Level.INFO, "Neo4j detected a deadlock and aborted the transaction. Restarting the transaction and trying again");
			return executeQuery(query, mapper);
		}
	}

	@Override
	public void close() {
		managementService.shutdown();
	}

}
