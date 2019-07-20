// IBookManager.aidl
package me.lynnchurch.samples.aidl;

// Declare any non-default types here with import statements

import me.lynnchurch.samples.aidl.Book_AIDL;

interface IBookManager {
    List<Book_AIDL> getBookList();
    void addBook(in Book_AIDL bookAIDL);
    void delBook(in long bookId);
}
