package com.metamedia.citizentv.api;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.metamedia.tools.*;

public class ServerProxy 
{	
	// ----- Attributes ----- //	
	/**
	 * The host of the server ("www.example.com").
	 */
	private String host;
	
	/**
	 * The port where the connection will be directed to (80 by default).
	 */
	private int port;
	
	/**
	 * The REST path that will be used ("/example/api")
	 */
	private String rootPath;
	
	// ----- Constructors ----- //
	public ServerProxy(String host, String roothPath) {
		super();
		this.host = host;
		this.rootPath = roothPath;
		this.port = 80;
	}
	
	// ----- Getters & Setters ----- //
	public String getHost() 
	{
		return host;
	}
	
	public void setHost(String host) 
	{
		this.host = host;
	}

	public int getPort() 
	{
		return port;
	}
	
	public void setPort(int port) 
	{
		this.port = port;
	}
	
	public String getRootPath() 
	{
		return rootPath;
	}
	
	public void setRootPath(String rootPath) 
	{
		this.rootPath = rootPath;
	}
	
	// ----- Public Methods ----- //
	/**
	 * Converts an ApiRequest into a HTTP POST request
	 * @param request The custom ApiRequest.
	 * @return An HTTP POST request ready to be fired, or null if error.
	 */
	public HttpPost createRequestFromApiRequest(ApiRequest request)
	{
		// Creating the URL
		String url = "http://" + this.host + ":" + this.port + "/" + this.rootPath + "/" + request.getMethod();
		
		// Creating the HttpPost request
		HttpPost httpPostRequest = new HttpPost(url);
		
		// Creating the container for the POST values
        List<NameValuePair> postValues = new ArrayList<NameValuePair>(2);
        
        // Adding the default POST values
        postValues.addAll(ServerProxy.getPostValues(true, request.getSequence()));
        
        // Adding the ApiRequest arguments into the POST values container
        String jsonString = request.getCallArguments().toString();
        postValues.add(new BasicNameValuePair("request", jsonString));
        
        try 
        {
        	// Encoding the POST values and adding them to the HttpPost request
			httpPostRequest.setEntity(new UrlEncodedFormEntity(postValues));
		}
        catch (UnsupportedEncodingException e)
        {
			e.printStackTrace();
			return null;
		}
        // Returning the HttpPost request
		return httpPostRequest;
	}
	
	/**
	 * Converts an ApiBatchRequest into a HTTP POST request
	 * @param request The batch request.
	 * @param batchMethod The batch method to use with the server proxy.
	 * @return An HTTP POST request ready to be fired, or null if error.
	 */
	public HttpPost createBatchRequestFromApiBatchRequest(ApiBatchRequest request, String batchMethod)
	{
		/*
		 * Batch Requests "request" value must follow the following pattern:
		 * 
		 	[{"method":"<METHOD_NAME_1>",
		 	  "language":"<LANGUAGE_KEY_1>",
		 	  "arguments":{<CALL_ARGUMENTS_OF_AN_API_REQUEST_1>},
		 	  "sequence":<SEQUENCE_CODE_1>
		 	 },
		 	 {"method":"<METHOD_NAME_2>",
		 	  "language":"<LANGUAGE_KEY_2>",
		 	  "arguments":{<CALL_ARGUMENTS_OF_AN_API_REQUEST_2>},
		 	  "sequence":<SEQUENCE_CODE_2>
		 	 },
		 	 <...>
		 	]
		 * 
		 * Also, in order to recognize which response come from which request, 
		 * we use the "sequence" to assign label the request with the index in the array,
		 * so we can recognize and sort again the ApiResponses from the ApiBatchResponse.
		 */
		
		// Creating the URL
		String url = "http://" + this.host + ":" + this.port + "/" + this.rootPath + (batchMethod!=null?("/" + batchMethod):"");
				
		// Creating the HttpPost request
		HttpPost httpPostRequest = new HttpPost(url);
		
		// Creating the container for the POST values
        List<NameValuePair> postValues = new ArrayList<NameValuePair>(2);
        
        // Adding the default POST values
        postValues.addAll(ServerProxy.getPostValues(true, request.getSequence()));
		
        // Converting the array of apiRequests into a JSONArray.
        List<ApiRequest> apiRequests = request.getRequests();
        JSONArray jsonArray = new JSONArray();
        for (int index = 0; index < apiRequests.size(); ++index)
        {
        	ApiRequest apiRequest = apiRequests.get(index);
        	apiRequest.setSequence(Integer.valueOf(index));
        	
        	JSONObject jsonRequest = new JSONObject();
        	
        	try 
        	{
				jsonRequest.put("method", apiRequest.getMethod());
				jsonRequest.put("language", "nae6A8x70p"); // <--------------------------- TODO!
				jsonRequest.put("sequence", apiRequest.getSequence());
				jsonRequest.put("arguments", apiRequest.getCallArguments());
			}
        	catch (JSONException e) 
        	{
				e.printStackTrace();
				return null;
			}
        	jsonArray.put(jsonRequest);
        }
        
        // Adding the stringed JSON as the request value 
        postValues.add(new BasicNameValuePair("request", jsonArray.toString()));
        
        try 
        {
        	// Encoding the POST values and adding them to the HttpPost request
			httpPostRequest.setEntity(new UrlEncodedFormEntity(postValues));
		}
        catch (UnsupportedEncodingException e) 
        {
			e.printStackTrace();
			return null;
		}
        
		return httpPostRequest;
	}
	
	// ----- Private Methods ----- //
	static private List<NameValuePair> getPostValues(boolean addSession, int sequence)
	{
		List<NameValuePair> nameValues = new ArrayList<NameValuePair>(2);
		
		nameValues.add(new BasicNameValuePair("language", "nae6A8x70p"));
		nameValues.add(new BasicNameValuePair("user_ip", "" + Utils.convertIPStringToInteger(DeviceInfo.getIPAddress(true))));
		nameValues.add(new BasicNameValuePair("sequence", "" + sequence));
		nameValues.add(new BasicNameValuePair("client", "ctv-1.0"));
		nameValues.add(new BasicNameValuePair("version", "0.1"));
        nameValues.add(new BasicNameValuePair("software", "Android-" + DeviceInfo.getOsVersion()));
        nameValues.add(new BasicNameValuePair("hardware", DeviceInfo.getModel()));
        nameValues.add(new BasicNameValuePair("udid", DeviceInfo.getDeviceId()));
        
        // TODO: Get values from the correct place!
        
		return nameValues;
	}
	
}
