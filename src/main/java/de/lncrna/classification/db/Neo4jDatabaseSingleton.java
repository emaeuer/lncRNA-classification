package de.lncrna.classification.db;

public class Neo4jDatabaseSingleton {

	private static Neo4jQueryHelper<?, ?> queryHelper;
	
	private Neo4jDatabaseSingleton() {}
	
	public static void initInstance(boolean embeddedMode) {
		if (embeddedMode) {
			EmbeddedNeo4jHandler handler = new EmbeddedNeo4jHandler();
			Neo4jDatabaseSingleton.queryHelper = new EmbeddedNeo4jQueryHelper(handler);
		} else {
			ServerNeo4jHandler handler = new ServerNeo4jHandler();
			Neo4jDatabaseSingleton.queryHelper = new ServerNeo4jQueryHelper(handler);
		}
	}

	public static Neo4jQueryHelper<?, ?> getQueryHelper() {
		return Neo4jDatabaseSingleton.queryHelper;
	}
	

}
