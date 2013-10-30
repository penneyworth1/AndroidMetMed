package com.metamedia.citizentv;

public class AppStateVariables 
{
	//Error codes
	public static final String CONNECTION_ERROR = "connection error";
	
	public static boolean InitializingGlobalVarsFromServer; //When true, the server list, category list, etc are loading, and all threads that need this complete server list must wait.
	public static boolean AbleToReachServer; //When false, we were recently unable to reach the server and should stop processing requests until some action is taken to reset this state;
}
