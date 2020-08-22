package de.lncrna.classification.filter;

import org.json.JSONArray;
import org.json.JSONObject;

import de.lncrna.classification.util.lncipedia.api.LncipediaAPIHelper;

public class NumberOfIntronsFilter {

	private NumberOfIntronsFilter() {}
	
	public static boolean hasAtLeastOneIntron(String seqID) {
		JSONObject receivedJson = LncipediaAPIHelper.getJson(seqID);
		JSONArray exonArray = receivedJson.getJSONArray("exons");
		
		long start = receivedJson.getLong("start");
		long end = receivedJson.getLong("end");
		
		if (exonArray.length() > 1) {
			// Between exons is always an intron 
			// --> if sequence contains at least two exons it also contains at least one intron
			return true;
		} else if (exonArray.length() == 1) {
			JSONObject onlyExon = exonArray.getJSONObject(0);
			
			boolean result = start == onlyExon.getLong("start");
			result &= end == onlyExon.getLong("start");
			
			// is the exon isn't the complete seqeunce the sequence contains at least one intron
			return !result;
		} else {
			// if no exons exists and the sequence contains bases the sequences contains one intron
			return start != end;
		}
	}
	
}

