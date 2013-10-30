package com.metamedia.tools;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Security 
{
	public static String md5(String s) 
	{
		MessageDigest md = null;
		
		try 
		{
			md = MessageDigest.getInstance("MD5");
		}
		catch (NoSuchAlgorithmException e) 
		{
			e.printStackTrace();
			return null;
		}

		md.update(s.getBytes());

		byte digest[] = md.digest();
		StringBuffer result = new StringBuffer();

		for (int i = 0; i < digest.length; i++)
			result.append(Integer.toHexString(0xFF & digest[i]));
		
		return (result.toString());
	}
}
