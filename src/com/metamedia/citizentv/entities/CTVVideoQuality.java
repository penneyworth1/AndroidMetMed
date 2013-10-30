package com.metamedia.citizentv.entities;

import org.json.JSONObject;

public class CTVVideoQuality extends ModelEntity
{
	
	private static final long serialVersionUID = 1L;
	
	public int width;
	public int height;
	public String urlName;

	public CTVVideoQuality(String key)
	{
		super(key);
	}

	@Override
	public void parse(JSONObject jsonObject)
	{
		super.parse(jsonObject);
		
		this.setKey(jsonObject.optString("key", null));
		this.urlName = jsonObject.optString("url_name", null);
		this.width = jsonObject.optInt("width");
		this.height = jsonObject.optInt("height");
	}

}
