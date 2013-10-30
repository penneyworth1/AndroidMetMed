package com.metamedia.managers;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TranslationManager implements Serializable
{
	private static final long serialVersionUID = 1L;
	private Date lastUpdateDate;
	public static final String MAP_NAME_CATEGORIES = "categories";
	public static final long MILLIES_BEFORE_EXPIRATION = 1000L * 60L * 60L; //one hour
	private Map<String,Map<String,String>> mapOfTranslationMaps = new ConcurrentHashMap<String,Map<String,String>>();
	
	@Override
	public String toString() 
	{
		return "TranslationManager [lastUpdateDate=" + lastUpdateDate + ", mapOfMaps # of characters: " + mapOfTranslationMaps.toString().length();//+ ", mapOfTranslationMaps=" + mapOfTranslationMaps + "]";
	}

	public TranslationManager()
	{
		
	}
	
	public Date getLastUpdateDate()
	{
		return lastUpdateDate;
	}

	public void setLastUpdateDate(Date lastUpdateDate) 
	{
		this.lastUpdateDate = lastUpdateDate;
	}
	
	public String getTranslation(String outerKey, String innerKey)
	{
		Map<String,String> translationMap = mapOfTranslationMaps.get(outerKey);
		String translationString = translationMap.get(innerKey);
		return translationString;
	}
	
	public void addTranslationMap(String mapKey, Map<String,String> translationMap)
	{
		mapOfTranslationMaps.put(mapKey, translationMap);
	}
}
