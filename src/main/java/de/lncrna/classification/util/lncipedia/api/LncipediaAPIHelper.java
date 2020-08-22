package de.lncrna.classification.util.lncipedia.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONObject;

import de.lncrna.classification.filter.FailedToReceiveIntronException;
import de.lncrna.classification.util.PropertyHandler;
import de.lncrna.classification.util.PropertyKeyHelper.PropertyKeys;

public class LncipediaAPIHelper {

	private LncipediaAPIHelper() {}
	
	public static JSONObject getJson(String seqID) {
		String urlPattern = PropertyHandler.HANDLER.getPropertyValue(PropertyKeys.INTRON_API_URL_PATTERN, String.class);
		
		try {
			URL url = new URL(String.format(urlPattern, seqID));
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			
			StringBuffer content = new StringBuffer();
			try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
				String inputLine;
				while ((inputLine = in.readLine()) != null) {
				    content.append(inputLine);
				}
			}
			return new JSONObject(content.toString());
		} catch (IOException e) {
			throw new FailedToReceiveIntronException("Failed to receive information for sequence (" + seqID + ")", e);
		}
	}
	
	
	
}
