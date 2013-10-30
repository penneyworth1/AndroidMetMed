package com.metamedia.managers;

import android.content.Context;

import com.metamedia.citizentv.lists.FetchListPersistenceManager;
import com.metamedia.persistentmodel.*;


public class ModelManager 
{
	private Context applicationContext;
	private iPersistentStore persistentStore;
	private ObjectContext defaultObjectContext;
	private FetchListPersistenceManager fetchListManager;
	
	private boolean initializated = false;
	private static ModelManager instance = null;

	// ---------------------------------------------------------------------------------------------------------
	// Constructor & Singleton
	// ---------------------------------------------------------------------------------------------------------
	private ModelManager()
	{
		super();
	}
	
	public static ModelManager getInstance()
	{
		if(instance == null)
		{
			instance = new ModelManager();
		}
		else
		{
			if (instance.initializated == false)
				return null;
		}
		
		return instance;
	}
	
	// ---------------------------------------------------------------------------------------------------------
	// Public methods
	// ---------------------------------------------------------------------------------------------------------
	public void initialize(Context context)
	{
		if (this.initializated)
			return;
		
		this.applicationContext = context;
		this.persistentStore = new PersistentSQLiteStore(context, "PersistentModel");
		
//		this.persistentStore.reset();
		
		this.defaultObjectContext =  new ObjectContext(this.persistentStore);
		
		PersistentSQLiteStore fetchListStore = new PersistentSQLiteStore(context, "FetchListPersistentModel");
//		fetchListStore.reset();
		this.fetchListManager = new FetchListPersistenceManager(fetchListStore);
		
		this.initializated = true;
		
//		((PersistentSQLiteStore)persistentStore).reset();
	}

	// ---------------------------------------------------------------------------------------------------------
	// Getters & Setters
	// ---------------------------------------------------------------------------------------------------------
	
	public Context getApplicationContext() 
	{
		return applicationContext;
	}

	public iPersistentStore getPersistentStore() 
	{
		return persistentStore;
	}

	public ObjectContext getDefaultObjectContext() 
	{
		return defaultObjectContext;
	}

	public boolean isInitializated() 
	{
		return initializated;
	}

	public FetchListPersistenceManager getFetchListPersistenceManager() 
	{
		return fetchListManager;
	}
}
