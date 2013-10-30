package com.metamedia.imageStore;

public class ImageStoreRequest 
{
	public static enum REQUEST_TYPE
	{
		UNDEFINED,
		KEY,
		KEY_OPTIONS,
		OLDER_THAN_ACCESS_DATE,
		NEWER_THAN_ACCESS_DATE,
	}
	
	public static enum REQUEST_SIZE_TYPE
	{
		ANY_SIZE,
		SAME_SIZE,
	}

	private REQUEST_TYPE type;
	private String key = null;
	private String options;
	private long accessDate;
	//	private long creationDate;
	
	private REQUEST_SIZE_TYPE sizeType;
	private int width;
	private int height;

	// ---------------------------------------------------------------------------------------------------------
	// Constructor
	// ---------------------------------------------------------------------------------------------------------

	public ImageStoreRequest()
	{
		super();
		this.type = REQUEST_TYPE.UNDEFINED;
		this.sizeType = REQUEST_SIZE_TYPE.ANY_SIZE;
	}
	
	public ImageStoreRequest(String key)
	{
		super();
		this.type = REQUEST_TYPE.KEY;
		this.key = key;
		this.sizeType = REQUEST_SIZE_TYPE.ANY_SIZE;
	}

	public ImageStoreRequest(String key, String options)
	{
		super();
		this.type = REQUEST_TYPE.KEY_OPTIONS;
		this.options = options;
		this.key = key;
		this.sizeType = REQUEST_SIZE_TYPE.ANY_SIZE;
	}
	
	public ImageStoreRequest(String key, String options, int width, int height)
	{
		super();
		this.type = REQUEST_TYPE.KEY_OPTIONS;
		this.options = options;
		this.key = key;
		this.width = width;
		this.height = height;
		this.sizeType = REQUEST_SIZE_TYPE.SAME_SIZE;
	}

	// ---------------------------------------------------------------------------------------------------------
	// Getters & Setters
	// ---------------------------------------------------------------------------------------------------------

	public REQUEST_TYPE getType()
	{
		return type;
	}

	public void setType(REQUEST_TYPE type)
	{
		this.type = type;
	}

	public String getKey()
	{
		return key;
	}

	public void setKey(String key)
	{
		this.key = key;
	}

	public void setOptions(String options)
	{
		this.options = options;
	}

	public String getOptions()
	{
		return options;
	}
	
	public long getAccessDate()
	{
		return accessDate;
	}

	public void setAccessDate(long accessDate)
	{
		this.accessDate = accessDate;
	}

	public REQUEST_SIZE_TYPE getSizeType() 
	{
		return sizeType;
	}

	public void setSizeType(REQUEST_SIZE_TYPE sizeType) 
	{
		this.sizeType = sizeType;
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

	// ---------------------------------------------------------------------------------------------------------
	// Protected Methods
	// ---------------------------------------------------------------------------------------------------------

	protected String getWhereStatement()
	{
		// First, build the where statement regarding size
		String sizeWhere = null;
		
		switch (sizeType)
		{
		case ANY_SIZE:
			break;
			
		case SAME_SIZE:
			sizeWhere = ImageStore.TABLE_IMAGES + "." + ImageStore.IMAGES_COLUMN_WIDTH + "=" + width + " AND "
						+ ImageStore.TABLE_IMAGES + "." + ImageStore.IMAGES_COLUMN_HEIGHT+ "=" + height;
			break;
		}
		
		// Creating the full where statement depending on the request type.
		String where = null;

		switch (type)
		{
		case KEY:
			where = ImageStore.TABLE_IMAGES + "." + ImageStore.IMAGES_COLUMN_KEY + "=\"" + key + "\"";
			break;

		case KEY_OPTIONS:
			where = ImageStore.TABLE_IMAGES + "." + ImageStore.IMAGES_COLUMN_KEY + "=\"" + key + "\"" + " AND "
					+ ImageStore.TABLE_IMAGES + "." + ImageStore.IMAGES_COLUMN_OPTIONS + "=\"" + options + "\"";
			break;
			
		case OLDER_THAN_ACCESS_DATE:
			where = ImageStore.TABLE_IMAGES + "." + ImageStore.IMAGES_COLUMN_ACCESS_DATE + "<" + accessDate;
			break;
		
		case NEWER_THAN_ACCESS_DATE:
			where = ImageStore.TABLE_IMAGES + "." + ImageStore.IMAGES_COLUMN_ACCESS_DATE + ">=" + accessDate;
			break;
			
		case UNDEFINED:
		default:
			break;
		}
		
		// Concatenate the "WHERE" statement with the size "WHERE" sub-statement, if needed
		if (where != null)
		{
			if (sizeWhere != null)
			{
				where = where + " AND " + sizeWhere;
			}
		}

		return where;
	}
}
