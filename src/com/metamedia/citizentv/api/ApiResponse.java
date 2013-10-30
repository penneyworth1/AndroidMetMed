package com.metamedia.citizentv.api;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

public class ApiResponse
{		
	// ----- Attributes ----- //
	private String method;
	private int error;
	private JSONObject payload;
	private int sequence;
	private double time;
	private double executionTime;
	private String session;
	private String serverKey;
	
	// ----- Constructors ----- //
	public ApiResponse(String method, HttpResponse httpResponse)
	{
		super();
		this.method = method;
		this.setAttributesFromHttpResponse(httpResponse);
	}
	
	public ApiResponse(String method, String stringResponse) 
	{
		super();
		this.method = method;
		this.setAttributesFromStringResponse(stringResponse);
	}
	
	public ApiResponse(String method, int error) 
	{
		super();
		this.method = method;
		this.error = error;
	}
	
	public ApiResponse(String method, JSONObject jsonResponse) 
	{
		super();
		this.method = method;
		this.setAttributesFromJSONObjectResponse(jsonResponse);
	}
	
	// ----- Getters ----- //
	public String getMethod() 
	{
		return method;
	}
	
	public int getError() 
	{
		return error;
	}
	
	public JSONObject getPayload() 
	{
		return payload;
	}
	
	public int getSequence() 
	{
		return sequence;
	}
	
	public double getTime() 
	{
		return time;
	}
	
	public double getExecutionTime() 
	{
		return executionTime;
	}
	
	public String getSession() 
	{
		return session;
	}
	
	public String getServerKey() 
	{
		return serverKey;
	}
	
	// ----- Private Methods ----- //
	private void setAttributesFromHttpResponse(HttpResponse httpResponse) 
	{
		int statusCode = httpResponse.getStatusLine().getStatusCode();

		if (statusCode == HttpStatus.SC_OK)
		{
			HttpEntity entity = httpResponse.getEntity();
			try 
			{
				InputStream inputStream = entity.getContent();
				BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream,"iso-8859-1"));
				this.setAttributesFromBufferedReader(reader);
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
	
	private void setAttributesFromStringResponse(String stringResponse)
	{ 
		InputStream is = new ByteArrayInputStream(stringResponse.getBytes());
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
		
		try 
		{
			this.setAttributesFromBufferedReader(bufferedReader);
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
			this.error = ApiError.PARSING_ERROR;
			return;
		}
	}
	
	private void setAttributesFromBufferedReader(BufferedReader reader) throws IOException
	{
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
			
			String[] parts = line.split("=",2); //SMS try getting exactly two parts so as not to split in the middle of the payload e.g. href=blahblah
			
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
			else if (parts[0].equalsIgnoreCase("time"))
			{
				this.time = Double.parseDouble(parts[1]);
			}
			else if (parts[0].equalsIgnoreCase("time_execution"))
			{
				this.executionTime = Double.parseDouble(parts[1]);
			}
			else if (parts[0].equalsIgnoreCase("return_value"))
			{
				JSONObject jsonObject;
				try 
				{
					jsonObject = new JSONObject(parts[1]);
					this.payload = jsonObject;
				}
				catch (JSONException e) 
				{
					e.printStackTrace();
					this.error = ApiError.PARSING_ERROR;
				}
			}
			
			line = reader.readLine();
		}
	}
	
	public void setAttributesFromJSONObjectResponse(JSONObject jsonResponse)
	{
		try 
		{
			if (jsonResponse.has("error_code"))
				this.error = ApiError.errorFromString(jsonResponse.getString("error_code"));
			if (jsonResponse.has("time"))
				this.time = jsonResponse.getDouble("time");
			if (jsonResponse.has("time_execution"))
				this.executionTime = jsonResponse.getDouble("time_execution");
			if (jsonResponse.has("session"))
				this.session = jsonResponse.getString("session");
			if (jsonResponse.has("sequence"))
				this.sequence = jsonResponse.getInt("sequence");
			if (jsonResponse.has("server_key"))
				this.serverKey = jsonResponse.getString("server_key");	
			if (jsonResponse.has("return_value"))
				this.payload = jsonResponse.getJSONObject("return_value");
//				this.payload = new JSONObject(jsonResponse.getString("return_value")); // <--- Not sure if this is the correct one or the upper line
		} 
		catch (JSONException e) 
		{
			e.printStackTrace();
			this.error = ApiError.PARSING_ERROR;
			return;
		};
	}

	// ----- Default Methods ----- //
	@Override
	public String toString() 
	{
		return "ApiResponse [method=" + method + ", error=" + error
				+ ", sequence=" + sequence + ", time="
				+ time + ", executionTime=" + executionTime + ", session="
				+ session + ", serverKey=" + serverKey + ", payload=" + payload + "]";
	}
}
