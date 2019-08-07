// IBinderPool.aidl
package me.lynnchurch.samples.aidl;

// Declare any non-default types here with import statements

interface IBinderPool {
    IBinder queryBinder(int binderCode);
}
