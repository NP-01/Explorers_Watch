package com.example.multipleactivitydemp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.util.ArrayList;

import static java.util.Arrays.asList;

public class MainActivity extends AppCompatActivity {

    ListView friendsList;

    public void goToNext(String name) {
        Intent intent = new Intent(getApplicationContext(), SecondActivity.class);
        intent.putExtra("Name",name);

        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        friendsList = findViewById(R.id.friendsList);
        final ArrayList<String> myFriends = new ArrayList<>(asList("Devesh", "Pranav", "Prathmesh", "Saumitra", "Vijit"));

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, myFriends);

        friendsList.setAdapter(arrayAdapter);

        friendsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                goToNext(myFriends.get(position));
            }
        });
    }
}
