package me.lynnchurch.samples.db.dao;

import android.database.Cursor;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import me.lynnchurch.samples.db.entity.User;


@Dao
public interface UserDao {
    @Query("select * from user")
    List<User> getUsers();

    @Query("select * from user")
    Cursor getUsersCursor();

    @Insert
    long addUser(User user);

    @Delete
    void delUser(User user);

}
