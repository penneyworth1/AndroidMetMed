package com.metamedia.citizentv.api;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;

public class ApiBatchResponse 
{
	// ----- Attributes ----- //
	private int error;
	private JSONArray payload;
	private int sequence;
	private String session;
	private String serverKey;
	private List<ApiResponse> apiResponses;

	// ----- Constructor ----- //
	public ApiBatchResponse(int error) 
	{
		super();
		this.error = error;
	}

	public ApiBatchResponse(ApiBatchRequest batchRequest, HttpResponse httpResponse)
	{
		super();
		this.setAttributesFromHttpResponse(batchRequest, httpResponse);
	}

	public ApiBatchResponse(ApiBatchRequest batchRequest, String stringResponse)
	{
		super();
		this.setAttributesFromStringResponse(batchRequest, stringResponse);
	}

	// ----- Getters ----- //
	public int getError() 
	{
		return error;
	}

	public JSONArray getPayload() 
	{
		return payload;
	}

	public int getSequence() 
	{
		return sequence;
	}

	public String getSession() 
	{
		return session;
	}

	public String getServerKey() 
	{
		return serverKey;
	}

	public List<ApiResponse> getApiResponses() 
	{
		return apiResponses;
	}

	// ----- Private Methods ----- //
	private void setAttributesFromHttpResponse(ApiBatchRequest batchRequest, HttpResponse httpResponse) 
	{
		int statusCode = httpResponse.getStatusLine().getStatusCode();

		if (statusCode == HttpStatus.SC_OK)
		{
			HttpEntity entity = httpResponse.getEntity();
			try 
			{
				InputStream inputStream = entity.getContent();
				BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream,"iso-8859-1"));
				this.setAttributesFromBufferedReader(batchRequest, reader);
				inputStream.close();
			} 
			catch (IllegalStateException e) 
			{
				e.printStackTrace();
				this.error = ApiError.PARSING_ERROR;
				return;
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
				this.error = ApiError.PARSING_ERROR;
				return;
			}
		}
		else if (statusCode == HttpStatus.SC_UNAUTHORIZED)
		{
			this.error = ApiError.HTTP_AUTHENTICATION_ERROR;
		}
		else if (statusCode == HttpStatus.SC_REQUEST_TIMEOUT)
		{
			this.error = ApiError.HTTP_TIMEOUT;
		}
		else
		{
			this.error = ApiError.HTTP_ERROR;
		}
	}

	private void setAttributesFromStringResponse(ApiBatchRequest batchRequest, String stringResponse)
	{ 
		InputStream is = new ByteArrayInputStream(stringResponse.getBytes());
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));

		try 
		{
			this.setAttributesFromBufferedReader(batchRequest, bufferedReader);
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
			this.error = ApiError.PARSING_ERROR;
			return;
		}
	}

	private void setAttributesFromBufferedReader(ApiBatchRequest batchRequest, BufferedReader reader) throws IOException
	{
		// "return_value" must be the last handled attribute. 
		// Lets store the data in some variable and handle it at the end of the method.
		String rawReturnValue = null;

		String line = reader.readLine();
		while (line != null) 
		{
			try 
			{
				line = URLDecoder.decode(line, "UTF-8");
			}
			catch (UnsupportedEncodingException e) 
			{
				e.printStackTrace();
				this.error = ApiError.PARSING_ERROR; 
			}

			String[] parts = line.split("=");

			if (parts[0].equalsIgnoreCase("session"))
			{
				this.session = parts[1];
			}
			else if (parts[0].equalsIgnoreCase("server_key"))
			{
				this.serverKey = parts[1];
			}
			else if (parts[0].equalsIgnoreCase("error_code"))
			{
				this.error = ApiError.errorFromString(parts[1]);
			}
			else if (parts[0].equalsIgnoreCase("sequence"))
			{
				this.sequence = Integer.parseInt(parts[1]);
			}
			else if (parts[0].equalsIgnoreCase("return_value"))
			{
				rawReturnValue = parts[1];
			}

			line = reader.readLine();
		}

		// Lets handle the "return_value"
		if (rawReturnValue != null)
		{
			JSONArray jsonArray;
			try 
			{
				jsonArray = new JSONArray(rawReturnValue);
				this.payload = jsonArray;
				this.setApiResponsesFromJSONArray(batchRequest, jsonArray);
			} 
			catch (JSONException e) 
			{
				e.printStackTrace();
				this.error = ApiError.PARSING_ERROR;
			}
		}
	}

	@SuppressLint("UseSparseArrays")
	private void setApiResponsesFromJSONArray(ApiBatchRequest batchRequest, JSONArray jsonArray) throws JSONException
	{
		// Before starting, we create a mapping of ApiRequests by "sequence".
		Map<Integer,ApiRequest> requests = new HashMap<Integer,ApiRequest>();
		for (ApiRequest apiRequest: batchRequest.getRequests())
			requests.put(Integer.valueOf(apiRequest.getSequence()), apiRequest);

		// First step is to create from the JSONArray the ApiResponses.
		// Then we are going to put the ApiResposnes in a map keyed by the "sequence".
		Map<Integer,ApiResponse> responses = new HashMap<Integer, ApiResponse>();
		for(int index=0; index<jsonArray.length(); ++index)
		{
			ApiRequest apiRequest = requests.get(Integer.valueOf(index));
			JSONObject jsonResponse = jsonArray.getJSONObject(index);

			// We manually add those properties that only came in the batch response headers.
			jsonResponse.put("session", this.session);
			jsonResponse.put("server_key", this.serverKey);

			ApiResponse apiResponse = new ApiResponse(apiRequest.getMethod(), jsonResponse);			
			responses.put(Integer.valueOf(apiResponse.getSequence()), apiResponse);
		}

		List<ApiResponse> list = new ArrayList<ApiResponse>();
		for (int index=0; index<responses.size(); ++index)
		{
			ApiResponse apiResponse = responses.get(Integer.valueOf(index));

			if (apiResponse == null)
			{
				this.error = ApiError.PARSING_ERROR;
				return;
			}

			list.add(apiResponse);
		}
		this.apiResponses = list;
	}

	// ----- Default Methods ----- //
	@Override
	public String toString() 
	{
		return "ApiBatchResponse [error=" + error + ", sequence=" + sequence + ", session=" + session
				+ ", serverKey=" + serverKey + ", payload=" + payload
				+ ", apiResponses=" + apiResponses
				+ "]";
	}
}
