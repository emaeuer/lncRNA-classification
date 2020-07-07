package de.lncrna.classification.db;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.neo4j.driver.Value;
import org.neo4j.driver.Values;

import de.lncrna.classification.util.data.DistanceDAO;

public class Neo4JCypherQueriesServer {

	public static List<String> getAllSequenceNames() {
		return Neo4JHelperServer.CONNECTION.executeQuery(
				"MATCH (seq:Sequence) RETURN collect(seq.seqName) AS names", 
				r -> (List<String>) r.next().get("names").asList(Value::asString));
	}
	
	public static void addDistance(DistanceDAO dao) {
		Neo4JHelperServer.CONNECTION.commitQuery(
				String.format(
						"MERGE(seq1:Sequence {seqName:\"%s\"}) " + 
						"MERGE(seq2:Sequence {seqName:\"%s\"}) " + 
						"MERGE (seq1)-[r:distance]-(seq2) " + 
						"SET r.%s=%s", 
						dao.getSeq1(), dao.getSeq2(), dao.getDistanceName(), dao.getDistanceValue()));
	}
	
	public static DistanceDAO getDistances(DistanceDAO dao) {
		return Neo4JHelperServer.CONNECTION.executeQuery(
				String.format(
						"MATCH (:Sequence {seqName:\"%s\"}) - [rel:distance] - (:Sequence {seqName:\"%s\"})" + 
						"RETURN rel.%s AS distance",
						dao.getSeq1(), dao.getSeq2(), dao.getDistanceName()), 
				r -> {
					if (r.hasNext()) {
						dao.setDistanceValue(Double.valueOf((double) r.next().get("distance", -1)).floatValue());
					} else {
						dao.setDistanceValue(-1f);;
					}
					return dao;
				});
	}
	
	public static double calculateAverageDistanceOfSequence(String sequence, String distanceName) {
		return Neo4JHelperServer.CONNECTION.executeQuery(
				String.format(
						"MATCH (:Sequence {name:\"%s\"}) -[r:distance]- ()" + 
						"RETURN avg(r.%s) AS avgDistance",
						sequence, distanceName), 
				r -> {
					if (r.hasNext()) {
						return Double.valueOf((double) r.next().get("avgDistance", -1));
					} 
					return -1.0;
				});
	}
	
	public static LinkedList<DistanceDAO> getDistancesOrdered(String distanceName, int limit) {
		return Neo4JHelperServer.CONNECTION.executeQuery(
				String.format(
						"MATCH (seq1:Sequence) -[r:distance]- (seq2:Sequence) " + 
						"RETURN seq1.seqName AS seq1, r.%s AS distance, seq2.seqName AS seq2 " + 
						"ORDER BY distance ASCENDING " + 
						"LIMIT %d",
						distanceName, limit), 
				r -> {
					return r.stream()
						.map(m -> new DistanceDAO(m.get("seq1").asString(), m.get("seq2").asString(), distanceName, m.get("distance").asFloat())) 
						.collect(Collectors.toCollection(LinkedList::new));
				});
	}
	
	public static String findClustroid(Collection<String> sequences, String distanceName) {
		return Neo4JHelperServer.CONNECTION.executeQuery(
				String.format(
						"MATCH (seq:Sequence)" +
						"WHERE seq.seqName IN %s " +
						"WITH collect(seq) AS sequencesOfCluster " +
						"MATCH (seq1:Sequence) -[r:distance]- (seq2:Sequence) " +
						"WHERE seq1 IN sequencesOfCluster AND seq2 IN sequencesOfCluster " +
						"WITH {seqName:seq1.seqName, avgDistance:avg(r.%s)} AS avgDistances " +
						"RETURN avgDistances.seqName AS seqName " +
						"ORDER BY avgDistances.avgDistance " +
						"LIMIT 1",
						distanceName), 
				r -> {
					return r.stream()
							.map(m -> m.get("seqName").asString())
							.findFirst()
							.orElse(null);
				});
	}
	
	public static double getAverageClusterDistance(Collection<String> sequences, String distanceName) {
		if (sequences.size() == 1) {
			return 0.0;
		} else {
			return Neo4JHelperServer.CONNECTION.executeQuery(
					String.format(
							"MATCH (seq:Sequence) " +
							"WHERE seq.seqName IN %s " +
							"WITH collect(seq) AS sequencesOfCluster " +
							"MATCH (seq1:Sequence) -[r:distance]- (seq2:Sequence) " +
							"WHERE seq1 IN sequencesOfCluster AND seq2 IN sequencesOfCluster " +
							"RETURN avg(r.%s) AS avgClusterDistance",
							toCollectionString(sequences), distanceName), 
					r -> {
						return r.stream()
							.mapToDouble(m -> m.get("avgClusterDistance").asDouble())
							.findFirst()
							.orElse(1.0);
					});
		}
	}
	
	public static Map<Boolean, List<String>> getSequencesWithinTresholds(String center, float tightTreshold, float looseTreshold, String distanceName) {
		return Neo4JHelperServer.CONNECTION.executeQuery(
				String.format(
						"MATCH (:Sequence{seqName:\"%1$s\"})-[r:distance]-(seq:Sequence) " +
						"WHERE r.%4$s < %2$s " +
						"WITH collect(seq.seqName) AS tightSequences " +
						"MATCH (:Sequence{seqName:\"%1$s\"})-[r:distance]-(seq:Sequence) " +
						"WHERE %2$s <= r.%4$s <= %3$s " +
						"WITH collect(seq.seqName) AS looseSequences, tightSequences " +
						"RETURN {tightSequences:tightSequences, looseSequences:looseSequences} AS result",
						center, tightTreshold, looseTreshold, distanceName), 
				r -> {
					Map<Boolean, List<String>> result = new HashMap<>();
					if (r.hasNext()) {
						Map<String, List<String>> queryResult = (Map<String, List<String>>) r.next().get("result").asMap(Values.ofList(o -> o.asString()));
						result.put(true, queryResult.get("tightSequences"));
						result.put(false, queryResult.get("looseSequences"));
					}
					return result;
				});
	}

	private static String toCollectionString(Collection<String> sequences) {
		StringBuilder builder = new StringBuilder("[");
		sequences.stream()
			.forEach(s -> builder.append(String.format("\"%s\", ", s)));
		
		builder.replace(builder.length() - 2, builder.length(), "]");
		return builder.toString();
	}
	
}
