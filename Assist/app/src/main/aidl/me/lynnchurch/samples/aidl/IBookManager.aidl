// IBookManager.aidl
package me.lynnchurch.samples.aidl;

// Declare any non-default types here with import statements

import me.lynnchurch.samples.aidl.Book_AIDL;
import me.lynnchurch.samples.aidl.IOnBookArrivedListener;

interface IBookManager {
    List<Book_AIDL> getBookList();
    void addBook(in Book_AIDL bookAIDL);
    void delBook(in long bookId);
    void addIOnBookArrivedListener(in IOnBookArrivedListener iOnBookArrivedListener);
    void removeIOnBookArrivedListener(in IOnBookArrivedListener iOnBookArrivedListener);
}
