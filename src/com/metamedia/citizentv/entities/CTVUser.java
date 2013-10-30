package com.metamedia.citizentv.entities;

import org.json.JSONArray;
import org.json.JSONObject;

import com.metamedia.citizentv.entities.helpers.*;

public class CTVUser extends ModelEntity 
{
	private static final long serialVersionUID = 1L;
	
	public String username;
	public String aboutMe;
	public String cityKeyFrom;
	public String cityKeyLocation;
	public String email;
	public String experienceDescription;
	public String genderKey;
	public String interests;
	public String judgePresentationKey;
	public String name;
	public String occupation;
	public String phonePrivate;
	public String phonePublic;
	public String talentDescription;
	public String gplusId;
	public String biographyVideoKey;
	public String wallKey;
	public String websiteUrl;
	public String locationKeyFrom;
	public String locationKey;
	public String origin;
	
	public JSONArray experiences; // <-- Raw parsing!
	public JSONArray languages; // <-- Raw parsing!
	public JSONArray contentAdminCategoryList; // <-- Raw parsing!
	
	public CTVResource avatar;
	public CTVResource moodCover;
	public CTVResource biographyVideoPreview;
	
	public CropRect avatarCrop;
	public CropRect moodCoverCrop;
	public CropRect biographyVideoPreviewCrop;
	
	public boolean isJudge;
	public boolean ageVerified;
	public boolean isContentAdmin;
	public boolean isCashWinner;
	
	public int creditVideoCount;
	public int mediaCount;
	public int profileView;
	public int userFollowCount;
	public int userFollowedCount;
	public int videoCount;
	public int videoCountTotal;
	public int videoViewsTotalCount;
	public int imageCount;
	public int documentCount;
	public int audioCount;
	public int playlistCount;
	public int projectCount;
	public int creditCount;
	public int twitterId;
	public int facebookId;
	
	public long timeBirth;
	public long timeCreate;
	public long timeLastLogin;
	public long timeLastUpdate;
	
	// ---- Logged In Based Values ---- //
	public long timeFollow;
	public boolean emailVerified;
	
	// ---------------------------------------------------------------------------------------------------------
	// Creating instances and initializing
	// ---------------------------------------------------------------------------------------------------------
	public CTVUser(String key) 
	{
		super(key);
	}
	
	// ---------------------------------------------------------------------------------------------------------
	// BusinessObject interface
	// ---------------------------------------------------------------------------------------------------------
	@Override
	public void parse(JSONObject jsonObject) 
	{
		super.parse(jsonObject);
		
		this.username = jsonObject.optString("username", username);
		this.aboutMe = jsonObject.optString("about_me", aboutMe);
		this.cityKeyFrom = jsonObject.optString("city_key_from", cityKeyFrom);
		this.cityKeyLocation = jsonObject.optString("city_key_location", cityKeyLocation);
		this.email = jsonObject.optString("email", email);
		this.gplusId = jsonObject.optString("gplus_id", gplusId);
		this.genderKey = jsonObject.optString("gender_key", genderKey);
		this.judgePresentationKey = jsonObject.optString("judge_presentation_key", judgePresentationKey);
		this.experienceDescription = jsonObject.optString("experience_description", judgePresentationKey);
		this.interests = jsonObject.optString("interests", interests);
		this.name = jsonObject.optString("name", name);
		this.occupation = jsonObject.optString("occupation", occupation);
		this.phonePrivate = jsonObject.optString("phone_private", phonePrivate);
		this.phonePublic = jsonObject.optString("phone_public", phonePublic);
		this.talentDescription = jsonObject.optString("talent_description", talentDescription);
		this.origin = jsonObject.optString("origin", origin);
		this.websiteUrl = jsonObject.optString("website_url", websiteUrl);
		this.locationKeyFrom = jsonObject.optString("location_key_from", locationKeyFrom);
		this.locationKey = jsonObject.optString("location_key", locationKey);
		
		this.facebookId = jsonObject.optInt("facebook_id", facebookId);
		this.imageCount = jsonObject.optInt("image_count", imageCount);
		this.mediaCount = jsonObject.optInt("media_count", mediaCount);
		this.profileView = jsonObject.optInt("profile_view", profileView);
		this.twitterId = jsonObject.optInt("twitter_id", twitterId);
		this.userFollowCount = jsonObject.optInt("user_follow_count", userFollowCount);
		this.userFollowedCount = jsonObject.optInt("user_followed_count", userFollowedCount);
		this.videoCount = jsonObject.optInt("video_count", videoCount);
		this.videoCountTotal = jsonObject.optInt("video_count_total", videoCountTotal);
		this.videoViewsTotalCount = jsonObject.optInt("video_views_total_count", videoViewsTotalCount);
		this.audioCount = jsonObject.optInt("audio_count", audioCount);
		this.documentCount = jsonObject.optInt("document_count", documentCount);
		this.playlistCount = jsonObject.optInt("channel_count", playlistCount);
		this.projectCount = jsonObject.optInt("project_count", projectCount);
		this.creditCount = jsonObject.optInt("credit_count", creditCount);
		this.creditVideoCount = jsonObject.optInt("credit_video_count", creditVideoCount);
		
		this.isJudge = jsonObject.optBoolean("is_judge", isJudge);
		this.ageVerified = jsonObject.optBoolean("age_verified", ageVerified);
		this.isContentAdmin = jsonObject.optBoolean("is_content_admin", isContentAdmin);
		this.emailVerified = jsonObject.optBoolean("email_verified", emailVerified);
		
		this.timeBirth = jsonObject.optLong("time_birth", timeBirth);
		this.timeCreate = jsonObject.optLong("time_create", timeCreate);
		this.timeLastLogin = jsonObject.optLong("time_last_login", timeLastLogin);
		this.timeLastUpdate = jsonObject.optLong("time_last_update", timeLastUpdate);
		this.timeFollow = jsonObject.optLong("time_follow", timeFollow);
		
		if (jsonObject.has("avatar")) 
			this.avatar = new CTVResource(jsonObject.optJSONObject("avatar"));
		if (jsonObject.has("mood_cover"))
			this.moodCover = new CTVResource(jsonObject.optJSONObject("mood_cover"));
		if (jsonObject.has("user_bio_video_preview"))
			this.biographyVideoPreview = new CTVResource(jsonObject.optJSONObject("user_bio_video_preview"));
				
		if (jsonObject.has("avatar_crop"))
			this.avatarCrop = new CropRect(jsonObject.optJSONObject("avatar_crop"));
		if (jsonObject.has("mood_cover_crop"))
			this.moodCoverCrop = new CropRect(jsonObject.optJSONObject("mood_cover_crop"));
		if (jsonObject.has("user_bio_video_preview_crop"))
			this.biographyVideoPreviewCrop = new CropRect(jsonObject.optJSONObject("user_bio_video_preview_crop"));
		
		if (jsonObject.has("wall"))
		{
			JSONObject wallObject = jsonObject.optJSONObject("wall");
			
			String key = CTVWall.parseKey(wallObject);
			CTVWall wall = (CTVWall)this.getContext().getObjectForKey(key);
			if (wall == null)
			{
				wall = new CTVWall(key);
				this.getContext().insertObject(wall);
			}
			wall.parse(wallObject);
			this.wallKey = key;
		}
		
		if (jsonObject.has("user_bio_video"))
		{
			JSONObject videoObject = jsonObject.optJSONObject("user_bio_video");
			
			String key = CTVVideo.parseKey(videoObject);
			CTVVideo video = (CTVVideo)this.getContext().getObjectForKey(key);
			if (video == null)
			{
				video = new CTVVideo(key);
				this.getContext().insertObject(video);
			}
			video.parse(videoObject);
			this.biographyVideoKey = key;
		}
		
		if (jsonObject.has("experience"))
			this.experiences = jsonObject.optJSONArray("experience");
		if (jsonObject.has("language"))
			this.languages = jsonObject.optJSONArray("language");
		if (jsonObject.has("content_admin_category_list"))
			this.contentAdminCategoryList = jsonObject.optJSONArray("content_admin_category_list");
	}
}
