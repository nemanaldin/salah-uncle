package com.androidhive.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.datatransport.BuildConfig;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.Timer;
import java.util.TimerTask;

public class HandleReceivedJson {

  // region Constants
  private static final String TAG = "HandleReceivedJson";

  private static final String BAZAR_PACKAGE = "com.farsitel.bazaar";
  private static final String MYKET_PACKAGE = "ir.mservices.market";
  private static final String IRAN_APPS_PACKAGE = "ir.tgbs.android.iranapp";
  private static final String GOOGLE_PLAY_PACKAGE = "com.android.vending";
  private static final String INSTAGRAM_PACKAGE = "com.instagram.android";
  // endregion

  // region Variables
  private final Context context;
  private final JSONObject jsonObject;
  private final PackageManager packageManager;
  // endregion

  // region Initialization
  public HandleReceivedJson(Context context, JSONObject jsonObject) {
    this.context = context;
    this.jsonObject = jsonObject;
    this.packageManager = context.getPackageManager();
  }
  // endregion

  // region Main Handler
  public void handleJson() {
    try {
      // Check API Level and Version Code
      if (!checkApiAndVersionCode()) {
        Log.w(TAG, "API or Version Code check failed. JSON ignored.");
        return;
      }

      // Check Operator
      if (!checkOperator()) {
        Log.w(TAG, "Operator check failed. JSON ignored.");
        return;
      }

      // Route based on 'key'
      if (jsonObject.has("key")) {
        String key = jsonObject.getString("key");

        // استفاده از if-else if به جای switch
        if ("link".equalsIgnoreCase(key)) {
          openUri();
        } else if ("bazar".equalsIgnoreCase(key)) {
          openStore(BAZAR_PACKAGE, "bazaar://details?id=");
        } else if ("myket".equalsIgnoreCase(key)) {
          openStore(MYKET_PACKAGE, "myket://details?id=");
        } else if ("iran-apps".equalsIgnoreCase(key)) {
          openStore(IRAN_APPS_PACKAGE, "iranapps://app/");
        } else if ("google-play".equalsIgnoreCase(key)) {
          openStore(GOOGLE_PLAY_PACKAGE, "https://play.google.com/store/apps/details?id=");
        } else if ("instagram".equalsIgnoreCase(key)) {
          openStore(INSTAGRAM_PACKAGE, "https://instagram.com/_u/");
        } else if ("popup1".equalsIgnoreCase(key)) {
          handlePopup(1);
        } else if ("popup2".equalsIgnoreCase(key)) {
          handlePopup(2);
        }
      } else {
        Log.w(TAG, "JSON does not contain a 'key'.");
      }
    } catch (JSONException e) {
      Log.e(TAG, "Error parsing JSON: ", e);
    }
  }
  // endregion

  // region Helper Methods
  private boolean checkApiAndVersionCode() throws JSONException {
    int userCurrentApi = Build.VERSION.SDK_INT;
    int userCurrentVersionCode = BuildConfig.VERSION_CODE;

    if (jsonObject.has("min-api") && userCurrentApi <= jsonObject.getInt("min-api")) {
      return false;
    }
    if (jsonObject.has("max-api") && userCurrentApi >= jsonObject.getInt("max-api")) {
      return false;
    }
    if (jsonObject.has("min-vc") && userCurrentVersionCode <= jsonObject.getInt("min-vc")) {
      return false;
    }
    if (jsonObject.has("max-vc") && userCurrentVersionCode >= jsonObject.getInt("max-vc")) {
      return false;
    }
    return true;
  }

  private boolean checkOperator() throws JSONException {
    if (jsonObject.has("operator")) {
      String jsonOperator = jsonObject.getString("operator").toLowerCase();
      if (!TextUtils.isEmpty(jsonOperator) && !"all".equalsIgnoreCase(jsonOperator)) {
        String userOperator = getOperatorName();
        if (!"all".equalsIgnoreCase(userOperator) && !jsonOperator.contains(userOperator)) {
          return false;
        }
      }
    }
    return true;
  }

  // New unified method for opening stores
  private void openStore(String packageName, String uriPrefix) {
    try {
      if (!jsonObject.has("link") || jsonObject.getString("link").isEmpty()) {
        Log.w(TAG, "Link is missing or empty.");
        return;
      }

      String linkId = jsonObject.getString("link");
      Intent intent = new Intent(Intent.ACTION_VIEW);
      intent.setData(Uri.parse(uriPrefix + linkId));
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

      if (isInstalledPackage(packageName)) {
        intent.setPackage(packageName);
        context.startActivity(intent);
      } else {
        // Fallback to Google Play if the specific store is not installed
        Intent fallbackIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + linkId));
        fallbackIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(fallbackIntent);
      }

    } catch (JSONException e) {
      Log.e(TAG, "Error opening store link: ", e);
    }
  }

  private void openUri() {
    try {
      if (!jsonObject.has("link") || jsonObject.getString("link").isEmpty()) {
        Log.w(TAG, "Link is missing or empty.");
        return;
      }

      String link = jsonObject.getString("link");
      Intent intent = new Intent(Intent.ACTION_VIEW);
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      intent.setData(Uri.parse(link));

      if (jsonObject.has("pk")) {
        String[] packages = jsonObject.getString("pk").split(",");
        for (String item : packages) {
          if (isInstalledPackage(item)) {
            intent.setPackage(item);
            break;
          }
        }
      }
      context.startActivity(intent);

    } catch (JSONException e) {
      Log.e(TAG, "Error opening URI: ", e);
    }
  }

  private void handlePopup(int popupType) {
    try {
      if (!jsonObject.has("link") || jsonObject.getString("link").isEmpty()) {
        Log.w(TAG, "Link is missing for popup.");
        return;
      }

      String link = jsonObject.getString("link");
      int delay = 2500;

      if (jsonObject.has("delay") && !jsonObject.getString("delay").isEmpty()) {
        delay = jsonObject.getInt("delay");
      }
      if (popupType == 2) {
        new Timer().schedule(new TimerTask() {
          @Override
          public void run() {
            openUri(link);
          }
        }, delay);
      } else {
        openUri(link);
      }
    } catch (JSONException e) {
      Log.e(TAG, "Error handling popup: ", e);
    }
  }

  // Overloaded method to simplify popup logic
  private void openUri(String link) {
    try {
      Intent intent = new Intent(Intent.ACTION_VIEW);
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      intent.setData(Uri.parse(link));
      context.startActivity(intent);
    } catch (Exception e) {
      Log.e(TAG, "Failed to open URI: " + link, e);
    }
  }

  private boolean isInstalledPackage(String packageName) {
    try {
      packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
      return true;
    } catch (PackageManager.NameNotFoundException e) {
      Log.i(TAG, "Package not found: " + packageName);
      return false;
    }
  }

  private String getOperatorName() {
    TelephonyManager tManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
    if (tManager == null) {
      return "all";
    }
    String carrierName = tManager.getNetworkOperatorName().toLowerCase();

    if (carrierName.contains("mci") || carrierName.contains("ir_") || carrierName.contains("tci")) {
      return "mci";
    } else if (carrierName.contains("mtn") || carrierName.contains("irancell")) {
      return "mtn";
    } else if (carrierName.contains("tel") || carrierName.contains("righ")) {
      return "rightel";
    } else {
      return "all";
    }
  }
}