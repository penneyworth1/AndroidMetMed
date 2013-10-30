package com.metamedia.tools;

import android.graphics.Paint;
import android.graphics.Rect;
import android.widget.TextView;

public class Utils 
{
	public static int convertIPStringToInteger(String ip)
	{
		String [] list = ip.split("\\.");		
		int ip1 = Integer.parseInt(list[0]);
		int ip2 = Integer.parseInt(list[1]);
		int ip3 = Integer.parseInt(list[2]);
		int ip4 = Integer.parseInt(list[3]);
		return ip1 + (ip2 << 8) + (ip3 << 16) + (ip4 << 24);
	}
	
	public static String convertIPIntegerToString(int ip)
	{
		return (ip & 0xFF) + "." + ((ip >> 8) & 0xFF) + "." + ((ip >> 16) & 0xFF) + "." + ((ip >> 24) & 0xFF);
	}
	
	public static int getTextWidthOfTextviewInPixels(TextView textView)
	{
		Rect bounds = new Rect();
		Paint textPaint = textView.getPaint();
		textPaint.getTextBounds(textView.getText().toString(),0,textView.getText().length(),bounds);
		//int height = bounds.height();
		int width = bounds.width();
		return width;
	}
}
