package com.metamedia.citizentv.entities;

import org.json.JSONObject;

public class CTVCategory extends ModelEntity
{
	private static final long serialVersionUID = 1L;
	
	public String name = ""; //the name in the current language associated with the given category key

	public CTVCategory(String key) 
	{
		super(key);
	}
	
	public long expiringTimeOffset()
	{
		// Subclasses may override and set a different value!
		// Time length is returned in milliseconds
		return 600000L; //10 minutes
	}

	@Override
	public void parse(JSONObject jsonObject) 
	{
		super.parse(jsonObject);
		
		//TODO - if it is needed
		
	}

}
