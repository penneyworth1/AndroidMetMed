package com.metamedia.citizentv.asynctasks;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.metamedia.citizentv.MainActivity;
import com.metamedia.citizentv.iBlankDelegate;
import com.metamedia.citizentv.api.ApiManager;
import com.metamedia.citizentv.api.ApiParser;
import com.metamedia.citizentv.api.ApiRequest;
import com.metamedia.citizentv.api.ApiRequestCompletionDelegate;
import com.metamedia.citizentv.api.ApiResponse;
import com.metamedia.citizentv.entities.CTVVideo;
import com.metamedia.citizentv.entities.CTVVideoQuality;
import com.metamedia.citizentv.entities.ModelEntity;
import com.metamedia.citizentv.entities.helpers.CTVVideoSource;
import com.metamedia.managers.ModelManager;
import com.metamedia.persistentmodel.ObjectContext;

import android.os.AsyncTask;
import android.util.Log;

public class GetVideoDetailsTask extends AsyncTask<String, Void, String>
{
	CTVVideo ctvVideo;
	ArrayList<String> videoQualityList = new ArrayList<String>();
	
	iBlankDelegate completionDelegate;
    
    public GetVideoDetailsTask(CTVVideo ctvVideoPar, iBlankDelegate completionDelegatePar)
    {
    	completionDelegate = completionDelegatePar;
    	ctvVideo = ctvVideoPar;
    }

	@Override
    protected void onPreExecute()
    {
    	Log.d("CTV", "Begining fetch of video details from server...");
    }
	
	@Override
	protected String doInBackground(String... params)
	{
		ApiRequest request = new ApiRequest(ApiRequest.API_METHOD_VIDEO_DETAILS);
		
		try 
		{
			//JSONObject jsoCriteriaFilter = new JSONObject();
			JSONArray jsaVideoKey = new JSONArray();
			jsaVideoKey.put(ctvVideo.getKey());
			//jsoCriteriaFilter.put("video_key", jsaVideoKey);
		
			request.putCriteriaFilterValue("video_key", jsaVideoKey);
			request.putOutputValue("video_source_list", true);
			request.putOutputValue("category_tag_list", false);
			request.putOutputValue("credit_list", false);
			request.putOutputValue("description_list", false);
			request.putOutputValue("keyword_list", false);
			request.putOutputValue("location_list", false);
			request.putOutputValue("nation_ban_list", false);
			request.putOutputValue("preview_list", false);
			request.putOutputValue("rating_adult_list", false);
			request.putOutputValue("title_list", false);
		
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		

		ApiManager apiManager = ApiManager.getInstance();
		apiManager.performApiRequest(request, new ApiRequestCompletionDelegate ()
		{
			public void onCompletion(ApiResponse apiResponse)
			{
				try 
				{
					//Log.d("API Response", apiResponse.toString());
					
					ObjectContext objectContext = ModelManager.getInstance().getDefaultObjectContext();
					
					ModelEntity [] objects = ApiParser.parseApiResponse(apiResponse, objectContext);
					CTVVideo video = (CTVVideo)objects[0];
					
					
					
					
					for(CTVVideoSource videoSource : video.videoSourceList)
					{
						String videoQualityKey = videoSource.videoQualityKey;
						Log.d("video quality key for video ()()()()()()()()()()", videoQualityKey);
						
						CTVVideoQuality videoQuality = MainActivity.videoQualityMap.get(videoQualityKey);
						videoQualityList.add(videoQuality.urlName);
					}
					
					String chosenQuality = "";
					boolean available480p = false;
					boolean available360p = false;
					boolean available144p = false;
					boolean available240p = false;
					boolean available720p = false;
					for(String qualityString : videoQualityList)
					{
						chosenQuality = qualityString;
						if(qualityString.equalsIgnoreCase("480p"))
							available480p = true;
						if(qualityString.equalsIgnoreCase("360p"))
							available360p = true;
						if(qualityString.equalsIgnoreCase("144p"))
							available144p = true;
						if(qualityString.equalsIgnoreCase("240p"))
							available240p = true;
						if(qualityString.equalsIgnoreCase("720p"))
							available720p = true;
					}
					if(available720p)
						chosenQuality = "720p";
					if(available144p)
						chosenQuality = "144p";
					if(available240p)
						chosenQuality = "240p";
					if(available360p)
						chosenQuality = "360p";
					if(available480p)
						chosenQuality = "480p";
					
					ctvVideo.videoUrl = "http://" + MainActivity.frontendServerHashMap.get(ctvVideo.frontendServerKey).videoHost + "/" + chosenQuality + "/ios-original-" + ctvVideo.getKey() + ".mp4";
					
					objectContext.save();
					
//					for(int i=0;i<numArrayElementsInResultSet;i++)
//					{
//						JSONObject videoJsonObject = resultSet.optJSONObject(i);
//						CTVVideo ctvVideoSearchResult = new CTVVideo("");
//						ctvVideoSearchResult.parse(videoJsonObject);
//						videoList.add(ctvVideoSearchResult);
//						
//						Log.d("video key", ctvVideoSearchResult.getKey());
//					}
					
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		});

		return null;
	}
	
	@Override
	protected void onPostExecute(String stringPar)
    {
		completionDelegate.executeCallback();
    }
	
}
