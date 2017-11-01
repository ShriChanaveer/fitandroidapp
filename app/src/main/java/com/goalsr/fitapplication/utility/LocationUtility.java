package com.goalsr.fitapplication.utility;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.location.LocationManager;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;

import com.goalsr.fitapplication.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

/**
 * Created by umakant.angadi on 21-10-2017.
 */
public class LocationUtility {
    private static final String TAG = "LocationUtility";
    public static final int REQUEST_CHECK_SETTINGS = 123;
    private LocationManager locationManager;
    private Context mCtx;
    private static LocationUtility sLocationUtility;
    private boolean isGPSEnabled, isNetworkEnabled;

    private LocationUtility(Context ctx) {
        mCtx = ctx;
        locationManager = (LocationManager) ctx
                .getSystemService(Context.LOCATION_SERVICE);
    }

    public static LocationUtility getInstance(Context ctx) {
        if (sLocationUtility == null) {
            sLocationUtility = new LocationUtility(ctx);
        }
        return sLocationUtility;
    }

    AlertDialog.Builder alertDialog;
    DialogInterface dialog1;

    public void showSettingsAlert(final Activity ctx) {
        alertDialog = new AlertDialog.Builder(ctx, R.style.MaterialDialog);

        Resources res = ctx.getResources();
        alertDialog.setTitle("Enable Location!");
        alertDialog.setCancelable(false);

        alertDialog.setMessage("Please enable Location Services to access the feature");
        alertDialog.setPositiveButton("ENABLE", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                ctx.startActivityForResult(intent, 1);

            }
        });

        alertDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                ctx.finish();
            }
        });
        alertDialog.show();
    }


    public void displayLocationSettingsRequest(final Activity context) {
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API).build();
        googleApiClient.connect();

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(10000 / 2);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        Log.i(TAG, "All location settings are satisfied.");
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        Log.i(TAG, "Location settings are not satisfied. Show the user a dialog to upgrade location settings ");

                        try {
                            // Show the dialog by calling startResolutionForResult(), and check the result
                            // in onActivityResult().
                            status.startResolutionForResult(context, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            Log.i(TAG, "PendingIntent unable to execute request.");
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Log.i(TAG, "Location settings are inadequate, and cannot be fixed here. Dialog not created.");
                        break;
                }
            }
        });
    }


    private void SavePreferences(String key, Boolean value, Context ctx) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.commit();

    }

    public DialogInterface getDialog1() {
        return dialog1;
    }


    public boolean isGPSEnabled() {
        // getting GPS status
        isGPSEnabled = locationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER);

        // getting network status
        /*isNetworkEnabled = locationManager
                .isProviderEnabled(LocationManager.NETWORK_PROVIDER);*/
        return isGPSEnabled;

    }

    public final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    public static Dialog checkPlayServices(Activity activity) {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(activity);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                return GooglePlayServicesUtil.getErrorDialog(resultCode, activity,
                        PLAY_SERVICES_RESOLUTION_REQUEST);
            } else {
                Log.i("LocationUtility", "This device is not supported.");
                return GooglePlayServicesUtil.getErrorDialog(ConnectionResult.SERVICE_MISSING, activity,
                        PLAY_SERVICES_RESOLUTION_REQUEST);
            }
        }
        return null;
    }

}
