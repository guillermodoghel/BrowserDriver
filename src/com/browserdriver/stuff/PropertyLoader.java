package com.browserdriver.stuff;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertyLoader {
	private static PropertyLoader instance = new PropertyLoader();

	private static Properties prop;

	private PropertyLoader() {
		try {
			prop = new Properties();
			InputStream input = null;
			input = getClass().getClassLoader().getResourceAsStream("config.properties");
			prop.load(input);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static PropertyLoader getInstance() {
		return instance;
	}


	public static String getPulserasKey() {
		return prop.getProperty("pulseras.contains");
	}
	public static String getEtiquetasKey() {
		return prop.getProperty("etiquetas.contains");
	}
	public static String getStuff() {
		return prop.getProperty("mensaje.inicio");
	}

}
