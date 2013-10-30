package com.metamedia.citizentv.entities.helpers;

import org.json.JSONObject;

public class CTVVideoSource extends EntityHelper 
{
	private static final long serialVersionUID = 1L;

	public static enum STATE
	{
		UNKNOWN, CREATED, TRANSCODE_IN_PROGRESS, TRANSCODE_COMPLETED, TRANSCODE_FAILED
	}
	
	public String videoQualityKey;
	public int frameWidth;
	public int frameHeight;
	public STATE state;
	
	public CTVVideoSource(JSONObject jsonObject) 
	{
		super(jsonObject);
	}

	@Override
	public void parse(JSONObject jsonObject) 
	{
		this.videoQualityKey = jsonObject.optString("video_quality_key");
		this.frameWidth = jsonObject.optInt("width");
		this.frameHeight = jsonObject.optInt("height");
		this.state = getStateFromInt(jsonObject.optInt("state"));
	}
	
	private STATE getStateFromInt(int i)
	{
		if (i==0)
			return STATE.CREATED;
		else if (i==1)
			return STATE.TRANSCODE_IN_PROGRESS;
		else if (i==2)
			return STATE.TRANSCODE_COMPLETED;
		else if (i==3)
			return STATE.TRANSCODE_FAILED;
		
		return STATE.UNKNOWN;
	}
}
