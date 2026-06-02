package com.example.sit708_71p;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface AdvertDao {

    @Insert
    void insertAdvert(Advert advert);

    @Update
    void updateAdvert(Advert advert);

    @Delete
    void deleteAdvert(Advert advert);

    @Query("SELECT * FROM advert_table ORDER BY id DESC")
    List<Advert> getAllAdverts();

    @Query("SELECT * FROM advert_table WHERE category = :category ORDER BY id DESC")
    List<Advert> getAdvertsByCategory(String category);

    @Query("SELECT * FROM advert_table WHERE id = :advertId LIMIT 1")
    Advert getAdvertById(int advertId);
}