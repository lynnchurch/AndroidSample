package me.lynnchurch.samples.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import me.lynnchurch.samples.aidl.Book;
import me.lynnchurch.samples.aidl.IBookManager;

public class BooksService extends Service {
    private static final String TAG = BooksService.class.getSimpleName();
    private ArrayList<Book> mBooks = new ArrayList<>();

    public BooksService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mBooks.add(new Book(1, "天龙八部"));
        mBooks.add(new Book(2, "倚天屠龙记"));
        mBooks.add(new Book(3, "书剑恩仇录"));
    }

    @Override
    public IBinder onBind(Intent intent) {
        return bookManager.asBinder();
    }

    private IBookManager.Stub bookManager = new IBookManager.Stub() {

        @Override
        public List<Book> getBookList() {
            Log.i(TAG, "getBookList size:" + mBooks.size());
            synchronized (mBooks) {
                return mBooks;
            }
        }

        @Override
        public void addBook(Book book) {
            Log.i(TAG, "addBook:" + book);
            synchronized (mBooks) {
                mBooks.add(0, book);
            }
        }

        @Override
        public void delBook(int position) {
            Log.i(TAG, "delBook:" + position);
            synchronized (mBooks) {
                mBooks.remove(position);
            }
        }
    };
}
