package com.metamedia.imageStore;

public class ImageStoreResult 
{
	private long dbID;
	private String key;
	private long creationDate;
	private long accessDate;
	private String options;
	private int width;
	private int height;
	private byte[] data;
 	
	// ---------------------------------------------------------------------------------------------------------
	// Constructor
	// ---------------------------------------------------------------------------------------------------------
	
	public ImageStoreResult(long dbID)
	{
		super();
		this.dbID = dbID;
	}
	
	// ---------------------------------------------------------------------------------------------------------
	// Getters & Setters
	// ---------------------------------------------------------------------------------------------------------

	public long getDbID() 
	{
		return dbID;
	}

	public String getKey()
	{
		return key;
	}

	public void setKey(String key)
	{
		this.key = key;
	}

	public long getCreationDate()
	{
		return creationDate;
	}

	public void setCreationDate(long creationDate)
	{
		this.creationDate = creationDate;
	}

	public long getAccessDate()
	{
		return accessDate;
	}

	public void setAccessDate(long accessDate) 
	{
		this.accessDate = accessDate;
	}

	public String getOptions() 
	{
		return options;
	}

	public void setOptions(String options) 
	{
		this.options = options;
	}
	
	public int getWidth()
	{
		return width;
	}
	
	public void setWidth(int width)
	{
		this.width = width;
	}
	
	public int getHeight()
	{
		return height;
	}
	
	public void setHeight(int height)
	{
		this.height = height;
	}

	public byte[] getData()
	{
		return data;
	}

	public void setData(byte[] data) 
	{
		this.data = data;
	}
}