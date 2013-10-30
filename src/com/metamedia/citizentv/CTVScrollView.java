package com.metamedia.citizentv;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ScrollView;

public class CTVScrollView extends ScrollView 
{
	private iBlankDelegate scrolledToTheBottomDelegate; //For an instance of this type of scroll view, different actions may be taken when the user scrolls to the bottom. These actions will be defined in this delegate.
	private long milliesSinceLastScrollToBottom = 0; //A simple way to keep the logic for the scroll-to-the-bottom event from firing a bunch of times in a row.
	
	public void setScrolledToTheBottomDelegate(iBlankDelegate scrolledToTheBottomDelegate) 
	{
		this.scrolledToTheBottomDelegate = scrolledToTheBottomDelegate;
	}

	public CTVScrollView(Context context) 
	{
		super(context);

	}

	public CTVScrollView(Context context, AttributeSet attrs) 
	{
		super(context, attrs);

	}

	public CTVScrollView(Context context, AttributeSet attrs, int defStyle) 
	{
		super(context, attrs, defStyle);

	}
	
	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt)
	{
		//Detect if the user has scrolled to the bottom and if so, initiate loading of more items at the bottom.
        View bottomChildView = (View) getChildAt(getChildCount()-1);
        int diff = (bottomChildView.getBottom()-(getHeight()+getScrollY()));// Calculate the scrolldiff
        if((diff == 0 && t != oldt) && (System.currentTimeMillis() - milliesSinceLastScrollToBottom > 500L) && MainActivity.canLoadMoreVideoItemsNow)
		{
        	//If diff is zero, then the bottom has been reached. The second condition is to stop the method from firing if the scroller was already at the bottom. The third is to make sure the previous load-more request has completed.
            scrolledToTheBottomDelegate.executeCallback();
            milliesSinceLastScrollToBottom = System.currentTimeMillis();
		}
        
        
		
        super.onScrollChanged(l, t, oldl, oldt);
	}

}
