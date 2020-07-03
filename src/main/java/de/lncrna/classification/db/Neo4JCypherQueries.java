package de.lncrna.classification.db;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import de.lncrna.classification.util.data.DistanceDAO;

public class Neo4JCypherQueries {

	@SuppressWarnings("unchecked")
	public static List<String> getAllSequenceNames() {
		return Neo4JHelper.CONNECTION.executeQuery("MATCH (seq:Sequence) RETURN collect(seq.seqName) AS names", r -> (List<String>) r.next().get("names"));
	}
	
	public static void addDistance(DistanceDAO dao) {
		Neo4JHelper.CONNECTION.commitQuery(
				String.format(
						"MERGE(seq1:Sequence {seqName:\"%s\"}) " + 
						"MERGE(seq2:Sequence {seqName:\"%s\"}) " + 
						"MERGE (seq1)-[r:distance]-(seq2) " + 
						"SET r.%s=%s", 
						dao.getSeq1(), dao.getSeq2(), dao.getDistanceName(), dao.getDistanceValue()));
	}
	
	public static DistanceDAO getDistances(DistanceDAO dao) {
		return Neo4JHelper.CONNECTION.executeQuery(
				String.format(
						"MATCH (:Sequence {seqName:\"%s\"}) - [rel:distance] - (:Sequence {seqName:\"%s\"})" + 
						"RETURN rel.%s AS distance",
						dao.getSeq1(), dao.getSeq2(), dao.getDistanceName()), 
				r -> {
					if (r.hasNext()) {
						dao.setDistanceValue(Double.valueOf((double) r.next().getOrDefault("distance", -1)).floatValue());
					} else {
						dao.setDistanceValue(-1f);;
					}
					return dao;
				});
	}
	
	public static double calculateAverageDistanceOfSequence(String sequence, String distanceName) {
		return Neo4JHelper.CONNECTION.executeQuery(
				String.format(
						"MATCH (:Sequence {name:\"%s\"}) -[r:distance]- ()" + 
						"RETURN avg(r.%s) AS avgDistance",
						sequence, distanceName), 
				r -> {
					if (r.hasNext()) {
						return Double.valueOf((double) r.next().getOrDefault("avgDistance", -1));
					} 
					return -1.0;
				});
	}
	
	public static LinkedList<DistanceDAO> getDistancesOrdered(String distanceName, int limit) {
		return Neo4JHelper.CONNECTION.executeQuery(
				String.format(
					"MATCH (seq1:Sequence) -[r:distance]- (seq2:Sequence) " + 
					"RETURN seq1.seqName AS seq1, r.%s AS distance, seq2.seqName AS seq2 " + 
					"ORDER BY distance ASCENDING " + 
					"LIMIT %d",
				distanceName, limit), 
				r -> {
					return r.stream()
						.map(m -> new DistanceDAO((String) m.get("seq1"), (String) m.get("seq2"), distanceName, Double.valueOf((double) m.get("distance")).floatValue())) 
						.collect(Collectors.toCollection(LinkedList::new));
				});
	}
	
	public static double getAverageClusterDistance(Collection<String> sequences, String distanceName) {
		if (sequences.size() == 1) {
			return 0.0;
		} else {
			return Neo4JHelper.CONNECTION.executeQuery(
					String.format(
						"MATCH (seq1:Sequence) -[r:distance]- (seq2:Sequence) " + 
						"WHERE seq1.seqName IN %1$s " + 
						"AND seq2.seqName IN %1$s " + 
						"RETURN avg(r.%2$s) AS avgClusterDistance", 
						toCollectionString(sequences), distanceName), 
					r -> {
						return r.stream()
							.mapToDouble(m -> (double) m.get("avgClusterDistance"))
							.findFirst()
							.orElse(1.0);
					});
		}
	}

	private static String toCollectionString(Collection<String> sequences) {
		StringBuilder builder = new StringBuilder("[");
		sequences.stream()
			.forEach(s -> builder.append(String.format("\"%s\", ", s)));
		
		builder.replace(builder.length() - 2, builder.length(), "]");
		return builder.toString();
	}
	
}
