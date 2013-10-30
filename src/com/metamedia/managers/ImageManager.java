package com.metamedia.managers;

import java.io.InputStream;
import java.net.URL;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.LruCache;

import com.metamedia.citizentv.api.resources.ApiImageRequest;
import com.metamedia.imageStore.*;
import com.metamedia.imageStore.ImageStoreRequest.REQUEST_SIZE_TYPE;
import com.metamedia.imageStore.ImageStoreRequest.REQUEST_TYPE;

public class ImageManager
{
	// Persistent cache
	private ImageStore imageStore;

	// Dynamic cache
	// For more information read http://developer.android.com/training/displaying-bitmaps/cache-bitmap.html
	private LruCache<String, Bitmap> memoryCache;

	
	private boolean initializated = false;
	private static ImageManager instance = null;

	// ---------------------------------------------------------------------------------------------------------
	// Constructor & Singleton
	// ---------------------------------------------------------------------------------------------------------
	private ImageManager()
	{
		super();
	}
	
	public static ImageManager getInstance()
	{
		if(instance == null)
		{
			instance = new ImageManager();
		}
		else
		{
			if (instance.initializated == false)
				return null;
		}
		
		return instance;
	}
	
	// ---------------------------------------------------------------------------------------------------------
	// Constructor
	// ---------------------------------------------------------------------------------------------------------

	public void initialize(Context context)
	{
		// Instantiating the persistent cache
		imageStore = new ImageStore(context, "ImageStore");

		// Configuring the dynamic cache

		// Get max available VM memory, exceeding this amount will throw an OutOfMemory exception. 
		// Stored in kilobytes as LruCache takes an integer in its constructor.
		final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

		// Use 1/8th of the available memory for this memory cache.
		final int cacheSize = maxMemory / 8;

		memoryCache = new LruCache<String, Bitmap>(cacheSize) {
			@Override
			protected int sizeOf(String key, Bitmap bitmap) 
			{
				// The cache size will be measured in kilobytes rather than number of items.
				return bitmap.getByteCount() / 1024;
			}
		};
		
		initializated = true;
	}

	// ---------------------------------------------------------------------------------------------------------
	// Public Methods
	// ---------------------------------------------------------------------------------------------------------

	public Bitmap executeImageRequestInCache(ApiImageRequest request)
	{
		// Getting the request identifier
		String requestIdentifier = request.getIdentifier();

		// Getting the image from dynamic cache
		Bitmap bitmap = getBitmapFromMemoryCache(requestIdentifier);

		// If available, return the image
		if (bitmap != null)
			return bitmap;

		// Otherwise, try to get the image from the persistent cache
		bitmap = getBitmapFromPersistentCache(request);
		
		// If available, return the image
		if (bitmap != null)
		{
			// but before returning the image, store the image in the dynamic cache
			addBitmapToMemoryCache(requestIdentifier, bitmap);
			return bitmap;
		}

		// Otherwise image is not cached previously, return null.
		return null;
	}

	public Bitmap executeImageRequest(ApiImageRequest request)
	{
		// Getting the image from cache
		Bitmap bitmap = this.executeImageRequestInCache(request);

		// If available, return
		if (bitmap != null)
			return bitmap;

		// If not available, download the image.		
		String imageUrl = request.getUrlPath();
        try
        {
            InputStream in = new URL(imageUrl).openStream();
            bitmap = BitmapFactory.decodeStream(in);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

		// Once download done, store the image in both dynamic and persistent cache
		addBitmapToPersistentCache(request, bitmap);
		addBitmapToMemoryCache(request.getIdentifier(), bitmap);

		return bitmap;
	}

	// ---------------------------------------------------------------------------------------------------------
	// Private Methods
	// ---------------------------------------------------------------------------------------------------------


	private Bitmap getBitmapFromMemoryCache(String key)
	{
		return memoryCache.get(key);
	}
	
	private void addBitmapToMemoryCache(String key, Bitmap bitmap) 
	{
		// Caching the bitmap only if not cached yet
		if (getBitmapFromMemoryCache(key) == null) 
		{
			memoryCache.put(key, bitmap);
		}
	}

	private Bitmap getBitmapFromPersistentCache(ApiImageRequest request)
	{
		// Build the persistent cache request
		// ImageStoreRequest storeRequest = new ImageStoreRequest(request.getResource().getIdentifier(), request.getTransform().getId(), request.getWidth(), request.getHeight());
		ImageStoreRequest storeRequest = new ImageStoreRequest();

		storeRequest.setKey(request.getResource().getIdentifier());
		storeRequest.setOptions(getOptionsString(request));
		storeRequest.setType(REQUEST_TYPE.KEY_OPTIONS);

		storeRequest.setWidth(request.getWidth()); 
		storeRequest.setHeight(request.getHeight()); 
		storeRequest.setSizeType(REQUEST_SIZE_TYPE.SAME_SIZE);

		// Execute the persistent cache request
		Bitmap[] results = imageStore.executeRequest(storeRequest);

		// If more than one result, return the first result.
		if (results.length > 0)
		{
			// Get the first image
			Bitmap bitmap = results[0];

			// Recycle the others
			for (int i=1; i<results.length; ++i)
				results[i].recycle();
			
			// Return the image
			return bitmap;
		}
		
		return null;
	}

	private boolean addBitmapToPersistentCache(ApiImageRequest request, Bitmap bitmap)
	{
		return imageStore.storeImage(bitmap, request.getResource().getIdentifier(), getOptionsString(request));
	}
	
	private String getOptionsString(ApiImageRequest request)
	{
		// Setting the options value from the selected transform.
		// Crop transform need a custom value.
		String optionsStr = null;
		if (request.getTransform() == ApiImageRequest.TRANSFORM.CROP)
			optionsStr = "" + request.getTransform().getId() + "<" 
					+ request.getCropRect().top + "," 
					+ request.getCropRect().left + "," 
					+ request.getCropRect().bottom + "," 
					+ request.getCropRect().right + ">"; 
		else
			optionsStr = "" + request.getTransform().getId();

		return optionsStr;
	}
}
