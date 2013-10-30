package com.metamedia.citizentv.api;

public interface iApiRequestCompletionDelegate
{
	public void onCompletion(ApiResponse apiResponse);
	public void onCompletion(ApiBatchResponse apiBatchResponse, int connectionKey);
}