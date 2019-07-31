package me.lynnchurch.samples.activity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import me.lynnchurch.samples.R;
import me.lynnchurch.samples.adapter.LibraryAdapter;
import me.lynnchurch.samples.anim.BookItemAnimator;
import me.lynnchurch.samples.db.entity.Book;
import me.lynnchurch.samples.db.entity.User;
import me.lynnchurch.samples.utils.Utils;

public class ContentProviderActivity extends BaseActivity {
    private static final String TAG = ContentProviderActivity.class.getSimpleName();
    @BindView(R.id.rvLibrary)
    RecyclerView rvLibrary;
    private LibraryAdapter mLibraryAdapter;
    private ArrayList<LibraryAdapter.Library> mLibraries = new ArrayList<>();
    private Random mRandom = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle(getResources().getStringArray(R.array.titles)[2]);
        init();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_content_provider;
    }

    private void init() {
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
        rvLibrary.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        rvLibrary.setAdapter(mLibraryAdapter);
        rvLibrary.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        BookItemAnimator bookItemAnimator = new BookItemAnimator();
        rvLibrary.setItemAnimator(bookItemAnimator);
        initLibrary();
        remoteInvokeContentProviderCall();
    }

    private void initLibrary() {
        Observable.create((ObservableOnSubscribe<List<LibraryAdapter.Library>>) emitter -> {
            List<LibraryAdapter.Library> libraries = new ArrayList<>();
            Uri getBookUri = Uri.parse("content://me.lynnchurch.assist.provider/book/getBooks");
            Cursor bookCursor = getContentResolver().query(getBookUri, null, null, null, null);
            while (null != bookCursor && bookCursor.moveToNext()) {
                LibraryAdapter.Library library = new LibraryAdapter.Library();
                Book book = new Book();
                book.set_id(bookCursor.getLong(0));
                book.setName(bookCursor.getString(1));
                library.type = LibraryAdapter.Library.TYPE_BOOK;
                library.book = book;
                libraries.add(library);
            }

            Uri getUserUri = Uri.parse("content://me.lynnchurch.assist.provider/user/getUsers");
            Cursor userCursor = getContentResolver().query(getUserUri, null, null, null, null);
            while (null != userCursor && userCursor.moveToNext()) {
                LibraryAdapter.Library library = new LibraryAdapter.Library();
                User user = new User();
                user.set_id(userCursor.getLong(0));
                user.setName(userCursor.getString(1));
                user.setAge(userCursor.getInt(2));
                library.type = LibraryAdapter.Library.TYPE_USER;
                library.user = user;
                libraries.add(library);
            }
            emitter.onNext(libraries);
        }).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(libraries -> {
                    mLibraries.addAll(libraries);
                    mLibraryAdapter.notifyDataSetChanged();
                }, throwable ->
                        Log.e(TAG, throwable.getMessage(), throwable), () -> {
                });
    }

    private void remoteInvokeContentProviderCall() {
        Uri callUri = Uri.parse("content://me.lynnchurch.assist.provider");
        try {
            Bundle bundle = getContentResolver().call(callUri, "hello", "Lynn", null);
            if (null != bundle) {
                toast(bundle.getString("return"), Toast.LENGTH_SHORT);
            }
        } catch (IllegalArgumentException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    private void showPopupMenu(View ancherView, final int position) {
        PopupMenu popupMenu = new PopupMenu(this, ancherView, Gravity.RIGHT);
        MenuInflater inflater = popupMenu.getMenuInflater();
        inflater.inflate(R.menu.manage_library, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.delete:
                    LibraryAdapter.Library library = mLibraries.get(position);
                    mLibraries.remove(position);
                    mLibraryAdapter.notifyItemRemoved(position);
                    mLibraryAdapter.notifyItemRangeChanged(position, mLibraries.size());

                    new Thread(() -> {
                        long id = 0;
                        Uri uri = null;
                        switch (library.type) {
                            case LibraryAdapter.Library.TYPE_BOOK:
                                id = library.book.get_id();
                                uri = Uri.parse("content://me.lynnchurch.assist.provider/book/delBook");
                                break;
                            case LibraryAdapter.Library.TYPE_USER:
                                id = library.user.get_id();
                                uri = Uri.parse("content://me.lynnchurch.assist.provider/user/delUser");
                                break;
                        }
                        getContentResolver().delete(uri, null, new String[]{String.valueOf(id)});
                    }).start();
                    break;
            }
            return true;
        });
        popupMenu.show();
    }

    @OnClick(R.id.btnAddBook)
    void addBook() {
        Observable.create((ObservableOnSubscribe<LibraryAdapter.Library>) emitter -> {
            Book book = new Book();
            book.setName("书籍 " + Math.abs(mRandom.nextInt()));
            ContentValues contentValues = new ContentValues();
            contentValues.put("name", book.getName());
            Uri uri = getContentResolver().insert(Uri.parse("content://me.lynnchurch.assist.provider/book/addBook"), contentValues);
            long id = Long.parseLong(uri.getPath().substring(1));
            Log.i(TAG, "bookId:" + id);
            book.set_id(id);
            LibraryAdapter.Library library = new LibraryAdapter.Library();
            library.type = LibraryAdapter.Library.TYPE_BOOK;
            library.book = book;
            emitter.onNext(library);
        }).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(library -> {
                            mLibraries.add(0, library);
                            mLibraryAdapter.notifyItemInserted(0);
                            mLibraryAdapter.notifyItemRangeChanged(0, mLibraries.size());
                            rvLibrary.scrollToPosition(0);
                        }, throwable -> Log.e(TAG, throwable.getMessage(), throwable)
                        , () -> {
                        });

    }

    @OnClick(R.id.btnAddUser)
    void addUser() {
        Observable.create((ObservableOnSubscribe<LibraryAdapter.Library>) emitter -> {
            User user = new User();
            user.setName("用户 " + Math.abs(mRandom.nextInt()));
            user.setAge(Math.abs(mRandom.nextInt(100)));
            ContentValues contentValues = new ContentValues();
            contentValues.put("name", user.getName());
            contentValues.put("age", user.getAge());
            Uri uri = getContentResolver().insert(Uri.parse("content://me.lynnchurch.assist.provider/user/addUser"), contentValues);
            long id = Long.parseLong(uri.getPath().substring(1));
            Log.i(TAG, "userId:" + id);
            user.set_id(id);
            LibraryAdapter.Library library = new LibraryAdapter.Library();
            library.type = LibraryAdapter.Library.TYPE_USER;
            library.user = user;
            emitter.onNext(library);
        }).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(library -> {
                    mLibraries.add(0, library);
                    mLibraryAdapter.notifyItemInserted(0);
                    mLibraryAdapter.notifyItemRangeChanged(0, mLibraries.size());
                    rvLibrary.scrollToPosition(0);
                }, throwable -> Log.e(TAG, throwable.getMessage(), throwable), () -> {
                });
    }

}
