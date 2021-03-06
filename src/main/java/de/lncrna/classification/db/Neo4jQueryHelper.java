package de.lncrna.classification.db;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.lncrna.classification.clustering.Cluster;
import de.lncrna.classification.distance.DistancePair;

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
			"MATCH (s:Sequence) " +
			"WHERE NOT s.seqName IN %1$s " +
			"WITH collect(s) AS sequences " +
			"MATCH (clustroid:Sequence) " +
			"WHERE clustroid.seqName IN %1$s " +
			"WITH sequences, collect(clustroid) AS clustroids " +
			"UNWIND sequences AS sequence " +
			"CALL { " +
			"	 WITH sequence, clustroids " +
			"    MATCH (sequence:Sequence) - [d:distance] - (clustroid:Sequence) " +
			"	 WHERE clustroid IN clustroids " +
			"    RETURN sequence.seqName AS seqName, clustroid.seqName AS clustroidName " +
			"    ORDER BY d.%2$s " +
			"    LIMIT 1 " +
			"} " +
			"RETURN  clustroidName AS c_name, collect(seqName) AS s_names";
	
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
			"Skip %d LIMIT %d";
	
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
	
	public static final String GET_AVERAGE_DISTANCE_WITHIN_CLUSTER =
			"MATCH (seq:Sequence) " +
			"WHERE seq.seqName IN %1$s " +
			"WITH collect(seq) AS cluster " +
			"UNWIND cluster AS seq1 " +
			"UNWIND cluster AS seq2 " +
			"OPTIONAL MATCH (seq1:Sequence) -[r:distance]- (seq2:Sequence) " +				
			"WITH CASE WHEN seq1 = seq2 THEN 0 WHEN r.%2$s IS NULL THEN %3$d ELSE r.%2$s END AS distance, CASE WHEN size(cluster) > 50 THEN 50 ELSE size(cluster) END AS clusterSize " +
			"LIMIT 2500 " + // Equal to 50^2
			"RETURN CASE clusterSize WHEN 1 THEN 0 ELSE sum(distance) / (clusterSize^2 - clusterSize) END AS avgClusterDistance";
	
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
	
	public static final String GET_MAXIMAL_DISTINCE_WITHIN_CLUSTER = 
			"MATCH (seq:Sequence) " +
			"WHERE seq.seqName IN %s " +
			"WITH collect(seq) AS cluster " +
			"UNWIND cluster AS seq1 " +
			"UNWIND cluster AS seq2 " +
			"MATCH (seq1:Sequence)-[r:distance]->(seq2:Sequence) " +
			"WITH r " +
			"LIMIT 2500 " +
			"RETURN max(r.%s) AS maxDistance";
	
	public static final String GET_AVERAGE_DISTANCE_OF_SEQUENCE_WITHIN_CLUSTER =
			"MATCH (seq:Sequence) " +
			"WHERE seq.seqName IN %1$s AND seq.seqName <> \"%2$s\" " +
			"WITH collect(seq) AS cluster " +
			"UNWIND cluster AS seq " +
			"OPTIONAL MATCH (s:Sequence {seqName:\"%2$s\"})-[r:distance]-(seq:Sequence) " +
			"RETURN avg(coalesce(r.%3$s, 1)) AS avgDistance";	
	
	public static final String GET_AVERAGE_DISTANCE_TO_CLUSTER_OF_NEAREST_CLUSTROID =	
			"MATCH (seq:Sequence {seqName:\"%1$s\"}) " +
			"MATCH (seq)-[:in_cluster]-(clusterOfSeq:Cluster {algorithm:\"%3$s\"})-[:clustroid_of]-(clustroidOfSeq:Sequence) " + 
			"MATCH (seq:Sequence)-[d:distance]-(clustroidOther:Sequence)-[:in_cluster]-(otherCluster:Cluster {algorithm:\"%3$s\"}) " +
			"WHERE otherCluster <> clusterOfSeq " +
			"WITH seq, otherCluster " + 
			"ORDER BY d.%2$s " +
			"LIMIT 1 " +
			"MATCH (otherSequence:Sequence)-[:in_cluster]-(otherCluster:Cluster) " +
			"WITH collect(otherSequence) AS sequencesOfOtherCluster, seq " +
			"UNWIND sequencesOfOtherCluster AS otherSequence " +
			"OPTIONAL MATCH (seq:Sequence)-[d:distance]-(otherSequence:Sequence) " + 
			"RETURN coalesce(avg(coalesce(d.%2$s, %4$f)), %4$f) AS avgDistance";
	
	public static final String GET_AVERAGE_DISTANCE_TO_NEAREST_CLUSTER =
			"MATCH (seq:Sequence {seqName:\"%1$s\"})-[:in_cluster]-(c:Cluster {algorithm:\"%3$s\"}) " + 
			"WITH seq, c " + 
			"MATCH (seq:Sequence)-[d:distance]-(seq2:Sequence)-[:in_cluster]-(c2:Cluster {algorithm:\"%3$s\"}) " + 
			"WHERE NOT (seq2:Sequence)-[:in_cluster]-(c:Cluster) " + 
			"WITH c2, min(d.%2$s) AS minDistance, seq " + 
			"ORDER BY minDistance " + 
			"LIMIT 1 " + 
			"MATCH (cSeq:Sequence)-[:in_cluster]-(c2:Cluster) " + 
			"OPTIONAL MATCH (seq:Sequence)-[d:distance]-(cSeq:Sequence) " +  
			"RETURN avg(coalesce(d.%2$s, %4$d)) AS avgDistance";
	
			
	
	public List<String> getAllSequenceNames();

	public List<String> getRandomSequenceNames(int minDistancRelationships, int amount);

	public Map<String, List<String>> getSequencesWithinCluster(List<String> centroids, String distanceName);

	public void insertAllSequences(List<String> nodes);
	
	public DistancePair getDistances(DistancePair dao);
	
	public double calculateAverageDistanceOfSequence(String sequence, String distanceName);
	
	public LinkedList<DistancePair> getDistancesOrdered(String distanceName, int limit, int offset);
	
	public String findClustroid(Collection<String> sequences, String distanceName);
	
	public double getAverageClusterDistance(Collection<String> sequences, String distanceName);
	
	public Map<Boolean, List<String>> getSequencesWithinTresholds(String center, float tightTreshold, float looseTreshold, String distanceName);
	
	public void updateClusterInformation(List<? extends Cluster<?>> clusters);
	
	public float getMaxDistanceWithinCluster(Collection<String> sequences, String distanceName);
	
	public Map<Long, List<String>> getClusters(String distanceName, String algorithmName);

	public void setClusterPersisted(long id);
	
	public void addDistance(DistancePair item);

	public double getAverageDistanceOfSequenceInCluster(String sequence, Collection<String> sequences, String distanceName);

	public double getAverageDistanceToNearestCluster(String sequence, String distanceName, String clusteringName);

}
