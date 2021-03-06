package com.example.mapsgps.location.user;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;

import com.example.mapsgps.R;
import com.example.mapsgps.location.Camera;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

/**
 * This class is responsible for gps connection
 * Process clicks on Gps FAB
 */
public class GpsConnection {

    private static final int FAST_UPDATE_INTERVAL = 5;
    private static final int DEFAULT_UPDATE_INTERVAL = 30;

    private boolean isFoundLocation = false;

    LocationRequest locationRequest;
    LocationCallback locationCallBack;
    public FusedLocationProviderClient fusedLocationProviderClient;
    boolean requestingLocationUpdates = true;

    FloatingActionButton gps_fab;
    UserTracker user;
    Context context;

    public GpsConnection(FloatingActionButton gps_fab, UserTracker user, Context context) {
        gps_fab.setOnClickListener(view -> {
            if (isEnabledGPS()) {
                if (isFoundLocation) {
                    Camera.updateCamera(user.getPosition());
                }
            } else {
                disconnectGPS();
                buildAlertMessageNoGps();
            }
        });
        this.gps_fab = gps_fab;
        this.user = user;
        this.context = context;

        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000 * DEFAULT_UPDATE_INTERVAL);
        locationRequest.setFastestInterval(1000 * FAST_UPDATE_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);


        locationCallBack = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                successUpdate(locationResult.getLastLocation());
            }
        };
    }

    /**
     * @return boolean Value is gps connected on user's phone
     */
    private boolean isEnabledGPS() {
        final LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    /**
     * Update ui when gps is connected
     */
    private void connectGPS() {
        isFoundLocation = true;
        user.getMarker().setVisible(true);
        gps_fab.setImageResource(R.drawable.gps_fixed);
        Camera.updateCamera(user.getPosition());
    }

    /**
     * Update ui when gps is disconnected
     */
    private void disconnectGPS() {
        isFoundLocation = false;
        user.getMarker().setVisible(false);
        gps_fab.setImageResource(R.drawable.gps_not_fixed);
    }

    /**
     * Show dialog window which offer user to turn on gps
     * If user choose "Yes", open page where user can turn it on
     * Otherwise, close dialog window
     */
    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this.context);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", (dialog, id) -> context.startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)))
                .setNegativeButton("No", (dialog, id) -> dialog.cancel());
        final AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * If request receive not-null location - gps is connected
     *
     * @param location - location returned by request
     */
    public void successUpdate(Location location) {
        if (location != null) {
            user.setPosition(new LatLng(location.getLatitude(), location.getLongitude()));
            if (!user.getMarker().isVisible()) {
                connectGPS();
            }
        }
    }

    /**
     * Start to track and show user's location
     */
    public void startLocationUpdates() {
        if (requestingLocationUpdates) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallBack, null);
            }
        }
    }

    /**
     * Stop to track and show user's location
     */
    public void stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallBack);
    }
}
