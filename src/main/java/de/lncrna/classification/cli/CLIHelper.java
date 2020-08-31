package de.lncrna.classification.cli;

import de.lncrna.classification.util.PropertyHandler;
import de.lncrna.classification.util.PropertyKeyHelper.PropertyKeys;

public class CLIHelper {

	private CLIHelper() {}
	
	public static boolean refreshIfNecessary(PropertyKeys key, double value) {
		if (value != -1) {
			PropertyHandler.HANDLER.setPropertieValue(key, value);
			return true;
		}	
		return false;
	}
	
	public static boolean refreshIfNecessary(PropertyKeys key, int value) {
		if (value != -1) {
			PropertyHandler.HANDLER.setPropertieValue(key, value);
			return true;
		}	
		return false;
	}
	
	public static boolean refreshIfNecessary(PropertyKeys key, Object value) {
		if (value != null) {
			Object old = PropertyHandler.HANDLER.getPropertyValue(key, value.getClass());
			if (old == null || (old != null && !old.equals(value))) {
				PropertyHandler.HANDLER.setPropertieValue(key, value);
				return true;
			}
		}	
		return false;
	}
	
}
