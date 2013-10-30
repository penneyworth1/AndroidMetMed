package com.metamedia.citizentv.api;

import java.util.HashMap;
import java.util.Map;

/*
 * Class containing only static attributes and methods.
 */
public class ApiError 
{	
	public static final int MASK_INVALID				= 0x00000;
	public static final int MASK_VALID					= 0x10000;
	
	// ----- Custom Errors ----- //
	public static final int UNDEFINED 					= 0 | MASK_INVALID;
	public static final int EMPTY_DATA 					= 1 | MASK_INVALID; 
	public static final int HTTP_ERROR					= 2 | MASK_INVALID;
	public static final int HTTP_AUTHENTICATION_ERROR	= 3 | MASK_INVALID;
	public static final int HTTP_TIMEOUT				= 5 | MASK_INVALID;
	public static final int PARSING_ERROR				= 4 | MASK_INVALID;
	
	// ----- Server Errors ----- //
	public static final int OK							= 6 | MASK_VALID;
	public static final int INVALID_ARGUMENTS			= 7 | MASK_INVALID;
	public static final int NOT_FOUND					= 8 | MASK_VALID;
	public static final int INTERNAL_ERROR				= 9 | MASK_INVALID;
	public static final int NOT_LOGGED_ON				= 10 | MASK_VALID;
	public static final int CANNOT_CREATE				= 11 | MASK_VALID;
	public static final int AUTHENTICATION_FAILED		= 12 | MASK_VALID;
	public static final int ALREADY_ERROR				= 13 | MASK_VALID;
	public static final int USER_ALREADY_EXISTS			= 14 | MASK_VALID;
	public static final int EMAIL_INCORRECT				= 15 | MASK_VALID;
	public static final int INVALID_ACCESS				= 16 | MASK_VALID;
	public static final int CANNOT_LOGIN_BY_GPLUS_ID	= 17 | MASK_VALID;
	public static final int NO_ACCESS_BY_NATION			= 18 | MASK_VALID;
	public static final int INCOMPLETE_DATA				= 19 | MASK_INVALID;
	public static final int DATA_LOCKED					= 20 | MASK_VALID;
	public static final int EMAIL_NOT_VERIFIED			= 21 | MASK_INVALID;
	public static final int EMAIL_SENT_RECENT			= 22 | MASK_VALID;
	public static final int EMAIL_ALREADY_EXISTS		= 23 | MASK_INVALID;
	
	static private Map<String, Integer> errorCodeMap = null;
	public static final int errorFromString(String string) 
	{	
		if (errorCodeMap == null)
		{
			errorCodeMap = new HashMap<String, Integer>();
			
			errorCodeMap.put("E_OK", 						Integer.valueOf(OK));
			errorCodeMap.put("E_INVALID_ARGUMENTS", 		Integer.valueOf(INVALID_ARGUMENTS));
			errorCodeMap.put("E_NOT_FOUND",					Integer.valueOf(NOT_FOUND));
			errorCodeMap.put("E_INTERNAL_ERROR", 			Integer.valueOf(INTERNAL_ERROR));
			errorCodeMap.put("E_NOT_LOGGED_ON", 			Integer.valueOf(NOT_LOGGED_ON));
			errorCodeMap.put("E_CANNOT_CREATE", 			Integer.valueOf(CANNOT_CREATE));
			errorCodeMap.put("E_AUTHENTICATION_FAILED", 	Integer.valueOf(AUTHENTICATION_FAILED));
			errorCodeMap.put("E_ALREADY",			 		Integer.valueOf(ALREADY_ERROR));
			errorCodeMap.put("E_USER_ALREADY_EXISTS", 		Integer.valueOf(USER_ALREADY_EXISTS));
			errorCodeMap.put("E_EMAIL_INCORRECT", 			Integer.valueOf(EMAIL_INCORRECT));
			errorCodeMap.put("E_INVALID_ACCESS", 			Integer.valueOf(INVALID_ACCESS));
			errorCodeMap.put("E_CANNOT_LOGIN_BY_GPLUS_ID", 	Integer.valueOf(CANNOT_LOGIN_BY_GPLUS_ID));
			errorCodeMap.put("E_NO_ACCESS_BY_NATION", 		Integer.valueOf(NO_ACCESS_BY_NATION));
			errorCodeMap.put("E_INCOMPLETE_DATA", 			Integer.valueOf(INCOMPLETE_DATA));
			errorCodeMap.put("E_DATA_LOCKED", 				Integer.valueOf(DATA_LOCKED));
			errorCodeMap.put("E_EMAIL_NOT_VERIFIED", 		Integer.valueOf(EMAIL_NOT_VERIFIED));
			errorCodeMap.put("E_EMAIL_SENT_RECENT", 		Integer.valueOf(EMAIL_SENT_RECENT));
			errorCodeMap.put("E_EMAIL_ALREADY_EXISTS", 		Integer.valueOf(EMAIL_ALREADY_EXISTS));
		}
		
		if (errorCodeMap.containsKey(string))
		{
			Integer errorCode = errorCodeMap.get(string);
			return errorCode.intValue();
		}
		
		return UNDEFINED;
	}
}
