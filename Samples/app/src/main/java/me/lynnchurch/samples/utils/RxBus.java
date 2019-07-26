/*
 * Created by 邱志立 on 17-2-21 下午10:13
 * Copyright (c) 2017. All rights reserved.
 */

package me.lynnchurch.samples.utils;

import io.reactivex.Flowable;
import io.reactivex.annotations.NonNull;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;

/**
 * 替代EventBus
 */
public class RxBus
{
    private final FlowableProcessor<Object> mBus;

    private RxBus()
    {
        mBus = PublishProcessor.create().toSerialized();
    }

    private static class Holder
    {
        private static final RxBus instance = new RxBus();
    }

    public static RxBus getInstance()
    {
        return Holder.instance;
    }

    public void post(@NonNull Object obj)
    {
        mBus.onNext(obj);
    }

    public <T> Flowable<T> register(Class<T> clz)
    {
        return mBus.ofType(clz);
    }

    public Flowable<Object> register()
    {
        return mBus;
    }

    public void unregisterAll()
    {
        // 将所有由mBus生成的Flowable都置completed状态,后续的所有消息将无法收到
        mBus.onComplete();
    }

    public boolean hasSubscribers()
    {
        return mBus.hasSubscribers();
    }
}