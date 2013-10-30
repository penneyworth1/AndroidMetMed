package com.metamedia.citizentv.lists;

import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;

import android.util.Log;

import com.metamedia.citizentv.api.ApiRequest;
import com.metamedia.citizentv.entities.ModelEntity;
import com.metamedia.managers.ModelManager;
import com.metamedia.persistentmodel.ObjectContext;
import com.metamedia.persistentmodel.PersistentObject;
import com.metamedia.persistentmodel.iPersistentStore;
import com.metamedia.tools.Security;

public class FetchListPersistenceManager 
{

	private iPersistentStore persistentStore;

	// ----------------------------------------------------------------------------------------------------
	// Constructor 
	// ----------------------------------------------------------------------------------------------------

	public FetchListPersistenceManager(iPersistentStore persistentStore)
	{
		super();
		this.persistentStore = persistentStore;
	}

	// ----------------------------------------------------------------------------------------------------
	// Managing persistent fetch lists
	// ----------------------------------------------------------------------------------------------------

	public FetchList getFetchList(ApiRequest request, ObjectContext objectContext, int expireTimeOffset, int chunkSize)
	{
		String key = getRequestIdentifier(request);
		PersistentObject po = persistentStore.getPersistentObject(key);

		FetchList fetchList = null;
		
		if (po != null)
		{
			fetchList = FetchList.deserialize(po.getData());
			fetchList.setLastUpdate(new Date(po.getLastUpdate()));
			fetchList.setApiRequest(request);
			fetchList.setExpiringTimeOffset(expireTimeOffset);
			fetchList.setChunkSize(chunkSize);

			// Check consistency of its members (do they exist in the given object context?)
			fetchList.setObjectContext(objectContext);

			// Check if all objects can be awakened from persistence, otherwise the fetch list must be destroyed.
			for (int index=0; index < fetchList.size(); ++index)
			{
				ModelEntity me = fetchList.get(index);

				if (me == null)
				{
					deleteFetchList(fetchList);
					persistentStore.save();
					fetchList = null;
					break;
				}
			}
		}
		
		if (fetchList == null)
		{
			Log.d("CTV - FetchListPersistenceManager","FETCH LIST NOT FOUND IN PERSISTENCE!");
			fetchList = new FetchList(request, objectContext);
			fetchList.setExpiringTimeOffset(expireTimeOffset);
			fetchList.setChunkSize(chunkSize);
		}
		else
			Log.d("CTV - FetchListPersistenceManager","FETCH LIST LOADED FROM PERSISTENCE!");


		return fetchList;
	}

	//	public FetchList getFetchList(ApiRequest request)
	//	{
	//		String key = getRequestIdentifier(request);
	//		PersistentObject po = persistentStore.getPersistentObject(key);
	//		
	//		if (po == null)
	//			return null;
	//		
	//		FetchList fetchList = FetchList.deserialize(po.getData());
	//		return fetchList;
	//	}

	public boolean saveFetchList(FetchList fetchList)
	{
		String key = getRequestIdentifier(fetchList.getApiRequest());
		PersistentObject po = persistentStore.getPersistentObject(key);

		if (po == null)
			po = persistentStore.createPersistentObject(key);

		po.setData(fetchList.serialize());
		po.setType("FetchList");
		po.setLastUpdate(fetchList.getLastUpdate().getTime());

		return persistentStore.save();
	}

	public boolean deleteFetchList(FetchList fetchList)
	{
		String key = getRequestIdentifier(fetchList.getApiRequest());
		persistentStore.deletePersistentObject(key);

		return persistentStore.save();
	}

	//	public boolean deleteFetchLists(String type)
	//	{
	//		// TODO: Delete from persistence all fetch lists of the given type. 
	//		return false;
	//	}

	public void deleteUnusedFetchLists(long expiringTime)
	{
		persistentStore.deletePersistentObjects(null, expiringTime, iPersistentStore.DELETE_POLICY.ACCESS_DATE);;
	}

	public void deleteAll()
	{
		persistentStore.reset();
	}

	// ----------------------------------------------------------------------------------------------------
	// Managing persistent fetch lists
	// ----------------------------------------------------------------------------------------------------

	private String getRequestIdentifier(ApiRequest request)
	{
		JSONObject cf = (JSONObject)request.getCallArgumentValue("criteria_filter");
		JSONObject cs = (JSONObject)request.getCallArgumentValue("criteria_sort");
		
		JSONArray jsonArray = new JSONArray();
		jsonArray.put(cf);
		jsonArray.put(cs);
		
		String jsonString = jsonArray.toString();
		String identifier = Security.md5(jsonString);
		
		return identifier;
	}
}
