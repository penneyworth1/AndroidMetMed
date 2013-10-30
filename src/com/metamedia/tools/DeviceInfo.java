package com.metamedia.tools;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.apache.http.conn.util.InetAddressUtils;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.TelephonyManager;

public class DeviceInfo 
{
	private static boolean initialized = false;
	
	private static String imeiNumber = null;
	private static String wifiMacAddress = null;
	private static String deviceID = null;

	// This method must be called before other method
	public static void init(Context context) 
	{
		try 
		{
			imeiNumber = getImei(context);
			wifiMacAddress = getWifiMacAddress(context);
			deviceID = getDeviceId(context);
			initialized = true;
		} 
		catch (Exception e) 
		{
			// Nothing to do.
		}
	}

	// ---- Public Methods ---- //
	public static boolean isInitialized()
	{
		return initialized;
	}
	
	public static String getDeviceId() 
	{
		return deviceID;
	}

	public static String getImei() 
	{
		return imeiNumber;
	}

	public static String getWifiMacAddress() 
	{
		return wifiMacAddress;
	}

	public static String getModel() 
	{
		return Build.MODEL;
	}

	public static String getOsVersion() 
	{
		return Build.VERSION.RELEASE;
	}
	
//	public static int getIPAddress(Context context)
//	{
//		WifiManager manager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
//		DhcpInfo dhcpInfo = manager.getDhcpInfo();
//		return dhcpInfo.ipAddress;
//	}

    public static String getIPAddress(boolean useIPv4) 
    {
        try 
        {            
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) 
            {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) 
                {
                    if (!addr.isLoopbackAddress()) 
                    {
                        String sAddr = addr.getHostAddress();
                        boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr); 
                        if (useIPv4) 
                        {
                            if (isIPv4) 
                                return sAddr;
                        } 
                        else 
                        {
                            if (!isIPv4) 
                            {
                                int delim = sAddr.indexOf('%'); // drop ip6 port suffix
                                return delim<0 ? sAddr : sAddr.substring(0, delim);
                            }
                        }
                    }
                }
            }
        } 
        catch (Exception ex) 
        {
        	// for now eat exceptions
        	//Log.d("Exception while getting ip address from device:", ex.toString());
        	ex.printStackTrace();
        } 
        return "0.0.0.0";
    }

	// ----- Private Methods ----- //
	private static String getDeviceId(Context context) throws Exception 
	{
		TelephonyManager tm = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);

	    final String tmDevice, tmSerial, androidId;
	    
	    tmDevice = "" + tm.getDeviceId();
	    tmSerial = "" + tm.getSimSerialNumber();
	    androidId = "" + android.provider.Settings.Secure.getString(context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

	    UUID deviceUuid = new UUID(androidId.hashCode(), ((long)tmDevice.hashCode() << 32) | tmSerial.hashCode());
	    
		return deviceUuid.toString();
	}

	private static String getWifiMacAddress(Context context) throws Exception 
	{
		WifiManager manager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInfo = manager.getConnectionInfo();
		
		if (wifiInfo == null || wifiInfo.getMacAddress() == null)
			return Security.md5(UUID.randomUUID().toString());
		else 
			return wifiInfo.getMacAddress().replace(":", "").replace(".", "");
	}

	private static String getImei(Context context) 
	{
		TelephonyManager m = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
		String imei = m != null ? m.getDeviceId() : null;
		return imei;
	}
}
