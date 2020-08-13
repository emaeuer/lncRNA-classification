package de.lncrna.classification.db;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.neo4j.graphdb.Result;

import de.lncrna.classification.clustering.Cluster;
import de.lncrna.classification.distance.DistancePair;
import de.lncrna.classification.util.data.DistanceDAO;

public class EmbeddedNeo4jQueryHelper implements Neo4jQueryHelper<EmbeddedNeo4jHandler, Result> {

	private final EmbeddedNeo4jHandler handler;
	
	public EmbeddedNeo4jQueryHelper(EmbeddedNeo4jHandler handler) {
		this.handler = handler;
	}
	
	@Override
	public void insertAllSequences(List<String> nodes) {
		nodes.parallelStream()
			.map(s -> String.format(Neo4jQueryHelper.INSERT_ALL_SEQUENCES, s))
			.forEach(this.handler::commitQuery);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<String> getAllSequenceNames() {
		return this.handler.executeQuery(
				Neo4jQueryHelper.GET_ALL_SEQUENCES, 
				r -> (List<String>) r.next().get("names"));
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
						dao.setDistanceValue(Double.valueOf((double) r.next().getOrDefault("distance", -1)).floatValue());
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
						return Double.valueOf((double) r.next().getOrDefault("avgDistance", -1));
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
						.map(m -> new DistanceDAO((String) m.get("seq1"), (String) m.get("seq2"), distanceName, Double.valueOf((double) m.get("distance")).floatValue())) 
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
							.map(m -> m.get("seqName"))
							.map(s -> (String) s)
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
							.mapToDouble(m -> (double) m.get("avgClusterDistance"))
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
						@SuppressWarnings("unchecked") // Generics can't be casted without warning
						Map<String, List<String>> queryResult = (Map<String, List<String>>) r.next().get("result");
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
			.filter(c -> c.getClusterSize() > 1) // only persist 'real' clusters with at least two sequences
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
							.map(m -> m.get("maxDistance"))
							.map(s -> Double.valueOf((double) s).floatValue())
							.findFirst()
							.orElse(1f);
				});
	}
	
	private String toCollectionString(Collection<String> sequences) {
		StringBuilder builder = new StringBuilder("[");
		sequences.stream()
			.forEach(s -> builder.append(String.format("\"%s\", ", s)));
		
		if (builder.length() > 1) {
			builder.replace(builder.length() - 2, builder.length(), "]");
		}
		return builder.toString();
	}

	@Override
	public void addDistance(DistancePair item) {
		this.handler.commitQuery(
				String.format(
						Neo4jQueryHelper.INSERT_DISTANCE, 
						item.getSequenceName1(), item.getSequenceName2(), item.getDistanceType().name(), item.getDistance()));
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<Long, List<String>> getClusters(String distanceName, String algorithmName) {
		return this.handler.executeQuery(
				String.format(
						Neo4jQueryHelper.GET_ALL_CLUSTERS,
						distanceName, algorithmName), 
				r -> {
					return r.stream()
							.collect(Collectors.toMap(result -> (Long) result.get("id"), result -> (List<String>) result.get("sequences")));
				});
	}
	
	@Override
	public void setClusterPersisted(long id) {
		this.handler.commitQuery(String.format(Neo4jQueryHelper.SET_CLUSTER_PERSISTED, id));
	}

	
}
