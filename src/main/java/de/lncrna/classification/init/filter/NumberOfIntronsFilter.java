package de.lncrna.classification.init.filter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONObject;

import de.lncrna.classification.util.PropertyHandler;
import de.lncrna.classification.util.PropertyKeyHelper.PropertyKeys;

public class NumberOfIntronsFilter {

	private static final String URL_GET_PATTERN = PropertyHandler.HANDLER.getPropertyValue(PropertyKeys.INTRON_API_URL_PATTERN, String.class);
	
	private NumberOfIntronsFilter() {}
	
	public static boolean hasAtLeastOneIntron(String seqID) {
		JSONObject receivedJson = new JSONObject(getJson(seqID));
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
	
	private static String getJson(String seqID) {
		try {
			URL url = new URL(String.format(URL_GET_PATTERN, seqID));
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			
			StringBuffer content = new StringBuffer();
			try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
				String inputLine;
				while ((inputLine = in.readLine()) != null) {
				    content.append(inputLine);
				}
			}
			return content.toString();
		} catch (IOException e) {
			throw new FailedToReceiveIntronException("Faile to receive information for sequence (" + seqID + ")", e);
		}
	}
	
}
