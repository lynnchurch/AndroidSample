package me.lynnchurch.samples.aidl;

import android.os.Parcel;
import android.os.Parcelable;

import me.lynnchurch.samples.db.entity.Book;

public class Book_AIDL implements Parcelable {
    private long id;
    private String name;

    public Book_AIDL(long id, String name) {
        this.id = id;
        this.name = name;
    }

    protected Book_AIDL(Parcel in) {
        id = in.readLong();
        name = in.readString();
    }

    @Override
    public String toString() {
        return "Book_AIDL{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(name);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Book_AIDL> CREATOR = new Creator<Book_AIDL>() {
        @Override
        public Book_AIDL createFromParcel(Parcel in) {
            return new Book_AIDL(in);
        }

        @Override
        public Book_AIDL[] newArray(int size) {
            return new Book_AIDL[size];
        }
    };

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Book convertToBook() {
        Book book = new Book();
        book.set_id(getId());
        book.setName(getName());
        return book;
    }
}
