package de.lncrna.classification.util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PropertyHandler {
	private static final Logger LOG = Logger.getLogger("logger");
	public static final PropertyHandler HANDLER = new PropertyHandler("properties/config.properties");
	private final Properties properties = new Properties();
	private final String fileName;

	private PropertyHandler(String fileName) {
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
		this.properties.put(key.name(), value.toString());

		try (FileOutputStream output = new FileOutputStream(this.fileName)) {
			this.properties.store(output, "Properties of lncRNA classification");
		} catch (IOException e) {
			LOG.log(Level.WARNING, "Failed to update properties", e);
		}
	}

	public <T> T getPropertyValue(PropertyKeys key, Class<T> valueType) {
		String value = (String) this.properties.get(key.name());
		return convertToValueType(value, valueType);
	}

	private <T> T convertToValueType(String value, Class<T> valueType) {
		if (String.class == valueType) {
			return valueType.cast(value);
		} else if (Integer.class == valueType) {
			return valueType.cast(Integer.valueOf(value));
		} else {
			throw new IllegalArgumentException(String.format("Can't convert object with value %s to %s", value, valueType.getName()));
		}
			
	}
}