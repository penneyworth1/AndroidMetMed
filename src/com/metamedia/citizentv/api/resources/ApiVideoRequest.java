package com.metamedia.citizentv.api.resources;

import com.metamedia.citizentv.MainActivity;
import com.metamedia.citizentv.entities.CTVFrontendServer;
import com.metamedia.citizentv.entities.CTVVideoQuality;
import com.metamedia.citizentv.entities.helpers.CTVResource;

public class ApiVideoRequest extends ApiResourceRequest 
{
	private CTVVideoQuality videoQuality;
	
	// ---------------------------------------------------------------------------------------------------------
	// Constructor
	// ---------------------------------------------------------------------------------------------------------

	public ApiVideoRequest(CTVResource resource, CTVVideoQuality videoQuality) 
	{
		super(resource);
	}
	
	// ---------------------------------------------------------------------------------------------------------
	// Getters & Setters
	// ---------------------------------------------------------------------------------------------------------

	public CTVVideoQuality getVideoQuality() 
	{
		return videoQuality;
	}

	public void setVideoQuality(CTVVideoQuality videoQuality) 
	{
		this.videoQuality = videoQuality;
	}

	// ---------------------------------------------------------------------------------------------------------
	// Public Methods
	// ---------------------------------------------------------------------------------------------------------

	@Override
	public String getUrlPath() 
	{
		// Get the used storage server
	    CTVFrontendServer storageServer = MainActivity.frontendServerHashMap.get(this.getResource().storageServerKey);
	    
	    // If no storage server, URL cannot be build. Return null.
	    if (storageServer == null)
	        return null;
	    
	    // Finally, build the URL
	    String urlPath = "http://" + storageServer.videoHost + "/" + videoQuality.urlName + "/" + this.getUserFriendlyText() + "-" + this.getResource().resourceKey + ".mp4";
	    
		return urlPath;
	}
	
	public String getVideoPreviewUrlPath()
	{
		// Get the used storage server
	    CTVFrontendServer storageServer = MainActivity.frontendServerHashMap.get(this.getResource().storageServerKey);
	    
	    // If no storage server, URL cannot be build. Return null.
	    if (storageServer == null)
	        return null;
	    
	    // Finally, build the URL
	    String urlPath = "http://" + storageServer.videoHost + "/" + this.getUserFriendlyText() + "-" + this.getResource().resourceKey + ".dat";
	    
		return urlPath;
	}
}
