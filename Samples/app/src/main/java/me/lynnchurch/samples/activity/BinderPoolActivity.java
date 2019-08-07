package me.lynnchurch.samples.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import me.lynnchurch.samples.R;
import me.lynnchurch.samples.aidl.BinderManager;
import me.lynnchurch.samples.aidl.IModuleA;
import me.lynnchurch.samples.aidl.IModuleB;

public class BinderPoolActivity extends BaseActivity {
    private IModuleA mModuleA;
    private IModuleB mModuleB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init() {
        getSupportActionBar().setTitle(BinderPoolActivity.class.getSimpleName());
        initModules();
    }

    private void initModules() {
        new Thread(() -> {
            mModuleA = IModuleA.Stub.asInterface(BinderManager.getInstance(this).queryBinder(BinderManager.BINDER_MODULE_A));
            mModuleB = IModuleB.Stub.asInterface(BinderManager.getInstance(this).queryBinder(BinderManager.BINDER_MODULE_B));
        }).start();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_binder_pool;
    }

    public void accessModuleA(View v) {
        if (null == mModuleA) {
            toast("Module A 未准备好", Toast.LENGTH_SHORT);
            return;
        }
        Single.create((SingleOnSubscribe<String>) emitter -> {
            String msg = mModuleA.hello("Lynn");
            emitter.onSuccess(msg);
        }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(msg -> toast(msg, Toast.LENGTH_LONG));
    }

    public void accessModuleB(View v) {
        if (null == mModuleB) {
            toast("Module B 未准备好", Toast.LENGTH_SHORT);
            return;
        }
        Single.create((SingleOnSubscribe<String>) emitter -> {
            String msg = mModuleB.hello("Jack");
            emitter.onSuccess(msg);
        }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(msg -> toast(msg, Toast.LENGTH_LONG));
    }
}
