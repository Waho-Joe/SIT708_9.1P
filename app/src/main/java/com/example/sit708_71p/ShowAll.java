package com.example.sit708_71p;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class ShowAll extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 300;

    private Spinner filterSpinner;
    private Button btnApplyFilter, btnSearchRadius;
    private EditText editRadius;
    private ListView advertListView;

    private double currentLatitude = 0.0;
    private double currentLongitude = 0.0;
    private double searchRadiusKm = 10.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_show_all);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        filterSpinner = findViewById(R.id.filterSpinner);
        btnApplyFilter = findViewById(R.id.btnApplyFilter);
        btnSearchRadius = findViewById(R.id.btnSearchRadius);
        editRadius = findViewById(R.id.editRadius);
        advertListView = findViewById(R.id.advertListView);

        setupFilterSpinner();

        btnApplyFilter.setOnClickListener(v -> {
            applyCombinedFilter();
        });

        btnSearchRadius.setOnClickListener(v -> {
            applyCombinedFilter();
        });

        advertListView.setOnItemClickListener((parent, view, position, id) -> {
            Advert selectedAdvert = (Advert) parent.getItemAtPosition(position);

            Intent intent = new Intent(ShowAll.this, ItemDetails.class);
            intent.putExtra("advert_id", selectedAdvert.getId());
            startActivity(intent);
        });

        loadAllAdverts();
    }

    private void setupFilterSpinner() {
        String[] categories = {
                "All",
                "Electronics",
                "Pets",
                "Wallets",
                "Keys",
                "Bags",
                "Documents",
                "Other"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                categories
        );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterSpinner.setAdapter(adapter);
    }

    private void applyCombinedFilter() {
        String selectedCategory = filterSpinner.getSelectedItem().toString();
        String radiusText = editRadius.getText().toString().trim();

        boolean useRadius = !radiusText.isEmpty();

        if (useRadius) {
            try {
                searchRadiusKm = Double.parseDouble(radiusText);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Please enter a valid radius", Toast.LENGTH_SHORT).show();
                return;
            }

            getCurrentLocationForList();
        } else {
            if (selectedCategory.equals("All")) {
                loadAllAdverts();
            } else {
                loadAdvertsByCategory(selectedCategory);
            }
        }
    }

    private void loadAllAdverts() {
        new Thread(() -> {
            AdvertDatabase db = AdvertDatabase.getInstance(this);
            List<Advert> adverts = db.advertDao().getAllAdverts();

            runOnUiThread(() -> {
                ArrayAdapter<Advert> adapter = new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_list_item_1,
                        adverts
                );

                advertListView.setAdapter(adapter);

                if (adverts.isEmpty()) {
                    Toast.makeText(this, "No adverts found", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    private void loadAdvertsByCategory(String category) {
        new Thread(() -> {
            AdvertDatabase db = AdvertDatabase.getInstance(this);
            List<Advert> adverts = db.advertDao().getAdvertsByCategory(category);

            runOnUiThread(() -> {
                ArrayAdapter<Advert> adapter = new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_list_item_1,
                        adverts
                );

                advertListView.setAdapter(adapter);

                if (adverts.isEmpty()) {
                    Toast.makeText(this, "No adverts found in " + category, Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    private void getCurrentLocationForList() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE
            );

            return;
        }

        LocationManager locationManager =
                (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (locationManager == null) {
            Toast.makeText(this, "Location service not available", Toast.LENGTH_SHORT).show();
            return;
        }

        LocationListener locationListener = location -> {
            currentLatitude = location.getLatitude();
            currentLongitude = location.getLongitude();

            loadAdvertsWithinRadiusAndCategory();
        };

        try {
            locationManager.requestSingleUpdate(
                    LocationManager.GPS_PROVIDER,
                    locationListener,
                    null
            );
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Unable to get current location", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadAdvertsWithinRadiusAndCategory() {
        new Thread(() -> {
            AdvertDatabase db = AdvertDatabase.getInstance(this);
            List<Advert> allAdverts = db.advertDao().getAllAdverts();

            List<Advert> filteredAdverts = new ArrayList<>();

            String selectedCategory = filterSpinner.getSelectedItem().toString();

            for (Advert advert : allAdverts) {
                LatLng itemLatLng = parseLocation(advert.getLocation());

                if (itemLatLng != null) {
                    double distance = calculateDistanceKm(
                            currentLatitude,
                            currentLongitude,
                            itemLatLng.latitude,
                            itemLatLng.longitude
                    );

                    boolean withinRadius = distance <= searchRadiusKm;

                    boolean categoryMatches =
                            selectedCategory.equals("All")
                                    || advert.getCategory().equalsIgnoreCase(selectedCategory);

                    if (withinRadius && categoryMatches) {
                        filteredAdverts.add(advert);
                    }
                }
            }

            runOnUiThread(() -> {
                ArrayAdapter<Advert> adapter = new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_list_item_1,
                        filteredAdverts
                );

                advertListView.setAdapter(adapter);

                String selectedCategoryText = filterSpinner.getSelectedItem().toString();

                Toast.makeText(
                        this,
                        filteredAdverts.size()
                                + " items found in "
                                + selectedCategoryText
                                + " within "
                                + searchRadiusKm
                                + " km",
                        Toast.LENGTH_SHORT
                ).show();
            });
        }).start();
    }

    private LatLng parseLocation(String locationText) {
        try {
            if (locationText == null || !locationText.contains(",")) {
                return null;
            }

            String[] parts = locationText.split(",");

            if (parts.length != 2) {
                return null;
            }

            double latitude = Double.parseDouble(parts[0].trim());
            double longitude = Double.parseDouble(parts[1].trim());

            return new LatLng(latitude, longitude);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private double calculateDistanceKm(double lat1, double lon1, double lat2, double lon2) {
        float[] results = new float[1];

        Location.distanceBetween(
                lat1,
                lon1,
                lat2,
                lon2,
                results
        );

        return results[0] / 1000.0;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocationForList();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (advertListView != null) {
            loadAllAdverts();
        }
    }
}