package com.metamedia.citizentv.api.resources;

import com.metamedia.citizentv.MainActivity;
import com.metamedia.citizentv.entities.CTVFrontendServer;
import com.metamedia.citizentv.entities.helpers.CTVResource;

public class ApiAudioRequest extends ApiResourceRequest
{		
	// ---------------------------------------------------------------------------------------------------------
	// Constructor
	// ---------------------------------------------------------------------------------------------------------

	public ApiAudioRequest(CTVResource resource) 
	{
		super(resource);
	}

	// ---------------------------------------------------------------------------------------------------------
	// Getters & Setters
	// ---------------------------------------------------------------------------------------------------------

	// no getters or setters needed

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
		String urlPath = "http://" + storageServer.audioHost + "/" + this.getUserFriendlyText() + "-" + this.getResource().resourceKey + ".mp4";

		return urlPath;
	}
}