package me.lynnchurch.samples.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Logger;

import me.lynnchurch.samples.BookItemAnimator;
import me.lynnchurch.samples.R;
import me.lynnchurch.samples.adapter.BooksAdapter;
import me.lynnchurch.samples.aidl.Book;
import me.lynnchurch.samples.aidl.IBookManager;
import me.lynnchurch.samples.service.BooksService;

public class IPCActivity extends BaseActivity {
    private static final String TAG = IPCActivity.class.getSimpleName();
    private RecyclerView rvBooks;
    private Button btnAddBook;
    private BooksAdapter mBooksAdapter;
    private ArrayList<Book> mBooks = new ArrayList<>();
    private IBookManager mBookManager;
    private Random mRandom = new Random();

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBookManager = IBookManager.Stub.asInterface(service);
            try {
                mBooks.addAll(mBookManager.getBookList());
                mBooksAdapter.notifyDataSetChanged();
            } catch (RemoteException e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ipc);
        getSupportActionBar().setTitle(getResources().getStringArray(R.array.titles)[1]);
        bindService(new Intent(this, BooksService.class), serviceConnection, BIND_AUTO_CREATE);
        init();
    }

    private void init() {
        rvBooks = findViewById(R.id.rvBooks);
        btnAddBook = findViewById(R.id.btnAddBook);

        mBooksAdapter = new BooksAdapter(mBooks);
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
        BookItemAnimator bookItemAnimator = new BookItemAnimator();
        bookItemAnimator.setAddDuration(600);
        bookItemAnimator.setRemoveDuration(600);
        rvBooks.setItemAnimator(bookItemAnimator);
        btnAddBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long id = mRandom.nextLong();
                try {
                    Book book = new Book(id, "书籍" + id);
                    mBooks.add(0, book);
                    mBooksAdapter.notifyItemInserted(0);
                    mBooksAdapter.notifyItemRangeChanged(0, mBooks.size());
                    mBookManager.addBook(book);
                } catch (RemoteException e) {
                    Log.e(TAG, e.getMessage(), e);
                }
            }
        });
    }

    private void showPopupMenu(View ancherView, final int position) {
        PopupMenu popupMenu = new PopupMenu(this, ancherView, Gravity.RIGHT);
        MenuInflater inflater = popupMenu.getMenuInflater();
        inflater.inflate(R.menu.manage_book, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.deleteBook:
                        mBooks.remove(position);
                        mBooksAdapter.notifyItemRemoved(position);
                        mBooksAdapter.notifyItemRangeChanged(position, mBooks.size());
                        try {
                            mBookManager.delBook(position);
                        } catch (RemoteException e) {
                            Log.e(TAG, e.getMessage(), e);
                        }
                        break;
                }
                return true;
            }
        });
        popupMenu.show();
    }

}
