package com.cloudfiveapp.cordova.plugins.push;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.os.Bundle;
import android.util.Log;

import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;

import com.google.android.gcm.*;

/**
 * @author awysocki, bsamson
 */

public class CloudFivePush extends CordovaPlugin {
	public static final String TAG = "CloudFivePush";

	public static final String REGISTER = "register";
	public static final String UNREGISTER = "unregister";

	private static CordovaWebView gWebView;
	private static String notificationCallback = "CloudFivePush._messageCallback";
	private static Bundle gCachedExtras = null;
	private static boolean gForeground = false;
	private static String userIdentifier;

	/**
	 * Gets the application context from cordova's main activity.
	 * @return the application context
	 */
	private Context getApplicationContext() {
		return this.cordova.getActivity().getApplicationContext();
	}

	@Override
	public boolean execute(String action, JSONArray data, CallbackContext callbackContext) {

		boolean result = false;

		Log.v(TAG, "execute: action=" + action);

		if (REGISTER.equals(action)) {

			Log.v(TAG, "execute: data=" + data.toString());

			try {
				String userIdentifier = data.getString(0);
				if ("".equals(userIdentifier) || "null".equals(userIdentifier)) {
					userIdentifier = null;
				}
				setUserIdentifier(userIdentifier);

				gWebView = this.webView;
						
				Log.v(TAG, "execute: ECB=" + notificationCallback + " senderID=" + getGcmSenderId());

				GCMRegistrar.register(getApplicationContext(), getGcmSenderId());
				result = true;
				callbackContext.success();
			} catch (JSONException e) {
				Log.e(TAG, "execute: Got JSON Exception " + e.getMessage());
				result = false;
				callbackContext.error(e.getMessage());
			}

			if ( gCachedExtras != null) {
				Log.v(TAG, "sending cached extras");
				sendExtras(gCachedExtras);
				gCachedExtras = null;
			}

		} else if (UNREGISTER.equals(action)) {

			GCMRegistrar.unregister(getApplicationContext());

			Log.v(TAG, "UNREGISTER");
			result = true;
			callbackContext.success();
		} else {
			result = false;
			Log.e(TAG, "Invalid action : " + action);
			callbackContext.error("Invalid action : " + action);
		}

		return result;
	}

	/*
	 * Sends a json object to the client as parameter to a method which is defined in gECB.
	 */
	public static void sendJavascript(JSONObject _json) {
		String _d = "javascript:" + notificationCallback + "(" + _json.toString() + ")";
		Log.v(TAG, "sendJavascript: " + _d);

		if (notificationCallback != null && gWebView != null) {
			gWebView.sendJavascript(_d);
		}
	}

	/*
	 * Sends the pushbundle extras to the client application.
	 * If the client application isn't currently active, it is cached for later processing.
	 */
	public static void sendExtras(Bundle extras)
	{
		if (extras != null) {
			if (notificationCallback != null && gWebView != null) {
				sendJavascript(convertBundleToJson(extras));
			} else {
				Log.v(TAG, "sendExtras: caching extras to send at a later time.");
				gCachedExtras = extras;
			}
		}
	}

	@Override
	public void initialize(CordovaInterface cordova, CordovaWebView webView) {
		super.initialize(cordova, webView);
		gForeground = true;
	}

	@Override
	public void onPause(boolean multitasking) {
		super.onPause(multitasking);
		gForeground = false;
	}

	@Override
	public void onResume(boolean multitasking) {
		super.onResume(multitasking);
		gForeground = true;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		gForeground = false;
		notificationCallback = null;
		gWebView = null;
	}

	/*
	 * serializes a bundle to JSON.
	 */
	 private static JSONObject convertBundleToJson(Bundle extras)
	 {
		 try
		 {
			 JSONObject json;
			 json = new JSONObject().put("event", "message"); //If you want to change this, set it in the extras

			 JSONObject jsondata = new JSONObject();
			 Iterator<String> it = extras.keySet().iterator();
			 while (it.hasNext())
			 {
				 String key = it.next();
				 Object value = extras.get(key);

				 // System data from Android
				 if (key.equals("from") || key.equals("collapse_key") || key.equals("event"))
				 {
					 json.put(key, value);
				 }
				 else if (key.equals("foreground"))
				 {
					 json.put(key, extras.getBoolean("foreground"));
				 }
				 else if (key.equals("coldstart"))
				 {
					 json.put(key, extras.getBoolean("coldstart"));
				 }
				 else
				 {
					 if ( value instanceof String ) {
						 // Try to figure out if the value is another JSON object

						 String strValue = (String)value;
						 if (strValue.startsWith("{")) {
							 try {
								 JSONObject json2 = new JSONObject(strValue);
								 jsondata.put(key, json2);
							 }
							 catch (Exception e) {
								 jsondata.put(key, value);
							 }
							 // Try to figure out if the value is another JSON array
						 }
						 else if (strValue.startsWith("["))
						 {
							 try
							 {
								 JSONArray json2 = new JSONArray(strValue);
								 jsondata.put(key, json2);
							 }
							 catch (Exception e)
							 {
								 jsondata.put(key, value);
							 }
						 }
						 else
						 {
							 jsondata.put(key, value);
						 }
					 }
				 }
			 } // while
			 json.put("payload", jsondata);

			 Log.v(TAG, "extrasToJSON: " + json.toString());

			 return json;
		 }
		 catch( JSONException e)
		 {
			 Log.e(TAG, "extrasToJSON: JSON exception");
		 }
		 return null;
	 }

	 public static boolean isInForeground()
	 {
		 return gForeground;
	 }

	 public static boolean isActive()
	 {
		 return gWebView != null;
	 }

	 public static String getUserIdentifier() {
		 return userIdentifier;
	 }

	 public static void setUserIdentifier(String userIdentifier) {
		 CloudFivePush.userIdentifier = userIdentifier;
	 }

	 private String getGcmSenderId() {
	 	int id = getApplicationContext().getResources().getIdentifier("config", "xml", getApplicationContext().getPackageName());
	 	XmlResourceParser xrp = getApplicationContext().getResources().getXml(id);
	 	int eventType;
	 	try {
	 		eventType = xrp.getEventType();
	 		while (eventType != XmlPullParser.END_DOCUMENT)
	 		{
	 			if ( eventType == XmlPullParser.START_TAG) {
	 				if ( xrp.getName().equals("gcmSenderId") ) { 
	 					xrp.next();
	 					return xrp.getText();
	 				}
	 			}
	 			eventType = xrp.next();
	 		}
	 	} catch (XmlPullParserException e) {
			 // TODO Auto-generated catch block
	 		e.printStackTrace();
	 	} catch (IOException e) {
			// TODO Auto-generated catch block
	 		e.printStackTrace();
	 	}
	 	return "";

	 }
}