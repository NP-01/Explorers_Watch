package com.example.explorerswatch;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.ActionProvider;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.security.Provider;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    LocationManager locationManager;
    LocationListener locationListener;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;

    private float[] lastAccelerometer = new float[3];
    private float[] lastMagnetometer = new float[3];
    private boolean lastAccelerometerSet = false;
    private boolean lastMagnetometerSet = false;
    private float[] mR = new float[9];
    private float[] orientation = new float[3];
    private float currentDegree = 0f;

    TextView latitudeView;
    TextView longitudeView;
    TextView altitudeView;
    TextView directionView;
    TextView accuracyView;
    TextView addressView;

    ImageView compassImage;
    Button button;

    Boolean isSensorAvailable = true;

    public void addToMemorablePlaces(View view) {
        Intent intent = new Intent(this, MemorablePlacesActivity.class);
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, locationListener);
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, locationListener);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
                assert sensorManager != null;
                accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

                compassImage = findViewById(R.id.compassImage);
                button = findViewById(R.id.compassButton);

                latitudeView = findViewById(R.id.latitudeView);
                longitudeView = findViewById(R.id.longitudeView);
                altitudeView = findViewById(R.id.altitudeView);
                directionView = findViewById(R.id.directionView);
                accuracyView = findViewById(R.id.accuracyView);
                addressView = findViewById(R.id.addressView);

            }
        });


        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        Criteria criteria = new Criteria();

        criteria.setSpeedAccuracy(Criteria.ACCURACY_HIGH);
        criteria.setPowerRequirement(Criteria.POWER_LOW);

        locationManager.getProviders(criteria, true);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.ENGLISH);
                String address = "";

                try {
                    List<Address> listAddresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);

                    if (listAddresses != null && listAddresses.size() > 0) {

                        if (listAddresses.get(0).getAddressLine(0) != null) {
                            address += listAddresses.get(0).getAddressLine(0) + ", ";
                        }

                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }


                latitudeView.setText("Latitude : " + String.format("%.6f", location.getLatitude()));
                longitudeView.setText("Longitude : " + String.format("%.6f", location.getLongitude()));
                altitudeView.setText("Altitude : " + String.format("%.6f", location.getAltitude()));
                accuracyView.setText("Accuracy: " + (int) location.getAccuracy() + "%");
                addressView.setText("Address :\n" + address);

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), 2);
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, locationListener);
                }
            }
        };

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                } else {
                    if(!locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                        startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), 2);
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, locationListener);
                    } else {
                        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, locationListener);
                    }
                }
            }
        });


    }

    @Override
    public void onResume() {
        super.onResume();

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                if( accelerometer != null ) {
                    sensorManager.registerListener(MainActivity.this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
                } else {
                    directionView.setText("Direction unavailable due to lack of hardware");
                    isSensorAvailable = false;
                }
                if( magnetometer != null ) {
                    sensorManager.registerListener(MainActivity.this, magnetometer, SensorManager.SENSOR_DELAY_GAME);
                } else {
                    directionView.setText("Direction unavailable due to lack of hardware");
                    isSensorAvailable = false;
                }
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this, accelerometer);
        sensorManager.unregisterListener(this, magnetometer);
    }

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    @Override
    public void onSensorChanged(final SensorEvent event) {

        final String[] directions = {"North", "North-West", "West", "South-West",  "South", "South-East", "East", "North-East"};
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                if (event.sensor == accelerometer) {
                    System.arraycopy(event.values,0,lastAccelerometer,0, event.values.length);
                    lastAccelerometerSet = true;
                } else if (event.sensor == magnetometer) {
                    System.arraycopy(event.values,0,lastMagnetometer,0,event.values.length);
                    lastMagnetometerSet = true;
                }

                if (lastMagnetometerSet && lastAccelerometerSet) {
                    SensorManager.getRotationMatrix(mR,null,lastAccelerometer,lastMagnetometer);
                    SensorManager.getOrientation(mR,orientation);

                    float azimuthInRadians = orientation[0];
                    float azimuthInDegrees = (float) ((Math.toDegrees(azimuthInRadians) + 360) % 360);

                    RotateAnimation rotateAnimation = new RotateAnimation(currentDegree, -azimuthInDegrees, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                    rotateAnimation.setDuration(250);
                    rotateAnimation.setFillAfter(true);

                    compassImage.startAnimation(rotateAnimation);
                    currentDegree = -azimuthInDegrees;
                }
            }
        });

//        directionView.setText("Direction: " + "Heading " + String.format("%.4f",-currentDegree) + " degrees");
        directionView.setText("Direction: " + directions[Math.round(((currentDegree %= 360) < 0 ? currentDegree + 360 : currentDegree) / 45) % 8]);

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {  }

    public void showCompass(View view) {
        if(isSensorAvailable) {
            button.setVisibility(View.GONE);
            compassImage.setVisibility(View.VISIBLE);
        } else {
            Toast.makeText(MainActivity.this, "Error! Not able to load the compass.", Toast.LENGTH_SHORT).show();
        }
    }

}
