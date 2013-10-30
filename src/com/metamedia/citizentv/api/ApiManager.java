package com.metamedia.citizentv.api;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.util.Log;

import com.metamedia.citizentv.AppStateVariables;
import com.metamedia.citizentv.api.asyncconnection.AsyncApiBatchConnectionTask;
import com.metamedia.citizentv.api.asyncconnection.AsyncApiConnectionTask;
import com.metamedia.citizentv.api.asyncconnection.iApiAsyncConnectionCompletionDelegate;

public class ApiManager 
{
	// ---------------------------------------------------------------------------------------------------------
	// Attributes
	// ---------------------------------------------------------------------------------------------------------

	public static enum PROXY_TYPE 
	{
		DEFAULT, BATCH, UPLOAD, i18n, AUTH, CONFIG, LINK_CREATOR;
	};
	
	private String host;
	private ServerProxy defaultProxy;
	private ServerProxy batchProxy;
	private ServerProxy uploadProxy;
	private ServerProxy i18nProxy;
	private ServerProxy authProxy;
	private ServerProxy configProxy;
	private ServerProxy linkCreatorProxy;

	// Debug attributes
	public boolean logRequests = false;
	public boolean logResponses = false;

	// ---------------------------------------------------------------------------------------------------------
	// Singleton Pattern
	// ---------------------------------------------------------------------------------------------------------

	private static ApiManager instance = null;
	public static ApiManager getInstance()
	{
		if(instance == null)
			instance = new ApiManager();
		return instance;
	}

	// ---------------------------------------------------------------------------------------------------------
	// Constructor
	// ---------------------------------------------------------------------------------------------------------
	
	protected ApiManager()
	{
		super();
		//this.host = "www.testing.citizen.tv";
		this.host = "www.citizen.tv";

		// Create all proxy servers
		this.defaultProxy = new ServerProxy(this.host, "mobile/api");
		this.batchProxy = new ServerProxy(this.host, "mobile/api_batch");
		this.uploadProxy = new ServerProxy(this.host, "mobile/api_upload");
		this.i18nProxy = new ServerProxy(this.host, "mobile/i18n");
		this.authProxy = new ServerProxy(this.host, "mobile/api_auth");
		this.configProxy = new ServerProxy(this.host, "mobile/config");
		this.linkCreatorProxy = new ServerProxy(this.host, "mobile/api_link");
	}

	// ---------------------------------------------------------------------------------------------------------
	// Getters & Setters
	// ---------------------------------------------------------------------------------------------------------

	public String getHost()
	{
		return host;
	}
	
	// ---------------------------------------------------------------------------------------------------------
	// Public Methods
	// ---------------------------------------------------------------------------------------------------------

	public int performAsyncApiRequest(ApiRequest apiRequest, iApiAsyncConnectionCompletionDelegate completion)
	{
		return this.performAsyncApiRequest(apiRequest, PROXY_TYPE.DEFAULT, completion);
	}
	
	public int performAsyncApiRequest(ApiRequest apiRequest, PROXY_TYPE type, iApiAsyncConnectionCompletionDelegate completion)
	{
		ServerProxy serverProxy = this.serverProxyFromProxyType(type);
		
		if (serverProxy == null)
			return -1;
		
		AsyncApiConnectionTask connectionTask = new AsyncApiConnectionTask(serverProxy, completion);
		int connectionTaskKey = connectionTask.getTaskKey();
		connectionTask.execute(apiRequest);
		
		return connectionTaskKey;
	}
	
	public int performApiRequest(ApiRequest apiRequest, iApiRequestCompletionDelegate completion)
	{
		return this.performApiRequest(apiRequest, PROXY_TYPE.DEFAULT, completion);
	}
	
	public int performApiRequest(ApiRequest apiRequest, PROXY_TYPE type, iApiRequestCompletionDelegate completion)
	{
		ServerProxy serverProxy = this.serverProxyFromProxyType(type);
		if (serverProxy == null)
			return -1; // <------ RETURN INVALID VALUE
		
		HttpClient httpClient = new DefaultHttpClient();
		HttpPost httpPost = serverProxy.createRequestFromApiRequest(apiRequest);
		
		try 
		{
			String value = EntityUtils.toString(httpPost.getEntity());
			//Log.d("CTV - ApiManager", "Request: " + value);
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		// Performing the connection
		HttpResponse httpResponse = null;
		try 
		{
			httpResponse = httpClient.execute(httpPost);
			
			// Generating the ApiResponse
			ApiResponse apiResponse = new ApiResponse(apiRequest.getMethod(), httpResponse);
			
			//Log.d("CTV - ApiManager - response", apiResponse.toString());
			
			if (completion != null)
				completion.onCompletion(apiResponse);
		}
		catch (Exception e) 
		{ 
			e.printStackTrace(); 
			AppStateVariables.AbleToReachServer = false;
		}
		
		return 0;
	}
	
	public int performApiBatchRequest(ApiBatchRequest apiBatchRequest, iApiAsyncConnectionCompletionDelegate completion)
	{
		AsyncApiBatchConnectionTask connectionTask = new AsyncApiBatchConnectionTask(batchProxy, completion);
		int connectionTaskKey = connectionTask.getTaskKey();
		connectionTask.execute(apiBatchRequest);
		
		return connectionTaskKey;
	}
	
	// ---------------------------------------------------------------------------------------------------------
	// Private Methods
	// ---------------------------------------------------------------------------------------------------------

	private ServerProxy serverProxyFromProxyType(PROXY_TYPE type)
	{
		switch (type) 
		{
			case DEFAULT:
				return defaultProxy;
			case BATCH:
				return batchProxy;
			case UPLOAD:
				return uploadProxy;
			case i18n:
				return i18nProxy;
			case AUTH:
				return authProxy;
			case CONFIG:
				return configProxy;
			case LINK_CREATOR:
				return linkCreatorProxy;
			default:
				return null;
		}
	}
}
