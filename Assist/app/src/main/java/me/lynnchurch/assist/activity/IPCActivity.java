package me.lynnchurch.assist.activity;

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
import android.widget.Button;

import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import me.lynnchurch.assist.R;
import me.lynnchurch.assist.adapter.BooksAdapter;
import me.lynnchurch.assist.config.Constants;
import me.lynnchurch.samples.aidl.Book_AIDL;
import me.lynnchurch.samples.aidl.IBookManager;
import me.lynnchurch.samples.aidl.IOnBookArrivedListener;

public class IPCActivity extends BaseActivity {
    private static final String TAG = IPCActivity.class.getSimpleName();
    private RecyclerView rvBooks;
    private Button btnAddBook;
    private BooksAdapter mBooksAdapter;
    private ArrayList<Book_AIDL> mBookAIDLS = new ArrayList<>();
    private IBookManager mBookManager;
    private Random mRandom = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle("Assist IPCActivity");
        Intent intent = new Intent();
        intent.setClassName("me.lynnchurch.samples", "me.lynnchurch.samples.service.BooksService");
        bindService(intent, mServiceConnection, BIND_AUTO_CREATE);
        init();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_ipc;
    }

    private void init() {
        rvBooks = findViewById(R.id.rvBooks);
        btnAddBook = findViewById(R.id.btnAddBook);

        mBooksAdapter = new BooksAdapter(mBookAIDLS);
        mBooksAdapter.setOnItemClickListener(new BooksAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {

            }

            @Override
            public void onItemLongClick(View v, int position) {
                showPopupMenu(v, position);
            }
        });
        rvBooks.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        rvBooks.setAdapter(mBooksAdapter);
        rvBooks.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        btnAddBook.setOnClickListener(v -> {
            if (null == mBookManager) {
                return;
            }
            long id = mRandom.nextLong();
            Book_AIDL bookAIDL = new Book_AIDL(id, "书籍" + id);
            mBookAIDLS.add(0, bookAIDL);
            mBooksAdapter.notifyItemInserted(0);
            mBooksAdapter.notifyItemRangeChanged(0, mBookAIDLS.size());
            rvBooks.scrollToPosition(0);

            new Thread(() -> {
                try {
                    mBookManager.addBook(bookAIDL);
                } catch (RemoteException e) {
                    Log.i(TAG, e.getMessage(), e);
                }
            }).start();

        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.messenger, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.messenger:
                startActivity(new Intent(IPCActivity.this, MessengerActivity.class));
                break;
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
                    long id = mBookAIDLS.get(position).getId();
                    mBookAIDLS.remove(position);
                    mBooksAdapter.notifyItemRemoved(position);
                    mBooksAdapter.notifyItemRangeChanged(position, mBookAIDLS.size());

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
            Intent intent = new Intent();
            intent.setClassName("me.lynnchurch.samples", "me.lynnchurch.samples.service.BooksService");
            bindService(intent, mServiceConnection, BIND_AUTO_CREATE);
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
                    mBookAIDLS.add(0, book_aidl);
                    mBooksAdapter.notifyItemInserted(0);
                    mBooksAdapter.notifyItemRangeChanged(0, mBookAIDLS.size());
                    rvBooks.scrollToPosition(0);
                    break;
                case Constants.MSG_GET_BOOK_LIST:
                    List<Book_AIDL> books = (List<Book_AIDL>) msg.obj;
                    mBookAIDLS.addAll(books);
                    mBooksAdapter.notifyDataSetChanged();
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
