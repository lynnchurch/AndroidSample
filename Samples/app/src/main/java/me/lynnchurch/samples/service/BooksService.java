package me.lynnchurch.samples.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import me.lynnchurch.samples.aidl.Book_AIDL;
import me.lynnchurch.samples.aidl.IBookManager;
import me.lynnchurch.samples.db.LynnDatabase;
import me.lynnchurch.samples.db.bean.Book;

public class BooksService extends Service {
    private static final String TAG = BooksService.class.getSimpleName();

    public BooksService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return bookManager.asBinder();
    }

    private IBookManager.Stub bookManager = new IBookManager.Stub() {

        @Override
        public List<Book_AIDL> getBookList() {
            List<Book> books = LynnDatabase.getInstance(getApplicationContext()).getBookDao().getBooks();
            List<Book_AIDL> book_aidls = new ArrayList<>();
            for (Book book : books) {
                book_aidls.add(convertToBook_AIDL(book));
            }
            Log.i(TAG, "getBookList size:" + book_aidls.size());
            return book_aidls;
        }

        @Override
        public void addBook(Book_AIDL bookAIDL) {
            Log.i(TAG, "addBook:" + bookAIDL);
            LynnDatabase.getInstance(getApplicationContext()).getBookDao().addBook(convertToBook(bookAIDL));
        }

        @Override
        public void delBook(long id) {
            LynnDatabase.getInstance(getApplicationContext()).getBookDao().delBook(new Book.BookId(id));
            Log.i(TAG, "delBook:" + id);
        }
    };

    public Book_AIDL convertToBook_AIDL(Book book) {
        Book_AIDL book_aidl = new Book_AIDL(book.getId(), book.getName());
        return book_aidl;
    }

    public Book convertToBook(Book_AIDL book_aidl) {
        Book book = new Book(book_aidl.getId(), book_aidl.getName());
        return book;
    }
}
