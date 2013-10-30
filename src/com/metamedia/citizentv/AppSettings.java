package com.metamedia.citizentv;

public class AppSettings 
{
	//Settings that do not depend on device-specific factors
	public static final int VIDEO_LIST_EXPIRATION_TIME_MILLIES = 120000;
	

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////// VIDEO LIST VIEW
	public static int homeTopBarHeight;
	public static int homeTabBarHeight;
	public static int homeTitleTextHeight;
	public static int homeTitleTextTopMargin;
	public static int homeTabContainierHeight; //This height will apply to all three tabs.
	public static int homeTabRecentContainerWidth;
	public static int homeTabTextHeight;
	public static int videoItemHeight;
	public static int videoItemBottomPanelHeight;
	public static int videoItemTitleTextHeight;
	public static int videoItemTitleTextPaddingBottom;
	public static int videoItemTitleTextSidePadding;
	public static int videoItemCategoryTextBottomPadding;
	public static int videoItemCategoryTextLeftPadding;
	public static int videoItemCategoryTextRightPadding;
	public static int ratingStarsLeftPadding;
	public static int ratingStarsBottomPadding;
	public static int ratingStarWidth;
	public static int topBarMenuButtonSideLength;
	public static int imageLoadingProgressBarTopPadding;
	public static int videoViewsLabelBottomPadding;
	public static int videoViewsLabelRightPadding;
	public static int tabUnderlineTopMargin;
	public static int tabUnderlineBottomMargin;
	public static int tabUnderlineRightMargin;
	public static int tabUnderlineLeftMargin;
	public static int videoListChunkSize; //how many videos to initially put into a video list view, and how many are to be added when scrolling to the bottom
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////// VIDEO VIEW
	public static int videoTopBarHeight;
	public static int videoTitleTextHeight;
	public static int videoTitleTextTopMargin;

	public static void initLayoutSettings(int screenWidth, int screenHeight)
	{
		//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		//The following values are used to ensure the proper dimensions of objects on the VIDEO LIST view based on the width and height of the screen in pixels.
		homeTopBarHeight = (int)(screenWidth*.17);
		homeTabBarHeight = (int)(screenWidth*.17);
		homeTitleTextHeight = (int)(homeTopBarHeight*.5);
		homeTabTextHeight = (int)(homeTabBarHeight*.3);
		homeTitleTextTopMargin = (int)(homeTopBarHeight*.21);
		videoItemHeight = (int)(screenWidth*.46);
		videoItemBottomPanelHeight = (int)(videoItemHeight*.666);
		videoItemTitleTextHeight = (int)(videoItemHeight*.083333);
		videoItemTitleTextPaddingBottom = (int)(videoItemHeight*.12);
		videoItemTitleTextSidePadding = (int)(screenWidth*.02);
		videoItemCategoryTextBottomPadding = (int)(videoItemHeight*.025);
		videoItemCategoryTextLeftPadding = (int)(screenWidth*.27);
		videoItemCategoryTextRightPadding = (int)(screenWidth*.26);
		ratingStarsLeftPadding = (int)(screenWidth*.02);
		ratingStarsBottomPadding = (int)(videoItemHeight*.042);
		ratingStarWidth = (int)(screenWidth*.05);
		topBarMenuButtonSideLength = (int)(homeTopBarHeight*.999);
		imageLoadingProgressBarTopPadding = (int)(homeTopBarHeight*.33333);
		videoViewsLabelBottomPadding = (int)(videoItemHeight*.025);
		videoViewsLabelRightPadding = (int)(screenWidth*.02);
		tabUnderlineTopMargin = (int)(homeTabBarHeight*.75);
		tabUnderlineBottomMargin = (int)(homeTabBarHeight*.18);
		videoListChunkSize = ((int)(screenHeight/videoItemHeight)) + 1;
		
		//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		//The following values are used to ensure the proper dimensions of objects on the VIDEO view based on the width and heigh of the screen in pixels.
		videoTopBarHeight = (int)(screenWidth*.14);
		videoTitleTextHeight = (int)(videoTopBarHeight*.6);
		videoTitleTextTopMargin = (int)(videoTopBarHeight*.18);
	}

}
