package com.example.sit708_71p;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Advert.class}, version = 1)
public abstract class AdvertDatabase extends RoomDatabase{
    private static AdvertDatabase instance;

    public abstract AdvertDao advertDao();

    public static synchronized AdvertDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AdvertDatabase.class,
                            "lost_found_database"
                    ).build();
        }

        return instance;
    }
}
