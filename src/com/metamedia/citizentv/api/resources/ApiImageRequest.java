package com.metamedia.citizentv.api.resources;

import com.metamedia.citizentv.MainActivity;
import com.metamedia.citizentv.entities.CTVFrontendServer;
import com.metamedia.citizentv.entities.helpers.CropRect;
import com.metamedia.citizentv.entities.helpers.CTVResource;

public class ApiImageRequest extends ApiResourceRequest
{
	static public enum TRANSFORM
	{
		ORIGINAL(1), /**< Get the original image. */
	    RESIZE(2), /**< Generates a resize of the original image to the output size. */
	    BEST_CROP(3), /**< Generates a best crop and resizes the original images for the given output size. Centered H and top V.*/
	    BEST_CROP_CENTER(4), /**< Generates a best crop and resizes the original images for the given output size. Centered V+H*/
	    CROP(5); /**< Generates a crop and a resize for the given output size and crop rectangle. */
		
		private final int id;

		TRANSFORM(int id) 
		{
		    this.id = id;
		}
		
		public int getId()
		{
			return id;
		}
	}
	
	// Transform related attributes
	private TRANSFORM transform;	
	private int width;
	private int height;
	private CropRect cropRect;
	
	// ---------------------------------------------------------------------------------------------------------
	// Constructor
	// ---------------------------------------------------------------------------------------------------------

	public ApiImageRequest(CTVResource resource)
	{
		super(resource);
		this.transform = TRANSFORM.ORIGINAL;
	}
	
	// ---------------------------------------------------------------------------------------------------------
	// Getters & Setters
	// ---------------------------------------------------------------------------------------------------------
	
	public TRANSFORM getTransform()
	{
		return transform;
	}
	
	public void setTransform(TRANSFORM transform)
	{
		this.transform = transform;
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
	
	public CropRect getCropRect()
	{
		return cropRect;
	}
	
	public void setCropRect(CropRect cropRect)
	{
		this.cropRect = cropRect;
	}

	// ---------------------------------------------------------------------------------------------------------
	// Public Methods
	// ---------------------------------------------------------------------------------------------------------

	@Override
	public String getUrlPath()
	{
		// Get the used storage server
	    CTVFrontendServer storageServer = MainActivity.frontendServerHashMap.get(this.getResource().storageServerKey);
	    
	    // If no storage server, URL cannot be build. Return null.
	    if (storageServer == null)
	        return null;
	    
	    // Build the transform string
	    String transformStr = null;
	    switch (transform)
	    {
	    case ORIGINAL:
	    	transformStr = "original";
	    	break;
	    	
	    case RESIZE:
	    	transformStr = "resize-" + width + "-" + height;
			break;
			
	    case BEST_CROP:
	    	transformStr = "best-crop-" + width + "-" + height;
	    	break;
	    	
	    case BEST_CROP_CENTER:
	    	transformStr = "best-crop-center-" + width + "-" + height;
	    	break;
	    	
	    case CROP:
	    	if (cropRect == null)
	    		return null;
	    	transformStr = "crop-" + width + "-" + height + "-" + cropRect.top + "-" + cropRect.left + "-" + cropRect.bottom + "-" + cropRect.right;
	    	break;
	    }
	    	    
	    // Finally, build the URL
	    String urlPath = "http://" + storageServer.imageHost + "/" + this.getUserFriendlyText() + "-" + transformStr +"-" + this.getResource().resourceKey + ".jpg";
    
	    // Return the URL
	    return urlPath;
	}
}
