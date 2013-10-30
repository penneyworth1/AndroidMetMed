package com.metamedia.citizentv.entities;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.metamedia.citizentv.MainActivity;
import com.metamedia.citizentv.entities.helpers.*;
import com.metamedia.managers.TranslationManager;

public class CTVVideo extends ModelEntity
{
	private static final long serialVersionUID = 1L;
	
	// ---------------------------------------------------------------------------------------------------------
	// Entity Attributes
	// ---------------------------------------------------------------------------------------------------------
	
	public String frontendServerKey;
	public String privacyDescription;
	public String descriptionText;
	public String descriptionLanguageKey;
	public String titleText;
	public String titleLanguageKey;
	public String privacyKey;
	public String videoUrl;
	
	public String uploaderKey;
//	public CTVUser uploader;
//	public Object videoCompetition;
//	public Object wallReference;
	
	public CTVResource previewImage;
	public CTVResource halfImage;
	public CTVResource firstImage;
	
	public CTVVideoSource [] videoSourceList;
	
	public boolean isFeatureFilm;
	public boolean isBiography;
	public boolean isShowReel;
	public boolean isInterview;
	public boolean isAdvertisement;
	public boolean isSeries;
	public boolean isBehindTheScenes;
	public boolean isShortFilm;
	public boolean isTutorial;
	public boolean isVideoBlog;
	public boolean isNews;
	public boolean isReview;
	public boolean isEvent;
	public boolean isAdultVideo;
	public boolean isPreview;
	public boolean isWinner;
	public boolean restrictAdult;
	public boolean restrictNation;
	
	public int online;
	public int height;
	public int width;
	public int videoState;
	public int videoQuality;
	public int deleted;
	public int views;
	public int competitionEnterOnStateChange;
	
	public long updateDate;
	public long uploadDate;
	public long releaseDate;
	
	public double rating;
	public double duration;
	public double ratingNum;
	
	public ArrayList<String> categoryLeafNames = new ArrayList<String>(); //the word "leaf" is part of this variable name because the categories we will display for this video are the leaves of the category tree that this video contains
	public String categoriesConcatenated;
	
	// ---------------------------------------------------------------------------------------------------------
	// Creating instances and initializing
	// ---------------------------------------------------------------------------------------------------------
	
	public CTVVideo(String key) 
	{
		super(key);
	}

	// ---------------------------------------------------------------------------------------------------------
	// Abstract Methods
	// ---------------------------------------------------------------------------------------------------------

	static public String parseKey(JSONObject jsonObject)
	{
		return jsonObject.optString("video_key", null);
	}
	
	@Override
	public void parse(JSONObject jsonObject)
	{
		super.parse(jsonObject);
		
		this.setKey(CTVVideo.parseKey(jsonObject));

		this.frontendServerKey 	= jsonObject.optString("frontend_server_key", null);
		this.privacyDescription = jsonObject.optString("video_privacy_description", null);
		this.privacyKey 		= jsonObject.optString("video_privacy_key", null);
		
		//videoUrl will be set when running the task to get the video details that finds the correct quality at which to stream.
		//this.videoUrl = "http://" + MainActivity.frontendServerHashMap.get(this.frontendServerKey).videoHost + "/480p/ios-original-" + jsonObject.optString("video_key", "videonotfound") + ".mp4";
	
		this.isFeatureFilm 		= jsonObject.optBoolean("is_feature_film");
		this.isBiography 		= jsonObject.optBoolean("is_biography");
		this.isAdvertisement 	= jsonObject.optBoolean("is_advertisement");
		this.isShowReel 		= jsonObject.optBoolean("is_show_reel");
		this.isInterview 		= jsonObject.optBoolean("is_interview");
		this.isSeries 			= jsonObject.optBoolean("is_series");
		this.isBehindTheScenes	= jsonObject.optBoolean("is_behind_the_scenes");
		this.isShortFilm		= jsonObject.optBoolean("is_short_film");
		this.isTutorial 		= jsonObject.optBoolean("is_tutorial");
		this.isVideoBlog 		= jsonObject.optBoolean("is_video_blog");
		this.isNews 			= jsonObject.optBoolean("is_news");
		this.isReview 			= jsonObject.optBoolean("is_review");
		this.isEvent 			= jsonObject.optBoolean("is_event");
		this.isAdultVideo 		= jsonObject.optBoolean("is_adult_video");
		this.isShortFilm 		= jsonObject.optBoolean("is_short_film");
		this.isPreview 			= jsonObject.optBoolean("is_preview");
		this.isWinner 			= jsonObject.optBoolean("is_winner");
		this.restrictAdult 		= jsonObject.optBoolean("video_restrict_adult");
		this.restrictNation 	= jsonObject.optBoolean("video_restrict_nation");
		
		this.width 							= jsonObject.optInt("video_width");
		this.height 						= jsonObject.optInt("video_height");
		this.videoState						= jsonObject.optInt("video_state");
		this.videoQuality 					= jsonObject.optInt("video_quality");
		this.online 						= jsonObject.optInt("video_online");
		this.deleted 						= jsonObject.optInt("video_deleted");
		this.views 							= jsonObject.optInt("video_views");
		this.competitionEnterOnStateChange 	= jsonObject.optInt("competition_enter_on_state_change");
		
		this.updateDate = jsonObject.optLong("video_update_date");
		this.uploadDate = jsonObject.optLong("video_upload_date");
		this.releaseDate = jsonObject.optLong("video_release_date");
		
		this.rating 	= jsonObject.optDouble("video_rating", 0.0);
		this.duration 	= jsonObject.optDouble("video_duration", 0.0);
		this.ratingNum	= jsonObject.optDouble("video_rating_num", 0.0);
		
		JSONObject jsoUploader = jsonObject.optJSONObject("uploader");
		this.uploaderKey = CTVUser.parseKey(jsoUploader);
		//this.uploader = new CTVUser(CTVUser.parseKey(uploader));
		//this.uploader.parse(uploader);
		
		//TODO - Populate video competition
		
		JSONObject jsoVideoDescription = jsonObject.optJSONObject("video_description");
		this.descriptionText = jsoVideoDescription.optString("text");
		this.descriptionLanguageKey = jsoVideoDescription.optString("language");
		
//		loadUploader(jsonObject, this);
		
		JSONObject jsoVideoTitle = jsonObject.optJSONObject("video_title");
		this.titleText = jsoVideoTitle.optString("text");
		this.titleLanguageKey = jsoVideoTitle.optString("language");
		
		this.previewImage = new CTVResource(jsonObject.optJSONObject("image_preview"));
		this.halfImage = new CTVResource(jsonObject.optJSONObject("image_half"));
		this.firstImage = new CTVResource(jsonObject.optJSONObject("image_first"));
		
		JSONArray jsaCategories = jsonObject.optJSONArray("category");
		parseCategories(jsaCategories);
		
//		JSONObject jsoWallReference = jsonObject.optJSONObject("wall");
//		this.wallReference = new CTVWallReference();
//		this.wallReference.entryCount = jsoWallReference.getInt("wall_entry_count");
//		this.wallReference.key = jsoWallReference.getString("key");

		if (jsonObject.has("video_source_list"))
		{
			JSONArray jsonVideoSourceList = jsonObject.optJSONArray("video_source_list");
			CTVVideoSource [] videoSourceList = new CTVVideoSource[jsonVideoSourceList.length()];
			for(int i=0; i<jsonVideoSourceList.length(); ++i)
			{
				try 
				{
					JSONObject rawVideoSource = jsonVideoSourceList.getJSONObject(i);
					CTVVideoSource videoSource = new CTVVideoSource(rawVideoSource);
					videoSourceList[i] = videoSource;
				} 
				catch (JSONException e) 
				{
					e.printStackTrace();
				}

			}
			this.videoSourceList = videoSourceList;
		}
	}
	
	private void parseCategories(JSONArray categoryRootArray)
	{
		categoriesConcatenated = "";
		for(int i=0;i<categoryRootArray.length();i++)
		{
			JSONObject jsoCategory;
			try 
			{
				jsoCategory = categoryRootArray.getJSONObject(i);
				JSONArray jsaSubCategories = jsoCategory.optJSONArray("children");
				if(jsaSubCategories.length() > 0)
				{
					parseCategories(jsaSubCategories);
				}
				else //this category is a leaf
				{
					String leafCategoryKey = jsoCategory.optString("key");
					String leafCategoryName = MainActivity.translationManager.getTranslation(TranslationManager.MAP_NAME_CATEGORIES, leafCategoryKey);
					categoryLeafNames.add(leafCategoryName);
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			
		}
		boolean firstCategoryAdded = false;
		for(int i=0;i<categoryLeafNames.size();i++)
		{
			if(!firstCategoryAdded)
			{
				categoriesConcatenated = categoryLeafNames.get(i);
				firstCategoryAdded = true;
			}
			else
				categoriesConcatenated += ", " + categoryLeafNames.get(i);
		}
	}
}
