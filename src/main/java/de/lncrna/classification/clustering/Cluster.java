package de.lncrna.classification.clustering;

import java.util.List;

import org.biojava.nbio.core.sequence.RNASequence;

import de.lncrna.classification.clustering.algorithms.ClusteringAlgorithm;

/**
 * Context of strategy pattern. Passes all the invocations directly to the 
 * concrete implementation
 * 
 * @author emaeu
 *
 * @param <T> The concrete clustering implementation
 */
public class Cluster<T extends ClusteringAlgorithm> {

	private final T algorithm;
	
	public Cluster(final T algorithm, List<RNASequence> sequences) {
		this.algorithm = algorithm;
		this.algorithm.initCluster(sequences);
	}
	
	public void mergeWithOther(Cluster<T> other) {
		this.algorithm.mergeWithOther(other);
	}
	
	public void addLncRNA(RNASequence data) {
		this.algorithm.addSequence(data);
	}

	public int getClusterSize() {
		return this.algorithm.getSequences().size();
	}
	
	public boolean containsSequence(RNASequence sequence) {
		return this.algorithm.getSequences().contains(sequence);
	}
	
	public double distanceTo(Cluster<T> other) {
		return this.algorithm.distanceTo(other);
	}
	
	public T getAlgorithm() {
		return this.algorithm;
	}
	
	public void clear() {
		this.getAlgorithm().getSequences().clear();
	}
	
	public double calcualteAverageClusterDistance() {
		// TODO
		return 0.0;
//		List<PairwiseSequenceScorer<RNASequence,NucleotideCompound>> scores = Alignments.getAllPairsScorers(
//				new ArrayList<>(this.algorithm.getSequences()), AlignmentConstants.SCORER_TYPE, 
//				AlignmentConstants.GAP_PENALTY, AlignmentConstants.SUBSTITUTION_MATRIX);
//		
//		return scores.parallelStream()
//			.mapToDouble(PairwiseSequenceScorer::getDistance)
//			.average().orElse(0);
	}
	
	@Override
		public String toString() {
			StringBuilder builder = new StringBuilder("Cluster[");
			getAlgorithm().getSequences()
				.forEach(sequence -> builder.append(sequence.getDescription() + ", "));
			builder.replace(builder.length() - 2, builder.length(), "");
			builder.append("]");
			return builder.toString();
		}
	
}
