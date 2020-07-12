package de.lncrna.classification.db;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.biojava.nbio.core.sequence.RNASequence;
import org.neo4j.driver.Result;
import org.neo4j.driver.Value;
import org.neo4j.driver.Values;

import de.lncrna.classification.clustering.Cluster;
import de.lncrna.classification.util.data.DistanceDAO;

public class ServerNeo4jQueryHelper implements Neo4jQueryHelper<ServerNeo4jHandler, Result> {

	private final ServerNeo4jHandler handler;
	
	public ServerNeo4jQueryHelper(ServerNeo4jHandler handler) {
		this.handler = handler;
	}
	
	@Override
	public List<String> getAllSequenceNames() {
		return this.handler.executeQuery(
				Neo4jQueryHelper.GET_ALL_SEQUENCES, 
				r -> (List<String>) r.next().get("names").asList(Value::asString));
	}
	
	@Override
	public void insertAllSequences(List<RNASequence> nodes) {
		nodes.parallelStream()
			.map(s -> String.format(Neo4jQueryHelper.INSERT_ALL_SEQUENCES, s.getDescription(), s.getSequenceAsString()))
			.forEach(this.handler::commitQuery);
	}
	
	@Override
	public void addDistance(DistanceDAO dao) {
		this.handler.commitQuery(
				String.format(
						Neo4jQueryHelper.INSERT_DISTANCE, 
						dao.getSeq1(), dao.getSeq2(), dao.getDistanceName(), dao.getDistanceValue()));
	}
	
	@Override
	public DistanceDAO getDistances(DistanceDAO dao) {
		return this.handler.executeQuery(
				String.format(
						Neo4jQueryHelper.GET_DISTANCE,
						dao.getSeq1(), dao.getSeq2(), dao.getDistanceName()), 
				r -> {
					if (r.hasNext()) {
						dao.setDistanceValue(Double.valueOf(r.next().get("distance").asDouble()).floatValue());
					} else {
						dao.setDistanceValue(-1f);;
					}
					return dao;
				});
	}
	
	@Override
	public double calculateAverageDistanceOfSequence(String sequence, String distanceName) {
		return this.handler.executeQuery(
				String.format(
						Neo4jQueryHelper.GET_AVERAGE_DISTANCE_SEQUENCE,
						sequence, distanceName), 
				r -> {
					if (r.hasNext()) {
						return Double.valueOf((double) r.next().get("avgDistance", -1));
					} 
					return -1.0;
				});
	}
	
	@Override
	public LinkedList<DistanceDAO> getDistancesOrdered(String distanceName, int limit) {
		return this.handler.executeQuery(
				String.format(
						Neo4jQueryHelper.GET_ORDERED_DISTANCES,
						distanceName, limit), 
				r -> {
					return r.stream()
						.map(m -> new DistanceDAO(m.get("seq1").asString(), m.get("seq2").asString(), distanceName, Double.valueOf(m.get("distance").asDouble()).floatValue())) 
						.collect(Collectors.toCollection(LinkedList::new));
				});
	}
	
	@Override
	public String findClustroid(Collection<String> sequences, String distanceName) {
		return this.handler.executeQuery(
				String.format(
						Neo4jQueryHelper.GET_CLUSTROID,
						toCollectionString(sequences), distanceName, 1), 
				r -> {
					return r.stream()
							.map(m -> m.get("seqName").asString())
							.findFirst()
							.orElse(null);
				});
	}
	
	@Override
	public double getAverageClusterDistance(Collection<String> sequences, String distanceName) {
		if (sequences.size() == 1) {
			return 0.0;
		} else {
			return this.handler.executeQuery(
					String.format(
							Neo4jQueryHelper.GET_AVERAGE_DISTANCE_CLUSTER,
							toCollectionString(sequences), distanceName, 1), 
					r -> {
						return r.stream()
							.mapToDouble(m -> m.get("avgClusterDistance").asDouble())
							.findFirst()
							.orElse(1.0);
					});
		}
	}
	
	@Override
	public Map<Boolean, List<String>> getSequencesWithinTresholds(String center, float tightTreshold, float looseTreshold, String distanceName) {
		return this.handler.executeQuery(
				String.format(
						Neo4jQueryHelper.GET_SEQUENCES_WITHIN_TRESHOLD,
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
	
	@Override
	public void updateClusterInformation(List<? extends Cluster<?>> clusters) {
		if (clusters.isEmpty()) {
			return;
		}
		
		String clusterAlgorithm = clusters.get(0).getAlgorithm().getName();
		String distanceAlgorithm = clusters.get(0).getAlgorithm().getDistanceAlgortithm().name();
		
		// Remove old clusters corresponding to this configuration
		this.handler.commitQuery(
				String.format(
						Neo4jQueryHelper.REMOVE_CLUSTER_INFORMATION,
						clusterAlgorithm, distanceAlgorithm));
		
		// Insert new clusters corresponding to this configuration
		clusters.parallelStream()
			.peek(c -> System.out.println(c.getSequences()))
			.forEach(c -> this.handler.commitQuery(
					String.format(
							Neo4jQueryHelper.INSERT_CLUSTER_INFORMATION, 
							clusterAlgorithm, distanceAlgorithm, toCollectionString(c.getSequences()), c.getClustroid())));
	}
	
	@Override
	public float getMaxDistanceWithinCluster(Collection<String> sequences, String distanceName) {
		return this.handler.executeQuery(
				String.format(
						Neo4jQueryHelper.GET_MAXIMAL_DISTINCE_WITHIN_CLUSTER,
						toCollectionString(sequences), distanceName, 1), 
				r -> {
					return r.stream()
							.map(m -> m.get("maxDistance").asDouble())
							.map(d -> Double.valueOf(d).floatValue())
							.findFirst()
							.orElse(1f);
				});
	}

	private String toCollectionString(Collection<String> sequences) {
		StringBuilder builder = new StringBuilder("[");
		sequences.stream()
			.forEach(s -> builder.append(String.format("\"%s\", ", s)));
		
		builder.replace(builder.length() - 2, builder.length(), "]");
		return builder.toString();
	}
	
}
