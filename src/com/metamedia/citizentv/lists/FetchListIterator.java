package com.metamedia.citizentv.lists;

import java.util.Iterator;
import java.util.NoSuchElementException;

import com.metamedia.citizentv.entities.ModelEntity;

public class FetchListIterator implements Iterator<ModelEntity> 
{
	// ----------------------------------------------------------------------------------------------------
	// Attributes
	// ----------------------------------------------------------------------------------------------------
	
	private FetchList fetchList;
	private int currentIteration;
	
	public FetchList getFetchList()
	{
		return fetchList;
	}
	
	// ----------------------------------------------------------------------------------------------------
	// Constructor
	// ----------------------------------------------------------------------------------------------------
	
	public FetchListIterator(FetchList fetchList)
	{
		super();
		this.fetchList = fetchList;
		this.currentIteration = 0;
	}

	// ----------------------------------------------------------------------------------------------------
	// Iterator Interface
	// ----------------------------------------------------------------------------------------------------
	
	@Override
	public boolean hasNext() 
	{
		if (currentIteration < fetchList.size())
			return true;
		
		return false;	 
	}

	@Override
	public ModelEntity next() 
	{
		if (currentIteration == fetchList.size())
			throw new NoSuchElementException();

		currentIteration++;

		return fetchList.get(currentIteration - 1);
	}

	@Override
	public void remove() 
	{
		throw new UnsupportedOperationException();
	}
}
