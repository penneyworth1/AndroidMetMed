package com.metamedia.citizentv;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.metamedia.managers.ImageManager;
import com.metamedia.managers.ModelManager;
import com.metamedia.managers.TranslationManager;
import com.metamedia.persistentmodel.*;
import com.metamedia.citizentv.entities.*;
import com.metamedia.citizentv.lists.FetchList;
import com.metamedia.citizentv.lists.FetchListPersistenceManager;
import com.metamedia.citizentv.api.*;
import com.metamedia.tools.*;
import com.metamedia.citizentv.api.asyncconnection.ApiAsyncConnectionCompletionDelegate;
import com.metamedia.citizentv.api.resources.ApiImageRequest;
import com.metamedia.citizentv.asynctasks.*;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.OnHierarchyChangeListener;
import android.view.WindowManager;
import android.view.animation.Interpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Scroller;
import android.widget.TextView;
import android.widget.VideoView;
import android.view.animation.*;
import android.view.animation.Animation.AnimationListener;

public class MainActivity extends FragmentActivity implements SensorEventListener
{
	public static Context baseContext; //We need a static reference to *this* so it can be used in anonymous delegates.
	public static Activity baseActivity;
	public static SimplePagerAdapter pagerAdapter;
	public static ViewPager viewPager;
	public static int currentStackLevel;
	public static LayoutInflater layoutInflater;
	public static NonSwipeableViewPager mainViewPager;
	public static ArrayList<View> pageList = new ArrayList<View>(); //We store the stack of views here
	boolean firstViewLoaded = false; //We must wait until at least one view is added to the pager before we can populate it with the home layout, and the views are not loaded until well after the activity has initialized. Therefore we will set a listener to capture when a child is added, and only then add the home page content if this boolean value is false. Then we set it to true in order to not attempt to load the homepage again.
	public static int screenWidth, screenHeight;
	public static boolean screenRotationAllowed;
	
	//Vars to be initialized by the network when the app starts
	public static Map<String,CTVFrontendServer> frontendServerHashMap = new ConcurrentHashMap<String,CTVFrontendServer>();
	//public static Map<String,String> categoryHashMap = new ConcurrentHashMap<String,String>();
	public static Map<String,CTVVideoQuality> videoQualityMap = new ConcurrentHashMap<String,CTVVideoQuality>();
	public static TranslationManager translationManager;
	
	//Vars Concerning pages with lists of videos
	private static FetchList recentVideosFetchList;
	public static boolean canLoadMoreVideoItemsNow; //When this is false, no more requests can be made to load more videos when scrolling to the bottom of the screen.

	//Vars concerning the current video playing or to be played.
	public static VideoView currentVideoView; //This is a reference to the video that is playing or needs to be started.
	public static CTVVideo currentVideo;
	public static RelativeLayout currentVideoContainer; //This reference will be set when the video view page finishes initializing. When scrolling is complete, we will load the current video into this container.
	public static boolean waitingToStartVideo = false; //Once we navigate to the video view, and the view pager finishes scrolling, this will be set to true, alerting the viewPager's on scroll listener to start the current video once the scroller achieves an idle state.
	public static ProgressBar currentVideoLoadingProgressBar;
	public static TextView currentVideoErrorTextView;
	private static boolean currentViewIsVideo; //Whether the view we are currently looking at is the video playback view
	
	//NavDrawer vars
	public static DrawerLayout drawerLayout;
    public static ListView drawerList;
    public static String[] mPlanetTitles;
    
    //UI vars
    public static Typeface normalFont;
    public static Typeface boldFont;
    
    //Sensor reading vars
    public static SensorManager sensorManager;
    public static Sensor accelerometer;
    public static int currentOrientation;
    
    //Which tab is selected
    public static byte chosenTab;
    public static final byte TAB_RECENT = 1;
    public static final byte TAB_WHATS_HOT = 2;
    public static final byte TAB_WINNERS = 3;
    
    //Tab underline indicator
    public static RelativeLayout rlTabUnderline;
    public static int tabUnderlineCurrentX;
    public static int tabUnderlineCurrentWidth;
    
    //Widths of text for each tab. We need to store these values to use in the animation of the underline bar that indicates which tab is chosen.
    public static int recentTabTextWidth;
    public static int whatsHotTabTextWidth;
    public static int winnersTabTextWidth;
    
    //Fixed identifiers for views in the stack for later retrieval and mutation.
    public static final int VIDEO_VIEW_IDENTIFIER = 10001;
    public static final int VIDEO_VIEW_TOP_BAR_IDENTIFIER = 10002;
    public static final int VIDEO_VIEW_VIDEOVIEW_IDENTIFIER = 10003;
    public static final int BOTTOM_SCROLL_LOADING_BAR_IDENTIFIER = 10004;
    
    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        
        if(currentOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE || currentOrientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE)
        {
        	getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        	getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        }
        else
        {
        	getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		baseContext = this;
		baseActivity = this;
		
		/////////////////////////////////////
		//Init persistence mechanism
		ModelManager.getInstance().initialize(this.getApplicationContext());
		ImageManager.getInstance().initialize(this.getApplicationContext());
		/////////////////////////////////////
		
		//Turn off screen rotation in the beginning. We will only allow screen rotation during video playback in order to view it full-screen
		screenRotationAllowed = false;
		currentViewIsVideo = false;
		canLoadMoreVideoItemsNow = true;
		
		//Begin the task to load the server list, categories, and other resources from the network that will not change as the app is running.
		AppStateVariables.AbleToReachServer = true; //We will begin by assuming the server is reachable. This will be set to false during an http request that fails because of connectivity.
		new InitVarsFromServerTask().execute("");
		
		//Init display and ui
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //The initial orientation we will set as portrait. The device will no longer attempt to change the orientation automatically upon tilt. We will change the orientation manually using accelerometer data.
		currentOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
		Display display = getWindowManager().getDefaultDisplay();
		Point screenSize = new Point(); display.getSize(screenSize);
		screenWidth = screenSize.x; screenHeight = screenSize.y;
		AppSettings.initLayoutSettings(screenWidth, screenHeight); //Init the rest of the settings to allow the app to be drawn for the current screen size once we have the width and height of the screen.
		normalFont = Typeface.createFromAsset(getAssets(),"fonts/ag.ttf");
		boldFont = Typeface.createFromAsset(getAssets(),"fonts/agb.ttf");
		
		//NavDrawer setup
		///////////////////////////////////////////////////////////
        mPlanetTitles = getResources().getStringArray(R.array.planets_array);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerList = (ListView) findViewById(R.id.left_drawer);

        // set a custom shadow that overlays the main content when the drawer opens
        drawerLayout.setDrawerShadow(R.drawable.drawer_shadow_left, 0x03); //hex value for gravity left
        drawerLayout.setDrawerShadow(R.drawable.drawer_shadow_right, 0x05); //hex value for gravity right
        // set up the drawer's list view with items and click listener
        drawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list_item, mPlanetTitles));
        drawerList.setOnItemClickListener(new DrawerItemClickListener());

        //if (savedInstanceState == null)
        //{
            selectItem(0);
        //}
		//END NavDrawer setup
		
		layoutInflater = getLayoutInflater(); //Later, we will need to inflate layouts within fragments at a time different than fragment creation time. Therefore we will have no access to the inflator provided by the oncreate method.
		mainViewPager = (NonSwipeableViewPager)findViewById(R.id.pager); //Same as previous.
		pagerAdapter = new SimplePagerAdapter(getSupportFragmentManager());
		currentStackLevel = 1;
		viewPager = (ViewPager) mainViewPager;
		viewPager.setAdapter(pagerAdapter);
		viewPager.removeAllViews(); //In case we return to the app after it has gone into the background.
		pageList.clear();
		
		//This delegate allows us to listen for the first page to be added to our view pager. When the first page is added for the first time, we must populate it with the homepage layout.
		OnHierarchyChangeListener hierarchyChangeListener = new OnHierarchyChangeListener()
		{
			@Override
			public void onChildViewAdded(View parent, View child)
			{
				if(!firstViewLoaded)
				{
					populateViewInStackWithHomePage(0);
					firstViewLoaded = true;
				}
			}
			@Override
			public void onChildViewRemoved(View parent, View child)
			{

			}
		};
		viewPager.setOnHierarchyChangeListener(hierarchyChangeListener);
		
		OnPageChangeListener pageChangeListener = new OnPageChangeListener()
		{
			@Override
			public void onPageScrollStateChanged(int viewPagerScrollState)
			{
				if(viewPagerScrollState == ViewPager.SCROLL_STATE_SETTLING)
				{
					
				}
				if(viewPagerScrollState == ViewPager.SCROLL_STATE_IDLE)
				{
					if(waitingToStartVideo)
					{
						try
						{
							currentVideoContainer.addView(currentVideoView);
							currentVideoContainer.addView(currentVideoLoadingProgressBar);
							
							if (currentVideo.isExpired() || !currentVideo.isFullyFetchedOnce()) // <---- check if the video needs to be updated from server
							{
								new GetVideoDetailsTask(currentVideo, new iBlankDelegate()
								{
									@Override
									public void executeCallback() 
									{
										Log.d("VIDEO PATH", currentVideo.videoUrl);
										
										currentVideoView.setVideoPath(currentVideo.videoUrl);
										currentVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener()
								        {
											@Override
								            public void onPrepared(MediaPlayer mp)
								            {
												currentVideoContainer.setVisibility(View.VISIBLE);
												currentVideoLoadingProgressBar.setVisibility(ProgressBar.GONE);
								            }
								        });
										currentVideoView.start();
										waitingToStartVideo = false;
									}
								}).execute("");
							}
							else
							{
								Log.d("VIDEO PATH", currentVideo.videoUrl);
								
								currentVideoView.setVideoPath(currentVideo.videoUrl);
								currentVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener()
						        {
									@Override
						            public void onPrepared(MediaPlayer mp)
						            {
										currentVideoContainer.setVisibility(View.VISIBLE);
										currentVideoLoadingProgressBar.setVisibility(ProgressBar.GONE);
						            }
						        });
								currentVideoView.start();
								waitingToStartVideo = false;
							}
						}
						catch(Exception ex)
						{
							ex.printStackTrace();
							Log.d("ERROR LOADING VIDEO", ex.toString());
							currentVideoContainer.removeAllViews(); //We need to remove the VideoView object from the layout if it is in an error state. Otherwise it bleeds onto other fragments in the view pager. (!!for some unknown reason!!)
							currentVideoErrorTextView.setText("VIDEO UNAVAILABLE TO STREAM FROM SERVER. PLEASE TRY AGAIN LATER :(");// + ex.toString());
							currentVideoContainer.addView(currentVideoErrorTextView);
						}
					}
				}
			}
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2)
			{

			}
			@Override
			public void onPageSelected(int arg0)
			{

			}
		};
		viewPager.setOnPageChangeListener(pageChangeListener);
		
		//Here we set a custom scroller for the viewPager so we can control the speed at which the pages change.
		try
		{
            Field mScroller;
            mScroller = ViewPager.class.getDeclaredField("mScroller");
            mScroller.setAccessible(true);
            FixedSpeedScroller scroller = new FixedSpeedScroller(viewPager.getContext()); //, sInterpolator);
            mScroller.set(viewPager, scroller);
        }
		catch (Exception e)
		{
			e.printStackTrace();
        }
        
	}
	
	@Override
	protected void onResume() 
	{
	    super.onResume();
	    //Init sensor vars
	    sensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
	  	accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
	  	sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
	}
	 
	@Override
	protected void onPause()
	{
	    super.onPause();
	    sensorManager.unregisterListener(this);
	}
	
	/* The click listener for ListView in the navigation drawer */
    private class DrawerItemClickListener implements ListView.OnItemClickListener 
    {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
        {
            selectItem(position);
        }
    }
    
    private void selectItem(int position)
    {
        // update selected item and title, then close the drawer
        drawerList.setItemChecked(position, true);
        drawerLayout.closeDrawer(drawerList);
    }
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
	    if (keyCode == KeyEvent.KEYCODE_BACK)
	    {
	    	if(currentViewIsVideo && (currentOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE || currentOrientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE))
	    	{ 
	    		//For now, pressing the back button while the video is playing in full screen landscape will cause the orientation to change to portrait, and disallow further orientation changes for this view. The video will continue to play.
	    		//TODO - set a flag to cause the video to stop and the view to be popped when the orientation change completes.
	    		
	    		screenRotationAllowed = false;
	    		currentOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
	    		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	    		showOrHideVieoViewComponentsForFullScreen(false);
	    		return true; //Do not go on to pop the page.
	    	}
	    	else if(currentVideoView != null)
	    	{
	    		currentVideoView.stopPlayback();
	    	}
	    	
	    	if(currentStackLevel > 1)
	    	{
	    		popPage();
	    		return true;
	    	}
	    	else
	    	{
	    		finish();
	    		return false;
	    	}
	    }
	    else
	    {
	    	return false;
	    }
	}
	
	@Override
	public void onSensorChanged(SensorEvent event)
	{
	    if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
	    {
	    	float xAccel = event.values[0];
	    	float yAccel = event.values[1];
	    	
	    	if(yAccel < 5 && xAccel < -5 && currentOrientation != ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE && screenRotationAllowed)
	    	{
	    		currentOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
	    		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
	    		
	    		showOrHideVieoViewComponentsForFullScreen(true);
	    	}
	    	if(yAccel < 5 && xAccel > 5 && currentOrientation != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE && screenRotationAllowed)
	    	{
	    		currentOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
	    		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
	    		
	    		showOrHideVieoViewComponentsForFullScreen(true);
	    	}
	    	if(yAccel >= 5 && currentOrientation != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) // || !screenRotationAllowed)
	    	{
	    		currentOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
	    		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	    		
	    		showOrHideVieoViewComponentsForFullScreen(false);
	    	}
	    	
	    	//Log.d("accel x", Float.toString(event.values[0]));
	    	//Log.d("accel y", Float.toString(event.values[1]));
	    	//Log.d("accel z", Float.toString(event.values[2]));
	    }
	    
	}
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy)
	{

	}
	
	public static void pushPage()
	{
		currentStackLevel++;
		pagerAdapter.notifyDataSetChanged();
		viewPager.setCurrentItem(currentStackLevel-1, true); // -1 because currentStackLevel is one-based while position within the pager is zero-based.
	}
	public static void popPage()
	{
		//TODO make sure the view we are popping down to is not a video view before turning off screen rotation.
		screenRotationAllowed = false;
		currentViewIsVideo = false; //!! TODO - This is not always true. Need to determine whether to do this or not.
		
		currentStackLevel--;
		pagerAdapter.notifyDataSetChanged();
		viewPager.setCurrentItem(currentStackLevel-1, true); // -1 because currentStackLevel is one-based while position within the pager is zero-based.
	}

	@Override
    public boolean onCreateOptionsMenu(Menu menu) 
	{
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }
    
	public class SimplePagerAdapter extends FragmentStatePagerAdapter
	{
		public SimplePagerAdapter(FragmentManager fm)
		{
			super(fm);
		}
		@Override
		public Fragment getItem(int position)
		{
			Fragment fragment = new BlankFragment();
			Bundle args = new Bundle();
			args.putInt(BlankFragment.ARG_LEVEL, position);
			fragment.setArguments(args);
			return fragment;
		}
		@Override
		public int getCount()
		{
			return currentStackLevel + 1; //plus 1 is to make sure there is a blank fragment in front of the current fragment so we do not lose the animation when navigating to it.
		}
	}

	public static class BlankFragment extends Fragment 
	{
		public static final String ARG_LEVEL = "arg_level";

		public BlankFragment()
		{
		
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
		{
			int viewLevel = getArguments().getInt(ARG_LEVEL);
			if(pageList.size() <= viewLevel)
			{
				LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
				LinearLayout linearLayout = new LinearLayout(getActivity());
				//linearLayout.setBackgroundColor(Color.argb(255, 150, 50, 50));
				linearLayout.setOrientation(LinearLayout.VERTICAL);
				linearLayout.setLayoutParams(layoutParams);
				linearLayout.setTag(viewLevel);
				pageList.add(viewLevel, linearLayout);
			}
			
			if(container.getChildAt(viewLevel) != null) //This unreadable garbage prevents an exception that occurs when we try to add a view (from the arraylist) that already is a child of the viewPager. Stupidly, we must remove it and then reinsert it in the same place because there seems to be no option in onCreate to simply keep the current view and pass it back to the pager. It requires you to return a view to be inserted into the pager,
				((ViewGroup)pageList.get(viewLevel).getParent()).removeView(pageList.get(viewLevel)); //This code is horrendously ugly. If there is a better way to do this, let's find it.
			
			return pageList.get(viewLevel);
		}
	}
	
	public class FixedSpeedScroller extends Scroller
	{
	    private int mDuration = 800;

	    public FixedSpeedScroller(Context context)
	    {
	        super(context);
	    }
	    public FixedSpeedScroller(Context context, Interpolator interpolator)
	    {
	        super(context, interpolator);
	    }
	    public FixedSpeedScroller(Context context, Interpolator interpolator, boolean flywheel)
	    {
	        super(context, interpolator, flywheel);
	    }
	    @Override
	    public void startScroll(int startX, int startY, int dx, int dy, int duration)
	    {
	        //Ignore received duration, use fixed one instead
	        super.startScroll(startX, startY, dx, dy, mDuration);
	    }
	    @Override
	    public void startScroll(int startX, int startY, int dx, int dy)
	    {
	        //Ignore received duration, use fixed one instead
	        super.startScroll(startX, startY, dx, dy, mDuration);
	    }
	}
	
	
	
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//Methods to populate the fragments in the stack with particular types of pages.
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////
	private static void populateViewInStackWithHomePage(final int stackLevel)
	{
		//Log.d("populateViewInStackWithHomePage starting", "stackLevel: " + Integer.toString(stackLevel));
		
		
		ApiRequest request = new ApiRequest(ApiRequest.API_METHOD_VIDEO_SEARCH);
		request.setResultOffset(0);
		request.putCriteriaFilterValue("video_online", Integer.valueOf(1));
		//request.putCriteriaFilterValue("text_search", "metal");
		request.putCriteriaSortValue("upload_date", "desc");
		
		FetchListPersistenceManager fetchListManager = ModelManager.getInstance().getFetchListPersistenceManager();
		ObjectContext objectContext = ModelManager.getInstance().getDefaultObjectContext();

		recentVideosFetchList = fetchListManager.getFetchList(request, objectContext, AppSettings.VIDEO_LIST_EXPIRATION_TIME_MILLIES, AppSettings.videoListChunkSize);
		recentVideosFetchList.loadCount = 0;
		chosenTab = TAB_RECENT;
		
		
		
		
		//These lines are required at the beginning of all populate methods. We must find the next container fragment on the view stack to be populated with the subsequent logic. We must also remove all views that may still be in this container so they can be garbage collected.
		final LinearLayout nextViewRootLayout = (LinearLayout)viewPager.findViewWithTag(stackLevel);
		nextViewRootLayout.removeAllViews();
		
		final View homeLayoutView = layoutInflater.inflate(R.layout.home_layout, mainViewPager, false);
		final LinearLayout llHomeVideoListContainer = (LinearLayout) homeLayoutView.findViewById(R.id.llHomeVideoListContainer); //A pointer to this view will be passed to the loadVideoItem so that the generated video item will be added to this container view.
		final RelativeLayout btnOpenLeftDrawer = (RelativeLayout) homeLayoutView.findViewById(R.id.rlHomeThreeLinesContainer);
		final RelativeLayout btnOpenRightDrawer = (RelativeLayout) homeLayoutView.findViewById(R.id.rlHomeThreeDotsContainer);
		final TextView tvHomeTitle = (TextView) homeLayoutView.findViewById(R.id.tvHomeTitle);
		tvHomeTitle.setTypeface(normalFont);
		final RelativeLayout rlTopBar = (RelativeLayout) homeLayoutView.findViewById(R.id.rlHomeTopBar);
		final RelativeLayout rlTopBarFiller = (RelativeLayout) homeLayoutView.findViewById(R.id.rlTopBarFiller);
		final RelativeLayout rlTabBar = (RelativeLayout) homeLayoutView.findViewById(R.id.rlHomeTabBar);
		final RelativeLayout rlRecentTab = (RelativeLayout) homeLayoutView.findViewById(R.id.rlHomeRecentTab);
		final RelativeLayout rlWhatsHotTab = (RelativeLayout) homeLayoutView.findViewById(R.id.rlWhatsHotTab);
		final RelativeLayout rlWinnersTab = (RelativeLayout) homeLayoutView.findViewById(R.id.rlWinnersTab);
		final TextView tvHomeRecentText = (TextView) homeLayoutView.findViewById(R.id.tvHomeRecentText);
		final TextView tvHomeWhatsHotText = (TextView) homeLayoutView.findViewById(R.id.tvHomeWhatsHotText);
		final TextView tvHomeWinnersText = (TextView) homeLayoutView.findViewById(R.id.tvHomeWinnersText);
		tvHomeRecentText.setTypeface(boldFont);
		tvHomeWhatsHotText.setTypeface(boldFont);
		tvHomeWinnersText.setTypeface(boldFont);
		final ProgressBar pbHomeInitializing = (ProgressBar) homeLayoutView.findViewById(R.id.pbHomeInitializing);
		rlTabUnderline = (RelativeLayout) homeLayoutView.findViewById(R.id.rlTabUnderline);
		
		/////////////////////////
		/////////////////////////
		final CTVScrollView sv = (CTVScrollView) homeLayoutView.findViewById(R.id.svHomeVideoListContainer);
		sv.setScrolledToTheBottomDelegate(new iBlankDelegate()
		{
			@Override
			public void executeCallback()
			{
				Log.d("CTV - scroller", "CTVScrollView: Bottom has been reached on the home page scroll view." );
				addChunkOfVideoItemsToView(llHomeVideoListContainer, null, stackLevel);
			}
		});
		/////////////////////////
		/////////////////////////
		
		//Adjust this view to the screen size of the current device.
		rlTopBar.getLayoutParams().height = AppSettings.homeTopBarHeight;
		rlTabBar.getLayoutParams().height = AppSettings.homeTabBarHeight;
		rlRecentTab.getLayoutParams().width = (int)(screenWidth/3) - 1;
		rlWhatsHotTab.getLayoutParams().width = (int)(screenWidth/3);
		rlWinnersTab.getLayoutParams().width = (int)(screenWidth/3) - 1;
		rlTopBarFiller.getLayoutParams().height = AppSettings.homeTopBarHeight + AppSettings.homeTabBarHeight;
		tvHomeTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, AppSettings.homeTitleTextHeight);
		tvHomeTitle.setPadding(0, AppSettings.homeTitleTextTopMargin, 0, 0);
		tvHomeRecentText.setTextSize(TypedValue.COMPLEX_UNIT_PX, AppSettings.homeTabTextHeight);
		tvHomeWhatsHotText.setTextSize(TypedValue.COMPLEX_UNIT_PX, AppSettings.homeTabTextHeight);
		tvHomeWinnersText.setTextSize(TypedValue.COMPLEX_UNIT_PX, AppSettings.homeTabTextHeight);
		recentTabTextWidth = Utils.getTextWidthOfTextviewInPixels(tvHomeRecentText);
		whatsHotTabTextWidth = Utils.getTextWidthOfTextviewInPixels(tvHomeWhatsHotText);
		winnersTabTextWidth = Utils.getTextWidthOfTextviewInPixels(tvHomeWinnersText);
		//We need the distances from the right and left of the screen to the bottom of the text of each tab. These values are used to know how to move the underline when a tab is selected.
		final int distFromLeftToRecent = (int)(((screenWidth/3) - 1 - recentTabTextWidth)/2);
		final int distFromRightToRecent = screenWidth - (distFromLeftToRecent + recentTabTextWidth);
		final int distFromLeftToWhatsHot = (int)(screenWidth/3) - 1 + (int)(((screenWidth/3) - whatsHotTabTextWidth)/2);
		final int distFromRightToWhatsHot = screenWidth - (distFromLeftToWhatsHot + whatsHotTabTextWidth);
		final int distFromLeftToWinners = (int)(screenWidth/3) - 1 + (int)(screenWidth/3) + (int)(((screenWidth/3) - 1 - winnersTabTextWidth)/2);
		final int distFromRightToWinners = screenWidth - (distFromLeftToWinners + winnersTabTextWidth);
		
		//Begin with the position of the underline under the RECENT tab
		tabUnderlineCurrentX = distFromLeftToRecent;
		tabUnderlineCurrentWidth = recentTabTextWidth;
		tvHomeWhatsHotText.setTypeface(normalFont);
		tvHomeWinnersText.setTypeface(normalFont);
		
		//TODO - move this to a seperate function:
		//Set margins for tab underline for the "recent" tab being selected.
		AppSettings.tabUnderlineLeftMargin = (int)(((screenWidth/3)-recentTabTextWidth)/2);
		AppSettings.tabUnderlineRightMargin = screenWidth - ((int)(((screenWidth/3)-recentTabTextWidth)/2) + recentTabTextWidth);
		
		((RelativeLayout.LayoutParams)rlTabUnderline.getLayoutParams()).setMargins(AppSettings.tabUnderlineLeftMargin, AppSettings.tabUnderlineTopMargin, AppSettings.tabUnderlineRightMargin, AppSettings.tabUnderlineBottomMargin);
		
		btnOpenLeftDrawer.getLayoutParams().width = AppSettings.topBarMenuButtonSideLength; btnOpenLeftDrawer.getLayoutParams().height = AppSettings.topBarMenuButtonSideLength;
		btnOpenRightDrawer.getLayoutParams().width = AppSettings.topBarMenuButtonSideLength; btnOpenRightDrawer.getLayoutParams().height = AppSettings.topBarMenuButtonSideLength;
		
		btnOpenLeftDrawer.setOnTouchListener(new OnTouchListener()
		{
			@Override
			public boolean onTouch(View v, MotionEvent event)
			{
				if(event.getAction() == MotionEvent.ACTION_UP)
				{
					drawerLayout.openDrawer(0x03); //hex code for left gravity
				}
				return true;
			}
		});
		btnOpenRightDrawer.setOnTouchListener(new OnTouchListener()
		{
			@Override
			public boolean onTouch(View v, MotionEvent event)
			{
				if(event.getAction() == MotionEvent.ACTION_UP)
				{
					drawerLayout.openDrawer(0x05); //hex code for right gravity
				}
				return true;
			}
		});
		tvHomeTitle.setOnTouchListener(new OnTouchListener()
		{
			@Override
			public boolean onTouch(View v, MotionEvent event)
			{
				return true;
			}
		});
		rlRecentTab.setOnTouchListener(new OnTouchListener()
		{
			@Override
			public boolean onTouch(View v, MotionEvent event)
			{
				if(event.getAction() == MotionEvent.ACTION_UP)
				{
					//animate the underline bar
					AnimationSet animationSet = new AnimationSet(true);
					TranslateAnimation translateAnimation = new TranslateAnimation(Animation.ABSOLUTE, 0, Animation.ABSOLUTE, distFromLeftToRecent - tabUnderlineCurrentX, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0);
					ScaleAnimation scaleAnimation = new ScaleAnimation(1, (float)recentTabTextWidth/tabUnderlineCurrentWidth, 1, 1, Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0);
					translateAnimation.setDuration(200);
					scaleAnimation.setDuration(200);
					animationSet.addAnimation(scaleAnimation);
					animationSet.addAnimation(translateAnimation);
					animationSet.setFillEnabled(true);
					animationSet.setFillAfter(true);
					animationSet.setAnimationListener(new AnimationListener()
					{
						@Override
						public void onAnimationEnd(Animation animation)
						{
							((RelativeLayout.LayoutParams)rlTabUnderline.getLayoutParams()).setMargins(distFromLeftToRecent, AppSettings.tabUnderlineTopMargin, distFromRightToRecent, AppSettings.tabUnderlineBottomMargin);
							rlTabUnderline.setAnimation(null);
							
							rlTabUnderline.invalidate();
							tabUnderlineCurrentX = distFromLeftToRecent;
							tabUnderlineCurrentWidth = recentTabTextWidth;
							
							tvHomeRecentText.setTypeface(boldFont); //IMPORTANT NOTE!!!!!! These calls to change fonts are actually responsible for the redrawing of the screen in a timely manner!! THIS IS STUPID, SO IF THERE IS ANOTHER WAY TO ACCOMPLISH THE REDRAW IMMDEDIATELY WITHIN THIS ATROCIOUS ANDROID ANIMATION FRAMEWORK, WE SHOULD TRY TO FIND IT.
							tvHomeWhatsHotText.setTypeface(normalFont);
							tvHomeWinnersText.setTypeface(normalFont);
						}
						@Override
						public void onAnimationRepeat(Animation animation) {}
						@Override
						public void onAnimationStart(Animation animation) {}
					});
					rlTabUnderline.startAnimation(animationSet);
				}
				return true;
			}
		});
		rlWhatsHotTab.setOnTouchListener(new OnTouchListener()
		{
			@Override
			public boolean onTouch(View v, MotionEvent event)
			{
				if(event.getAction() == MotionEvent.ACTION_UP)
				{
					//animate the underline bar
					AnimationSet animationSet = new AnimationSet(true);
					TranslateAnimation translateAnimation = new TranslateAnimation(Animation.ABSOLUTE, 0, Animation.ABSOLUTE, distFromLeftToWhatsHot - tabUnderlineCurrentX, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0);
					ScaleAnimation scaleAnimation = new ScaleAnimation(1, (float)whatsHotTabTextWidth/tabUnderlineCurrentWidth, 1, 1, Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0);
					translateAnimation.setDuration(200);
					scaleAnimation.setDuration(200);
					
					animationSet.addAnimation(scaleAnimation);
					animationSet.addAnimation(translateAnimation);
					animationSet.setFillEnabled(true);
					animationSet.setFillAfter(true);
					animationSet.setAnimationListener(new AnimationListener()
					{
						@Override
						public void onAnimationEnd(Animation animation)
						{
							((RelativeLayout.LayoutParams)rlTabUnderline.getLayoutParams()).setMargins(distFromLeftToWhatsHot, AppSettings.tabUnderlineTopMargin, distFromRightToWhatsHot, AppSettings.tabUnderlineBottomMargin);
							rlTabUnderline.setAnimation(null);
							
							rlTabUnderline.invalidate();
							tabUnderlineCurrentX = distFromLeftToWhatsHot;
							tabUnderlineCurrentWidth = whatsHotTabTextWidth;
							
							tvHomeRecentText.setTypeface(normalFont); //IMPORTANT NOTE!!!!!! These calls to change fonts are actually responsible for the redrawing of the screen in a timely manner!! THIS IS STUPID, SO IF THERE IS ANOTHER WAY TO ACCOMPLISH THE REDRAW IMMDEDIATELY WITHIN THIS ATROCIOUS ANDROID ANIMATION FRAMEWORK, WE SHOULD TRY TO FIND IT.
							tvHomeWhatsHotText.setTypeface(boldFont);
							tvHomeWinnersText.setTypeface(normalFont);
						}
						@Override
						public void onAnimationRepeat(Animation animation) {}
						@Override
						public void onAnimationStart(Animation animation) {}
					});
					
					rlTabUnderline.startAnimation(animationSet);
				}
				return true;
			}
		});
		rlWinnersTab.setOnTouchListener(new OnTouchListener()
		{
			@Override
			public boolean onTouch(View v, MotionEvent event)
			{
				if(event.getAction() == MotionEvent.ACTION_UP)
				{
					//animate the underline bar
					AnimationSet animationSet = new AnimationSet(true);
					TranslateAnimation translateAnimation = new TranslateAnimation(Animation.ABSOLUTE, 0, Animation.ABSOLUTE, distFromLeftToWinners - tabUnderlineCurrentX, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0);
					ScaleAnimation scaleAnimation = new ScaleAnimation(1, (float)winnersTabTextWidth/tabUnderlineCurrentWidth, 1, 1, Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0);
					translateAnimation.setDuration(200);
					scaleAnimation.setDuration(200);
					
					animationSet.addAnimation(scaleAnimation);
					animationSet.addAnimation(translateAnimation);
					animationSet.setFillEnabled(true);
					animationSet.setFillAfter(true);
					animationSet.setAnimationListener(new AnimationListener()
					{
						@Override
						public void onAnimationEnd(Animation animation)
						{
							((RelativeLayout.LayoutParams)rlTabUnderline.getLayoutParams()).setMargins(distFromLeftToWinners, AppSettings.tabUnderlineTopMargin, distFromRightToWinners, AppSettings.tabUnderlineBottomMargin);
							rlTabUnderline.setAnimation(null);
							
							rlTabUnderline.invalidate();
							tabUnderlineCurrentX = distFromLeftToWinners;
							tabUnderlineCurrentWidth = winnersTabTextWidth;
							
							tvHomeRecentText.setTypeface(normalFont); //IMPORTANT NOTE!!!!!! These calls to change fonts are actually responsible for the redrawing of the screen in a timely manner!! THIS IS STUPID, SO IF THERE IS ANOTHER WAY TO ACCOMPLISH THE REDRAW IMMDEDIATELY WITHIN THIS ATROCIOUS ANDROID ANIMATION FRAMEWORK, WE SHOULD TRY TO FIND IT.
							tvHomeWhatsHotText.setTypeface(normalFont);
							tvHomeWinnersText.setTypeface(boldFont);
						}
						@Override
						public void onAnimationRepeat(Animation animation) {}
						@Override
						public void onAnimationStart(Animation animation) {}
					});
					
					rlTabUnderline.startAnimation(animationSet);
				}
				return true;
			}
		});
		
		//nextViewRootLayout is the container view in the view stack in the view pager. Here we connect the ui content we are creating in this function to that view on the stack.
		nextViewRootLayout.addView(homeLayoutView);
		
		addChunkOfVideoItemsToView(llHomeVideoListContainer, pbHomeInitializing, stackLevel);		
	}
	
	
	private static void addChunkOfVideoItemsToView(final LinearLayout llVideoItemContainer, final ProgressBar pbToRemove, final int stackLevel)
	{
		//Asynchronously populate views on this page with data from server.
		final ArrayList<CTVVideo> RecentVideoList = new ArrayList<CTVVideo>();
		
		new VideoSearchTask(RecentVideoList, recentVideosFetchList, new iBlankDelegate ()
		{
			@Override
			public void executeCallback() //All the logic for loading the images and links to the videos from the search is in this callback. This is executed as soon as the list of results is returned from the server.
			{
				if(pbToRemove != null) pbToRemove.setVisibility(View.GONE); //Remove the progress bar that was showing in place of the videos to be loaded.
				
				for(CTVVideo ctvVideo : RecentVideoList)
				{
					generateAndLoadVideoItem(llVideoItemContainer, ctvVideo, stackLevel);
				}
				//Remove current loading bar if one was already at the bottom.
				RelativeLayout oldLoadingBar = (RelativeLayout)llVideoItemContainer.findViewById(BOTTOM_SCROLL_LOADING_BAR_IDENTIFIER);
				if(oldLoadingBar != null)
				{
					llVideoItemContainer.removeView(oldLoadingBar);
					oldLoadingBar = null;
				}
				
				//Add loading bar the the bottom to indicate more items are being loaded.
				RelativeLayout rlBottomScrollLoadingBar = new RelativeLayout(baseContext);
				rlBottomScrollLoadingBar.setId(BOTTOM_SCROLL_LOADING_BAR_IDENTIFIER);
				rlBottomScrollLoadingBar.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, (int)(AppSettings.videoItemHeight/3)));
				llVideoItemContainer.addView(rlBottomScrollLoadingBar);
				ProgressBar pbMoreLoading = new ProgressBar(baseContext);
				RelativeLayout.LayoutParams pbMoreLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
				pbMoreLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
				//pbMoreLoading.setPadding(0, 10, 0, 10);
				pbMoreLoading.setLayoutParams(pbMoreLayoutParams);
				rlBottomScrollLoadingBar.addView(pbMoreLoading);
			}
		}).execute("");
		
	}
	
	private static void generateAndLoadVideoItem(LinearLayout llHomeVideoListContainer, CTVVideo ctvVideo, final int stackLevel)
	{
		RelativeLayout rlVideoItem = new RelativeLayout(baseContext);
		rlVideoItem.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, AppSettings.videoItemHeight));
		rlVideoItem.setBackgroundColor(0XFFBBBBBB);
		llHomeVideoListContainer.addView(rlVideoItem);
		
		ProgressBar imageLoadingProgressBar = new ProgressBar(baseContext);
		RelativeLayout.LayoutParams imageLoadingProgressBarLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		imageLoadingProgressBarLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
		imageLoadingProgressBar.setPadding(0, AppSettings.imageLoadingProgressBarTopPadding, 0, 0);
		imageLoadingProgressBar.setLayoutParams(imageLoadingProgressBarLayoutParams);
		rlVideoItem.addView(imageLoadingProgressBar);
		
		ImageView ivVideoPreviewImage = new ImageView(baseContext);
		ivVideoPreviewImage.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		ivVideoPreviewImage.setVisibility(View.INVISIBLE);

		ApiImageRequest imageRequest = new ApiImageRequest(ctvVideo.previewImage);
		imageRequest.setTransform(ApiImageRequest.TRANSFORM.BEST_CROP_CENTER);
		imageRequest.setWidth(screenWidth);
		imageRequest.setHeight(AppSettings.videoItemHeight);
		
		////////Fire the asynchronous task to load the image into the container
		new DownloadImageTask(ivVideoPreviewImage, imageLoadingProgressBar).execute(imageRequest);
		///////////////////////////////////////////////////////////////////////
		
		rlVideoItem.addView(ivVideoPreviewImage);
		
		//add a partially transparent bar to the bottom of each video preview panel on which info about the video will be displayed
		RelativeLayout rlBottomPanel = new RelativeLayout(baseContext);
		RelativeLayout.LayoutParams bottomPanelLayoutParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, AppSettings.videoItemBottomPanelHeight);
		bottomPanelLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		rlBottomPanel.setLayoutParams(bottomPanelLayoutParams);
		Drawable drBottomBlackGradient = baseContext.getResources().getDrawable(R.drawable.bottom_black_gradient);
		rlBottomPanel.setBackgroundDrawable(drBottomBlackGradient);
		rlVideoItem.addView(rlBottomPanel);
		
		//add the title of the video to the bottom bar
		TextView tvVideoItemTitle = new TextView(baseContext);
		tvVideoItemTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, AppSettings.videoItemTitleTextHeight);
		tvVideoItemTitle.setTextColor(baseContext.getResources().getColor(R.color.videoTumbnailOverlayTextColor));
		tvVideoItemTitle.setTypeface(boldFont);
		tvVideoItemTitle.setText(ctvVideo.titleText);
		tvVideoItemTitle.setSingleLine(true);
		tvVideoItemTitle.setEllipsize(TextUtils.TruncateAt.END);
		RelativeLayout.LayoutParams bottomLeftLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		bottomLeftLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		bottomLeftLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		tvVideoItemTitle.setLayoutParams(bottomLeftLayoutParams);
		tvVideoItemTitle.setPadding(AppSettings.videoItemTitleTextSidePadding, 0, AppSettings.videoItemTitleTextSidePadding, AppSettings.videoItemTitleTextPaddingBottom);
		rlBottomPanel.addView(tvVideoItemTitle);
		
		//add stars for the rating of this video
		for(int i=0;i<5;i++)
		{
			ImageView ivRatingStar = new ImageView(baseContext);
			ivRatingStar.setImageResource(R.drawable.rating_star);
			ivRatingStar.setPadding(AppSettings.ratingStarsLeftPadding + AppSettings.ratingStarWidth*i, 0, 0, AppSettings.ratingStarsBottomPadding);
			RelativeLayout.LayoutParams ivParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
			ivParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
			ivParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
			ivRatingStar.setLayoutParams(ivParams);
			if(ctvVideo.rating<i+1)
			{
				ivRatingStar.setAlpha(0.4f);
			}
			rlBottomPanel.addView(ivRatingStar);
			if(ctvVideo.rating<i+1 && ctvVideo.rating>i) //draw a partial star
			{
				double starPercentage = ctvVideo.rating - i;
				BitmapFactory.Options o = new BitmapFactory.Options();
				Bitmap bmp = BitmapFactory.decodeResource(baseContext.getResources(), R.drawable.rating_star, o);
				int starImageWidth = bmp.getWidth();
				int starImageHeight = bmp.getHeight();
				Bitmap bmpPartialStar = Bitmap.createBitmap(bmp, 0, 0, (int)(starImageWidth*starPercentage), starImageHeight);
				ImageView ivPartialStar = new ImageView(baseContext);
				ivPartialStar.setImageBitmap(bmpPartialStar);
				RelativeLayout.LayoutParams ivPartialStarParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
				ivPartialStarParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
				ivPartialStarParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
				ivPartialStar.setLayoutParams(ivPartialStarParams);
				ivPartialStar.setPadding(AppSettings.ratingStarsLeftPadding + AppSettings.ratingStarWidth*i, 0, 0, AppSettings.ratingStarsBottomPadding);
				rlBottomPanel.addView(ivPartialStar);
			}
		}
		
		//add a label to show the categories this video belongs to
		TextView tvCategoryList = new TextView(baseContext);
		RelativeLayout.LayoutParams catParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
		catParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		catParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		tvCategoryList.setLayoutParams(catParams);
		tvCategoryList.setTextSize(TypedValue.COMPLEX_UNIT_PX, AppSettings.videoItemTitleTextHeight);
		tvCategoryList.setTextColor(baseContext.getResources().getColor(R.color.videoTumbnailOverlayTextColor));
		tvCategoryList.setTypeface(normalFont);
		tvCategoryList.setText(ctvVideo.categoriesConcatenated);
		tvCategoryList.setSingleLine(true);
		tvCategoryList.setEllipsize(TextUtils.TruncateAt.END);
		tvCategoryList.setPadding(AppSettings.videoItemCategoryTextLeftPadding,0,AppSettings.videoItemCategoryTextRightPadding,AppSettings.videoItemCategoryTextBottomPadding);
		rlBottomPanel.addView(tvCategoryList);
		
		//add a label to display the number of views 
		TextView tvVideoViews = new TextView(baseContext);
		tvVideoViews.setTextSize(TypedValue.COMPLEX_UNIT_PX, AppSettings.videoItemTitleTextHeight);
		tvVideoViews.setTextColor(baseContext.getResources().getColor(R.color.videoTumbnailOverlayTextColor));
		tvVideoViews.setTypeface(normalFont);
		tvVideoViews.setText(Integer.toString(ctvVideo.views) + " views");
		tvVideoViews.setSingleLine(true);
		tvVideoViews.setEllipsize(TextUtils.TruncateAt.END);
		RelativeLayout.LayoutParams videoViewsLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		videoViewsLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		videoViewsLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		tvVideoViews.setLayoutParams(videoViewsLayoutParams);
		tvVideoViews.setPadding(0, 0, AppSettings.videoViewsLabelRightPadding, AppSettings.videoViewsLabelBottomPadding);
		rlBottomPanel.addView(tvVideoViews);
		
		final CTVVideo finalVideo = ctvVideo; //we need a final reference to the video in order to pass it to the anonymous delegate.
		rlVideoItem.setOnTouchListener(new OnTouchListener()
		{
			@Override
			public boolean onTouch(View v, MotionEvent event)
			{
				if(event.getAction() == MotionEvent.ACTION_UP)
				{
					populateViewInStackWithVideoViewingPage(stackLevel + 1, finalVideo);
					pushPage();
				}
				return true;
			}
		});
	}
	
	public static void populateViewInStackWithVideoViewingPage(final int stackLevel, final CTVVideo ctvVideo)
	{
		//Allow screen rotation for full-screen viewing
		screenRotationAllowed = true;
		currentViewIsVideo = true;
		
		LinearLayout nextViewRootLayout = (LinearLayout)viewPager.findViewWithTag(stackLevel);
		nextViewRootLayout.removeAllViews();
		
		final View videoLayoutView = layoutInflater.inflate(R.layout.video_layout, mainViewPager, false); videoLayoutView.setId(VIDEO_VIEW_IDENTIFIER);
		final TextView tvVideoTitle = (TextView) videoLayoutView.findViewById(R.id.tvVideoTitle);
		tvVideoTitle.setTypeface(normalFont);
		final RelativeLayout rlTopBar = (RelativeLayout) videoLayoutView.findViewById(R.id.rlVideoTopBar); rlTopBar.setId(VIDEO_VIEW_TOP_BAR_IDENTIFIER);
		final RelativeLayout rlVideoContainer = (RelativeLayout) videoLayoutView.findViewById(R.id.rlVideoContainer);
		final RelativeLayout btnVideoBackButton = (RelativeLayout) videoLayoutView.findViewById(R.id.rlVideoBackButton);
		btnVideoBackButton.setOnTouchListener(new OnTouchListener()
		{
			@Override
			public boolean onTouch(View v, MotionEvent event)
			{
				if(event.getAction() == MotionEvent.ACTION_UP)
				{
					if(currentVideoView != null)
			    	{
			    		currentVideoView.stopPlayback();
			    	}
					popPage();
				}
				return true;
			}
		});
		final ProgressBar pbVideoLoading = new ProgressBar(baseContext);
		RelativeLayout.LayoutParams progressBarLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		progressBarLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
		pbVideoLoading.setLayoutParams(progressBarLayoutParams);
		final TextView tvVideoLoadingError = new TextView(baseContext); //(TextView) videoLayoutView.findViewById(R.id.tvVideoLoadingError);
		tvVideoLoadingError.setText("Error loading video");
		tvVideoLoadingError.setTextColor(0xFFFF0000);
		//final RelativeLayout rlVideoProgressBarContainer = (RelativeLayout) videoLayoutView.findViewById(R.id.rlVideoProgressBarContainer);
		
		//Adjust this view to the screen size of the current device.
		rlTopBar.getLayoutParams().height = AppSettings.videoTopBarHeight;
		tvVideoTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, AppSettings.videoTitleTextHeight);
		tvVideoTitle.setPadding(0, AppSettings.videoTitleTextTopMargin, 0, 0);
		
		final VideoView videoView = new VideoView(baseContext);
		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
		layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT,-1);
		videoView.setLayoutParams(layoutParams);
		videoView.setId(VIDEO_VIEW_VIDEOVIEW_IDENTIFIER);
		videoView.setMediaController(new MediaController(baseContext));
		
		//////////////////////////////////////////////
		//videoView.setVideoPath(ctvVideo.videoUrl);
		//////////////////////////////////////////////
		
		//videoView.setVisibility(VideoView.INVISIBLE);
		videoView.setOnErrorListener(new OnErrorListener ()
		{
			@Override
			public boolean onError(MediaPlayer mp, int what, int extra)
			{
				Log.d("VIDEO VIEW ERROR CAUGHT", "VIDEO VIEW ERROR CAUGHT");
				currentVideoView.stopPlayback();
				currentVideoContainer.removeAllViews(); //We need to remove the VideoView object from the layout if it is in an error state. Otherwise it bleeds onto other fragments in the view pager. (!!for some unknown reason!!)
				currentVideoContainer.addView(currentVideoErrorTextView);
				return true; //Don't let the system handle this error.
			}
		});
		
		currentVideoView = videoView; //set the reference of the video to be loaded into the container when scrolling is complete.
		currentVideoContainer = rlVideoContainer; //set the reference of the container to load the current video into when scrolling is complete.
		waitingToStartVideo = true; //alert the view pager scroll listener to load the video when scrolling is complete 
		currentVideoLoadingProgressBar = pbVideoLoading;
		currentVideoErrorTextView = tvVideoLoadingError;
		currentVideo = ctvVideo;
		
		//Log.d("VIDEO URL", ctvVideo.videoUrl);
		
		//nextViewRootLayout is the container view in the view stack in the view pager. Here we connect the ui content we are creating in this function to that view on the stack.
		nextViewRootLayout.addView(videoLayoutView);
	}
	
	public static void showOrHideVieoViewComponentsForFullScreen(boolean hide)
	{
		LinearLayout currentViewRootLayout = (LinearLayout)viewPager.findViewWithTag(currentStackLevel-1);
		if(currentViewRootLayout != null)
		{
			View videoLayoutView = currentViewRootLayout.findViewById(VIDEO_VIEW_IDENTIFIER);
			if(videoLayoutView != null)
			{
				RelativeLayout rlTopBar = (RelativeLayout)videoLayoutView.findViewById(VIDEO_VIEW_TOP_BAR_IDENTIFIER);
				if(rlTopBar != null)
				{
					if(hide)
						rlTopBar.setVisibility(View.GONE);
					else
						rlTopBar.setVisibility(View.VISIBLE);
				}
				
			}
		}
	}
	
	
	
//	public static void populateViewInStackWithDummy2(final int stackLevel)
//	{
//		LinearLayout nextViewRootLayout = (LinearLayout)viewPager.findViewWithTag(stackLevel);
//		nextViewRootLayout.removeAllViews();
//		View dummy2LayoutView = layoutInflater.inflate(R.layout.dummy_layout_2, mainViewPager, false);
//		Button btnGoto3 = (Button) dummy2LayoutView.findViewById(R.id.DL2_btnGoto3);
//		Button btnBackDL2 = (Button) dummy2LayoutView.findViewById(R.id.btnBackDL2);
//		btnGoto3.setOnTouchListener(new OnTouchListener()
//		{
//			@Override
//			public boolean onTouch(View v, MotionEvent event) 
//			{
//				if(event.getAction() == MotionEvent.ACTION_UP)
//				{
//					populateViewInStackWithDummy3(stackLevel + 1);
//					pushPage();
//				}
//				return true;
//			}
//		});
//		btnBackDL2.setOnTouchListener(new OnTouchListener()
//		{
//			@Override
//			public boolean onTouch(View v, MotionEvent event)
//			{
//				if(event.getAction() == MotionEvent.ACTION_UP)
//				{
//					popPage();
//					v.setEnabled(false);
//				}
//				return true;
//			}
//		});
//		
//		nextViewRootLayout.addView(dummy2LayoutView);
//	}
//	public static void populateViewInStackWithDummy3(final int stackLevel)
//	{
//		LinearLayout nextViewRootLayout = (LinearLayout)viewPager.findViewWithTag(stackLevel);
//		nextViewRootLayout.removeAllViews();
//		View dummy3LayoutView = layoutInflater.inflate(R.layout.dummy_layout_3, mainViewPager, false);
//		Button btnBackDL3 = (Button) dummy3LayoutView.findViewById(R.id.btnBackDL3);
//		btnBackDL3.setOnTouchListener(new OnTouchListener()
//		{
//			@Override
//			public boolean onTouch(View v, MotionEvent event) 
//			{
//				if(event.getAction() == MotionEvent.ACTION_UP)
//				{
//					popPage();
//					v.setEnabled(false);
//				}
//				return true;
//			}
//		});
//		
//		nextViewRootLayout.addView(dummy3LayoutView);
//	}
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//END : Methods to populate the fragments in the stack with particular types of pages.
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
///////////Test functions
	
	private void testApiParser()
	{
		ApiRequest request = new ApiRequest(ApiRequest.API_METHOD_VIDEO_SEARCH);
		request.putCriteriaFilterValue("video_online", 1);
		request.putCriteriaSortValue("upload_date", "desc");
		request.setResultLimit(20);
		request.setResultOffset(0);
		
		ApiManager.getInstance().performAsyncApiRequest(request, new ApiAsyncConnectionCompletionDelegate() {
			public void onCompletion(ApiResponse apiResponse, int connectionKey)
			{
				if (apiResponse.getError() == ApiError.OK)
				{
					// Get the defautl object context
					ObjectContext objectContext = ModelManager.getInstance().getDefaultObjectContext();
					
					// Parse the ApiResponse into the object context.
					// This method will retrieve persisted objects or create new ones, if needed,
					// and update their content by parsing the ApiResponse's payload.
					ModelEntity[] objects = ApiParser.parseApiResponse(apiResponse, objectContext); 
					
					for (int i=0; i<objects.length; ++i)
					{
						CTVVideo video = (CTVVideo)objects[i];
						Log.d("CTV", "API PARSER [" + i + "]: " + video.toString());
					}
					
					// Finally, if we want to save the changes, we must save the object context.
					// Uncomment the line to perform the save!
					//objectContext.save();
				}
			}
		});
	}
	
/*
	private void testPersistence()
	{
		ObjectContext context = ModelManager.getInstance().getDefaultObjectContext();
		
		User user = (User)context.getObjectForKey("1234");
		
		Log.d("CTV", "Persistent User: " + user);
		Log.d("CTV", "ObjectContext: " + context);
		
		if (user == null)
		{
			Log.d("CTV", "Any persist user found!");
			user = new User("1234");
			user.name = "Joan";
			
			context.insertObject(user);
			context.save();
			
			Log.d("CTV", "ObjectContext: " + context + " (HAS CHANGES: " + context.hasChanges() + ")");
		}
		
		user.deleteObjectFromContext();
		context.save();
	}
	
	private void testConnectionAndPersistence()
	{
		ObjectContext context = ModelManager.getInstance().getDefaultObjectContext();
		
		BaseObject[] allVideos = context.getObjectsOfType(User.class.getSimpleName());
		
		Log.d("CTV", "All Persisted users (" + allVideos.length + ") : ");
		for (BaseObject bo : allVideos)
		{
			Log.d("CTV", "Object: " + bo);
		}

		// Creating ApiRequest to get list of videos
		ApiRequest request = new ApiRequest(ApiRequest.API_METHOD_USER_NAME_SEARCH);
		request.putCriteriaFilterValue("prefix", "Jo");
		request.putCriteriaSortValue("user_key_skip", new JSONArray());
		request.setResultOffset(0);
		request.setResultLimit(10);
		
		// Performing the request
		ApiManager.getInstance().performApiRequest(request, new ApiConnectionCompletionDelegate() {
			@Override
			public void onCompletion(ApiResponse apiResponse, int connectionKey)
			{
				ObjectContext context = ModelManager.getInstance().getDefaultObjectContext();
				
				JSONObject payload = apiResponse.getPayload();
				
				try 
				{
					JSONArray resultSet = payload.getJSONArray("result_set");
					Log.d("CTV", "Result set count: " + resultSet.length());
					
					for (int index = 0; index < resultSet.length(); index++) 
					{
						JSONObject jsonObject = resultSet.getJSONObject(index);
												
						String userKey = User.parseKey(jsonObject);
												
						User user = (User)context.getObjectForKey(userKey);
						
						if (user == null)
						{
							user = new User(userKey);
							context.insertObject(user);
						}
						
						user.parse(jsonObject);
					}
				} 
				catch (JSONException e) 
				{
					Log.d("CTV", "Exception!");
					e.printStackTrace();
				}
				
				Log.d("CTV","Context after fetch: " + context);
				context.save();
			}
		});
	}
*/
	
}
