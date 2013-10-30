package com.metamedia.citizentv.entities;

import org.json.JSONObject;

public class CTVFrontendServer extends ModelEntity
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public String videoHost;
	public String audioHost;
	public String imageHost;
	public String description;
	public String backendPort;
	public String backendIp;
	public String documentHost;
	public String realDomain;
	
	public CTVFrontendServer(String key)
	{
		super(key);
	}
	
	public long expiringTimeOffset()
	{
		// Subclasses may override and set a different value!
		// Time length is returned in milliseconds
		return 600000L;
	}
	
	public String toString()
	{
		return "Video Host: " + videoHost + ", Audio Host: " + audioHost + ", Image Host: " + imageHost + ", Description: " + description + ", Key: " + getKey() + ", Backend Port: " + backendPort + ", Backend IP: " + backendIp + ", Document Host: " + documentHost + ", Real Domain: " + realDomain;
	}

	@Override
	public void parse(JSONObject jsonObject) 
	{
		super.parse(jsonObject);
		
		videoHost = jsonObject.optString("host_video");
		audioHost = jsonObject.optString("host_audio");
		imageHost = jsonObject.optString("host_image");
		description = jsonObject.optString("description");
		//= jsonObject.optString("key");
		backendPort = jsonObject.optString("backend_port");
		backendIp = jsonObject.optString("backend_ip");
		documentHost = jsonObject.optString("host_document");
		realDomain = jsonObject.optString("real_domain");
	}
}
