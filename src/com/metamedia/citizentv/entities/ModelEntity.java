package com.metamedia.citizentv.entities;

import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import com.metamedia.persistentmodel.BaseObject;

public abstract class ModelEntity extends BaseObject
{
	private static final long serialVersionUID = 1L;
	
	private boolean fullyFetchedOnce = false;
	
	// ---------------------------------------------------------------------------------------------------------
	// Creating instances and initializing
	// ---------------------------------------------------------------------------------------------------------
		
	public ModelEntity(String key) 
	{
		super(key);
	}
		
	// ---------------------------------------------------------------------------------------------------------
	// Main Methods
	// ---------------------------------------------------------------------------------------------------------
	
	final public boolean isExpired()
	{
		Date now = new Date();//System.currentTimeMillis();
		Date lastUpdate = this.getLastUpdate();
		long diff = now.getTime() - lastUpdate.getTime();
		long trustedOffset = this.expiringTimeOffset();
		
		boolean trustedData = diff < trustedOffset;
		return !trustedData;
	}
	
	public long expiringTimeOffset()
	{
		// Subclasses may override and set a different value!
		// Time length is returned in milliseconds
		return 3600000L;
	}
	
	final public boolean isFullyFetchedOnce() 
	{
		return fullyFetchedOnce;
	}
	
	final public void setServerFullUpdateDate(Date date)
	{
		this.setLastUpdate(date);
		this.fullyFetchedOnce = true;
	}
	
	/**
	 * Subclasses can override this method if the encoded key in the jsonObject doesn't come with the key "key".  
	 * @param jsonObject
	 * @return The key of the object.
	 * @throws JSONException
	 */
	static public String parseKey(JSONObject jsonObject)
	{
		return jsonObject.optString("key", null);
	}

	/**
	 * Call this method to parse a JSON object into the business object. 
	 * @param jsonObject
	 */
	public void parse(JSONObject jsonObject)
	{
		this.setHasChanges(jsonObject.length() > 0);
	}

	@Override
	public String toString() 
	{
		return "" + this.getBaseObjectType() + " [" + super.toString() + ", fullyFetchedOnce=" + fullyFetchedOnce + "]";
	}
}
