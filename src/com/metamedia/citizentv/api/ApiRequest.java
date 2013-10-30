package com.metamedia.citizentv.api;

import org.json.JSONException;
import org.json.JSONObject;

public class ApiRequest implements java.io.Serializable
{
	private static final long serialVersionUID = 1L;

	public static final String API_METHOD_NULL = "";
	
	// ----- Methods for DefaultProxy Server ----- //
	public static final String API_METHOD_LOGIN= "user_login";
	public static final String API_METHOD_USER_CREATE = "user_create";
	public static final String API_METHOD_IS_LOGGED_ON = "user_is_logged_on";
	public static final String API_METHOD_LOGOUT = "user_logout";
	public static final String API_METHOD_VIDEO_SEARCH = "video_search";
	public static final String API_METHOD_VIDEO_QUALITY_SEARCH = "video_quality_search";
	public static final String API_METHOD_VIDEO_TITLE_SEARCH = "video_title_search";
	public static final String API_METHOD_VIDEO_DETAILS = "video_details";
	public static final String API_METHOD_VIDEO_DELETE = "video_delete";
	public static final String API_METHOD_VIDEO_INFO_UPDATE = "video_info_update";
	public static final String API_METHOD_VIDEO_TITLE_UPDATE = "video_title_update";
	public static final String API_METHOD_VIDEO_DESCRPITION_UPDATE = "video_description_update";
	public static final String API_METHOD_VIDEO_CATEGORY_TAG_UPDATE = "video_category_tag_update";
	public static final String API_METHOD_VIDEO_VIEW_ADD = "video_view_add";
	public static final String API_METHOD_VIDEO_OPINION_ADD = "video_opinion_add";
	public static final String API_METHOD_VIDEO_COMPETITION_VOTE = "video_competition_vote";
	public static final String API_METHOD_VIDEO_COMPETITION_ENTER = "video_competition_enter";
	public static final String API_METHOD_VIDEO_COMPETITION_LEAVE = "video_competition_leave";
	public static final String API_METHOD_USER_INFO_GET = "user_info_get";
	public static final String API_METHOD_USER_FOLLOW = "user_follow";
	public static final String API_METHOD_USER_UNFOLLOW = "user_unfollow";
	public static final String API_METHOD_USER_FOLLOW_GET = "user_follow_get";
	public static final String API_METHOD_USER_INFO_UPDATE = "user_info_update";
	public static final String API_METHOD_USER_PROFILE_VIEW_ADD = "user_profile_view_add";
	public static final String API_METHOD_USER_SEARCH = "user_search";
	public static final String API_METHOD_USER_NAME_SEARCH = "user_name_search";
	public static final String API_METHOD_USER_PASSWORD_RECOVER = "user_password_recover";
	public static final String API_METHOD_CHANNEL_SEARCH = "channel_search";
	public static final String API_METHOD_CHANNEL_CREATE = "channel_create";
	public static final String API_METHOD_CHANNEL_DELETE = "channel_delete";
	public static final String API_METHOD_CHANNEL_TITLE_UPDATE= "channel_title_update";
	public static final String API_METHOD_CHANNEL_VIDEO_ADD = "channel_video_add";
	public static final String API_METHOD_CHANNEL_VIDEO_DELETE= "channel_video_delete";
	public static final String API_METHOD_CHANNEL_VIDEO_ORDER_SET = "channel_video_order_set";
	public static final String API_METHOD_CATEGORY_SEARCH_TREE = "category_search_tree";
	public static final String API_METHOD_IMAGE_SEARCH = "image_search";
	public static final String API_METHOD_IMAGE_DELETE = "image_delete";
	public static final String API_METHOD_IMAGE_DETAILS = "image_details";
	public static final String API_METHOD_AUDIO_SEARCH = "audio_search";
	public static final String API_METHOD_DOCUMENT_SEARCH = "document_search";
	public static final String API_METHOD_LANGUAGE_RESOLVE= "language_resolve";
	public static final String API_METHOD_WALL_READ = "wall_read";
	public static final String API_METHOD_WALL_UNREAD_GET = "wall_unread_get";
	public static final String API_METHOD_WALL_SEARCH = "wall_search";
	public static final String API_METHOD_WALL_ENTRY_DETAILS = "wall_entry_details";
	public static final String API_METHOD_WALL_COMMENT_POST = "wall_comment_post";
	public static final String API_METHOD_WALL_ENTRY_LIKE = "wall_entry_like";
	public static final String API_METHOD_WALL_ENTRY_REPLY = "wall_entry_reply";
	public static final String API_METHOD_WALL_ENTRY_DELETE = "wall_entry_delete";
	public static final String API_METHOD_COMPETITION_SEARCH = "competition_search";
	public static final String API_METHOD_COMPETITION_INTERVAL_SEARCH = "competition_interval_search";
	public static final String API_METHOD_COMPETITION_INFO_GET = "competition_info_get";
	public static final String API_METHOD_COMPETITION_VOTING_AVAILABLE_GET = "competition_voting_available_get";
	public static final String API_METHOD_LOCATION_DETAILS = "location_details";
	public static final String API_METHOD_PROJECT_SEARCH = "project_search";
	public static final String API_METHOD_MESSAGE_DELETE = "message_delete";
	public static final String API_METHOD_MESSAGE_SEND = "message_send";
	public static final String API_METHOD_MESSAGE_THREAD_DELETE = "message_thread_delete";
	public static final String API_METHOD_MESSAGE_THREAD_DETAIL_GET = "message_thread_detail_get";
	public static final String API_METHOD_MESSAGE_THREAD_LIST = "message_thread_list";
	public static final String API_METHOD_MESSAGE_THREAD_POST_SEARCH = "message_thread_post_search";
	public static final String API_METHOD_MESSAGE_THREAD_READ = "message_thread_read";
	public static final String API_METHOD_MESSAGE_UNREAD_GET = "message_unread_get";
	public static final String API_METHOD_EMAIL_TEMPLATE_GROUP_SEARCH = "email_template_group_search";
	public static final String API_METHOD_EMAIL_SUBSCRIPTION_GET = "email_subscription_get";
	public static final String API_METHOD_EMAIL_SUBSCRIPTION_SET = "email_subscription_set";
	public static final String API_METHOD_EMAIL_UNSUBSCRIBE_ALL_GET = "email_unsubscribe_all_get";
	public static final String API_METHOD_EMAIL_UNSUBSCRIBE_ALL_SET = "email_unsubscribe_all_set";
	public static final String API_METHOD_EMAIL_VALIDATE_SEND = "email_validate_send";
	public static final String API_METHOD_EMAIL_VALIDATE = "email_validate";
	// ----- Methods for UploadProxy Server ----- //
	public static final String API_METHOD_UPLOAD = "upload";
	public static final String API_METHOD_UPLOAD_PROGRESS= "upload_progress";
	public static final String API_METHOD_UPLOAD_PREVIEW = "upload_preview";
	public static final String API_METHOD_UPLOAD_SAVE_VIDEO = "save_video";
	public static final String API_METHOD_UPLOAD_SAVE_DOCUMENT = "save_document";
	public static final String API_METHOD_UPLOAD_SAVE_IMAGE = "save_image";
	public static final String API_METHOD_UPLOAD_SAVE_AUDIO = "save_audio";
	// ----- Methods for BatchProxy Server ----- //
	public static final String API_METHOD_BATCH_REQUEST = "";
	// ----- Methods for i18nProxy Server ----- //
	public static final String API_METHOD_i18n_KEY_MAPPING = "";
	// ----- Methods for AuthenticationProxy Server ----- //
	public static final String API_METHOD_AUTH_FACEBOOK = "facebook";
	public static final String API_METHOD_AUTH_TWITTER = "twitter";
	public static final String API_METHOD_AUTH_GPLUS = "gplus";
	
	// ----- Attributes ----- //
	public static final String CRITERIA_FILTER = "criteria_filter";
	public static final String CRITERIA_SORT= "criteria_sort";
	public static final String OUTPUT = "output";
	
	public static final String RESULT_LIMIT= "result_limit";
	public static final String RESULT_OFFSET= "result_offset";
	
	private String method;
	private int sequence;
	private JSONObject callArguments;
	
	// ----- Constructors ----- //
	public ApiRequest(String method) 
	{
		super();
		this.method = method;
		this.sequence = 0;
		this.callArguments = new JSONObject();
	}

	// ----- Getters & Setters ----- //
	public String getMethod() 
	{
		return method;
	}
	
	public void setMethod(String method)
	{
		this.method = method;
	}
	
	public int getSequence()
	{
		return sequence;
	}
	
	public void setSequence(int sequence)
	{
		this.sequence = sequence;
	}
	
	public JSONObject getCallArguments()
	{
		return callArguments;
	}
	
	public void setCallArguments(JSONObject callArguments)
	{
		this.callArguments = callArguments;
	}
	
	// ----- Public Methods ----- //
	public Object getCallArgumentValue(String key) 
	{
		Object object = null;
		try {
			object = this.callArguments.get(key);
		} catch (JSONException e) {
			return null;
		}
		return object;
	}
	
	public boolean putCallArgumentValue(String key, Object value) 
	{
		if (value != null) 
		{
			try 
			{
				this.callArguments.put(key, value);
			} 
			catch (JSONException e)
			{
				return false;
			}
		}
		else
		{
			this.callArguments.remove(key);
		}
		return true;
	}
	
	public void removeCallArgumentValue(String key) 
	{
		this.putCallArgumentValue(key, null);
	}
	
	public boolean putCriteriaFilterValue(String key, Object value) 
	{
		return this.putJSONObjectValue(CRITERIA_FILTER, key, value);
	}
	
	public boolean removeCriteriaFilterValue(String key) 
	{
		return this.putJSONObjectValue(CRITERIA_FILTER, key, null);
	}
	
	public Object getCriteriaFilterValue(String key) 
	{
		return this.getJSONObjectValue(CRITERIA_FILTER, key);
	}
	
	public boolean putCriteriaSortValue(String key, Object value) 
	{
		return this.putJSONObjectValue(CRITERIA_SORT, key, value);

	}
	
	public void removeCriteriaSortValue(String key) 
	{
		this.putJSONObjectValue(CRITERIA_SORT, key, null);
	}
	
	public Object getCriteriaSortValue(String key) 
	{
		return this.getJSONObjectValue(CRITERIA_SORT, key);
	}
	
	public void putOutputValue(String key, Object value) 
	{
		this.putJSONObjectValue("output", key, value);
	}
	
	public void removeOutputValue(String key) 
	{
		this.putJSONObjectValue(OUTPUT, key, null);
	}
	
	public Object getOutputValue(String key) 
	{
		return this.getJSONObjectValue(OUTPUT, key);
	}
	
	public boolean containsResultLimit() 
	{
		Integer resultLimit = (Integer)this.getCallArgumentValue(RESULT_LIMIT);
		return resultLimit != null;
	}
	
	public int getResultLimit()
	{
		Integer resultLimit = (Integer)this.getCallArgumentValue(RESULT_LIMIT);		
		if (resultLimit != null)
			return resultLimit.intValue();
		return 0;
	}
	
	public boolean setResultLimit(int resultLimit) 
	{
		return this.putCallArgumentValue(RESULT_LIMIT, Integer.valueOf(resultLimit));
	}
	
	public boolean containsResultOffset() 
	{
		Integer resultLimit = (Integer)this.getCallArgumentValue(RESULT_OFFSET);
		return resultLimit != null;
	}
	
	public int getResultOffset()  
	{
		Integer resultOffset = (Integer)this.getCallArgumentValue(RESULT_OFFSET);
		if (resultOffset != null)
			return resultOffset.intValue();
		return 0;
	}
	
	public boolean setResultOffset(int resultOffset)  
	{
		return this.putCallArgumentValue(RESULT_OFFSET, Integer.valueOf(resultOffset));
	}
	
	// ----- Private Methods ----- //
	private boolean putJSONObjectValue(String callArgumentKey, String key, Object value) 
	{
		try
		{
			if (value != null) 
			{
				JSONObject criteriaFilter = null;

				if (this.callArguments.has(callArgumentKey)) 
				{
					criteriaFilter = this.callArguments.getJSONObject(callArgumentKey);
				} 
				else
				{
					criteriaFilter = new JSONObject();
					this.callArguments.put(callArgumentKey, criteriaFilter);
				}
				criteriaFilter.put(key, value);
			} 
			else
			{
				JSONObject criteriaFilter = this.callArguments.getJSONObject(callArgumentKey);
				criteriaFilter.remove(key);
				if (criteriaFilter.length() == 0)
					this.callArguments.remove(callArgumentKey);
			}

		} 
		catch (JSONException e) 
		{
			return false;
		}
		return true;
	}
	
	private Object getJSONObjectValue(String callArgumentKey, String key) 
	{
		if (this.callArguments.has(callArgumentKey)) 
		{
			JSONObject criteriaFilter;
			Object object = null;
			try 
			{
				criteriaFilter = this.callArguments.getJSONObject(callArgumentKey);
				object = criteriaFilter.get(key);
			} 
			catch (JSONException e) 
			{
				return null;
			}
			return object;
		}
		return null;
	}

	// ----- Default Methods ----- //
	@Override
	public String toString() 
	{
		return "ApiRequest [method=" + method + ", sequence=" + sequence
				+ ", callArguments=" + callArguments + "]";
	}

	@Override
	public int hashCode() 
	{		
		JSONObject criteriaFilter = (JSONObject)this.getCallArgumentValue("criteria_filter");
		JSONObject criteriaSort = (JSONObject)this.getCallArgumentValue("criteria_filter");
		JSONObject output = (JSONObject)this.getCallArgumentValue("criteria_filter");
		
		final int prime = 31;
		int result = 1;
		result = prime * result + ((method == null) ? 0 : method.hashCode());
		result = prime * result + ((criteriaFilter == null) ? 0 : criteriaFilter.hashCode());
		result = prime * result + ((criteriaSort == null) ? 0 : criteriaFilter.hashCode());
		result = prime * result + ((output == null) ? 0 : output.hashCode());
		
		return result;
	}

	@Override
	public boolean equals(Object obj) 
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		
		ApiRequest other = (ApiRequest) obj;
		
		if (method == null) 
		{
			if (other.method != null)
				return false;
		} 
		else if (!method.equals(other.method))
		{
			return false;
		}
		
		if (callArguments == null) 
		{
			if (other.callArguments != null)
				return false;
		} 
		else
		{
			JSONObject criteriaFilter = (JSONObject)this.getCallArgumentValue("criteria_filter");
			JSONObject criteriaSort = (JSONObject)this.getCallArgumentValue("criteria_filter");
			JSONObject output = (JSONObject)this.getCallArgumentValue("criteria_filter");
			
			JSONObject otherCriteriaFilter = (JSONObject)other.getCallArgumentValue("criteria_filter");
			JSONObject otherCriteriaSort = (JSONObject)other.getCallArgumentValue("criteria_filter");
			JSONObject otherOutput = (JSONObject)other.getCallArgumentValue("criteria_filter");
			
			if (!criteriaFilter.equals(otherCriteriaFilter))
				 return false;
			if (!criteriaSort.equals(otherCriteriaSort))
				 return false;
			if (!output.equals(otherOutput))
				 return false;
		}
		
		return true;
	}
}
