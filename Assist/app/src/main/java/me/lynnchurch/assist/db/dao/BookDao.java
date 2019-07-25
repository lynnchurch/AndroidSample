package me.lynnchurch.assist.db.dao;

import android.database.Cursor;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import me.lynnchurch.assist.db.entity.Book;

@Dao
public interface BookDao {
    @Query("select * from book")
    List<Book> getBooks();

    @Query("select * from book")
    Cursor getBooksCursor();

    @Insert
    long addBook(Book book);

    @Delete
    void delBook(Book book);

}
