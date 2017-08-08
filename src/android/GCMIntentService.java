package com.cloudfiveapp.cordova.plugins.push;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.android.gcm.GCMBaseIntentService;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.provider.Settings;
import android.provider.Settings.Secure;
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
      json = new JSONObject().put("event", "registration");
      json.put("regid", regId);
      json.put("success", true);

      Log.v(TAG, "onRegistered: " + json.toString());

      // Send this JSON data to the JavaScript application above EVENT should be set to the msg type
      // In this case this is the registration ID
      CloudFivePush.sendJavascript( json );

    }
    catch( JSONException e)
    {
      try
      {
        json = new JSONObject().put("event", "registration");
        json.put("message", e.toString());
        json.put("success", false);
        // No message to the user is sent, JSON failed
        Log.e(TAG, "onRegistered: JSON exception");

        CloudFivePush.sendJavascript( json );
      }
      catch( JSONException error)
      {
        // No message to the user is sent, JSON failed
        Log.e(TAG, "onRegistered: JSON exception");
      }
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

      // if we are in the foreground, just surface the payload
      if (CloudFivePush.isInForeground()) {
        extras.putBoolean("foreground", true);
      } else {
        extras.putBoolean("foreground", false);
      }
      CloudFivePush.sendExtras(extras);
      
      //Post the notification if it contains an alert key
      if (alert != null) {
          createNotification(context, extras);
        }
    }
  }

  @SuppressLint("NewApi") public void createNotification(Context context, Bundle extras)
  {
    NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    String appName = getAppName(this);

    Intent notificationIntent = new Intent(this, PushHandlerActivity.class);
    notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
    notificationIntent.putExtra("pushBundle", extras);

    PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);


    String message = extras.getString("message");
    String alert = extras.getString("alert");
    if (message == null) {
      message = alert;
      alert = GCMIntentService.getAppName(context);
    }

//    NotificationCompat.Builder mBuilder =
//      new NotificationCompat.Builder(context)
    Notification.Builder mBuilder =
        new Notification.Builder(context)
        .setDefaults(Notification.DEFAULT_ALL)
        .setSmallIcon(context.getApplicationInfo().icon)
        .setWhen(System.currentTimeMillis())
        .setContentTitle(alert)
        .setTicker(alert)
        .setContentIntent(contentIntent);

    mBuilder.setContentText(message);

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
    String charset = "UTF-8";
    String url = "https://www.cloudfiveapp.com/push/register";

    try {
      // Add your data
      StringBuilder sbParams = new StringBuilder();
      //:device_token, :, :device_name, :device_version, :app_version
      sbParams.append("device_token").append("=").append(URLEncoder.encode(registrationId, charset));
      sbParams.append("&package_name").append("=").append(URLEncoder.encode(context.getPackageName(), charset));
      sbParams.append("&device_model").append("=").append(URLEncoder.encode(android.os.Build.MODEL, charset));
      sbParams.append("&device_name").append("=").append(URLEncoder.encode(android.os.Build.DISPLAY, charset));
      sbParams.append("&device_version").append("=").append(URLEncoder.encode(android.os.Build.VERSION.RELEASE, charset));
      sbParams.append("&device_identifier").append("=").append(URLEncoder.encode(Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID), charset));
      sbParams.append("&device_platform").append("=").append(URLEncoder.encode("android", charset));
      if (CloudFivePush.getUserIdentifier() != null) {
        sbParams.append("&user_identifier").append("=").append(URLEncoder.encode(CloudFivePush.getUserIdentifier(), charset));
      }

      String version;
      try {
        version = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
      } catch (NameNotFoundException e) {
        version = "unknown";
      }
      sbParams.append("&app_version").append("=").append(URLEncoder.encode(version, charset));

      URL urlObj = new URL(url);
      HttpURLConnection conn = (HttpURLConnection) urlObj.openConnection();
      conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
      conn.setRequestProperty("Host", "www.cloudfiveapp.com");
      conn.setDoOutput(true);
      conn.setRequestMethod("POST");
      conn.setRequestProperty("Accept-Charset", charset);
      conn.setReadTimeout(10000);
      conn.setConnectTimeout(15000);

      String paramsString = sbParams.toString();

      conn.setRequestProperty("Content-Length", String.valueOf(paramsString.length()));
      conn.connect();

      OutputStream os = conn.getOutputStream();
      BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
      writer.write(paramsString);

      writer.flush();
      writer.close();
      os.close();
      Log.i(TAG, "CloudFivePush response code: " + String.valueOf(conn.getResponseCode()));
      conn.disconnect();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      Log.w(TAG, "Unable to register with cloud five: " + e.getMessage());
      e.printStackTrace();

    }
  }
}
