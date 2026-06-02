package com.example.sit708_71p;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    Button create, showAll, showOnMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        create = findViewById(R.id.createAdvert);
        showAll = findViewById(R.id.showAll);
        showOnMap = findViewById(R.id.showOnMap);

        create.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CreateAdvert.class);
            startActivity(intent);
        });

        showAll.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ShowAll.class);
            startActivity(intent);
        });
        showOnMap.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MapsActivity.class);
            startActivity(intent);
        });
    }
}