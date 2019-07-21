package me.lynnchurch.samples.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import me.lynnchurch.samples.db.bean.Book;
import me.lynnchurch.samples.db.dao.BookDao;

@Database(entities = {Book.class}, version = 1, exportSchema = false)
public abstract class LynnDatabase extends RoomDatabase {
    private static final String DB_NAME = "LynnDatabase.db";
    private static volatile LynnDatabase instance;

    public static synchronized LynnDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context, LynnDatabase.class, DB_NAME).build();
        }
        return instance;
    }


    public abstract BookDao getBookDao();
}
