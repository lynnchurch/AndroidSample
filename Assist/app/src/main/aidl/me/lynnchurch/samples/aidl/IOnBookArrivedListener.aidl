// IOnBookArrivedListener.aidl
package me.lynnchurch.samples.aidl;

import me.lynnchurch.samples.aidl.Book_AIDL;

interface IOnBookArrivedListener {
    void onBookArrived(in Book_AIDL book_aidl);
}
