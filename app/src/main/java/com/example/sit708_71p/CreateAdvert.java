package com.example.sit708_71p;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.gms.common.api.Status;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import java.util.Arrays;
import java.util.List;
import android.location.LocationManager;
import android.location.LocationListener;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Calendar;

public class CreateAdvert extends AppCompatActivity {

    private RadioButton radioLost, radioFound;
    private EditText editName, editPhone, editDescription, editLocation, editDate;
    private Spinner categorySpinner;

    private Button btnSelectImage, btnSaveAdvert, btnGetCurrentLocation;
    private ImageView imagePreview;

    private String selectedImageUri = "";
    private ActivityResultLauncher<String[]> imagePickerLauncher;
    private ActivityResultLauncher<Intent> autocompleteLauncher;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;

    private double latitude = 0.0;
    private double longitude = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_advert);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        radioLost = findViewById(R.id.radioLost);
        radioFound = findViewById(R.id.radioFound);

        editName = findViewById(R.id.editName);
        editPhone = findViewById(R.id.editPhone);
        editDescription = findViewById(R.id.editDescription);
        editLocation = findViewById(R.id.editLocation);
        editDate = findViewById(R.id.editDate);

        categorySpinner = findViewById(R.id.categorySpinner);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        btnGetCurrentLocation = findViewById(R.id.btnGetCurrentLocation);
        btnSaveAdvert = findViewById(R.id.btnSaveAdvert);
        imagePreview = findViewById(R.id.imagePreview);

        setupCategorySpinner();
        setupImagePicker();

        editDate.setOnClickListener(v -> showDatePicker());

        btnSelectImage.setOnClickListener(v -> {
            imagePickerLauncher.launch(new String[]{"image/*"});
        });

        btnSaveAdvert.setOnClickListener(v -> saveAdvert());
        btnGetCurrentLocation.setOnClickListener(v -> {
            getCurrentLocation();
        });

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), "API_KEY");
        }
        setupAutocomplete();
        editLocation.setOnClickListener(v -> openPlaceAutocomplete());
        editDate.setOnClickListener(v -> showDatePicker());
    }
    private void setupCategorySpinner() {
        String[] categories = {
                "Electronics",
                "Other"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                categories
        );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                uri -> {
                    if (uri != null) {
                        selectedImageUri = uri.toString();

                        try {
                            getContentResolver().takePersistableUriPermission(
                                    uri,
                                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                            );
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        imagePreview.setImageURI(uri);
                    }
                }
        );
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                CreateAdvert.this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String selectedDate = selectedYear + "-"
                            + String.format("%02d", selectedMonth + 1) + "-"
                            + String.format("%02d", selectedDay);

                    showTimePicker(selectedDate);
                },
                year,
                month,
                day
        );

        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void showTimePicker(String selectedDate) {
        Calendar calendar = Calendar.getInstance();

        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                CreateAdvert.this,
                (view, selectedHour, selectedMinute) -> {
                    String selectedTime = String.format("%02d:%02d", selectedHour, selectedMinute);
                    editDate.setText(selectedDate + " " + selectedTime);
                },
                hour,
                minute,
                true
        );

        timePickerDialog.show();
    }

    private void saveAdvert() {
        String postType = radioLost.isChecked() ? "Lost" : "Found";
        String name = editName.getText().toString().trim();
        String phone = editPhone.getText().toString().trim();
        String description = editDescription.getText().toString().trim();
        String category = categorySpinner.getSelectedItem().toString();
        String location = editLocation.getText().toString().trim();
        String dateTime = editDate.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show();
            return;
        }

        if (phone.isEmpty()) {
            Toast.makeText(this, "Please enter your phone number", Toast.LENGTH_SHORT).show();
            return;
        }

        if (description.isEmpty()) {
            Toast.makeText(this, "Please enter item description", Toast.LENGTH_SHORT).show();
            return;
        }

        if (location.isEmpty()) {
            Toast.makeText(this, "Please enter location", Toast.LENGTH_SHORT).show();
            return;
        }

        if (dateTime.isEmpty()) {
            Toast.makeText(this, "Please select date and time", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedImageUri.isEmpty()) {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show();
            return;
        }

        Advert advert = new Advert(
                postType,
                name,
                phone,
                description,
                category,
                location,
                selectedImageUri,
                dateTime
        );

        new Thread(() -> {
            AdvertDatabase db = AdvertDatabase.getInstance(this);
            db.advertDao().insertAdvert(advert);

            runOnUiThread(() -> {
                Toast.makeText(this, "Advert saved successfully", Toast.LENGTH_SHORT).show();
                finish();
            });
        }).start();
    }

    private void getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

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
            latitude = location.getLatitude();
            longitude = location.getLongitude();

            String locationText = latitude + ", " + longitude;
            editLocation.setText(locationText);

            Toast.makeText(this, "Current location added", Toast.LENGTH_SHORT).show();
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
    private void setupAutocomplete() {
        autocompleteLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Place place = Autocomplete.getPlaceFromIntent(result.getData());

                        if (place.getLatLng() != null) {
                            latitude = place.getLatLng().latitude;
                            longitude = place.getLatLng().longitude;

                            String locationText = latitude + ", " + longitude;
                            editLocation.setText(locationText);

                            Toast.makeText(this, "Location selected", Toast.LENGTH_SHORT).show();
                        }
                    } else if (result.getResultCode() == AutocompleteActivity.RESULT_ERROR && result.getData() != null) {
                        Status status = Autocomplete.getStatusFromIntent(result.getData());
                        Toast.makeText(this, status.getStatusMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }
    private void openPlaceAutocomplete() {
        List<Place.Field> fields = Arrays.asList(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.LAT_LNG
        );

        Intent intent = new Autocomplete.IntentBuilder(
                AutocompleteActivityMode.OVERLAY,
                fields
        ).build(this);

        autocompleteLauncher.launch(intent);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}