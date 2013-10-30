package com.metamedia.imageStore;

import java.io.ByteArrayOutputStream;
import java.util.HashSet;
import java.util.Set;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/*
 * SQLite Table Overview:
 * 
 * Table IMAGES:
 * +-----------------------+-------+--------------+------------+---------+---------+---------+
 * |          id           |  key  | creationDate | accessDate | options |  width  | height  |
 * +-----------------------+-------+--------------+------------+---------+---------+---------+
 * | INTEGER (PRIMARY KEY) | TEXT  | INTEGER      | INTEGER    | TEXT    | INTEGER | INTEGER |
 * +-----------------------+-------+--------------+------------+---------+---------+---------+
 * 
 * Table DATA:
 * +--------------------------------------------------------+------+
 * |                           id                           | data |
 * +--------------------------------------------------------+------+
 * | INTEGER (PRIMARY KEY FOREIGN KEY REFERENCES IMAGES.id) | BLOB |
 * +--------------------------------------------------------+------+
 */

/**
 * This class provides a cache storage for images. 
 * Users can query to retrieve stored images, as well as storing new ones or deleting old images.
 */
public class ImageStore extends SQLiteOpenHelper 
{
	private static final int DATABASE_VERSION = 1;

	// Table name
	protected static final String TABLE_IMAGES= "Images";
	protected static final String TABLE_DATA= "Data";

	// Image Table Columns names
	protected static final String IMAGES_COLUMN_ID = "id";
	protected static final String IMAGES_COLUMN_KEY = "key";
	protected static final String IMAGES_COLUMN_CREATION_DATE = "creationDate";
	protected static final String IMAGES_COLUMN_ACCESS_DATE = "accessDate";
	protected static final String IMAGES_COLUMN_OPTIONS = "options";
	protected static final String IMAGES_COLUMN_WIDTH = "width";
	protected static final String IMAGES_COLUMN_HEIGHT = "height";

	// Data Table Columns names
	protected static final String DATA_COLUMN_ID = "id";
	protected static final String DATA_COLUMN_DATA = "data";
	
	//Locks
    private static Object dbLock = new Object(); //All db reads and writes will synchronize on this lock. Therefore there will be no concurrency with db access.

	// ---------------------------------------------------------------------------------------------------------
	// Constructor
	// ---------------------------------------------------------------------------------------------------------

	public ImageStore(Context context, String databaseName) 
	{
		super(context, databaseName, null, DATABASE_VERSION);
	}

	// ---------------------------------------------------------------------------------------------------------
	// Extending SQLiteOpenHelper
	// ---------------------------------------------------------------------------------------------------------

	@Override
	public void onCreate(SQLiteDatabase db) 
	{
		// TABLE_IMAGES creation query
		String CREATE_IMAGES_TABLE = "CREATE TABLE " + TABLE_IMAGES + " ("
				+ IMAGES_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ IMAGES_COLUMN_KEY + " TEXT, "
				+ IMAGES_COLUMN_CREATION_DATE + " INTEGER, "
				+ IMAGES_COLUMN_ACCESS_DATE + " INTEGER, "
				+ IMAGES_COLUMN_OPTIONS + " TEXT, "
				+ IMAGES_COLUMN_WIDTH + " INTEGER, "
				+ IMAGES_COLUMN_HEIGHT + " INTEGER"
				+ ")";

		// TABLE_DATA creation query
		String CREATE_DATA_TABLE = "CREATE TABLE " + TABLE_DATA + " ("
				+ DATA_COLUMN_ID + " INTEGER PRIMARY KEY, "
				+ DATA_COLUMN_DATA + " BLOB, " 
				+ "FOREIGN KEY(" + DATA_COLUMN_ID + ") REFERENCES " + TABLE_IMAGES + "(" + IMAGES_COLUMN_ID + ")" 
				+ ")";

		// Executing the queries
		db.execSQL(CREATE_IMAGES_TABLE);
		db.execSQL(CREATE_DATA_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
	{
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_DATA);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_IMAGES);
		this.onCreate(db);
	}

	// ---------------------------------------------------------------------------------------------------------
	// Public Methods
	// ---------------------------------------------------------------------------------------------------------

	/**
	 * Executes a request into the cache database.
	 * @param request The request to be executed
	 * @return An array of images as result of the request.
	 */
	public Bitmap[] executeRequest(ImageStoreRequest request)
	{
		// Perform the query
		ImageStoreResult[] results = this.select(request);
		
		// If no results, return null
		if (results == null)
			return null;
		
		// Create container for found images
		Bitmap[] images = new Bitmap[results.length];
		
		// Iterate over each result creating the image
		for (int index = 0; index < results.length; ++index)
		{
			ImageStoreResult result = results[index];
			
			byte[] bytes = result.getData();
			
			Bitmap image = BitmapFactory.decodeByteArray(result.getData(), 0, bytes.length);
			
			images[index] = image;
		}
		
		// Return the found images
		return images;
	}

	/**
	 * Stores an image into the cache system
	 * @param image The image to store.
	 * @param key The key identifying the image.
	 * @param options Users can use this value as an additional flag which is going to be stored in a row of the database.
	 * @return
	 */
	public boolean storeImage(Bitmap image, String key, String options)
	{
		return this.insert(image, key, options);
	}
	
	/**
	 * Delete the entries specified by the request.
	 * @param request The request.
	 * @return true if succeed, otherwise false.
	 */
	public boolean executeDelete(ImageStoreRequest request)
	{
		return this.delete(request);
	}

	// ---------------------------------------------------------------------------------------------------------
	// PrivateMethods
	// ---------------------------------------------------------------------------------------------------------

	/**
	 * Inserts and stores new image into the caching system.
	 * @param image The image to store.
	 * @param key An identifier for the image
	 * @param options Users can use this value as an additional flag which is going to be stored in a row of the database.
	 * @return true if insert was successful, otherwise false.
	 */
	private boolean insert(Bitmap image, String key, String options)
	{
		// If no image or no key, do nothing.
		if (image == null || key == null)
			return false;
		
		synchronized(dbLock)
		{
		
			// Getting the writable database
			SQLiteDatabase db = this.getWritableDatabase();
	
			// Flag if the insertion process is successful
			boolean successful = false;
	
			// Converting the bitmap into bytes
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			image.compress(Bitmap.CompressFormat.PNG, 100, stream);
			byte[] byteArray = stream.toByteArray();
	
			try
			{
				// Begin transaction
				db.beginTransaction();
	
				// Getting the current date
				long timestamp = System.currentTimeMillis();
	
				// Preparing new insertion for TABLE_IMAGES
				ContentValues objectValues = new ContentValues();
				objectValues.put(IMAGES_COLUMN_KEY, key);
				objectValues.put(IMAGES_COLUMN_CREATION_DATE, timestamp);
				objectValues.put(IMAGES_COLUMN_ACCESS_DATE, timestamp);
				objectValues.put(IMAGES_COLUMN_OPTIONS, options);
				objectValues.put(IMAGES_COLUMN_WIDTH, image.getWidth());
				objectValues.put(IMAGES_COLUMN_HEIGHT, image.getHeight());
	
				// Inserting to TABLE_IMAGES
				long objectsRowID = db.insertOrThrow(TABLE_IMAGES, null, objectValues);
	
				// Preparing new insertion for TABLE_DATA
				ContentValues dataValues = new ContentValues();
				dataValues.put(DATA_COLUMN_ID, objectsRowID);
				dataValues.put(DATA_COLUMN_DATA, byteArray);
	
				// Inserting to TABLE_DATA
				db.insertOrThrow(TABLE_DATA, null, dataValues);
	
				// Everything OK
				successful = true;
				db.setTransactionSuccessful();
			}
			catch (SQLException e)
			{
				// nothing to do
			}
			finally
			{
				// If no exception, end transaction
				db.endTransaction();
			}
	
			// Finally, close the database
			db.close();
	
			// Return if the action is successful
			return successful;
		}
	}

	/**
	 * Performs a select action into the database
	 * @param request The request to be executed
	 * @return The result after executing the request
	 */
	private ImageStoreResult[] select(ImageStoreRequest request)
	{
		if (request == null)
			return null;

		// If no where statement, do nothing
		String whereStatement = request.getWhereStatement();
		if (whereStatement == null)
			return null;

		synchronized(dbLock)
		{
			SQLiteDatabase db = this.getReadableDatabase();
	
			String sqlQuery = "SELECT " 
					+ TABLE_IMAGES + "." + IMAGES_COLUMN_ID + ", "
					+ TABLE_IMAGES + "." + IMAGES_COLUMN_KEY + ", "
					+ TABLE_IMAGES + "." + IMAGES_COLUMN_CREATION_DATE+ ", "
					+ TABLE_IMAGES + "." + IMAGES_COLUMN_ACCESS_DATE + ", "
					+ TABLE_IMAGES + "." + IMAGES_COLUMN_WIDTH + ", "
					+ TABLE_IMAGES + "." + IMAGES_COLUMN_HEIGHT + ", "
					+ TABLE_IMAGES + "." + IMAGES_COLUMN_OPTIONS + ", "
					+ TABLE_DATA 	+ "." + DATA_COLUMN_DATA + " "
					+ "FROM "
					+ TABLE_IMAGES + " JOIN " +  TABLE_DATA + " ON " 
					+ TABLE_IMAGES + "." + IMAGES_COLUMN_ID + " = " + TABLE_DATA + "." + DATA_COLUMN_ID + " " 
					+ "WHERE " + whereStatement;
	
			Cursor cursor = db.rawQuery(sqlQuery, null);
	
			Set<ImageStoreResult> results = new HashSet<ImageStoreResult>();
			
			while (cursor.moveToNext())
			{
				ImageStoreResult result = new ImageStoreResult(cursor.getInt(0));
				
				result.setKey(cursor.getString(1));
				result.setCreationDate(cursor.getLong(2));
				result.setAccessDate(cursor.getLong(3));
				result.setWidth(cursor.getInt(4));
				result.setHeight(cursor.getInt(5));
				result.setOptions(cursor.getString(6));
				result.setData(cursor.getBlob(7));
	
				results.add(result);
			}
	
			cursor.close();
	
			for (ImageStoreResult result : results)
				this.markAccessOfImageWithID(result.getDbID());
			
			return results.toArray(new ImageStoreResult[results.size()]);
		}
	}

	/**
	 * Modifies the column "accesDate" for the specified row of the images table.
	 * @param dbID The row identifier of the image.
	 * @return true if succeed, false otherwise.
	 */
	private boolean markAccessOfImageWithID(long dbID)
	{
		SQLiteDatabase db = this.getWritableDatabase();

		boolean successful = false;

		try
		{
			db.beginTransaction();

			ContentValues objectValues = new ContentValues();
			objectValues.put(IMAGES_COLUMN_ACCESS_DATE, System.currentTimeMillis()); 

			// Perform the update
			db.update(TABLE_IMAGES, objectValues, IMAGES_COLUMN_ID + "=" + dbID, null);

			successful = true;
			db.setTransactionSuccessful();
		}
		catch (SQLException e)
		{
			// nothing to do
		}
		finally
		{
			db.endTransaction();
		}

		db.close();

		return successful;
	}
	
	/**
	 * Delete from the database the entries result of the given request.
	 * @param request The request that specifies the rows to delete.
	 * @return true if succeed, false otherwise.
	 */
	private boolean delete(ImageStoreRequest request)
	{
		// If no where statement, do nothing
		String whereStatement = request.getWhereStatement();
		if (whereStatement == null)
			return false;
				
		synchronized(dbLock)
		{
			SQLiteDatabase db = this.getWritableDatabase();
			
			boolean successful = false;
			
			try
			{
				db.beginTransaction();
				
				// Getting the images table "WHERE" statement. 
				String imagesWhereQuery = whereStatement;
				
				// Creating the data table "WHERE" statement.
				String dataWhereQuery = DATA_COLUMN_ID + " IN ("
						+ "SELECT "
						+ TABLE_IMAGES+ "." + IMAGES_COLUMN_ID + " "
						+ "FROM " + TABLE_IMAGES + " "
						+ "WHERE " + imagesWhereQuery 
						+ ")";
	
				// Execute the queries
				db.delete(TABLE_DATA,  dataWhereQuery, null);
				db.delete(TABLE_IMAGES,  imagesWhereQuery, null);
				
				successful = true;
				db.setTransactionSuccessful();
			}
			catch (SQLException e)
			{
				// nothing to do
			}
			finally
			{
				db.endTransaction();
			}
			
			db.close();
			
			return successful;
		}
	}
}
