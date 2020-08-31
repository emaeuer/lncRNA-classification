package de.lncrna.classification.util;

public class PropertyKeyHelper {

	public enum PropertyKeys {
		// general properties
		FASTA_FILE_LOCATION(true), 
		FASTA_DOWNLOAD_URL(false),
		DISTANCE_CALCULATION_THREAD_COUNT(false),
		DISTANCE_CALCULATION_WAITING_TIME(false), 
		FILE_LOCATION(true),
		NEXT_RECORD(true),
		INTRON_API_URL_PATTERN(false),
		STAT_REFRESH_INTERVAL(false),
		STAT_LOG_FILE(false),
		
		// Neo4J-Properties
		NEO4J_LOCATION(false),
		NEO4J_DATABASE_NAME(false),
		NEO4J_CONFIG(false),
		
		// Needleman-Wunsch specific properties 
		PAIRWISE_SEQUENCE_ALIGNER_TYPE(false),
		GAP_OPEN_PENALTY(false),
		GAP_CLOSE_PENALTY(false), 
		
		// NGramDistance specific properties
		N_GRAM_LENGTH(false),
		
		// BlastDistance specific properties
		BLAST_RESULT_FILE_LOCATION(false),
		
		// Hierarchical specific properties
		HIERARCHICAL_CLUSTER_COUNT(false),
		HIERARCHICAL_AVERAGE_CLUSTER_DISTANCE_THRESHOLD(false),
		
		// KMeans specific properties
		KMEANS_CLUSTER_COUNT(false),
		KMEANS_AVERAGE_CLUSTER_DISTANCE_THRESHOLD(false),
		MAX_ITERATION(false),
		MIN_AMOUNT_SEQUENCES_IN_CLUSTER(false),
		
		// Canopy clustering specific properties
		LOOSE_THRESHOLD(false),
		TIGHT_THRESHOLD(false);

		private final boolean prefixNecessary;
		
		PropertyKeys(boolean prefixNecessary) {
			this.prefixNecessary = prefixNecessary;
		}

		public boolean isPrefixNecessary() {
			return prefixNecessary;
		}
	}
	
	private static String prefix;
	
	private PropertyKeyHelper() {}
	
	public static void setGlobalPrefix(String prefix) {
		PropertyKeyHelper.prefix = prefix.toUpperCase();
	}
	
	public static String getKey(PropertyKeys key) {
		if (key.isPrefixNecessary()) {
			return String.format("%s_%s", prefix, key.name());
		}
		return key.name();
	}
	
}
