package com.metamedia.citizentv.asynctasks;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import com.metamedia.citizentv.*;
import com.metamedia.citizentv.entities.*;
import com.metamedia.citizentv.lists.FetchList;
import com.metamedia.citizentv.lists.FetchListPersistenceManager;
import com.metamedia.citizentv.api.*;
import com.metamedia.citizentv.asynctasks.*;
import com.metamedia.managers.ModelManager;
import com.metamedia.managers.TranslationManager;
import com.metamedia.persistentmodel.BaseObject;
import com.metamedia.persistentmodel.ObjectContext;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

public class InitVarsFromServerTask extends AsyncTask<String, Void, String>
{
    Boolean errorOccurred = false;
    public InitVarsFromServerTask()
    {
        //this.imageView = imageViewPar;
    }
    
    protected void onPreExecute ()
    {
    	AppStateVariables.InitializingGlobalVarsFromServer = true;
    	Log.d("CTV", "Begining loading of global vars into memory...");
    }
    protected String doInBackground(String... urls)
    {
    	loadTranslationManager();
    	loadFrontendServerList();
    	//loadCategories();
    	loadVideoQualityList();
    	
    	return "";
    }
    protected void onPostExecute(String stringPar)
    {
    	if(!errorOccurred)
		{
    		
		}
    		
    	AppStateVariables.InitializingGlobalVarsFromServer = false;
    	Log.d("CTV", "Finished loading global vars.");
    }
    
    protected void loadVideoQualityList()
    {
    	MainActivity.videoQualityMap.clear();
    	
    	ApiRequest request = new ApiRequest(ApiRequest.API_METHOD_VIDEO_QUALITY_SEARCH);
		request.putCriteriaSortValue("width", "desc");
		request.setResultOffset(0);
		request.setResultLimit(99);
		request.putCallArgumentValue(ApiRequest.CRITERIA_FILTER, new JSONObject()); //we need to send "{}" for 

		FetchListPersistenceManager fetchListManager = ModelManager.getInstance().getFetchListPersistenceManager();
		ObjectContext objectContext = ModelManager.getInstance().getDefaultObjectContext();
		
		int expireTimeOffest = 600000;
		int chunkSize = 20;
		
		FetchList fetchList = fetchListManager.getFetchList(request, objectContext, expireTimeOffest, chunkSize);

		if (fetchList.isExpired())
		{
			Log.d("CTV - loadVideoQualityList ","FETCH LIST NEEDS A REFRESH");
			fetchList.refreshFetchList();
			
			objectContext.save();
			fetchListManager.saveFetchList(fetchList);
		}
		else
		{
			Log.d("CTV - loadVideoQualityList ","FETCH LIST NOT EXPIRED");
		}
		
		for (int index=0; index < fetchList.size(); ++index)
		{
			CTVVideoQuality videoQuality = (CTVVideoQuality)fetchList.get(index);
			videoQuality.setLastUpdate(new Date());
			//Log.d("CTV - loadVideoQualityList ", "Fetch List Video Quality [" + index + "]: " + videoQuality.toString());
			MainActivity.videoQualityMap.put(videoQuality.getKey(), videoQuality);
			//videoList.add(video);
		}
			
    }
    
    protected void loadFrontendServerList()
    {
    	MainActivity.frontendServerHashMap.clear();
    	
    	final ObjectContext objectContext = ModelManager.getInstance().getDefaultObjectContext();
    	BaseObject[] boList = objectContext.getObjectsOfType(CTVFrontendServer.class.getSimpleName());
    	CTVFrontendServer[] fesListFromPersistence = new CTVFrontendServer[boList.length];
    	boolean serversExpired = false;
    	for(int i=0;i<boList.length;i++)
    	{
    		//Log.d("CTV - loadFrontendServerList", "Objects found in persistence.");
    		
    		fesListFromPersistence[i] = (CTVFrontendServer) boList[i];
    		//Log.d("CTV - loadFrontendServerList lastUpdateTime:", boList[i].getLastUpdate().toString());
    		//Log.d("CTV - loadFrontendServerList key from persistence:", boList[i].getKey());
    		if(fesListFromPersistence[i].isExpired())
    		{
    			serversExpired = true;
    			//Log.d("CTV - loadFrontendServerList", "Front end server expired.");
    		}
    		else
    		{
    			MainActivity.frontendServerHashMap.put(fesListFromPersistence[i].getKey(), fesListFromPersistence[i]);
    		}
    	}
		
    	if(serversExpired || boList.length < 1)
    	{
    		MainActivity.frontendServerHashMap.clear(); //In the unlikely event that this list was only partially populated from persistence, we need to clear it.
    		
    		Log.d("CTV - loadFrontendServerList", "Fetching frontend servers from server. None or expired in persistence.");
    		
			// Getting the list of storage servers
			ApiRequest serverListRequest = new ApiRequest(ApiRequest.API_METHOD_NULL);
			serverListRequest.putCallArgumentValue("object", new JSONArray().put("frontend_server"));
			
			ApiManager apiManager = ApiManager.getInstance();
			apiManager.performApiRequest(serverListRequest, ApiManager.PROXY_TYPE.CONFIG, new ApiRequestCompletionDelegate ()
			{
				public void onCompletion(ApiResponse apiResponse)
				{
					try
					{
						//Log.d("CTV", "ServerList: " + apiResponse.toString());
						//tempString = apiResponse.getPayload().toString();
						
						JSONObject payload = apiResponse.getPayload();
						
						//Log.d("CTV - Frontend server payload", payload.toString());
						
						JSONObject jsoFrontendServerList = payload.getJSONObject("frontend_server");
						Iterator<?> iterator = jsoFrontendServerList.keys();
						while(iterator.hasNext())
						{
							String frontendServerKey = iterator.next().toString();
	
							CTVFrontendServer fes = (CTVFrontendServer)objectContext.getObjectForKey(frontendServerKey);
	
							if (fes == null)
							{
								fes = new CTVFrontendServer(frontendServerKey);
								objectContext.insertObject(fes);
							}
							
							JSONObject jsoServerAttributes = jsoFrontendServerList.getJSONObject(frontendServerKey);
							
							fes.parse(jsoServerAttributes);
							fes.setLastUpdate(new Date());
			
							MainActivity.frontendServerHashMap.put(frontendServerKey, fes);
						}
						objectContext.save();
					}
					catch (Exception e)
					{
						e.printStackTrace();
						errorOccurred = true;
					}
				}
			});
    	}
		
		//Log the contents of the
//		for(CTVFrontendServer fes : MainActivity.frontendServerHashMap.values())
//		{	
//			Log.d("FrontendServer object: ", fes.toString());
//		}
//		for(String s : MainActivity.frontendServerHashMap.keySet())
//		{
//			Log.d("server key:",s);
//		}
    }
    
    protected void loadTranslationManager() //Here we add maps of translations to the larger map of maps of translations. This map of maps is contained in a serializable object which is stored in a file.
    {
    	Log.d("CTV","Loading translation manager...");
    	
    	//File file = new File(MainActivity.baseContext.getFilesDir(), "translationManager");
    	
    	final String filename = "serializedTranslationManager";
    	File serializedTranslationManager = MainActivity.baseContext.getFileStreamPath(filename);
    	boolean fileExists = false;
    	boolean managerIsExpired = false;
        if(serializedTranslationManager.exists())
        {
        	Log.d("CTV - loadTranslationManager", "File found.");
        	fileExists = true;
        	try
        	{
	        	FileInputStream fis = MainActivity.baseContext.openFileInput(filename);
	        	ObjectInputStream is = new ObjectInputStream(fis);
	        	TranslationManager deserializedTranslationManager = (TranslationManager) is.readObject();
	        	is.close();
	        	
	        	Date now = new Date();
	        	long nowMillis = now.getTime();
	        	long lastUpdateMillies = deserializedTranslationManager.getLastUpdateDate().getTime();
	        	if((nowMillis - lastUpdateMillies) > TranslationManager.MILLIES_BEFORE_EXPIRATION)
	        		managerIsExpired = true;
	        	else
	        		MainActivity.translationManager = deserializedTranslationManager;
        	}
        	catch(Exception ex)
        	{
        		ex.printStackTrace();
        	}
        }
        else
        {
        	Log.d("CTV - loadTranslationManager", "File NOT found.");
        	serializedTranslationManager = new File(MainActivity.baseContext.getFilesDir(),filename);
        }
        
        if(!fileExists || managerIsExpired)
        {
        	//Fetch translations from the server.
        	Log.d("CTV - loadTranslationManager", "Fetching translations from server. Either the file did not exist or the translation manager is expired.");
        	
        	MainActivity.translationManager = new TranslationManager();
        	
        	JSONArray i18nCategories = new JSONArray();
        	i18nCategories.put("categories");
        	ApiRequest request = new ApiRequest(ApiRequest.API_METHOD_NULL);
        	request.putCallArgumentValue("object", i18nCategories);
        	
        	ApiManager.getInstance().performApiRequest(request, ApiManager.PROXY_TYPE.i18n, new ApiRequestCompletionDelegate()
        	{
        		public void onCompletion(ApiResponse apiResponse)
        		{
        			try
    				{
    					JSONObject payload = apiResponse.getPayload();
    					JSONObject categoryList = payload.getJSONObject("categories");
    					Map<String,String> categoryTranslationMap = new ConcurrentHashMap<String,String>();
    					Iterator<?> iterator = categoryList.keys();
    					while(iterator.hasNext())
    					{
    						String categoryKey = iterator.next().toString();
    						String categoryName = categoryList.optString(categoryKey);
    						categoryTranslationMap.put(categoryKey, categoryName);
    					}
    					MainActivity.translationManager.addTranslationMap(TranslationManager.MAP_NAME_CATEGORIES, categoryTranslationMap);
    					MainActivity.translationManager.setLastUpdateDate(new Date());
    					
    					FileOutputStream fileOutputStream = MainActivity.baseContext.openFileOutput(filename, Context.MODE_PRIVATE);
    					ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
    					objectOutputStream.writeObject(MainActivity.translationManager);
    					objectOutputStream.close();
    					
    				}
    				catch (Exception e)
    				{
    					e.printStackTrace();
    					errorOccurred = true;
    				}
        		}
        	});
        	
        }
    	
//    	FileInputStream inputStream;
//    	FileOutputStream outputStream;
//    	try 
//    	{
//    	  outputStream = MainActivity.baseContext.openFileOutput("translationManager", MainActivity.baseContext.MODE_PRIVATE);
//    	  //outputStream.write(string.getBytes());
//    	  outputStream.close();
//    	} 
//    	catch (Exception e) 
//    	{
//    	  e.printStackTrace();
//    	}
        
        Log.d("CTV - loadTranslationManager - translationManager:", MainActivity.translationManager.toString());
    	
    }
    
//    protected void loadCategories() //Here we match up the category keys with their names in the current language. We populate a mapping between key and name.
//    {
//    	Log.d("CTV","Loading categories...");
//    	
//    	MainActivity.categoryHashMap.clear();
//    	
//    	final ObjectContext objectContext = ModelManager.getInstance().getDefaultObjectContext();
//    	BaseObject[] boList = objectContext.getObjectsOfType(CTVCategory.class.getSimpleName());
//    	CTVCategory[] catListFromPersistence = new CTVCategory[boList.length];
//    	boolean categoriesExpired = false;
//    	for(int i=0;i<boList.length;i++)
//    	{
//    		//Log.d("CTV - loadCategories", "Objects found in persistence.");
//    		
//    		catListFromPersistence[i] = (CTVCategory) boList[i];
//    		//Log.d("CTV - loadCategories lastUpdateTime:", boList[i].getLastUpdate().toString());
//    		//Log.d("CTV - loadCategories key from persistence:", boList[i].getKey());
//    		if(catListFromPersistence[i].isExpired())
//    		{
//    			categoriesExpired = true;
//    			//Log.d("CTV - loadCategories", "category expired.");
//    		}
//    		else
//    		{
//    			MainActivity.categoryHashMap.put(catListFromPersistence[i].getKey(), catListFromPersistence[i].name);
//    		}
//    	}
//    	
//    	
//    	if(categoriesExpired || boList.length < 1)
//    	{
//    		Log.d("CTV - loadCategories", "Fetching categories from server. None or expired in persistence.");
//    		
//    		MainActivity.categoryHashMap.clear(); //In the unlikely event that this list was only partially populated from persistence, we need to clear it.
//    		
//    		JSONArray i18nCategories = new JSONArray();
//        	i18nCategories.put("categories");
// 
//        	ApiRequest request = new ApiRequest(ApiRequest.API_METHOD_NULL);
//        	request.putCallArgumentValue("object", i18nCategories);
//        	
//        	ApiManager.getInstance().performApiRequest(request, ApiManager.PROXY_TYPE.i18n, new ApiRequestCompletionDelegate()
//        	{
//        		public void onCompletion(ApiResponse apiResponse)
//        		{
//        			//Log.d("CTV", "Category Response: " + apiResponse.toString());
//        			
//        			try
//    				{
//    					//Log.d("CTV", "ServerList: " + apiResponse.toString());
//    					//tempString = apiResponse.getPayload().toString();
//    					
//    					JSONObject payload = apiResponse.getPayload();
//    					JSONObject categoryList = payload.getJSONObject("categories");
//    					Iterator<?> iterator = categoryList.keys();
//    					while(iterator.hasNext())
//    					{
//    						String categoryKey = iterator.next().toString();
//    						String categoryName = categoryList.optString(categoryKey);
//    						MainActivity.categoryHashMap.put(categoryKey, categoryName);
//    						
//    						//Add to persistent store.
//    						CTVCategory ctvCategory = (CTVCategory)objectContext.getObjectForKey(categoryKey);
//    						
//    						if (ctvCategory == null)
//    						{
//    							ctvCategory = new CTVCategory(categoryKey);
//    							objectContext.insertObject(ctvCategory);    							
//    						}
//    						
//    						ctvCategory.setLastUpdate(new Date());
//    						ctvCategory.setHasChanges(true);
//    						
//    						
//    						//Log.d("category added to global list:", categoryKey + ", " + categoryName);
//    					}
//    					objectContext.save();
//    				}
//    				catch (Exception e)
//    				{
//    					e.printStackTrace();
//    					errorOccurred = true;
//    				}
//        		}
//        	});
//    	}
//    }
}