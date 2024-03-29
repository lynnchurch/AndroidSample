package me.lynnchurch.samples.db.entity;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import me.lynnchurch.samples.aidl.Book_AIDL;

@Entity
public class Book implements Parcelable {
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


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this._id);
        dest.writeString(this.name);
    }

    public Book() {
    }

    protected Book(Parcel in) {
        this._id = in.readLong();
        this.name = in.readString();
    }

    public static final Parcelable.Creator<Book> CREATOR = new Parcelable.Creator<Book>() {
        @Override
        public Book createFromParcel(Parcel source) {
            return new Book(source);
        }

        @Override
        public Book[] newArray(int size) {
            return new Book[size];
        }
    };
}
