package com.gruposantoro.sedeso.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

/**
 * Created by ULISES on 15/02/2017.
 */

public class ServicesUbicacion extends Service {

    private static final long INTERVAL = 1000;
    private static final float DISTANCE = 0;
    private static boolean TERMINA_SERVICIO = false;
    private String TAG = "MyLocationService";
    public static Location loc;
    public static String lat = "", lang = "", direccionEnGrados = "", accuracy = "", velocidad = "";
    public static LocationManager myLocationManager = null;

    LocationListener[] locationListeners = new LocationListener[]{
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)};

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.e("START","Ubicación");
        try {
            TERMINA_SERVICIO = intent.getBooleanExtra("BanderaPosicion", false);
            //Log.e(TAG, "onStartCommand " + "try");
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (Exception e) {
            Log.e(TAG, "onStartCommand " + e.getMessage());
        }
        if (TERMINA_SERVICIO) {
            onDestroy();

        } else {
            onDestroy();
            onCreate();
        }
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initializeLocationManager();
        if (myLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            Intent intent3 = new Intent("gps_disable");
            intent3.putExtra("datosGPS", true);
            LocalBroadcastManager.getInstance(getApplicationContext())
                    .sendBroadcast(intent3);
        }else{
            Intent intent3 = new Intent("gps_disable");
            intent3.putExtra("datosGPS", false);
            LocalBroadcastManager.getInstance(getApplicationContext())
                    .sendBroadcast(intent3);
        }
        Log.e("CREATE","Ubicación");
        try {

            myLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, INTERVAL, DISTANCE,
                    locationListeners[1]);
            //Log.e(TAG, "primer try del onCreate terminando");
        } catch (SecurityException ex) {
            ex.printStackTrace();
        } catch (IllegalArgumentException ex) {
            Log.e(TAG, "network provider does not exist, " + ex.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            myLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, INTERVAL, DISTANCE,
                    locationListeners[0]);
        } catch (SecurityException ex) {
            ex.printStackTrace();
        } catch (IllegalArgumentException ex) {
            Log.e(TAG, "gps provider does not exist " + ex.getMessage());
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (myLocationManager != null) {
            for (int i = 0; i < locationListeners.length; i++) {
                try {
                    myLocationManager.removeUpdates(locationListeners[i]);
                } catch (SecurityException ex) {
                    ex.printStackTrace();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        //timerCoordenadas.cancel();
        myLocationManager = null;
    }

    private void initializeLocationManager() {
        //Log.e(TAG, "initializeLocationManager");
        // if (myLocationManager == null) {
        myLocationManager = (LocationManager) getApplicationContext()
                .getSystemService(Context.LOCATION_SERVICE);
        Log.e(TAG, "obtener LocationManager");
        // }
    }

    private class LocationListener implements android.location.LocationListener {

        Location lastKnowLocation;

        public LocationListener(String Provider) {
            lastKnowLocation = new Location(Provider);
            //Log.e(TAG,"LocationListener");
        }

        @Override
        public void onLocationChanged(Location location) {
            //Log.e(TAG, "onLocationChanged");
            if (location != null) {
                loc = location;
                lat = loc.getLatitude() + "";
                lang = loc.getLongitude() + "";
                direccionEnGrados = loc.getBearing() + "";
                accuracy = loc.getAccuracy() + "";
                velocidad = loc.getSpeed() + "";
                Log.e("onLocationChanged LAT ", loc.getLatitude() + "");
                Log.e("onLocationChanged LONG ", loc.getLongitude() + "");
            } else {
                Log.e("False", "false");
                Log.e("False", "false");
                Log.e("False", "false");
                Log.e("False", "false");
                Log.e("False", "false");
            }
            lastKnowLocation.set(location);
        }

        public boolean version() {
            int currentapiVersion = Build.VERSION.SDK_INT;
            if (currentapiVersion > Build.VERSION_CODES.HONEYCOMB) {
                // Toast.makeText(this, "Es mayor", Toast.LENGTH_LONG).show();
                return true;
            } else if (currentapiVersion <= Build.VERSION_CODES.HONEYCOMB) {
                // Toast.makeText(this, "es menor", Toast.LENGTH_LONG).show();
                return false;
            }
            return false;
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.e("Enable","tttttt");
            if (myLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                Log.e("Enable","IF");
                Intent intent3 = new Intent("gps_disable");
                intent3.putExtra("datosGPS", true);
                LocalBroadcastManager.getInstance(getApplicationContext())
                        .sendBroadcast(intent3);
            } else {
                Log.e("Enable","ELSE");
                Intent intent3 = new Intent("gps_disable");
                intent3.putExtra("datosGPS", false);
                LocalBroadcastManager.getInstance(getApplicationContext())
                        .sendBroadcast(intent3);
            }
            onDestroy();
            onCreate();
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.e("Disable","");
            if (myLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                Log.e("Disable","IF");
                Intent intent3 = new Intent("gps_disable");
                intent3.putExtra("datosGPS", true);
                LocalBroadcastManager.getInstance(getApplicationContext())
                        .sendBroadcast(intent3);
            } else {
                Log.e("Disable","ELSE");
                Intent intent3 = new Intent("gps_disable");
                intent3.putExtra("datosGPS", false);
                LocalBroadcastManager.getInstance(getApplicationContext())
                        .sendBroadcast(intent3);
            }
        }

    }

}
