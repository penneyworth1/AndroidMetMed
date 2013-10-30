package com.metamedia.citizentv.api.asyncconnection;

import com.metamedia.citizentv.api.ApiBatchResponse;
import com.metamedia.citizentv.api.ApiResponse;

public interface iApiAsyncConnectionCompletionDelegate
{
	public void onCompletion(ApiResponse apiResponse, int connectionKey);
	public void onCompletion(ApiBatchResponse apiBatchResponse, int connectionKey);
}