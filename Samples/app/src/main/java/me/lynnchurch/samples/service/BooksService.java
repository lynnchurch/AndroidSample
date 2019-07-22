package me.lynnchurch.samples.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import me.lynnchurch.samples.aidl.Book_AIDL;
import me.lynnchurch.samples.aidl.IBookManager;
import me.lynnchurch.samples.aidl.IOnBookArrivedListener;
import me.lynnchurch.samples.db.LynnDatabase;
import me.lynnchurch.samples.db.bean.Book;

public class BooksService extends Service {
    private static final String TAG = BooksService.class.getSimpleName();
    private AtomicBoolean mIsServiceDestroyed = new AtomicBoolean(false);
    private CopyOnWriteArrayList<IOnBookArrivedListener> mIOnBookArrivedListeners = new CopyOnWriteArrayList<>();
    private Disposable mDisposable;

    public BooksService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Observable.interval(10, TimeUnit.SECONDS).map(aLong -> {
            if (mIsServiceDestroyed.get()) {
                mDisposable.dispose();
                return null;
            }
            Log.i(TAG, "aLong:" + aLong);
            long bookId = System.currentTimeMillis();
            Book book = new Book(bookId, "新书 " + bookId);
            LynnDatabase.getInstance(getApplicationContext()).getBookDao().addBook(book);
            return convertToBook_AIDL(book);
        }).subscribeOn(Schedulers.io()).observeOn(Schedulers.newThread()).subscribe(new Observer<Book_AIDL>() {
            @Override
            public void onSubscribe(Disposable d) {
                mDisposable = d;
            }

            @Override
            public void onNext(Book_AIDL book_aidl) {
                for (IOnBookArrivedListener iOnBookArrivedListener : mIOnBookArrivedListeners) {
                    try {
                        iOnBookArrivedListener.onBookArrived(book_aidl);
                    } catch (RemoteException e) {
                        Log.e(TAG, e.getMessage(), e);
                    }
                }
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mIsServiceDestroyed.set(true);
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

        @Override
        public void addIOnBookArrivedListener(IOnBookArrivedListener iOnBookArrivedListener) throws RemoteException {
            mIOnBookArrivedListeners.add(iOnBookArrivedListener);
        }

        @Override
        public void removeIOnBookArrivedListener(IOnBookArrivedListener iOnBookArrivedListener) throws RemoteException {
            mIOnBookArrivedListeners.remove(iOnBookArrivedListener);
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
