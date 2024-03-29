package me.lynnchurch.samples.db.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import me.lynnchurch.samples.db.entity.Book;

@Dao
public interface BookDao {
    @Query("select * from book")
    List<Book> getBooks();

    @Insert
    long addBook(Book book);

    @Delete(entity = Book.class)
    void delBook(Book.BookId bookId);

}
