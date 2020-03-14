package com.example.explorerswatch;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.TileOverlay;

import java.io.IOException;
import java.util.ArrayList;

public class MemorablePlacesActivity extends AppCompatActivity {

    static ArrayList<String> places;
    static ArrayList<LatLng> locations;
    static ListView myMemorablePlaces;
    static ArrayAdapter<String> arrayAdapter;
    ArrayList<String> latitudes;
    ArrayList<String> longitudes;

    SharedPreferences sharedPreferences;


    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }

    public void onViewClicked(int position) {
        Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
        intent.putExtra("Position",position);
        startActivity(intent);
    }

    public void onDeleteClicked(final int position) {
        new AlertDialog.Builder(MemorablePlacesActivity.this)
                .setTitle("Delete Place")
                .setMessage("Do you really want to delete this place from MEMORABLE-PLACES ? ")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        places.remove(position);
                        try {
                            sharedPreferences.edit().putString("places", ObjectSerializer.serialize(places)).apply();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        arrayAdapter.notifyDataSetChanged();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).show();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memorable_places);

        sharedPreferences = this.getSharedPreferences("com.example.explorerswatch", Context.MODE_PRIVATE);

        latitudes = new ArrayList<>();
        longitudes = new ArrayList<>();
        places = new ArrayList<>();
        locations = new ArrayList<>();

//        places.clear();
//        latitudes.clear();
//        longitudes.clear();
//        locations.clear();


        try {

            places = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("places", ObjectSerializer.serialize(new ArrayList<String>())));
            latitudes = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("latitudes", ObjectSerializer.serialize(new ArrayList<String>())));
            longitudes = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("longitudes", ObjectSerializer.serialize(new ArrayList<String>())));

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (places.size() > 0 && latitudes.size() > 0 && longitudes.size() > 0){
            if (places.size() == latitudes.size() && places.size() == longitudes.size()) {
                for (int i=0; i<latitudes.size(); i++) {
                    locations.add(new LatLng(Double.parseDouble(latitudes.get(i)),Double.parseDouble(longitudes.get(i))));
                }
            }
        } else {
            places.add("Add new place");
            locations.add(new LatLng(0, 0));
        }

        myMemorablePlaces = findViewById(R.id.listView);
//        places = new ArrayList<>();
//        locations = new ArrayList<>();

        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, places);

        myMemorablePlaces.setAdapter(arrayAdapter);

        myMemorablePlaces.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

                if (position == 0) {
                    onViewClicked(position);
                }

                else {

                    new AlertDialog.Builder(MemorablePlacesActivity.this)
                            .setNeutralButton("Rename", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    final EditText newName = findViewById(R.id.newNameText);

                                    newName.setVisibility(View.VISIBLE);

                                    newName.setOnKeyListener(new View.OnKeyListener() {
                                        @Override
                                        public boolean onKey(View v, int keyCode, KeyEvent event) {
                                            if(keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {

                                                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                                                inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),0);

                                                newName.setVisibility(View.GONE);
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        if(newName.getText().toString().isEmpty()) {
                                                            Toast.makeText(getApplicationContext(),"Empty field not allowed !", Toast.LENGTH_SHORT).show();
                                                        } else {
                                                            places.set(position, newName.getText().toString());
                                                            try {
                                                                sharedPreferences.edit().putString("places", ObjectSerializer.serialize(places)).apply();
                                                            } catch (Exception e) {
                                                                e.printStackTrace();
                                                            }
                                                            arrayAdapter.notifyDataSetChanged();
                                                        }
                                                        newName.setText("");
                                                    };
                                                });
                                            }
                                            return false;
                                        }
                                    });
                                }
                            })
                            .setNegativeButton("View", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    onViewClicked(position);
                                }
                            })
                            .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    onDeleteClicked(position);
                                }
                            })
                            .show();
                }
            }
        });
    }
}
