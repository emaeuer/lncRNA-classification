package de.lncrna.classification.distance.calculation;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;

import de.lncrna.classification.distance.DistanceCalculator;
import de.lncrna.classification.distance.DistancePair;
import de.lncrna.classification.distance.DistanceType;
import de.lncrna.classification.util.lncipedia.api.LncipediaAPIHelper;

public class PropertyDistance extends DistanceCalculator {

	public static class SequenceProperties {
		
		private static AtomicInteger maxSequenceLength = new AtomicInteger(Integer.MIN_VALUE);
		
		private static AtomicInteger maxGenomeLength = new AtomicInteger(Integer.MIN_VALUE);
		
		private static AtomicInteger maxNrOfIntrons = new AtomicInteger(Integer.MIN_VALUE);
		
		private static AtomicInteger maxNrOfExons = new AtomicInteger(Integer.MIN_VALUE);
		
		private double sequenceLength;
		
		private double genomeLength;
		
		private double nrOfIntrons;
		
		private double nrOfExons;
		
		public static double calculateDistance(SequenceProperties seqProp1, SequenceProperties seqProp2) {
			double summedSquares = Math.pow(seqProp1.sequenceLength - seqProp2.sequenceLength, 2);
			summedSquares += Math.pow(seqProp1.genomeLength - seqProp2.genomeLength, 2);
			summedSquares += Math.pow(seqProp1.nrOfIntrons - seqProp2.nrOfIntrons, 2);
			summedSquares += Math.pow(seqProp1.nrOfExons - seqProp2.nrOfExons, 2);
			
			return Math.sqrt(summedSquares) / 2;
		}
		
		public SequenceProperties(int sequenceLength, int genomeLength, int nrOfIntrons, int nrOfExons) {			
			SequenceProperties.maxSequenceLength.getAndAccumulate(sequenceLength, Math::max);
			SequenceProperties.maxGenomeLength.getAndAccumulate(genomeLength, Math::max);
			SequenceProperties.maxNrOfIntrons.getAndAccumulate(nrOfIntrons, Math::max);
			SequenceProperties.maxNrOfExons.getAndAccumulate(nrOfExons, Math::max);
			
			this.sequenceLength = sequenceLength;
			this.genomeLength = genomeLength;
			this.nrOfIntrons = nrOfIntrons;
			this.nrOfExons = nrOfExons;
		}	
		
		public void normalize() {
			this.sequenceLength /= SequenceProperties.maxSequenceLength.get();
			this.genomeLength /= SequenceProperties.maxGenomeLength.get();
			this.nrOfIntrons /= SequenceProperties.maxNrOfIntrons.get();
			this.nrOfExons /= SequenceProperties.maxNrOfExons.get();
		}
	}
	
	private static Map<String, SequenceProperties> sequenceProperties;
	
	public static void initPropertiesOfSequences(List<String> sequences) {
		LOG.log(Level.INFO, "Started requesting sequence properties from lncipedia api");
		sequenceProperties = sequences.parallelStream()
			.map(s -> new AbstractMap.SimpleEntry<>(s, initPropertiesOfSequence(s)))
			.collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
		
		sequenceProperties.values()
			.parallelStream()
			.forEach(SequenceProperties::normalize);
		LOG.log(Level.INFO, "Finished requesting sequence properties from lncipedia api");
		
		System.out.println("SeqLength: " + SequenceProperties.maxSequenceLength);
		System.out.println("GenomeLength: " + SequenceProperties.maxGenomeLength);
		System.out.println("nrOfIntrons: " + SequenceProperties.maxNrOfIntrons);
		System.out.println("nrOfExons: " + SequenceProperties.maxNrOfExons);
	}
	
	private static SequenceProperties initPropertiesOfSequence(String sequence) {
		JSONObject sequenceJson = LncipediaAPIHelper.getJson(sequence);
		
		int sequenceLength = sequenceJson.getInt("transcriptSize");
		int genomeLength = sequenceJson.getInt("end") - sequenceJson.getInt("start");
		int nrOfExons = sequenceJson.getInt("nrExons");
		int nrOfIntrons = findNumberOfIntrons(sequenceJson);
		
		return new SequenceProperties(sequenceLength, genomeLength, nrOfIntrons, nrOfExons);
	}
	
	private static int findNumberOfIntrons(JSONObject receivedJson) {
		JSONArray exonArray = receivedJson.getJSONArray("exons");
		
		int nrExons = receivedJson.getInt("nrExons");
		
		if (nrExons == 0) {
			return 1;
		}
		
		long start = receivedJson.getLong("start");
		long end = receivedJson.getLong("end");
		
		JSONObject firstExon = exonArray.getJSONObject(0);
		JSONObject lastExon = exonArray.getJSONObject(exonArray.length() - 1);
		
		long exonStart = firstExon.getLong("start");
		long exonEnd = lastExon.getLong("end");
		
		if (exonStart == start && exonEnd == end) {
			return nrExons - 1;
		} else if (exonStart == start || exonEnd == end) {
			return nrExons;
		} else {
			return nrExons + 1;
		}
	}
	
	@Override
	public float getDistance(DistancePair pair) {
		SequenceProperties seqProp1 = sequenceProperties.get(pair.getSequenceName1());
		SequenceProperties seqProp2 = sequenceProperties.get(pair.getSequenceName2());
		
		float distance = Double.valueOf(SequenceProperties.calculateDistance(seqProp1, seqProp2)).floatValue();
		
		return distance;
//		return distance < 0.05 ? distance : -1f;
	}

	@Override
	public DistanceType getDistanceProperties() {
		return DistanceType.Property_Distance;
	}

}
