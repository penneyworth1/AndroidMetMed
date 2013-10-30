package com.metamedia.citizentv.api.asyncconnection;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.os.AsyncTask;
import android.util.Log;

import com.metamedia.citizentv.api.ApiBatchRequest;
import com.metamedia.citizentv.api.ApiBatchResponse;
import com.metamedia.citizentv.api.ServerProxy;

public class AsyncApiBatchConnectionTask extends AsyncTask<ApiBatchRequest, Integer, ApiBatchResponse> 
{
	private static int taskKeyCount = 0;
	private int taskKey;
	private int progress;
	private ServerProxy serverProxy;
	private iApiAsyncConnectionCompletionDelegate completionHandler;
	
	// ---------------------------------------------------------------------------------------------------------
	// Constructor
	// ---------------------------------------------------------------------------------------------------------
	public AsyncApiBatchConnectionTask(ServerProxy serverProxy, iApiAsyncConnectionCompletionDelegate completionHandler)
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
	protected ApiBatchResponse doInBackground(ApiBatchRequest... params) 
	{
		if (this.serverProxy == null)
			return null;
		
		if (params.length != 1)
		{
			return null;
		}
		
		HttpClient httpClient = new DefaultHttpClient();
		HttpPost httpPost = this.serverProxy.createBatchRequestFromApiBatchRequest(params[0], "");
		
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
		ApiBatchResponse apiBatchResponse = new ApiBatchResponse(params[0], httpResponse);
		
		return apiBatchResponse;
	}
	
	protected void onProgressUpdate(Integer... progress) 
	{
		this.progress = progress[0];
		Log.d("CTV", "Progress.." + progress[0]);
    }
	
	protected void onPostExecute(ApiBatchResponse result)
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