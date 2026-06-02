package com.example.sit708_71p;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "advert_table")
public class Advert {

    @PrimaryKey(autoGenerate = true)

    private int id;
    private String postType,name,phone,description,category,location,imageUri,dateTime;


    public Advert(String postType, String name, String phone, String description,
                  String category, String location, String imageUri, String dateTime) {
        this.postType = postType;
        this.name = name;
        this.phone = phone;
        this.description = description;
        this.category = category;
        this.location = location;
        this.imageUri = imageUri;
        this.dateTime = dateTime;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public String getPostType() {
        return postType;
    }

    public void setPostType(String postType) {
        this.postType = postType;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }


    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }


    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }


    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    @Override
    public String toString() {
        return postType + " - " + category + "\n"
                + description + "\n"
                + "Location: " + location + "\n"
                + "Date: " + dateTime;
    }
}