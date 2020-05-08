package de.lncrna.classification.clustering.algorithms;

import org.biojava.nbio.alignment.Alignments.PairwiseSequenceScorerType;
import org.biojava.nbio.alignment.SimpleGapPenalty;
import org.biojava.nbio.alignment.SubstitutionMatrixHelper;
import org.biojava.nbio.alignment.template.GapPenalty;
import org.biojava.nbio.alignment.template.SubstitutionMatrix;
import org.biojava.nbio.core.sequence.compound.NucleotideCompound;

public interface AlignmentConstants {

	public static final  SubstitutionMatrix<NucleotideCompound> SUBSTITUTION_MATRIX = SubstitutionMatrixHelper.getNuc4_4();

	public static final GapPenalty GAP_PENALTY = new SimpleGapPenalty(8, 1);
	
	public static final PairwiseSequenceScorerType SCORER_TYPE = PairwiseSequenceScorerType.GLOBAL;
	
}
