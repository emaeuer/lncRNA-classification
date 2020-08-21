package de.lncrna.classification.db;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.lncrna.classification.clustering.Cluster;
import de.lncrna.classification.distance.DistancePair;
import de.lncrna.classification.util.data.DistanceDAO;

public interface Neo4jQueryHelper <T extends Neo4jHandler<R>, R> {

	public static final String GET_ALL_SEQUENCES =
			"MATCH (seq:Sequence) " +
			"RETURN collect(seq.seqName) AS names";
	
	public static final String GET_RANDOM_SEQUENCE_NAMES =
			"Match (seq:Sequence) - [d:distance] - (seq2:Sequence) " +
			"WITH seq.seqName AS s_name, count(d) AS d_count " +
			"WHERE %s <= d_count " +
			"WITH s_name " +
			"ORDER BY rand() " +
			"LIMIT %s " + 
			"RETURN collect(s_name) AS s_names";

	public static final String GET_SEQUENCES_OF_CLUSTROID = 
			"MATCH (clustroids:Sequence) - [d:distance] - (s:Sequence) " +
			"WHERE clustroids.seqName IN %s " +
			"WITH s.seqName AS s_name, min(d.%s) AS min_distance, clustroids.seqName AS c_name " +
			"RETURN c_name, collect(s_name) AS s_names";
	
	public static final String INSERT_ALL_SEQUENCES = 
			"MERGE (:Sequence {seqName:\"%s\"})";
	
	public static final String INSERT_DISTANCE = 
			"MATCH (seq1:Sequence {seqName:\"%s\"}), (seq2:Sequence {seqName:\"%s\"}) " + 
			"MERGE (seq1)-[r:distance]-(seq2) " + 
			"SET r.%s=%s";
	
	public static final String GET_DISTANCE = 
			"MATCH (:Sequence {seqName:\"%s\"}) - [rel:distance] - (:Sequence {seqName:\"%s\"})" + 
			"RETURN rel.%s AS distance";
	
	public static final String GET_AVERAGE_DISTANCE_SEQUENCE = 
			"MATCH (:Sequence {seqName:\"%s\"}) -[r:distance]- ()" + 
			"RETURN avg(r.%s) AS avgDistance";
	
	public static final String GET_ORDERED_DISTANCES = 
			"MATCH (seq1:Sequence) -[r:distance]-> (seq2:Sequence) " + 
			"RETURN seq1.seqName AS seq1, r.%s AS distance, seq2.seqName AS seq2 " + 
			"ORDER BY distance ASCENDING " + 
			"LIMIT %d";
	
	public static final String GET_CLUSTROID = 
			"MATCH (seq:Sequence) " +
			"WHERE seq.seqName IN %s " +
			"WITH collect(seq) AS cluster " +
			"UNWIND cluster AS seq1 " +
			"UNWIND cluster AS seq2 " +
			"OPTIONAL MATCH (seq1:Sequence) -[r:distance]- (seq2:Sequence) " +
			"WITH seq1, r.%s AS distance, seq2 " +
			"WHERE seq1 <> seq2 " +
			"WITH {seqName:seq1.seqName, avgDistance:avg(coalesce(distance, %s))} AS avgDistances " +
			"RETURN avgDistances.seqName AS seqName, avgDistances.avgDistance " +
			"ORDER BY avgDistances.avgDistance " +
			"LIMIT 1";
	
	public static final String GET_AVERAGE_DISTANCE_CLUSTER = 
			"MATCH (seq:Sequence) " +
			"WHERE seq.seqName IN %s " +
			"WITH collect(seq) AS cluster " +
			"UNWIND cluster AS seq1 " +
			"UNWIND cluster AS seq2 " +
			"OPTIONAL MATCH (seq1:Sequence) -[r:distance]- (seq2:Sequence) " +
			"WITH seq1, r.%s AS distance, seq2 " +
			"WHERE seq1 <> seq2 " +
			"RETURN avg(coalesce(distance, %d))  AS avgClusterDistance";
	
	public static final String GET_SEQUENCES_WITHIN_TRESHOLD =
			"OPTIONAL MATCH (:Sequence{seqName:\"%1$s\"})-[r:distance]-(seq:Sequence) " +  
			"WHERE r.%4$s < %2$s " +
			"WITH collect(seq.seqName) AS tightSequences " +
			"OPTIONAL MATCH (:Sequence{seqName:\"%1$s\"})-[r:distance]-(seq:Sequence) " + 
			"WHERE %2$s <= r.%4$s <= %3$s " +
			"RETURN {tightSequences:tightSequences, looseSequences:collect(seq.seqName)} AS result";
	
	public static final String REMOVE_CLUSTER_INFORMATION =
			"MATCH (c:Cluster {algorithm:\"%s\", distance:\"%s\"}) " + 
			"DETACH DELETE c";
	
	public static final String INSERT_CLUSTER_INFORMATION = 
			"CREATE (c:Cluster {algorithm:\"%s\", distance:\"%s\"}) " +
			"WITH c " + 
			"MATCH (seq:Sequence) " +
			"WHERE seq.seqName IN %s " + 
			"CREATE (seq)-[:in_cluster]->(c) " + 
			"WITH c, seq " +
			"WHERE seq.seqName=\"%s\" " + 
			"CREATE (seq)-[:clustroid_of]->(c)";
	
	public static final String GET_ALL_CLUSTERS = 
			"MATCH (c:Cluster {distance:\"%s\", algorithm:\"%s\"}) " +
			"MATCH (seq:Sequence)-[:in_cluster]-(c) " +
//			"WHERE c.persisted IS NULL " +
			"RETURN ID(c) AS id, collect(seq.seqName) AS sequences " +
			"ORDER BY id";
	
	public static final String SET_CLUSTER_PERSISTED = 
			"MATCH (c:Cluster) " + 
			"WHERE ID(c) = %s " +
			"SET c.persisted = true";
	
	// TODO Non existing relations are not considered --> should eventually get distance 1 
	public static final String GET_MAXIMAL_DISTINCE_WITHIN_CLUSTER = 
			"MATCH (seq:Sequence) " +
			"WHERE seq.seqName IN %s " +
			"WITH collect(seq) AS cluster " +
			"UNWIND cluster AS seq1 " +
			"UNWIND cluster AS seq2 " +
			"MATCH (seq1)-[r:distance]-(seq2) " +
			"RETURN max(r.%s) AS maxDistance";
	
	
	
	public List<String> getAllSequenceNames();

	public List<String> getRandomSequenceNames(int minDistancRelationships, int amount);

	public Map<String, List<String>> getSequencesWithinCluster(List<String> centroids, String distanceName);

	public void insertAllSequences(List<String> nodes);
	
	public void addDistance(DistanceDAO dao);
	
	public DistanceDAO getDistances(DistanceDAO dao);
	
	public double calculateAverageDistanceOfSequence(String sequence, String distanceName);
	
	public LinkedList<DistanceDAO> getDistancesOrdered(String distanceName, int limit);
	
	public String findClustroid(Collection<String> sequences, String distanceName);
	
	public double getAverageClusterDistance(Collection<String> sequences, String distanceName);
	
	public Map<Boolean, List<String>> getSequencesWithinTresholds(String center, float tightTreshold, float looseTreshold, String distanceName);
	
	public void updateClusterInformation(List<? extends Cluster<?>> clusters);
	
	public float getMaxDistanceWithinCluster(Collection<String> sequences, String distanceName);
	
	public Map<Long, List<String>> getClusters(String distanceName, String algorithmName);

	public void setClusterPersisted(long id);
	
	public void addDistance(DistancePair item);

}
