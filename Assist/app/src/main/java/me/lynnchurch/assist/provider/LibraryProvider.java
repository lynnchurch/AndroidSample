package me.lynnchurch.assist.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

import me.lynnchurch.samples.db.LynnDatabase;
import me.lynnchurch.samples.db.entity.Book;
import me.lynnchurch.samples.db.entity.User;

public class LibraryProvider extends ContentProvider {
    private static final String TAG = LibraryProvider.class.getSimpleName();

    public static final String RESULT = "result";
    public static final String AUTHORITY = "me.lynnchurch.assist.provider";
    private static final String CONTENT_SCHEMA = "content://";
    public static final Uri BOOK_GET_BOOKS_URI = Uri.parse(CONTENT_SCHEMA + AUTHORITY + "/book/getBooks");
    public static final Uri BOOK_ADD_BOOK_URI = Uri.parse(CONTENT_SCHEMA + AUTHORITY + "/book/addBook");
    public static final Uri BOOK_DEL_BOOK_URI = Uri.parse(CONTENT_SCHEMA + AUTHORITY + "/book/delBook");
    public static final Uri USER_GET_USERS_URI = Uri.parse(CONTENT_SCHEMA + AUTHORITY + "/user/getUsers");
    public static final Uri USER_ADD_USER_URI = Uri.parse(CONTENT_SCHEMA + AUTHORITY + "/user/addUser");
    public static final Uri USER_DEL_USER_URI = Uri.parse(CONTENT_SCHEMA + AUTHORITY + "/user/delUser");

    public static final int BOOK_GET_BOOKS_CODE = 0;
    public static final int BOOK_ADD_BOOK_CODE = 1;
    public static final int BOOK_DEL_BOOK_CODE = 2;
    public static final int USER_GET_USERS_CODE = 3;
    public static final int USER_ADD_USER_CODE = 4;
    public static final int USER_DEL_USER_CODE = 5;

    private static final UriMatcher mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private static final RemoteMethodManager remoteMethodManager = new RemoteMethodManager();

    static {
        mUriMatcher.addURI(AUTHORITY, "book/getBooks", BOOK_GET_BOOKS_CODE);
        mUriMatcher.addURI(AUTHORITY, "book/addBook", BOOK_ADD_BOOK_CODE);
        mUriMatcher.addURI(AUTHORITY, "book/delBook", BOOK_DEL_BOOK_CODE);
        mUriMatcher.addURI(AUTHORITY, "user/getUsers", USER_GET_USERS_CODE);
        mUriMatcher.addURI(AUTHORITY, "user/addUser", USER_ADD_USER_CODE);
        mUriMatcher.addURI(AUTHORITY, "user/delUser", USER_DEL_USER_CODE);
    }

    private Context mContext;


    @Override
    public boolean onCreate() {
        Log.i(TAG, "onCreate, current thread:" + Thread.currentThread().getName());
        mContext = getContext();
        initRemoteMethods();
        return true;
    }

    private void initRemoteMethods() {
        RemoteMethod hello = new RemoteMethod() {
            @Override
            public String getMethodName() {
                return "hello";
            }

            @Override
            public Bundle invoke(@Nullable String arg, @Nullable Bundle extras) {
                Bundle bundle = new Bundle();
                bundle.putString(RESULT, "Hello " + arg + "!");
                return bundle;
            }
        };
        remoteMethodManager.put(hello);

        RemoteMethod getBooks = new RemoteMethod() {
            @Override
            public String getMethodName() {
                return "getBooks";
            }

            @Override
            public Bundle invoke(@Nullable String arg, @Nullable Bundle extras) {
                Bundle bundle = new Bundle();
                ArrayList<Book> books = new ArrayList<>();
                books.addAll(LynnDatabase.getInstance(mContext).getBookDao().getBooks());
                bundle.putParcelableArrayList(RESULT, books);
                return null;
            }
        };
        remoteMethodManager.put(getBooks);

        RemoteMethod addBook = new RemoteMethod() {
            @Override
            public String getMethodName() {
                return "addBook";
            }

            @Override
            public Bundle invoke(@Nullable String arg, @Nullable Bundle extras) {
                Book book = extras.getParcelable("book");
                long bookId = LynnDatabase.getInstance(mContext).getBookDao().addBook(book);
                Bundle bundle = new Bundle();
                bundle.putLong(RESULT, bookId);
                return bundle;
            }
        };
        remoteMethodManager.put(addBook);

        RemoteMethod delBook = new RemoteMethod() {
            @Override
            public String getMethodName() {
                return "delBook";
            }

            @Override
            public Bundle invoke(@Nullable String arg, @Nullable Bundle extras) {
                Book book = new Book();
                book.set_id(extras.getLong("id"));
                LynnDatabase.getInstance(mContext).getBookDao().delBook(book);
                return null;
            }
        };
        remoteMethodManager.put(delBook);

        RemoteMethod getUsers = new RemoteMethod() {
            @Override
            public String getMethodName() {
                return "getUsers";
            }

            @Override
            public Bundle invoke(@Nullable String arg, @Nullable Bundle extras) {
                Bundle bundle = new Bundle();
                ArrayList<User> users = new ArrayList<>();
                users.addAll(LynnDatabase.getInstance(mContext).getUserDao().getUsers());
                bundle.putParcelableArrayList(RESULT, users);
                return null;
            }
        };
        remoteMethodManager.put(getUsers);

        RemoteMethod addUser = new RemoteMethod() {
            @Override
            public String getMethodName() {
                return "addUser";
            }

            @Override
            public Bundle invoke(@Nullable String arg, @Nullable Bundle extras) {
                User user = extras.getParcelable("user");
                long userId = LynnDatabase.getInstance(mContext).getUserDao().addUser(user);
                Bundle bundle = new Bundle();
                bundle.putLong(RESULT, userId);
                return bundle;
            }
        };
        remoteMethodManager.put(addUser);

        RemoteMethod delUser = new RemoteMethod() {
            @Override
            public String getMethodName() {
                return "delUser";
            }

            @Override
            public Bundle invoke(@Nullable String arg, @Nullable Bundle extras) {
                User user = new User();
                user.set_id(extras.getLong("id"));
                LynnDatabase.getInstance(mContext).getUserDao().delUser(user);
                return null;
            }
        };
        remoteMethodManager.put(delUser);
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        Cursor cursor;
        switch (mUriMatcher.match(uri)) {
            case BOOK_GET_BOOKS_CODE:
                cursor = LynnDatabase.getInstance(mContext).getBookDao().getBooksCursor();
                break;
            case USER_GET_USERS_CODE:
                cursor = LynnDatabase.getInstance(mContext).getUserDao().getUsersCursor();
                break;
            default:
                cursor = null;
        }
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        switch (mUriMatcher.match(uri)) {
            case BOOK_ADD_BOOK_CODE:
                Book book = new Book();
                book.setName(values.getAsString("name"));
                long bookId = LynnDatabase.getInstance(mContext).getBookDao().addBook(book);
                uri = Uri.parse("content://me.lynnchurch.assist.provider/" + bookId);
                return uri;
            case USER_ADD_USER_CODE:
                User user = new User();
                user.setName(values.getAsString("name"));
                user.setAge(values.getAsInteger("age"));
                long userId = LynnDatabase.getInstance(mContext).getUserDao().addUser(user);
                uri = Uri.parse("content://me.lynnchurch.assist.provider/" + userId);
                return uri;
            default:
                return null;
        }
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        switch (mUriMatcher.match(uri)) {
            case BOOK_DEL_BOOK_CODE:
                Book book = new Book();
                book.set_id(Long.parseLong(selectionArgs[0]));
                LynnDatabase.getInstance(mContext).getBookDao().delBook(book);
                return 1;
            case USER_DEL_USER_CODE:
                User user = new User();
                user.set_id(Long.parseLong(selectionArgs[0]));
                LynnDatabase.getInstance(mContext).getUserDao().delUser(user);
                return 1;
            default:
                return 0;
        }
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Nullable
    @Override
    public Bundle call(@NonNull String method, @Nullable String arg, @Nullable Bundle extras) {
        RemoteMethod remoteMethod = remoteMethodManager.get(method);
        return remoteMethod == null ? null : remoteMethod.invoke(arg, extras);
    }

}
