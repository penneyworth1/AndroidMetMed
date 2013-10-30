package com.metamedia.citizentv.asynctasks;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import com.metamedia.citizentv.api.*;
import com.metamedia.citizentv.entities.CTVVideo;
import com.metamedia.citizentv.*;
import com.metamedia.citizentv.lists.FetchList;
import com.metamedia.citizentv.lists.FetchListPersistenceManager;
import com.metamedia.managers.ModelManager;
import com.metamedia.persistentmodel.ObjectContext;

import android.os.AsyncTask;
import android.util.Log;

public class VideoSearchTask extends AsyncTask<String, Void, String>
{
	//Search parameters
	//int numberOfResults;
    int resultsOffset;
    ArrayList<CTVVideo> videoList;
    FetchList fetchList;
    
    //Delegate to perform tasks on the ui thread after this async task is complete
    iBlankDelegate completionDelegate;
    
    public VideoSearchTask(ArrayList<CTVVideo> videoListPar, FetchList fetchListPar, iBlankDelegate completionDelegatePar)
    {
    	videoList = videoListPar;
    	completionDelegate = completionDelegatePar;
    	fetchList = fetchListPar;
    }
    
    @Override
    protected void onPreExecute ()
    {
    	Log.d("CTV", "Begining video search task...");
    	MainActivity.canLoadMoreVideoItemsNow = false;
    }
    
    @Override
	protected String doInBackground(String... params)
	{
		//Make sure the servers are loaded before continuing
		while(AppStateVariables.InitializingGlobalVarsFromServer)
		{
			Log.d("CTV - loading videos", "Waiting for initialization of global vars");
			if(!AppStateVariables.AbleToReachServer)
			{
				Log.d("CTV - loading videos", "UNABLE TO REACH NETWORK - ABORTING VIDEO SEARCH");
				return AppStateVariables.CONNECTION_ERROR;
			}
			try {Thread.sleep(100);} catch (InterruptedException e1) {e1.printStackTrace(); return "ERROR";}
		}
		
		FetchListPersistenceManager fetchListManager = ModelManager.getInstance().getFetchListPersistenceManager();
		ObjectContext objectContext = ModelManager.getInstance().getDefaultObjectContext();

		if (fetchList.isExpired())
		{
			Log.d("CTV - Video search task","FETCH LIST NEEDS A REFRESH");
			fetchList.refreshFetchList();
			fetchList.loadCount = 0;
			
			objectContext.save();
			fetchListManager.saveFetchList(fetchList);
		}
		else
		{
			Log.d("CTV - Video search task","FETCH LIST NOT EXPIRED");
			
			fetchList.loadMore();
			objectContext.save();
			fetchListManager.saveFetchList(fetchList);
		}
		
		for (int index=fetchList.loadCount; index < fetchList.size(); ++index)
		{
			CTVVideo video = (CTVVideo)fetchList.get(index);
			Log.d("CTV", "Fetch List Video [" + index + "]: " + video.toString());
			videoList.add(video);
		}
		fetchList.loadCount += AppSettings.videoListChunkSize;
		
		return null;
	}
	
	@Override
	protected void onPostExecute(String stringPar)
    {
		MainActivity.canLoadMoreVideoItemsNow = true;
		completionDelegate.executeCallback();
    }
}
