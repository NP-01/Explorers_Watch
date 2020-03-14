package com.example.explorerswatch;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.example.explorerswatch.MemorablePlacesActivity.arrayAdapter;
import static com.example.explorerswatch.MemorablePlacesActivity.locations;
import static com.example.explorerswatch.MemorablePlacesActivity.places;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private GoogleMap mMap;
    LocationManager locationManager;
    LocationListener locationListener;
    Intent intent;

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }

    public void setOnMapLocation(Location location, String title) {
        if(location != null) {
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(latLng).title(title));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10, locationListener);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        intent = getIntent();
        int position = intent.getIntExtra("Position",0);
        if(position == 0) {

            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    setOnMapLocation(location,"Your Location");
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            };

            if (Build.VERSION.SDK_INT < 23) {
                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10, locationListener);
                }
            } else {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                } else {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 100, locationListener);
                    Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    setOnMapLocation(lastKnownLocation, "Your Last Known Location");
                }
            }
            mMap.setOnMapLongClickListener(this);

        } else {
            Location placeLocation = new Location(LocationManager.GPS_PROVIDER);
            placeLocation.setLatitude(locations.get(position).latitude);
            placeLocation.setLongitude(locations.get(position).longitude);
            setOnMapLocation(placeLocation, places.get(position));
        }
    }

    @Override
    public void onMapLongClick(LatLng latLng) {

        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());

        try {
            List<Address> listAddresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);

            String placeName = " ";

            if(listAddresses != null && listAddresses.size() > 0) {
                if(listAddresses.get(0).getThoroughfare() != null) {
                    if (listAddresses.get(0).getSubThoroughfare() != null) {
                        placeName += listAddresses.get(0).getSubThoroughfare() + ", ";
                    }
                    placeName += listAddresses.get(0).getThoroughfare() + ", " + listAddresses.get(0).getLocality() + ", " + listAddresses.get(0).getAdminArea() + ".";
                } else {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd 'at' HH:mm:ss");
                    String currentDateTime = sdf.format(new Date());
                    placeName = currentDateTime;
                }

                mMap.addMarker(new MarkerOptions().position(latLng).title(placeName).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,15));

                places.add(placeName);
                locations.add(latLng);

                arrayAdapter.notifyDataSetChanged();

                SharedPreferences sharedPreferences = this.getSharedPreferences("com.example.explorerswatch", Context.MODE_PRIVATE);

                try {
                    ArrayList<String> latitudes = new ArrayList<>();
                    ArrayList<String> longitudes = new ArrayList<>();

                    for (LatLng coords : locations) {
                        latitudes.add(Double.toString(coords.latitude));
                        longitudes.add(Double.toString(coords.longitude));
                    }

                    sharedPreferences.edit().putString("places", ObjectSerializer.serialize(places)).apply();
                    sharedPreferences.edit().putString("latitudes", ObjectSerializer.serialize(latitudes)).apply();
                    sharedPreferences.edit().putString("longitudes", ObjectSerializer.serialize(longitudes)).apply();

                } catch (Exception e) {
                    e.printStackTrace();
                }

                Toast.makeText(MapsActivity.this, "Location Saved", Toast.LENGTH_SHORT).show();
            }

        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }

}
