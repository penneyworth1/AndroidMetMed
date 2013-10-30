package com.metamedia.citizentv.api.asyncconnection;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import com.metamedia.citizentv.api.ApiRequest;
import com.metamedia.citizentv.api.ApiResponse;
import com.metamedia.citizentv.api.ServerProxy;

import android.os.AsyncTask;
import android.util.Log;

public class AsyncApiConnectionTask extends AsyncTask<ApiRequest, Integer, ApiResponse> 
{
	private static int taskKeyCount = 0;
	private int taskKey;
	private int progress;
	private ServerProxy serverProxy;
	private iApiAsyncConnectionCompletionDelegate completionHandler;
	
	// ---------------------------------------------------------------------------------------------------------
	// Constructor
	// ---------------------------------------------------------------------------------------------------------
	public AsyncApiConnectionTask(ServerProxy serverProxy, iApiAsyncConnectionCompletionDelegate completionHandler)
	{
		super();
		
		this.taskKey = taskKeyCount;
		taskKeyCount++;
		
		this.serverProxy = serverProxy;
		this.progress = 0;
		this.setCompletionHandler(completionHandler);
	}
	
	// ---------------------------------------------------------------------------------------------------------
	// Getters
	// ---------------------------------------------------------------------------------------------------------
	public int getTaskKey()
	{
		return this.taskKey;
	}
	
	public int getProgress()
	{
		return this.progress;
	}
		
	public iApiAsyncConnectionCompletionDelegate getCompletionHandler()
	{
		return completionHandler;
	}

	public void setCompletionHandler(iApiAsyncConnectionCompletionDelegate completionHandler)
	{
		this.completionHandler = completionHandler;
	}
	
	// ---------------------------------------------------------------------------------------------------------
	// AsyncTask Methods
	// ---------------------------------------------------------------------------------------------------------		
	@Override
	protected ApiResponse doInBackground(ApiRequest... params) 
	{
		if (this.serverProxy == null)
			return null;
		
		if (params.length != 1)
		{
			return null;
		}
		
		HttpClient httpClient = new DefaultHttpClient();
		HttpPost httpPost = this.serverProxy.createRequestFromApiRequest(params[0]);
		
		logRequest(httpPost); // <--- DEBUG FLAG!
		
		// Performing the connection
		HttpResponse httpResponse = null;
		try 
		{
			httpResponse = httpClient.execute(httpPost);
		}
		catch (ClientProtocolException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		// Generating the ApiResponse
		ApiResponse apiResponse = new ApiResponse(params[0].getMethod(), httpResponse);
		
		return apiResponse;
	}
	
	protected void onProgressUpdate(Integer... progress)
	{
		this.progress = progress[0];
		Log.d("CTV", "Progress.." + progress[0]);
    }
	
	protected void onPostExecute(ApiResponse result)
	{
		if (completionHandler != null)
			completionHandler.onCompletion(result, this.taskKey);
	}

	// ---------------------------------------------------------------------------------------------------------
	// Private methods
	// ---------------------------------------------------------------------------------------------------------
	private void logRequest(HttpPost httpPost)
	{
		try 
		{
			Log.d("HTTP Request", "URL: " + httpPost.getURI().toURL().toString());
			Log.d("HTTP Request", "BODY: " + EntityUtils.toString(httpPost.getEntity()));
		}
		catch (Exception e) { }
	}
}
