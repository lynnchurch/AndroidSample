package me.lynnchurch.samples.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import me.lynnchurch.samples.aidl.Book_AIDL;
import me.lynnchurch.samples.aidl.IBookManager;
import me.lynnchurch.samples.aidl.IOnBookArrivedListener;
import me.lynnchurch.samples.db.LynnDatabase;
import me.lynnchurch.samples.db.entity.Book;

public class BooksService extends Service {
    private static final String TAG = BooksService.class.getSimpleName();
    private AtomicBoolean mIsServiceDestroyed = new AtomicBoolean(false);
    private RemoteCallbackList<IOnBookArrivedListener> mIOnBookArrivedListeners = new RemoteCallbackList<>();
    private Disposable mDisposable;
    private Random mRandom = new Random();

    public BooksService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Observable.interval(20, TimeUnit.SECONDS).map(aLong -> {
            if (mIsServiceDestroyed.get()) {
                mDisposable.dispose();
                return null;
            }
            Log.i(TAG, "aLong:" + aLong);
            Book book = new Book();
            book.setName("新书 " + Math.abs(mRandom.nextInt()));
            long id = LynnDatabase.getInstance(getApplicationContext()).getBookDao().addBook(book);
            Log.i(TAG, "addBook id:" + id);
            return book.convertToBook_AIDL();
        }).subscribe(new Observer<Book_AIDL>() {
            @Override
            public void onSubscribe(Disposable d) {
                mDisposable = d;
            }

            @Override
            public void onNext(Book_AIDL book_aidl) {
                final int listenerCount = mIOnBookArrivedListeners.beginBroadcast();
                for (int i = 0; i < listenerCount; i++) {
                    IOnBookArrivedListener iOnBookArrivedListener = mIOnBookArrivedListeners.getBroadcastItem(i);
                    if (null != iOnBookArrivedListener) {
                        try {
                            iOnBookArrivedListener.onBookArrived(book_aidl);
                        } catch (RemoteException e) {
                            Log.e(TAG, e.getMessage(), e);
                        }
                    }
                }
                mIOnBookArrivedListeners.finishBroadcast();
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
                book_aidls.add(book.convertToBook_AIDL());
            }
            Log.i(TAG, "getBookList size:" + book_aidls.size());
            return book_aidls;
        }

        @Override
        public long addBook(Book_AIDL bookAIDL) {
            Log.i(TAG, "addBook:" + bookAIDL);
            long id = LynnDatabase.getInstance(getApplicationContext()).getBookDao().addBook(bookAIDL.convertToBook());
            Log.i(TAG, "addBook id:" + id);
            return id;
        }

        @Override
        public void delBook(long id) {
            LynnDatabase.getInstance(getApplicationContext()).getBookDao().delBook(new Book.BookId(id));
            Log.i(TAG, "delBook:" + id);
        }

        @Override
        public void addIOnBookArrivedListener(IOnBookArrivedListener iOnBookArrivedListener) {
            mIOnBookArrivedListeners.register(iOnBookArrivedListener);
        }

        @Override
        public void removeIOnBookArrivedListener(IOnBookArrivedListener iOnBookArrivedListener) {
            mIOnBookArrivedListeners.unregister(iOnBookArrivedListener);
        }
    };
}
