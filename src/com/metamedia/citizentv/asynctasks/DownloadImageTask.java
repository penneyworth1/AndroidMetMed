package com.metamedia.citizentv.asynctasks;

import com.metamedia.citizentv.api.resources.ApiImageRequest;
import com.metamedia.managers.ImageManager;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

public class DownloadImageTask extends AsyncTask<ApiImageRequest, Void, Bitmap>
{
    ImageView imageView;
    ProgressBar progressBar;
    Boolean errorOccurred = false;
    
    public DownloadImageTask(ImageView imageViewPar, ProgressBar progressBarPar)
    {
        this.imageView = imageViewPar;
        this.progressBar = progressBarPar;
    }
    protected Bitmap doInBackground(ApiImageRequest... requests)
    {
    	ApiImageRequest request = requests[0];
    	
        Bitmap bitmap = ImageManager.getInstance().executeImageRequest(request);

        return bitmap;
    }
    protected void onPostExecute(Bitmap result)
    {
    	if(!errorOccurred)
    	{
    		imageView.setImageBitmap(result);
    		imageView.setVisibility(View.VISIBLE);
    		progressBar.setVisibility(View.INVISIBLE);
    	}
    }
}

