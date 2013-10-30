package com.metamedia.citizentv.entities.helpers;

import java.io.Serializable;

import org.json.JSONObject;

public abstract class EntityHelper implements Serializable 
{
	private static final long serialVersionUID = 1L;
	
	public EntityHelper(JSONObject jsonObject)
	{
		super();
		this.parse(jsonObject);
	}
	
	/**
	 * Call this method to parse a JSON object into the business object. 
	 * @param jsonObject
	 */
	abstract public void parse(JSONObject jsonObject);
}
