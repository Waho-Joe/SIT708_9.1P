package com.example.sit708_71p;

import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ItemDetails extends AppCompatActivity {

    private ImageView detailImage;
    private TextView detailTitle, detailPostType, detailCategory, detailName, detailPhone, detailDescription, detailLocation, detailDate;
    private Button btnRemoveAdvert;

    private int advertId;
    private Advert currentAdvert;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_item_details);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        detailImage = findViewById(R.id.detailImage);
        detailTitle = findViewById(R.id.detailTitle);
        detailPostType = findViewById(R.id.detailPostType);
        detailCategory = findViewById(R.id.detailCategory);
        detailName = findViewById(R.id.detailName);
        detailPhone = findViewById(R.id.detailPhone);
        detailDescription = findViewById(R.id.detailDescription);
        detailLocation = findViewById(R.id.detailLocation);
        detailDate = findViewById(R.id.detailDate);
        btnRemoveAdvert = findViewById(R.id.btnRemoveAdvert);

        advertId = getIntent().getIntExtra("advert_id", -1);

        if (advertId == -1) {
            Toast.makeText(this, "Invalid item", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadAdvertDetails();

        btnRemoveAdvert.setOnClickListener(v -> removeAdvert());
    }

    private void loadAdvertDetails() {
        new Thread(() -> {
            AdvertDatabase db = AdvertDatabase.getInstance(this);
            currentAdvert = db.advertDao().getAdvertById(advertId);

            runOnUiThread(() -> {
                if (currentAdvert == null) {
                    Toast.makeText(this, "Item not found", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                detailTitle.setText(currentAdvert.getPostType() + " Item Details");
                detailPostType.setText("Post Type: " + currentAdvert.getPostType());
                detailCategory.setText("Category: " + currentAdvert.getCategory());
                detailName.setText("Name: " + currentAdvert.getName());
                detailPhone.setText("Phone: " + currentAdvert.getPhone());
                detailDescription.setText("Description: " + currentAdvert.getDescription());
                detailLocation.setText("Location: " + currentAdvert.getLocation());
                detailDate.setText("Date: " + currentAdvert.getDateTime());

                String imageUri = currentAdvert.getImageUri();

                if (imageUri != null && !imageUri.isEmpty()) {

                    Toast.makeText(
                            this,
                            "Image URI: " + imageUri,
                            Toast.LENGTH_LONG
                    ).show();

                    try {
                        detailImage.setImageURI(Uri.parse(imageUri));
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Unable to load image", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(this, "No image URI saved", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    private void removeAdvert() {
        if (currentAdvert == null) {
            Toast.makeText(this, "No item to remove", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            AdvertDatabase db = AdvertDatabase.getInstance(this);
            db.advertDao().deleteAdvert(currentAdvert);

            runOnUiThread(() -> {
                Toast.makeText(this, "Item removed", Toast.LENGTH_SHORT).show();
                finish();
            });
        }).start();
    }
}