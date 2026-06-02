package com.example.sit708_71p;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.sit708_71p.databinding.ActivityMapsBinding;

import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 200;
    private double searchRadiusKm = 10.0;

    private double currentLatitude = 0.0;
    private double currentLongitude = 0.0;

    private EditText editRadius;
    private Button btnSearchRadius;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        editRadius = findViewById(R.id.editRadius);
        btnSearchRadius = findViewById(R.id.btnSearchRadius);

        btnSearchRadius.setOnClickListener(v -> {
            String radiusText = editRadius.getText().toString().trim();

            if (radiusText.isEmpty()) {
                Toast.makeText(this, "Please enter radius", Toast.LENGTH_SHORT).show();
                return;
            }

            searchRadiusKm = Double.parseDouble(radiusText);

            if (mMap != null) {
                getCurrentLocationAndShowItems();
            }
        });
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

        getCurrentLocationAndShowItems();
    }
    private void getCurrentLocationAndShowItems() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE
            );

            return;
        }

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (locationManager == null) {
            Toast.makeText(this, "Location service not available", Toast.LENGTH_SHORT).show();
            return;
        }

        LocationListener locationListener = location -> {
            currentLatitude = location.getLatitude();
            currentLongitude = location.getLongitude();

            LatLng currentLatLng = new LatLng(currentLatitude, currentLongitude);

            mMap.clear();

            mMap.addMarker(new MarkerOptions()
                    .position(currentLatLng)
                    .title("Current Location")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, getZoomLevelByRadius(searchRadiusKm)));

            loadItemsWithinRadius();
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

    private void loadItemsWithinRadius() {
        new Thread(() -> {
            AdvertDatabase db = AdvertDatabase.getInstance(this);
            List<Advert> adverts = db.advertDao().getAllAdverts();

            runOnUiThread(() -> {
                int markerCount = 0;

                for (Advert advert : adverts) {
                    LatLng itemLatLng = parseLocation(advert.getLocation());

                    if (itemLatLng != null) {
                        double distance = calculateDistanceKm(
                                currentLatitude,
                                currentLongitude,
                                itemLatLng.latitude,
                                itemLatLng.longitude
                        );

                        if (distance <= searchRadiusKm) {
                            String title = advert.getName();

                            if (title == null || title.isEmpty()) {
                                title = advert.getPostType() + " - " + advert.getCategory();
                            }

                            float markerColor;

                            if (advert.getPostType().equalsIgnoreCase("Lost")) {
                                markerColor = BitmapDescriptorFactory.HUE_GREEN;
                            } else if (advert.getPostType().equalsIgnoreCase("Found")) {
                                markerColor = BitmapDescriptorFactory.HUE_BLUE;
                            } else {
                                markerColor = BitmapDescriptorFactory.HUE_ORANGE;
                            }
                            mMap.addMarker(new MarkerOptions()
                                    .position(itemLatLng)
                                    .title(title)
                                    .snippet(advert.getPostType()
                                            + " | " + advert.getCategory()
                                            + " | " + String.format("%.2f", distance) + " km away")
                                    .icon(BitmapDescriptorFactory.defaultMarker(markerColor))
                            );

                            markerCount++;
                        }
                    }
                }

                Toast.makeText(
                        this,
                        markerCount + " items found within 10 km",
                        Toast.LENGTH_SHORT
                ).show();
            });
        }).start();
    }
    private float getZoomLevelByRadius(double radiusKm) {
        if (radiusKm <= 1) {
            return 16f;
        } else if (radiusKm <= 5) {
            return 14f;
        } else if (radiusKm <= 10) {
            return 13f;
        } else if (radiusKm <= 25) {
            return 11f;
        } else if (radiusKm <= 50) {
            return 10f;
        } else if (radiusKm <= 100) {
            return 9f;
        } else {
            return 7f;
        }
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
                getCurrentLocationAndShowItems();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
    @Override
    protected void onResume() {
        super.onResume();

        if (mMap != null) {
            getCurrentLocationAndShowItems();
        }
    }
}