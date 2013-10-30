package com.metamedia.citizentv.entities.helpers;

import org.json.JSONObject;

public class CropRect extends EntityHelper 
{
	private static final long serialVersionUID = 1L;

	public double top;
	public double bottom;
	public double left;
	public double right;
	
	public CropRect(JSONObject jsonObject) 
	{
		super(jsonObject);
	}

	@Override
	public void parse(JSONObject jsonObject) 
	{
		this.top = jsonObject.optDouble("top", 0);
		this.bottom = jsonObject.optDouble("bottom", 0);
		this.left = jsonObject.optDouble("left", 0);
		this.right = jsonObject.optDouble("right", 0);
	}
}
