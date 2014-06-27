package com.cloudfiveapp.cordova.plugins.push;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.google.android.gcm.GCMBaseIntentService;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.provider.Settings;
//import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class GCMIntentService extends GCMBaseIntentService {

  public static final int NOTIFICATION_ID = 237;
  private static final String TAG = "GCMIntentService";

  public GCMIntentService() {
    super("GCMIntentService");
  }

  @Override
  public void onRegistered(Context context, String regId) {

    Log.v(TAG, "onRegistered: "+ regId);
    notifyCloudFive(context, regId);
    JSONObject json;

    try
    {
      json = new JSONObject().put("event", "registered");
      json.put("regid", regId);

      Log.v(TAG, "onRegistered: " + json.toString());

      // Send this JSON data to the JavaScript application above EVENT should be set to the msg type
      // In this case this is the registration ID
      CloudFivePush.sendJavascript( json );

    }
    catch( JSONException e)
    {
      // No message to the user is sent, JSON failed
      Log.e(TAG, "onRegistered: JSON exception");
    }
  }

  @Override
  public void onUnregistered(Context context, String regId) {
    Log.d(TAG, "onUnregistered - regId: " + regId);
  }

  @Override
  protected void onMessage(Context context, Intent intent) {
	  Log.d(TAG, "onMessage - context: " + context);

	  // Extract the payload from the message
	  Bundle extras = intent.getExtras();
	  if (extras != null)
	  {
		  String alert = extras.getString("alert");
		  if (alert != null) {
			  createNotification(context, extras);
		  }
		  
		  // if we are in the foreground, just surface the payload, else post it to the statusbar
		  if (CloudFivePush.isInForeground()) {
			  extras.putBoolean("foreground", true);
			  CloudFivePush.sendExtras(extras);
		  } else {
			  extras.putBoolean("foreground", false);
			  // Send a notification if there is a message
		
		  }
	  }
  }

  public void createNotification(Context context, Bundle extras)
  {
    NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    String appName = getAppName(this);

    Intent notificationIntent = new Intent(this, PushHandlerActivity.class);
    notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
    notificationIntent.putExtra("pushBundle", extras);

    PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

//    NotificationCompat.Builder mBuilder =
//      new NotificationCompat.Builder(context)
    Notification.Builder mBuilder =
        new Notification.Builder(context)
        .setDefaults(Notification.DEFAULT_ALL)
        .setSmallIcon(context.getApplicationInfo().icon)
        .setWhen(System.currentTimeMillis())
        .setContentTitle(extras.getString("alert"))
        .setTicker(extras.getString("alert"))
        .setContentIntent(contentIntent);

    String message = extras.getString("message");
    if (message != null) {
      mBuilder.setContentText(message);
    } else {
      mBuilder.setContentText("<missing message content>");
    }

    String msgcnt = extras.getString("msgcnt");
    if (msgcnt != null) {
      mBuilder.setNumber(Integer.parseInt(msgcnt));
    }

    mNotificationManager.notify((String) appName, NOTIFICATION_ID, mBuilder.build());
  }

  public static void cancelNotification(Context context)
  {
    NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    mNotificationManager.cancel((String)getAppName(context), NOTIFICATION_ID);  
  }

  private static String getAppName(Context context)
  {
    CharSequence appName = 
        context
          .getPackageManager()
          .getApplicationLabel(context.getApplicationInfo());

    return (String)appName;
  }

  @Override
  public void onError(Context context, String errorId) {
    Log.e(TAG, "onError - errorId: " + errorId);
  }
  
  public void notifyCloudFive(Context context, String registrationId) {
	  HttpClient httpclient = new DefaultHttpClient();
	  HttpPost httppost = new HttpPost("http://192.168.0.129:3000/push/register");

	  try {
		  // Add your data
		  List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
		  //:device_token, :device_model, :device_name, :device_version, :app_version
		  nameValuePairs.add(new BasicNameValuePair("device_token", registrationId));
		  nameValuePairs.add(new BasicNameValuePair("package_name", context.getPackageName() ));
		  nameValuePairs.add(new BasicNameValuePair("device_model", android.os.Build.MODEL));
		  nameValuePairs.add(new BasicNameValuePair("device_name", android.os.Build.DISPLAY));
		  nameValuePairs.add(new BasicNameValuePair("device_version", android.os.Build.VERSION.RELEASE));
		  nameValuePairs.add(new BasicNameValuePair("device_identifier", Settings.Secure.ANDROID_ID));
		  nameValuePairs.add(new BasicNameValuePair("device_platform", "android"));
		  nameValuePairs.add(new BasicNameValuePair("user_identifier", CloudFivePush.getUserIdentifier()));
		  
		  String version;
		  try {
			  version = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
		  } catch (NameNotFoundException e) {
			  version = "unknown";
		  }
		  nameValuePairs.add(new BasicNameValuePair("app_version", version));
		  httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		  HttpResponse response = httpclient.execute(httppost);
	  } catch (ClientProtocolException e) {
		  // TODO Auto-generated catch block
	  } catch (IOException e) {
		  // TODO Auto-generated catch block
		  Log.w(TAG, "Unable to register with cloud five: " + e.getMessage());
		  e.printStackTrace();

	  }
  }
 
}