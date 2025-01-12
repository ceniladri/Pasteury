package com.example.pasteur;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.google.maps.android.SphericalUtil;


import com.google.android.gms.maps.model.LatLng;

import java.util.Date;

import afu.org.checkerframework.checker.nullness.qual.Nullable;


public class GPS_Service extends Service {

    private LocationListener listener; //location variables
    private LocationManager locationmanager;
    private String mLatitude, mLongitude;
    private int mDistance;
    private LatLng sydney, sydney1;
    private Ringtone ringtoneSound;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        //mLatitude = intent.getStringExtra("latitude");
        //mLongitude = intent.getStringExtra("longitude");
        //sydney = new LatLng(Double.parseDouble(mLatitude), Double.parseDouble(mLongitude));
        //Context context = getApplicationContext();
        //Toast.makeText(context, "Location to Track: "+sydney, Toast.LENGTH_SHORT).show();
        return null;

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {

        mLatitude = intent.getStringExtra("latitude");
        mLongitude = intent.getStringExtra("longitude");
        mDistance = Integer.parseInt(intent.getStringExtra("distance"));
        sydney = new LatLng(Double.parseDouble(mLatitude), Double.parseDouble(mLongitude));
        Context context = getApplicationContext();
        //Toast.makeText(context, "Location to Track: "+sydney, Toast.LENGTH_SHORT).show();
        return START_NOT_STICKY;
    }
    @Override
    public void onCreate(){
        Context context = getApplicationContext();
        listener = new LocationListener() {

            @Override
            public void onLocationChanged(Location location) {
                Intent i = new Intent("location update");
                Context context = getApplicationContext();

                //getting the location values
                final String lat_str = ""+ location.getLatitude();
                final String long_str = ""+ location.getLongitude();
                final String provider_str = location.getProvider();
                final double accuracy = location.getAccuracy();
                sydney1 = new LatLng(location.getLatitude(), location.getLongitude());


                double distance = SphericalUtil.computeDistanceBetween(sydney, sydney1);
                //final String id_str = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID); //device id

                Toast.makeText(context, "Output Distance: "+distance+",Input Distance: "+mDistance*1000, Toast.LENGTH_SHORT).show();
                if(distance<mDistance*1000) {
                    Toast.makeText(context, "Busted", Toast.LENGTH_SHORT).show();
                    Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
                    //Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    //Ringtone ringtoneSound = RingtoneManager.getRingtone(getApplicationContext(), notification);
                    ringtoneSound = RingtoneManager.getRingtone(getApplicationContext(), notification);
                    ringtoneSound.stop();
                    if (!ringtoneSound.isPlaying()) {
                        ringtoneSound.play();
                    }
                    //if (ringtoneSound != null) {
                    //    ringtoneSound.play();
                    //}
                }


                String currentTime = new java.text.SimpleDateFormat("HH:mm:ss").format(new Date());
                String currentDate = new java.text.SimpleDateFormat("yyyy-MMM-dd").format(new Date());
                final String link_str = provider_str+"@"+lat_str+"@"+long_str+"@"+distance;
                //Toast.makeText(context, "Current Location: "+link_str, Toast.LENGTH_SHORT).show();
                i.putExtra("GPS Location", link_str);
                sendBroadcast(i); //sending the values to MainActivity

            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {}
            @Override
            public void onProviderEnabled(String s) {}
            @Override
            public void onProviderDisabled(String s) {

                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);

            }

        };

        //Here we are invoking both GPS and Network Location. You may change the frequency if battery or data usage is an issue
        locationmanager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        locationmanager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, listener);
        locationmanager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 10, listener);
    }

    @Override
    public void onDestroy() {
        if (ringtoneSound !=null) {
            ringtoneSound.stop();
        }
                super.onDestroy();
        locationmanager.removeUpdates(listener); //stopping the location listener on exit
    }

}