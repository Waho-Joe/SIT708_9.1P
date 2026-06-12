# SIT708_9.1P

## Overview

This project is an Android Lost and Found Map App developed for SIT708 Task 9.1P. It is an improved version of the previous Lost and Found App. In this version, map and location features are added so users can create lost or found item adverts with location information and view items on a map.

The app allows users to post lost or found items, save item details locally, view all adverts, check item details, and search nearby items based on the user's current location and selected radius.

## Features

- Create lost item adverts
- Create found item adverts
- Add item name, phone number, description, category, location, image, and date/time
- Select or save item location
- Get current location
- Display lost and found items on Google Map
- Radius-based search for nearby items
- View all adverts in a list
- View advert details
- Remove adverts from the database
- Store advert data locally using Room Database
- Multi-activity navigation

## Project Structure

```text
app/src/main/java/com.example.sit708_71p/
├── MainActivity.java
├── Advert.java
├── AdvertDao.java
├── AdvertDatabase.java
├── CreateAdvert.java
├── ShowAll.java
├── ItemDetails.java
└── MapsActivity.java

app/src/main/res/layout/
├── activity_main.xml
├── activity_create_advert.xml
├── activity_show_all.xml
├── activity_item_details.xml
└── activity_maps.xml
