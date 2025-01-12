package com.example.pasteur;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.Arrays;
import java.util.Locale;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {


    private Button FindGPSLoc, StopGPSLoc;
    private TextView localAddress;
    private GoogleMap mMap;
    private BroadcastReceiver broadcastReceiver; //To Get the Link from GPS Service as a Broadcast
    private LatLng sydney, kolkata;
    private String[] distanceList;
    private int distanceInt;
    private String current_latitude, current_longitude, current_distance, current_provider;
    Marker marker_kolkata, marker_sydney;
    PolylineOptions poption;
    Polyline polyline;
    private FloatingActionButton fab;


    @Override
    protected void onResume() {
        super.onResume();
        //setting the broadcast to receive the the current location
        if (broadcastReceiver == null) {
            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String GPSLoc = ""+intent.getExtras().get("GPS Location");
                    String[] parts = GPSLoc.split("@");
                    current_provider = parts[0];
                    current_latitude = parts[1];
                    current_longitude = parts[2];
                    current_distance = parts[3];
                    String link_str= current_provider+" Location: "+current_latitude+","+current_longitude+",distance: "+current_distance+"m";
                    localAddress.setText(link_str);
                    kolkata = new LatLng(Double.parseDouble(current_latitude), Double.parseDouble(current_longitude));
                    if (marker_kolkata != null) {
                        marker_kolkata.remove();
                    }
                    marker_kolkata=mMap.addMarker(new MarkerOptions().position(kolkata).title(link_str).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                    poption = new PolylineOptions().add(sydney).add(kolkata).width(5).color(Color.RED).geodesic(true);
                    if (polyline != null) {
                        polyline.remove();
                    }
                    polyline=mMap.addPolyline(poption);
                }
            };
        }
        registerReceiver(broadcastReceiver, new IntentFilter("location update"));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FindGPSLoc = (Button) findViewById(R.id.findGPSLoc);
        StopGPSLoc = (Button) findViewById(R.id.stopGPSLoc);
        localAddress = (TextView) findViewById(R.id.LocalAddress);
        distanceList = getResources().getStringArray(R.array.distance);
        fab = findViewById(R.id.fab);
        //FindGPSLoc.setEnabled(false);
        StopGPSLoc.setEnabled(false);

        //Initializing the Place
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getResources().getString(R.string.google_place_key), Locale.US);
        }

        //Initiating the Map Fragment
        MapFragment Mapfragment = (MapFragment) getFragmentManager().findFragmentById(R.id.fragment);
        Mapfragment.getMapAsync(this);


        FindGPSLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(getApplicationContext(), "Button Clicked", Toast.LENGTH_SHORT).show();
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(MainActivity.this);
                mBuilder.setTitle("Please Select the distance for Alarm");
                mBuilder.setItems(distanceList, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position of the selected item
                        String[] parts = distanceList[which].split(" ");
                        distanceInt = Integer.parseInt(parts[0]);
                        //Toast.makeText(getApplicationContext(), "Selected: "+ distanceInt + " km", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(getApplicationContext(), GPS_Service.class);
                        LatLng center = mMap.getCameraPosition().target;
                        sydney=center;
                        if (marker_sydney != null) {
                            marker_sydney.remove();
                        }
                        marker_sydney=mMap.addMarker(new MarkerOptions().position(sydney).title(""));
                        i.putExtra("latitude", ""+sydney.latitude);
                        i.putExtra("longitude", ""+sydney.longitude);
                        i.putExtra("distance", ""+distanceInt);
                        startService(i); //Starting Location Tracking Service
                        StopGPSLoc.setEnabled(true);
                    }
                });
                mBuilder.show();

            }
        });
        StopGPSLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), GPS_Service.class);
                stopService(i); //Stopping the Location Tracking Service

            }
        });
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Snackbar.make(findViewById(R.id.clayout), "Click on row to know more details", Snackbar.LENGTH_LONG)
                Snackbar.make(view, "Click on row to know more details", Snackbar.LENGTH_LONG)
                        .setAction("OK", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                            }
                        }).show();
            }
        });

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (!runtime_permissions())
            enable_buttons();


        // Initialize the AutocompleteSupportFragment.
        final AutocompleteSupportFragment autocompleteFragment =
                (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.LAT_LNG));

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                //TODO: Get info about the selected place.
                //Log.i("Coordinates", "Place: " + place.getName() + ", " + place.getId());
                sydney =place.getLatLng();
                //Context context = getApplicationContext();
                //Toast.makeText(context, String.valueOf(place.getLatLng()), Toast.LENGTH_SHORT).show();
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 16.0f));
                localAddress.setText("Point Coordinates: "+ sydney);
                //FindGPSLoc.setEnabled(true);
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                //Log.i("Errors", "An error occurred: " + status);
                Context context = getApplicationContext();
                Toast.makeText(context, "Error while fetching coordinates, please re-enter", Toast.LENGTH_SHORT).show();
            }
        });


        //Google Map Geocoder Code below. It is replaced by Google Places API in this App.
        /*FindGPSLoc.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                String tracker_str = Edittext.getText().toString();
                Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                Context context = getApplicationContext();
                try {
                    List addressList = geocoder.getFromLocationName(tracker_str, 1);
                    if (addressList != null && addressList.size() > 0) {
                        Address address = (Address) addressList.get(0);
                        LatLng sydney = new LatLng(address.getLatitude(), address.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 16.0f));
                    }
                } catch (IOException e) {
                    Toast.makeText(context, "Error while fetching coordinates, please re-enter", Toast.LENGTH_SHORT).show();
                }
            }
        });
        */


    }


    public void enable_buttons() {
        //get the current location if the permissions are enabled
        //mMap.setMyLocationEnabled(true);
    }

    private boolean runtime_permissions() {

        if (Build.VERSION.SDK_INT >= 23 && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.INTERNET}, 100); //Dialog Box Created to request permission
            return true; //Got the Permission
        }

        return false;

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) { //Checking the permissions
                enable_buttons();
            } else {
                runtime_permissions(); //Start the dialog to request permission
            }
        }
    }


}
