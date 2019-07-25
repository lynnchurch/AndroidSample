package me.lynnchurch.assist.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import me.lynnchurch.assist.db.dao.BookDao;
import me.lynnchurch.assist.db.dao.UserDao;
import me.lynnchurch.assist.db.entity.Book;
import me.lynnchurch.assist.db.entity.User;


@Database(entities = {Book.class, User.class}, version = 1, exportSchema = false)
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

    public abstract UserDao getUserDao();
}
