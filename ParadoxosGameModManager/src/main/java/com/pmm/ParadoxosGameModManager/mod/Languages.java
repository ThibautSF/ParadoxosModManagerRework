package com.pmm.ParadoxosGameModManager.mod;

/**
 * @author GROSJEAN Nicolas (alias Mouchi)
 *
 */
public enum Languages {
	ENGLISH ("english"),
	FRENCH ("french"),
	GERMAN ("german"),
	SPANISH ("spanish"),
	POLISH ("polish"),
	ITALIAN ("italian"),
	SWEDISH ("swedish"),
	CZECH ("czech"),
	HUNGARIAN ("hungarian"),
	DUTCH ("dutch"),
	RUSSIAN ("russian"),
	FINNISH ("finnish"),
	BRAZ_PORTUGUESE("braz_por");
	
	private String name;
	
	private Languages(String name) {
		this.name = name;
	}
	
	public String getCode() {
		return "l_" + name;
	}
	
	public String getName() {
		return name;
	}
	
	public static Languages getLanguage(String language) {
		final Languages DEFAULT = ENGLISH; 
		if (language == null) {
			return DEFAULT;
		}
		for (Languages l : values()) {
			if (l.getName().toLowerCase().equals(language.toLowerCase())) {
				return l;
			}
		}
		return DEFAULT;
	}
}
