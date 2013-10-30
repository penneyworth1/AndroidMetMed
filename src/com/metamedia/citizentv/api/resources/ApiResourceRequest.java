package com.metamedia.citizentv.api.resources;

import com.metamedia.citizentv.entities.helpers.CTVResource;
import com.metamedia.tools.Security;

/**
 * Use this class as an interface to generate URL to the API services to retrieve resources.
 */
public abstract class ApiResourceRequest 
{
	private static String defaultUserFriendlyText = "android";
	
	private CTVResource resource;
	private String userFriendlyText;
	
	// ---------------------------------------------------------------------------------------------------------
	// Constructor
	// ---------------------------------------------------------------------------------------------------------

	/**
	 * Default constructor.
	 * @param resource The resource to generate a URL to.
	 */
	public ApiResourceRequest(CTVResource resource)
	{
		super();
		this.resource = resource;
		this.userFriendlyText = defaultUserFriendlyText;
	}

	// ---------------------------------------------------------------------------------------------------------
	// Getters & Setters
	// ---------------------------------------------------------------------------------------------------------

	public static String getDefaultUserFriendlyText()
	{
		return defaultUserFriendlyText;
	}
	
	public void setDefaultUserFriendlyText(String defaultUserFriendlyText)
	{
		ApiResourceRequest.defaultUserFriendlyText = defaultUserFriendlyText;
	}
	
	public CTVResource getResource() 
	{
		return resource;
	}

	public void setResource(CTVResource resource) 
	{
		this.resource = resource;
	}

	public String getUserFriendlyText() 
	{
		return userFriendlyText;
	}

	public void setUserFriendlyText(String userFriendlyText) 
	{
		this.userFriendlyText = userFriendlyText;
	}
	
	// ---------------------------------------------------------------------------------------------------------
	// Public Methods
	// ---------------------------------------------------------------------------------------------------------

	/**
	 * Builds the URL to the resource.
	 * @return A URL to retrieve the image resource or null if invalid request or unavailable storage server.
	 */
	abstract public String getUrlPath();
	
	/**
	 * Returns a MD5 hash of the URL. This value can be used as an identifier to match the request.
	 * @return An identifier. May be null if the URL is not defined.
	 */
	public String getIdentifier()
	{
		return Security.md5(getUrlPath());
	}
}

