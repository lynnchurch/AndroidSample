// IBookManager.aidl
package me.lynnchurch.samples.aidl;

// Declare any non-default types here with import statements

import me.lynnchurch.samples.aidl.Book;

interface IBookManager {
    List<Book> getBookList();
    void addBook(in Book book);
    void delBook(in int position);
}
