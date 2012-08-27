package com.camunda.fox.cycle.web;

import java.util.HashMap;
import java.util.Map;

public class I18n {
	
	Map<String, String> de = new HashMap<String, String>();
	Map<String, String> en = new HashMap<String, String>();
	
	Map<String, Map<String, String>> langs = new HashMap<String, Map<String, String>>();
	private String lang;
	
	public I18n(String lang) {
		this.lang = lang;
		HashMap<String, String> enMap = new HashMap<String, String>();
		enMap.put("Hello", "Hello");
		enMap.put("Roundtrip", "Roundtrip");
		langs.put("en-US", enMap);

		HashMap<String, String> deMap = new HashMap<String, String>();
		deMap.put("Hello", "Hallo");
		deMap.put("Roundtrip", "Rundreise");
		langs.put("de-DE", deMap);
	}
	
	public String tr(String key) {
		if (langs.get(lang).get(key) == null) {
			return key;
		}
		return langs.get(lang).get(key);
	}
}
