package com.cloudfiveapp.cordova.plugins.push;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.util.Log;

public class PushHandlerActivity extends Activity
{
  private static String TAG = "PushHandlerActivity"; 

  /*
   * this activity will be started if the user touches a notification that we own. 
   * We send it's data off to the push plugin for processing.
   * If needed, we boot up the main activity to kickstart the application. 
   * @see android.app.Activity#onCreate(android.os.Bundle)
   */
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    Log.v(TAG, "onCreate");

    boolean isPushPluginActive = CloudFivePush.isActive();
    processPushBundle(isPushPluginActive);

    GCMIntentService.cancelNotification(this);

    finish();

    if (!isPushPluginActive) {
      forceMainActivityReload();
    }
  }

  /**
   * Takes the pushBundle extras from the intent, 
   * and sends it through to the PushPlugin for processing.
   */
  private void processPushBundle(boolean isPushPluginActive)
  {
    Bundle extras = getIntent().getExtras();

    if (extras != null) {
      Bundle originalExtras = extras.getBundle("pushBundle");
            
            originalExtras.putBoolean("foreground", false);
            originalExtras.putBoolean("coldstart", !isPushPluginActive);

      CloudFivePush.sendExtras(originalExtras);
    }
  }

  /**
   * Forces the main activity to re-launch if it's unloaded.
   */
  private void forceMainActivityReload()
  {
    PackageManager pm = getPackageManager();
    Intent launchIntent = pm.getLaunchIntentForPackage(getApplicationContext().getPackageName());       
    startActivity(launchIntent);
  }

  public void showPushAlert(Bundle extras) {
	  AlertDialog.Builder builder = new AlertDialog.Builder(this);

	  // 2. Chain together various setter methods to set the dialog characteristics
	  String title = extras.getString("alert");
	  String message = extras.getString("message");
	  if (message == null) {
		  message = title;
		  PackageManager packageManager = this.getPackageManager();
		  try {
			  title = packageManager.getApplicationLabel(packageManager.getApplicationInfo(this.getPackageName(), 0)).toString();
		  } catch (NameNotFoundException e) {
			  // TODO Auto-generated catch block
			  e.printStackTrace();
		  }
	  }
	  builder.setMessage(message)
	  .setTitle(title);

	  // 3. Get the AlertDialog from create()
	  AlertDialog dialog = builder.create();
	  dialog.show();
  }

}