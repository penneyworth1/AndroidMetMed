package com.metamedia.citizentv.lists;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.metamedia.persistentmodel.BaseObject;
import com.metamedia.persistentmodel.ObjectContext;
import com.metamedia.citizentv.api.ApiBatchResponse;
import com.metamedia.citizentv.api.ApiError;
import com.metamedia.citizentv.api.ApiManager;
import com.metamedia.citizentv.api.ApiParser;
import com.metamedia.citizentv.api.ApiRequest;
import com.metamedia.citizentv.api.ApiResponse;
import com.metamedia.citizentv.api.iApiRequestCompletionDelegate;
import com.metamedia.citizentv.entities.ModelEntity;

public class FetchList implements iApiRequestCompletionDelegate, java.io.Serializable//, Iterable<ModelEntity>
{
	private static final long serialVersionUID = 1L;

	private ArrayList<String> contentList = new ArrayList<String>();
	private int listSizeOnServer = -1;

	// Attributes
	transient private ApiRequest request;
	transient private ObjectContext objectContext;
	private int chunkSize;
	private long expiringTimeOffset;
	transient private Date lastUpdate;
	transient public int loadCount; //This will be used to know where in this list to begin iterating when lazily loading more videos into a view.

	// ----------------------------------------------------------------------------------------------------
	// Constructors
	// ----------------------------------------------------------------------------------------------------

	public FetchList(ApiRequest request)
	{
		super();
		this.request = request;
	}

	public FetchList(ApiRequest request, ObjectContext objectContext)
	{
		super();
		this.request = request;
		this.objectContext = objectContext;
	}

	// ----------------------------------------------------------------------------------------------------
	// Configuring the fetch list
	// ----------------------------------------------------------------------------------------------------

	// Methods
	public boolean isExpired()
	{
		if (this.lastUpdate == null)
			return true;
		
		long now = new Date().getTime();//System.currentTimeMillis();
		long tempLastUpdate = this.lastUpdate.getTime();
		long diff = now - tempLastUpdate;
		long trustedOffset = this.expiringTimeOffset;
		
		Log.d("CTV - Last update", Long.toString(tempLastUpdate));
		Log.d("CTV - Diff", Long.toString(diff));
		Log.d("CTV - Trusted offset", Long.toString(trustedOffset));

		boolean trustedData = diff < trustedOffset;
		return !trustedData;
	}

	// Getters & Setters
	public ObjectContext getObjectContext()
	{
		return objectContext;
	}
	public void setObjectContext(ObjectContext objectContext)
	{
		this.objectContext = objectContext;
	}
	public ApiRequest getApiRequest()
	{
		return request;
	}
	protected void setApiRequest(ApiRequest request)
	{
		this.request = request;
	}
	public int getChunkSize()
	{
		return chunkSize;
	}
	public void setChunkSize(int chunkSize)
	{
		this.chunkSize = chunkSize;
	}
	public long getExpiringTimeOffset()
	{
		return expiringTimeOffset;
	}
	public void setExpiringTimeOffset(long expiringTimeOffset)
	{
		this.expiringTimeOffset = expiringTimeOffset;
	}

	// ----------------------------------------------------------------------------------------------------
	// Fetching Data
	// ----------------------------------------------------------------------------------------------------

	

	// Getters & Setters
	public Date getLastUpdate()
	{
		if(lastUpdate != null)
			return lastUpdate;
		else //We need to not return null here but rather a date that implies never having been updated.
		{
			Date nullDate = new Date();
			nullDate.setTime(0);
			return nullDate;
		}
	}
	public void setLastUpdate(Date lastUpdate) 
	{
		this.lastUpdate = lastUpdate;
	}

	// Methods
	public void reset()
	{
		contentList.clear();
		listSizeOnServer = -1;
		lastUpdate = null;
	}

	public void refreshFetchList()
	{
		reset();
		loadMore();
	}

	public void loadMore()
	{
		request.setResultLimit(chunkSize);
		request.setResultOffset(contentList.size());
		ApiManager apiManager = ApiManager.getInstance();
		apiManager.performApiRequest(request, this);
	}

	@Override
	public void onCompletion(ApiResponse apiResponse)
	{
		if (apiResponse.getError() != ApiError.OK)
		{
			// Return and do nothing!
			return;
		}
		
		//Log.d("API Response", apiResponse.toString());
		try
		{
			JSONObject payload = apiResponse.getPayload();
			
			listSizeOnServer = payload.getInt("num_result_total");
			
			ModelEntity[] objects = ApiParser.parseApiResponse(apiResponse, this.objectContext);
			
			ArrayList<String> keysToAdd = new ArrayList<String>();
			
			for (int i=0; i<objects.length; ++i)
			{
				ModelEntity me = objects[i];
				keysToAdd.add(me.getKey());
			}
			
//			JSONArray resultSet = payload.getJSONArray("result_set");
//
//			
//
//			int numArrayElementsInResultSet = resultSet.length();
//
//			
//			for(int i=0;i<numArrayElementsInResultSet;i++)
//			{
//				JSONObject jsonObject = resultSet.optJSONObject(i);
//
//				String key = ApiParser.parseKey(jsonObject, apiResponse.getMethod());
//
//				ModelEntity modelEntity = (ModelEntity)objectContext.getObjectForKey(key);
//
//				if (modelEntity == null)
//				{
//					modelEntity = ApiParser.createNewObject(key, apiResponse.getMethod());
//					objectContext.insertObject(modelEntity);
//				}
//
//				modelEntity.parse(jsonObject);
//
//				keysToAdd.add(key);
//			}

			contentList.addAll(keysToAdd);
			
			lastUpdate = new Date();
			
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void onCompletion(ApiBatchResponse apiBatchResponse, int connectionKey)
	{
		// Nothing happens here
	}

	// ----------------------------------------------------------------------------------------------------
	// Accessing & Modifying Contents
	// ----------------------------------------------------------------------------------------------------

	public int serverSize()
	{
		return listSizeOnServer;
	}

	public boolean hasMoreContent()
	{
		return contentList.size() < listSizeOnServer;
	}

	public int size()
	{
		return contentList.size();
	}

	public ModelEntity get(int index)
	{
		return (ModelEntity)objectContext.getObjectForKey(contentList.get(index));
	}

	public boolean contains(ModelEntity object)
	{
		return contentList.contains(object.getKey());
	}

	public int indexOf(ModelEntity object)
	{
		return contentList.indexOf(object.getKey());
	}

	public boolean add(ModelEntity object)
	{
		// TODO: Add the given object to the fetch list. Some attributes may be updated as serverSize or size itself.
		return false;
	}

	public boolean remove(int index)
	{
		// TODO: Remove the object at index. Some attributes may be updated as serverSize or size itself.
		return false;
	}

	public boolean remove(ModelEntity object)
	{
		// TODO: Remove the given object if contained. Some attributes may be updated as serverSize or size itself.
		return false;
	}

	public boolean exchange(int idx1, int idx2)
	{
		// TODO: Exchange objects at given indexes.
		return false;
	}

	public boolean exchange(ModelEntity obj1, ModelEntity obj2)
	{
		// TODO: Exchange the given objects if contained.
		return false;
	}



	// ----------------------------------------------------------------------------------------------------
	// Iterable Interface
	// ----------------------------------------------------------------------------------------------------

//	@Override
//	public Iterator<ModelEntity> iterator() 
//	{
//		FetchListIterator iterator = new FetchListIterator(this);
//		return iterator;
//	}

	// ---------------------------------------------------------------------------------------------------------
	// Serialization
	// ---------------------------------------------------------------------------------------------------------

	protected final byte[] serialize()
	{
		try
		{
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutput out = new ObjectOutputStream(bos);   
			out.writeObject(this);

			byte[] bytes = bos.toByteArray();

			out.close();
			bos.close();

			return bytes;
		} 
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	public static FetchList deserialize(byte[] bytes)
	{
		try 
		{
			ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
			ObjectInput in = null;

			in = new ObjectInputStream(bis);
			FetchList bo = (FetchList)in.readObject(); 

			bis.close();
			in.close();

			return bo;
		} 
		catch (Exception e) 
		{
			// Nothing to do, just catch the exception.
			e.printStackTrace();
		} 

		return null;
	}
}
