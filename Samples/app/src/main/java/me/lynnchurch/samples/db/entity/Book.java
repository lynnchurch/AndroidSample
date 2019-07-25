package me.lynnchurch.samples.db.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import me.lynnchurch.samples.aidl.Book_AIDL;

@Entity
public class Book {
    @PrimaryKey(autoGenerate = true)
    private long _id;
    private String name;

    public long get_id() {
        return _id;
    }

    public void set_id(long _id) {
        this._id = _id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static class BookId {
        private long _id;

        public BookId(long id) {
            this._id = id;
        }

        public long getId() {
            return _id;
        }

        public void setId(long id) {
            this._id = id;
        }
    }

    public Book_AIDL convertToBook_AIDL() {
        Book_AIDL book_aidl = new Book_AIDL(get_id(), getName());
        return book_aidl;
    }
}
