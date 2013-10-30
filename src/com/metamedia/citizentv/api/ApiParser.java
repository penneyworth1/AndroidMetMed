package com.metamedia.citizentv.api;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.metamedia.citizentv.entities.*;
import com.metamedia.persistentmodel.ObjectContext;


// ************************************************************************ //
// * 							      WARNING 						  	  * //
// *               For each new entity class or used API Method, 		  * //
// * 						this class must be updated!				 	  * //
// ************************************************************************ //

/**
 * This class add support to parse generic ApiResponse instances into ModelEntity business objects.
 */
public class ApiParser 
{
	// ---------------------------------------------------------------------------------------------------------
	// Public Methods
	// ---------------------------------------------------------------------------------------------------------

	/**
	 * Returns the object key (citizen.tv object identifier) contained in the given JSON object.
	 * @param jsonObject The business object represented as JSON 
	 * @param apiMethod The method with which the JSON object was received 
	 * @return The key of the object.
	 */
	static public String parseKey(JSONObject jsonObject, String apiMethod)
	{
		ENTITY_TYPE type = getEntityTypeFromApiMethod(apiMethod);
		return ApiParser.parseKey(jsonObject, type);
	}

	/**
	 * Creates a new instance for of the required type depending of the API method.
	 * @param key The key of the object. This value is the only attribute that will be set in the object.
	 * @param apiMethod The method will define the type of the object.
	 * @return An instance of a subclass of ModelEntity.
	 */
	static public ModelEntity createNewObject(String key, String apiMethod)
	{
		ENTITY_TYPE type = getEntityTypeFromApiMethod(apiMethod);
		return ApiParser.createNewModelEntityInstance(key, type);
	}

	/**
	 * Parse the content of an API response into business model objects.
	 * @param response
	 * @param objectContext
	 * @return An array with the parsed objects or null if error.
	 */
	static public ModelEntity[] parseApiResponse(ApiResponse response, ObjectContext objectContext)
	{
		if (response.getError() != ApiError.OK)
			return null;

		RESPONSE_TYPE type = ApiParser.getResponseTypeFromApiMethod(response.getMethod());

		ModelEntity[] objects = null;

		switch (type)
		{				
		case LIST:
			objects = getObjectsFromList(response, objectContext);
			break;

		case EXTENDED_LIST:
			objects = getObjectsFromExtendedList(response, objectContext);
			break;

		case RESULT_SET:
			objects = getObjectsFromResultSet(response, objectContext);
			break;

		case KEYS_LIST:
			objects = getObjectsFromKeysList(response, objectContext);
			break;

		case UNKNOWN:
			// Nothing to do
			break;
		}

		return objects;
	}

	// ---------------------------------------------------------------------------------------------------------
	// Private Methods
	// ---------------------------------------------------------------------------------------------------------

	// -------------------------------------- //
	// -------------- MAPPINGS -------------- //
	// -------------------------------------- //

	static private enum ENTITY_TYPE 
	{
		UNKNOWN,
		VIDEO,
		VIDEO_QUALITY,
		FRONTEND_SERVER,
		// <------------------------------------------------------------ TODO: ADD HERE NEW ENTITIES
	}

	static private Map<String, ENTITY_TYPE> entityTypeMap = null;
	static private ENTITY_TYPE getEntityTypeFromApiMethod(String method)
	{
		if (entityTypeMap == null)
		{
			entityTypeMap = new HashMap<String, ENTITY_TYPE>();

			entityTypeMap.put(ApiRequest.API_METHOD_VIDEO_SEARCH, ENTITY_TYPE.VIDEO);
			entityTypeMap.put(ApiRequest.API_METHOD_VIDEO_DETAILS, ENTITY_TYPE.VIDEO);
			entityTypeMap.put(ApiRequest.API_METHOD_VIDEO_QUALITY_SEARCH, ENTITY_TYPE.VIDEO_QUALITY);

			// <------------------------------------------------------------ TODO: ADD HERE NEW ENTRIES FOR THE METHOD->ENTITY_TYPE MAPPING
			
			//entityTypeMap.put(ApiRequest.<METHOD_NAME>, ENTITY_TYPE.<TYPE>);

			// NO SUPPORT FOR FRONTEND_SERVER, BECAUSE IS NOT IN DEFAULT PROXY!
			// Actually, this is an architectural problem: 
			// ApiResposnes not returned by the default server proxy cannot be parsed because its method name may be null!
		}

		if (entityTypeMap.containsKey(method))
			return entityTypeMap.get(method);

		return ENTITY_TYPE.UNKNOWN;
	}

	static private enum RESPONSE_TYPE 
	{
		UNKNOWN,
		LIST,
		EXTENDED_LIST,
		RESULT_SET,
		KEYS_LIST,
	}

	static private Map<String, RESPONSE_TYPE> responseTypeMap = null;
	static private RESPONSE_TYPE getResponseTypeFromApiMethod(String method)
	{
		if (responseTypeMap == null)
		{
			responseTypeMap = new HashMap<String, RESPONSE_TYPE>();

			responseTypeMap.put(ApiRequest.API_METHOD_VIDEO_SEARCH, RESPONSE_TYPE.RESULT_SET);
			responseTypeMap.put(ApiRequest.API_METHOD_VIDEO_DETAILS, RESPONSE_TYPE.EXTENDED_LIST);
			responseTypeMap.put(ApiRequest.API_METHOD_VIDEO_QUALITY_SEARCH, RESPONSE_TYPE.RESULT_SET);

			// <------------------------------------------------------------ TODO: ADD HERE NEW ENTRIES FOR THE METHOD->RESULT_TYPE MAPPING
			
			//responseTypeMap.put(ApiRequest.<METHOD_NAME>, RESPONSE_TYPE.<TYPE>);
		}

		if (responseTypeMap.containsKey(method))
			return responseTypeMap.get(method);

		return RESPONSE_TYPE.UNKNOWN;
	}
	
	static private ArrayList<String> fullyFetchingMethods = null;
	static private boolean isFullyFetchingMethod(String method)
	{
		if (fullyFetchingMethods == null)
		{
			fullyFetchingMethods = new ArrayList<String>();

			fullyFetchingMethods.add(ApiRequest.API_METHOD_VIDEO_DETAILS);
			fullyFetchingMethods.add(ApiRequest.API_METHOD_USER_INFO_GET);
			fullyFetchingMethods.add(ApiRequest.API_METHOD_CHANNEL_SEARCH);
			fullyFetchingMethods.add(ApiRequest.API_METHOD_CATEGORY_SEARCH_TREE);
			fullyFetchingMethods.add(ApiRequest.API_METHOD_WALL_ENTRY_DETAILS);
			fullyFetchingMethods.add(ApiRequest.API_METHOD_COMPETITION_INFO_GET);
			fullyFetchingMethods.add(ApiRequest.API_METHOD_LOCATION_DETAILS);
			fullyFetchingMethods.add(ApiRequest.API_METHOD_IMAGE_DETAILS);
			fullyFetchingMethods.add(ApiRequest.API_METHOD_VIDEO_QUALITY_SEARCH);
			
			// <------------------------------------------------------------ ADD HERE FULLY FETCHING METHOD NAMES
			
			// fullyFetchingMethods.add(ApiRequest.<METHOD_NAME>);
		}
		
		return fullyFetchingMethods.contains(method);
	}

	// -------------------------------------- //
	// --------------- HELPERS -------------- //
	// -------------------------------------- //

	static private ModelEntity createNewModelEntityInstance(String key, ENTITY_TYPE type)
	{
		switch(type)
		{
		case VIDEO:
			return new CTVVideo(key);
			
		case VIDEO_QUALITY:
			return new CTVVideoQuality(key);
			
		case FRONTEND_SERVER:
			return new CTVFrontendServer(key);
			
		// <------------------------------------------------------------ TODO: ADD HERE NEW CASES FOR CREATING INSTANCES!
			
		case UNKNOWN:
			return null;
			
		default:
			return null;
		}
	}

	static private String parseKey(JSONObject jsonObject, ENTITY_TYPE type)
	{
		switch(type)
		{
		case VIDEO:
			return CTVVideo.parseKey(jsonObject);
			
		case VIDEO_QUALITY:
			return CTVVideoQuality.parseKey(jsonObject);
		
		case FRONTEND_SERVER:
			return CTVFrontendServer.parseKey(jsonObject);
		
		// <------------------------------------------------------------ TODO: ADD HERE NEW CASES FOR PARSING KEYS!
			
		case UNKNOWN:
			return null;
			
		default:
			return null;
		}
	}

	// -------------------------------------- //
	// -------- PARSING API RESPONSES ------- //
	// -------------------------------------- //

	static private ModelEntity[] getObjectsFromList(ApiResponse response, ObjectContext objectContext)
	{
		/*
		 * Should parse a result with following structure and return an array of parsed objects.
		 * 
		 * {"<OBJ_KEY_1>":{<OBJECT_DESCRIPTION_1>},
		 *  "<OBJ_KEY_2>":{<OBJECT_DESCRIPTION_2>},
		 *  ...
		 *  "<OBJ_KEY_N>":{<OBJECT_DESCRIPTION_N>},
		 * }
		 */

		// Getting the entity type for the business objects from the method
		ENTITY_TYPE type = getEntityTypeFromApiMethod(response.getMethod());

		// Create a placeholder for the objects
		ArrayList<ModelEntity> objects = new ArrayList<ModelEntity>();
		
		// Getting boolean value indicating if the method returns complete information for each object
		boolean shouldSetFullUpdate = ApiParser.isFullyFetchingMethod(response.getMethod());

		// Getting the payload
		JSONObject payload = response.getPayload();

		Iterator<?> keys = payload.keys();
		while(keys.hasNext())
		{
			// Getting the key of each object
			String key = (String)keys.next();

			try 
			{
				// Getting the description of each object (as dictionary)
				JSONObject rawEntity = (JSONObject)payload.get(key);

				// Retrieve object from persistence if existing
				ModelEntity me = (ModelEntity)objectContext.getObjectForKey(key);

				if (me == null) // if not in persistence..
				{
					// create new object of the given entity.
					me = ApiParser.createNewModelEntityInstance(key, type);

					// Add the object to the object context
					objectContext.insertObject(me);
				}

				// Finally, parse the object.
				// Notice that the object is an instance of the correct entity type.
				me.parse(rawEntity);
				
				// If the method returns complete description of the object, mark the current date
				if (shouldSetFullUpdate)
					me.setServerFullUpdateDate(new Date());

				// Add the object to the array
				objects.add(me);
			} 
			catch (JSONException e) 
			{
				// If JSONException, just move to next object.
				e.printStackTrace();
			}
		}

		// Finally, return an array with the parsed objects
		return objects.toArray(new ModelEntity[objects.size()]);
	}

	static private ModelEntity[] getObjectsFromExtendedList(ApiResponse response, ObjectContext objectContext)
	{
		/*
		 * Should parse a result with following structure and return an array of parsed objects.
		 * 
		 * {"<OBJ_KEY_1>":{"error_code":<ERROR_CODE_1>,
		 *                 "result_set":{<OBJECT_DESCRIPTION_1>}
		 *                },
		 *  "<OBJ_KEY_2>":{"error_code":<ERROR_CODE_2>,
		 *                 "result_set":{<OBJECT_DESCRIPTION_2>}
		 *                },
		 *	...
		 *  "<OBJ_KEY_N>":{"error_code":<ERROR_CODE_N>,
		 *				   "result_set":{<OBJECT_DESCRIPTION_N>}
		 *				  },
		 * }
		 */

		// Getting the entity type for the business objects from the method
		ENTITY_TYPE type = getEntityTypeFromApiMethod(response.getMethod());

		// Create a placeholder for the objects
		ArrayList<ModelEntity> objects = new ArrayList<ModelEntity>();

		// Getting boolean value indicating if the method returns complete information for each object
		boolean shouldSetFullUpdate = ApiParser.isFullyFetchingMethod(response.getMethod());
		
		// Getting the payload
		JSONObject payload = response.getPayload();

		Iterator<?> keys = payload.keys();
		while(keys.hasNext())
		{
			// Getting the key of each object
			String key = (String)keys.next();

			try 
			{
				// Getting each entry
				JSONObject entry = (JSONObject)payload.get(key);
				
				// Get the error code and the object description
				String errorCodeStr = entry.getString("error_code");
				JSONObject rawEntity = entry.getJSONObject("return_value");

				// Parsing the error code
				int errorCode = ApiError.errorFromString(errorCodeStr);

				// Only parse if error code is OK.
				if (errorCode == ApiError.OK)
				{
					// Retrieve object from persistence if existing
					ModelEntity me = (ModelEntity)objectContext.getObjectForKey(key);

					if (me == null) // if not in persistence..
					{
						// create new object of the given entity.
						me = ApiParser.createNewModelEntityInstance(key, type);

						// Add the object to the object context
						objectContext.insertObject(me);
					}

					// Finally, parse the object.
					me.parse(rawEntity);

					// If the method returns complete description of the object, mark the current date
					if (shouldSetFullUpdate)
						me.setServerFullUpdateDate(new Date());
					
					// Add the object to the array
					objects.add(me);
				}
			}
			catch (JSONException e) 
			{
				// If JSONException, just move to next object.
				e.printStackTrace();
			}
		}

		// Finally, return an array with the parsed objects
		return objects.toArray(new ModelEntity[objects.size()]);
	}

	static private ModelEntity[] getObjectsFromKeysList(ApiResponse response, ObjectContext objectContext)
	{
		/*
		 * Should parse a result with following structure and return an array of parsed objects.
		 * 
		 * {"<A_STRING>": ["<OBJ_KEY_1>",
		 *     			   "<OBJ_KEY_2>",
		 *				   ...
		 *				   "<OBJ_KEY_N>"
		 *				  ]
		 * }
		 */

		// Getting the entity type for the business objects from the method
		ENTITY_TYPE type = getEntityTypeFromApiMethod(response.getMethod());

		// Create a placeholder for the objects
		ArrayList<ModelEntity> objects = new ArrayList<ModelEntity>();

		// Getting the payload from the response
		JSONObject payload = response.getPayload();

		if (payload.length() == 1)
		{
			// If payload contains more than one entry, undefined structure is found.
			// Just return null and do nothing.
			return null;
		}

		// Iterate in a single item list
		Iterator<?> keys = payload.keys();
		while(keys.hasNext())
		{
			// Getting the key containing the array of keys value 
			String irrelevantKeyName = (String)keys.next();

			// Create pointer for the array with list of keys
			JSONArray keysList = null;

			try 
			{
				// Get the list of keys
				keysList = payload.getJSONArray(irrelevantKeyName);
			}
			catch (JSONException e) 
			{
				// If error, return null
				e.printStackTrace();
				return null;
			}

			// Iterating for each element of the list
			for (int i=0; i<keysList.length(); ++i)
			{
				try 
				{
					// Getting the key from the list
					String key = (String)keysList.getString(i);

					// Retrieve object from persistence if existing
					ModelEntity me = (ModelEntity)objectContext.getObjectForKey(key);

					if (me == null) // if not in persistence..
					{
						// create new object of the given entity.
						me = ApiParser.createNewModelEntityInstance(key, type);

						// Add the object to the object context
						objectContext.insertObject(me);
					}

					// There is no rawEntity to parse.
					// In case object does not exist in persistence, return just an empty object.

					// Add the object to the array
					objects.add(me);
				}
				catch (JSONException e) 
				{
					// If error, move to next object
					e.printStackTrace();
				}
			}

		}

		// Finally, return an array with the parsed objects
		return objects.toArray(new ModelEntity[objects.size()]);
	}

	static private ModelEntity[] getObjectsFromResultSet(ApiResponse response, ObjectContext objectContext)
	{
		/*
		 * Should parse a result with following structure and return an array of parsed objects.
		 * 
		 * {"result_set":[{<OBJECT_DESCRIPTION_1>},
		 *     			  {<OBJECT_DESCRIPTION_2>},
		 *				  ...
		 *     			  {<OBJECT_DESCRIPTION_N>}
		 *     			 ],
		 *	"num_result_total":<VALUE>,
		 *	"num_result_count":<VALUE>,
		 * }
		 */

		// Getting the entity type for the business objects from the method
		ENTITY_TYPE type = getEntityTypeFromApiMethod(response.getMethod());

		// Create a placeholder for the objects
		ArrayList<ModelEntity> objects = new ArrayList<ModelEntity>();
		
		// Getting boolean value indicating if the method returns complete information for each object
		boolean shouldSetFullUpdate = ApiParser.isFullyFetchingMethod(response.getMethod());

		// Getting the payload from the response
		JSONObject payload = response.getPayload();

		// Creating pointer to raw results array
		JSONArray rawResults = null;
		
		try 
		{
			// Get the array of results
			rawResults = payload.getJSONArray("result_set");
		}
		catch (JSONException e) 
		{
			// If JSONException, return null.
			e.printStackTrace();
			return null;
		}
		
		// Iteration over each result item
		for (int i=0; i<rawResults.length(); ++i)
		{
			try 
			{
				// Get the object description for the given index
				JSONObject rawEntity = rawResults.getJSONObject(i);

				// Getting the key of each object by parsing the JSON object
				String key = ApiParser.parseKey(rawEntity, type);

				// Retrieve object from persistence if existing
				ModelEntity me = (ModelEntity)objectContext.getObjectForKey(key);

				if (me == null) // if not in persistence..
				{
					// create new object of the given entity.
					me = ApiParser.createNewModelEntityInstance(key, type);

					// Add the object to the object context
					objectContext.insertObject(me);
				}

				// Finally, parse the object.
				me.parse(rawEntity);
				
				// If the method returns complete description of the object, mark the current date
				if (shouldSetFullUpdate)
					me.setServerFullUpdateDate(new Date());

				// Add the object to the array
				objects.add(me);
			}
			catch (JSONException e) 
			{
				// If JSONException, do nothing
				e.printStackTrace();
			}
		}

		// Finally, return an array with the parsed objects
		return objects.toArray(new ModelEntity[objects.size()]);
	}
}
