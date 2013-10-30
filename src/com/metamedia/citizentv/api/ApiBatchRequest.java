package com.metamedia.citizentv.api;

import java.util.List;

public class ApiBatchRequest 
{	
	// ----- Attributes ----- //
	private List<ApiRequest> requests;
	private int sequence;

	// ----- Constructors ----- //
	public ApiBatchRequest() 
	{
		super();
		this.requests = null;
		this.sequence = 0;
	}
	
	public ApiBatchRequest(List<ApiRequest> requests) 
	{
		super();
		this.requests = requests;
		this.sequence = 0;
	}

	// ----- Getters & Setters ----- //
	public List<ApiRequest> getRequests() 
	{
		return requests;
	}
	
	public void setRequests(List<ApiRequest> requests) 
	{
		this.requests = requests;
	}
	
	public int getSequence() 
	{
		return sequence;
	}
	
	public void setSequence(int sequence) 
	{
		this.sequence = sequence;
	}

	// ----- Default Methods ----- //
	@Override
	public String toString() 
	{
		return "ApiBatchRequest [requests=" + requests + ", sequence=" + sequence + "]";
	}
}
