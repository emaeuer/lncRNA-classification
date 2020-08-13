package de.lncrna.classification.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.lncrna.classification.util.PropertyKeyHelper.PropertyKeys;

public class PropertyHandler {
	private static final Logger LOG = Logger.getLogger("logger");
	public static final PropertyHandler HANDLER = new PropertyHandler("properties/general.properties");
	
	private final Properties properties = new Properties();
	private final String fileName;

	public PropertyHandler(String fileName) {
		this.fileName = fileName;
		this.loadProperties();
	}

	private void loadProperties() {
		try (FileInputStream input = new FileInputStream(this.fileName)) {
			this.properties.load(input);
		} catch (IOException e) {
			LOG.log(Level.WARNING, "Failed to load properties", e);
		}
	}

	public void setPropertieValue(PropertyKeys key, Object value) {
		this.properties.put(PropertyKeyHelper.getKey(key), value.toString());

		try (FileOutputStream output = new FileOutputStream(this.fileName)) {
			this.properties.store(output, "Properties of lncRNA classification");
		} catch (IOException e) {
			LOG.log(Level.WARNING, "Failed to update properties", e);
		}
	}

	public <T> T getPropertyValue(PropertyKeys key, Class<T> valueType) {
		String value = (String) this.properties.get(PropertyKeyHelper.getKey(key));
		return convertToValueType(value, valueType);
	}

	private <T> T convertToValueType(String value, Class<T> valueType) {		
		if (String.class == valueType) {
			return valueType.cast(value);
		} else if (Integer.class == valueType) {
			if (value == null) {
				return valueType.cast(0);
			}
			return valueType.cast(Integer.valueOf(value));
		} else if (Float.class == valueType) {
			if (value == null) {
				return valueType.cast(0f);
			}
			return valueType.cast(Float.valueOf(value));
		} else if (Double.class == valueType) {
			if (value == null) {
				return valueType.cast(0.0);
			}
			return valueType.cast(Double.valueOf(value));
		} else if (File.class == valueType) {
			return valueType.cast(new File(value));
		} else {
			throw new IllegalArgumentException(String.format("Can't convert object with value %s to %s", value, valueType.getName()));
		}
			
	}
}