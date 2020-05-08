package de.lncrna.classification.clustering.algorithms.space;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

import org.biojava.nbio.alignment.Alignments;
import org.biojava.nbio.alignment.template.PairwiseSequenceScorer;
import org.biojava.nbio.core.sequence.RNASequence;
import org.biojava.nbio.core.sequence.compound.NucleotideCompound;

import de.lncrna.classification.clustering.Cluster;
import de.lncrna.classification.clustering.algorithms.AlignmentConstants;
import de.lncrna.classification.clustering.algorithms.implementations.HierarchicalClusteringMinimalDistance;

public class MinimalDistanceHierachicalClusteringSpace extends AbstractClusteringSpace<HierarchicalClusteringMinimalDistance> {
	
	private static final Comparator<PairwiseSequenceScorer<RNASequence, NucleotideCompound>> PAIRWISE_SCORE_COMPARATOR 
		= new Comparator<PairwiseSequenceScorer<RNASequence,NucleotideCompound>>() {
			@Override
			public int compare(PairwiseSequenceScorer<RNASequence, NucleotideCompound> o1, PairwiseSequenceScorer<RNASequence, NucleotideCompound> o2) {
				return Double.compare(o1.getDistance(), o2.getDistance());
			}
		};
	
	private final PriorityQueue<PairwiseSequenceScorer<RNASequence, NucleotideCompound>> distances 
		= new PriorityQueue<>(PAIRWISE_SCORE_COMPARATOR);

	public MinimalDistanceHierachicalClusteringSpace(List<RNASequence> data) {
		super(data);
	}

	@Override
	protected void initSpace(List<RNASequence> data) {
		data.parallelStream()
			.map(sequence -> new Cluster<>(new HierarchicalClusteringMinimalDistance(), Arrays.asList(sequence)))
			.forEach(cluster -> getClusters().add(cluster));
		
		this.distances.addAll(
				Alignments.getAllPairsScorers(data, AlignmentConstants.SCORER_TYPE, 
						AlignmentConstants.GAP_PENALTY, AlignmentConstants.SUBSTITUTION_MATRIX));
	}

	@Override
	public void nextIteration() {
		PairwiseSequenceScorer<RNASequence, NucleotideCompound> current = this.distances.poll();
		
		if (current == null) {
			return;
		}
		
		Cluster<HierarchicalClusteringMinimalDistance> c1 = findContainingCluster(current.getQuery());
		Cluster<HierarchicalClusteringMinimalDistance> c2 = findContainingCluster(current.getTarget());
		
		if (c1 != c2) {
			getClusters().remove(c2);
			c1.mergeWithOther(c2);	
		} else {
			// nextIteration() until two clusters are merged or singularity is reached
			nextIteration();
		}		
	}
	
	private Cluster<HierarchicalClusteringMinimalDistance> findContainingCluster(RNASequence sequence) {
		return getClusters().parallelStream()
			.filter(c -> c.containsSequence(sequence))
			.findFirst()
			.orElse(null);
	}

	
}
