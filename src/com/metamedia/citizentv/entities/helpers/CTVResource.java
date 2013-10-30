package com.metamedia.citizentv.entities.helpers;

import org.json.JSONObject;
import com.metamedia.tools.Security;

public class CTVResource extends EntityHelper 
{
	private static final long serialVersionUID = 1L;
	
	public String resourceKey;
	public String storageServerKey;
	public double imageWidth;
	public double imageHeight;

	// ---------------------------------------------------------------------------------------------------------
	// Constructors
	// ---------------------------------------------------------------------------------------------------------
	
	public CTVResource(JSONObject jsonObject) 
	{
		super(jsonObject);
	}
	
	// ---------------------------------------------------------------------------------------------------------
	// EntityHelper extension
	// ---------------------------------------------------------------------------------------------------------
	
	@Override
	public void parse(JSONObject jsonObject) 
	{
		if(jsonObject != null)
		{
			this.resourceKey = jsonObject.optString("key", null);
			this.storageServerKey = jsonObject.optString("frontend_server_key", null);
			this.imageWidth = jsonObject.optDouble("width", 0);
			this.imageWidth = jsonObject.optDouble("height", 0);
		}
		else
		{
			this.resourceKey = null;
			this.storageServerKey = null;
			this.imageWidth = 0;
			this.imageWidth = 0;
		}
	}

	// ---------------------------------------------------------------------------------------------------------
	// Public Methods
	// ---------------------------------------------------------------------------------------------------------
	
	public String getIdentifier()
	{
		if (resourceKey == null || storageServerKey == null)
			return null;
		
		return Security.md5(resourceKey + "/" +  storageServerKey);
	}
}
