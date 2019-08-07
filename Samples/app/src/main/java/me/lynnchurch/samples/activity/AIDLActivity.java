package me.lynnchurch.samples.activity;

import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import me.lynnchurch.samples.aidl.Book_AIDL;
import me.lynnchurch.samples.aidl.IOnBookArrivedListener;
import me.lynnchurch.samples.anim.BookItemAnimator;
import me.lynnchurch.samples.R;
import me.lynnchurch.samples.adapter.LibraryAdapter;
import me.lynnchurch.samples.aidl.IBookManager;
import me.lynnchurch.samples.config.Constants;
import me.lynnchurch.samples.service.BooksService;

public class AIDLActivity extends BaseActivity {
    private static final String TAG = AIDLActivity.class.getSimpleName();

    @BindView(R.id.rvBooks)
    RecyclerView rvBooks;
    private LibraryAdapter mLibraryAdapter;
    private ArrayList<LibraryAdapter.Library> mLibraries = new ArrayList<>();
    private IBookManager mBookManager;
    private Random mRandom = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle(getResources().getStringArray(R.array.titles)[1]);
        bindService(new Intent(this, BooksService.class), mServiceConnection, BIND_AUTO_CREATE);
        init();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_aidl;
    }

    protected void init() {
        mLibraryAdapter = new LibraryAdapter(mLibraries);
        mLibraryAdapter.setOnItemClickListener(new LibraryAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {

            }

            @Override
            public void onItemLongClick(View v, int position) {
                showPopupMenu(v, position);
            }
        });
        rvBooks.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        rvBooks.setAdapter(mLibraryAdapter);
        rvBooks.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        BookItemAnimator bookItemAnimator = new BookItemAnimator();
        bookItemAnimator.setAddDuration(300);
        bookItemAnimator.setRemoveDuration(300);
        rvBooks.setItemAnimator(bookItemAnimator);
    }

    @OnClick(R.id.btnAddBook)
    void addBook() {
        if (null == mBookManager) {
            return;
        }

        Observable.create((ObservableOnSubscribe<LibraryAdapter.Library>) emitter -> {
            Book_AIDL bookAIDL = new Book_AIDL(0, "书籍 " + Math.abs(mRandom.nextInt()));
            long id = 0;
            try {
                 id = mBookManager.addBook(bookAIDL);
            } catch (RemoteException e) {
                Log.i(TAG, e.getMessage(), e);
                emitter.onError(e);
                return;
            }
            bookAIDL.setId(id);
            LibraryAdapter.Library library = new LibraryAdapter.Library();
            library.type = LibraryAdapter.Library.TYPE_BOOK;
            library.book = bookAIDL.convertToBook();
            emitter.onNext(library);
        }).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(library -> {
                    mLibraries.add(0, library);
                    mLibraryAdapter.notifyItemInserted(0);
                    mLibraryAdapter.notifyItemRangeChanged(0, mLibraries.size());
                    rvBooks.scrollToPosition(0);
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.aidl, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.messenger:
                startActivity(new Intent(AIDLActivity.this, MessengerActivity.class));
                break;
            case R.id.binderPool:
                startActivity(new Intent(AIDLActivity.this,BinderPoolActivity.class));
            default:
                return onContextItemSelected(item);
        }
        return true;
    }

    private void showPopupMenu(View ancherView, final int position) {
        PopupMenu popupMenu = new PopupMenu(this, ancherView, Gravity.RIGHT);
        MenuInflater inflater = popupMenu.getMenuInflater();
        inflater.inflate(R.menu.manage_book, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.deleteBook:
                    if (null == mBookManager) {
                        return true;
                    }
                    long id = mLibraries.get(position).book.get_id();
                    mLibraries.remove(position);
                    mLibraryAdapter.notifyItemRemoved(position);
                    mLibraryAdapter.notifyItemRangeChanged(position, mLibraries.size());

                    new Thread(() -> {
                        try {
                            mBookManager.delBook(id);
                        } catch (RemoteException e) {
                            Log.i(TAG, e.getMessage(), e);
                        }
                    }).start();
                    break;
            }
            return true;
        });
        popupMenu.show();
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBookManager = IBookManager.Stub.asInterface(service);
            new Thread(() -> {
                try {
                    service.linkToDeath(mDeathRecipient, 0);
                    mBookManager.addIOnBookArrivedListener(mIOnBookArrivedListener);
                    List<Book_AIDL> books = mBookManager.getBookList();
                    Message message = mServerMsgHandler.obtainMessage(Constants.MSG_GET_BOOK_LIST, books);
                    message.obj = books;
                    mServerMsgHandler.sendMessage(message);
                } catch (RemoteException e) {
                    Log.e(TAG, e.getMessage(), e);
                }
            }).start();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    private IBinder.DeathRecipient mDeathRecipient = new IBinder.DeathRecipient() {
        @Override
        public void binderDied() {
            if (null == mBookManager) {
                return;
            }
            mBookManager.asBinder().unlinkToDeath(mDeathRecipient, 0);
            mBookManager = null;
            bindService(new Intent(AIDLActivity.this, BooksService.class), mServiceConnection, BIND_AUTO_CREATE);
        }
    };

    private IOnBookArrivedListener mIOnBookArrivedListener = new IOnBookArrivedListener.Stub() {
        @Override
        public void onBookArrived(Book_AIDL book_aidl) throws RemoteException {
            Message message = mServerMsgHandler.obtainMessage(Constants.MSG_NEW_BOOK_ARRIVED, book_aidl);
            message.obj = book_aidl;
            mServerMsgHandler.sendMessage(message);
        }
    };

    private Handler mServerMsgHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants
                        .MSG_NEW_BOOK_ARRIVED:
                    Book_AIDL book_aidl = (Book_AIDL) msg.obj;
                    LibraryAdapter.Library library = new LibraryAdapter.Library();
                    library.type = LibraryAdapter.Library.TYPE_BOOK;
                    library.book = book_aidl.convertToBook();
                    mLibraries.add(0, library);
                    mLibraryAdapter.notifyItemInserted(0);
                    mLibraryAdapter.notifyItemRangeChanged(0, mLibraries.size());
                    rvBooks.scrollToPosition(0);
                    break;
                case Constants.MSG_GET_BOOK_LIST:
                    List<Book_AIDL> books = (List<Book_AIDL>) msg.obj;
                    List<LibraryAdapter.Library> libraries = new ArrayList<>();
                    for(Book_AIDL book_aidl1 : books) {
                        LibraryAdapter.Library library1 = new LibraryAdapter.Library();
                        library1.type = LibraryAdapter.Library.TYPE_BOOK;
                        library1.book = book_aidl1.convertToBook();
                        libraries.add(library1);
                    }
                    mLibraries.addAll(libraries);
                    mLibraryAdapter.notifyDataSetChanged();
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        if (null != mBookManager) {
            try {
                mBookManager.removeIOnBookArrivedListener(mIOnBookArrivedListener);
            } catch (RemoteException e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }
    }

}
