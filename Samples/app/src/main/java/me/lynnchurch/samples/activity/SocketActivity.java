package me.lynnchurch.samples.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import me.lynnchurch.samples.R;
import me.lynnchurch.samples.adapter.ServerListAdapter;
import me.lynnchurch.samples.bean.NetAddress;
import me.lynnchurch.samples.bean.ServerItem;
import me.lynnchurch.samples.event.SocketEvent;
import me.lynnchurch.samples.service.LANSocketService;
import me.lynnchurch.samples.utils.RxBus;

public class SocketActivity extends BaseActivity {
    private LANSocketService mLANSocketService;
    MenuItem mServerMenuItem;
    MenuItem mClientMenuItem;
    MenuItem mSessionMenuItem;
    @BindView(R.id.tvHint)
    TextView tvHint;
    @BindView(R.id.rvServerList)
    RecyclerView rvServerList;
    private List<ServerItem> mServerList = new ArrayList<>();
    private ServerListAdapter mServerListAdapter = new ServerListAdapter(mServerList);
    private Disposable mClearInvalidServerAddressDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle(getResources().getStringArray(R.array.titles)[3]);
        bindService(new Intent(this, LANSocketService.class), mServiceConnection, BIND_AUTO_CREATE);
        init();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_socket;
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mLANSocketService = ((LANSocketService.SocketBinder) service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    private void init() {
        RxBus.getInstance().register(SocketEvent.class).compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(socketEvent -> {
                    switch (socketEvent.code) {
                        case SocketEvent.CODE_PLAIN:
                            tvHint.setText(socketEvent.msg);
                            break;
                        case SocketEvent.CODE_TCP_SERVER_ADDRESS:
                            showServerList();
                            NetAddress netAddress = new Gson().fromJson(socketEvent.data, NetAddress.class);
                            ServerItem serverItem = new ServerItem(netAddress, System.currentTimeMillis());
                            if (!mServerList.contains(serverItem)) {
                                mServerList.add(0, serverItem);
                            } else {
                                for (int i = 0; i < mServerList.size(); i++) {
                                    ServerItem serverItem1 = mServerList.get(i);
                                    if (serverItem.equals(serverItem1)) {
                                        mServerList.set(i, serverItem);
                                    }
                                }
                            }
                            mServerListAdapter.notifyDataSetChanged();
                            break;
                    }
                });

        rvServerList.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        mServerListAdapter.setStartSessionListenner(netAddress -> {
            mLANSocketService.startTcpClient(netAddress.getIp(), netAddress.getPort());
        });
        rvServerList.setAdapter(mServerListAdapter);
        rvServerList.addItemDecoration(new DividerItemDecoration(this, RecyclerView.VERTICAL));
    }

    private void clearInvalidServerAddress() {
        mClearInvalidServerAddressDisposable = Observable.interval(3, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread())
                .subscribe(aLong -> {
                    Iterator<ServerItem> serverItemIterator = mServerList.iterator();
                    while (serverItemIterator.hasNext()) {
                        ServerItem serverItem = serverItemIterator.next();
                        if (System.currentTimeMillis() - serverItem.getFoundTime() > 6000) {
                            serverItemIterator.remove();
                            mServerListAdapter.notifyDataSetChanged();
                            return;
                        }
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.socket, menu);
        mServerMenuItem = menu.getItem(0);
        mClientMenuItem = menu.getItem(1);
        mSessionMenuItem = menu.getItem(2);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.server:
                if (item.getTitle().equals(getString(R.string.start_server))) {
                    item.setTitle(R.string.stop_server);
                    mClientMenuItem.setEnabled(false);
                    mLANSocketService.startServer();
                    tvHint.setText("server is running ...");
                } else {
                    item.setTitle(R.string.start_server);
                    mClientMenuItem.setEnabled(true);
                    mLANSocketService.stopServer();
                }
                break;
            case R.id.client:
                if (item.getTitle().equals(getString(R.string.start_client))) {
                    item.setTitle(R.string.stop_client);
                    mServerMenuItem.setEnabled(false);
                    mLANSocketService.startClient();
                    clearInvalidServerAddress();
                    tvHint.setText("client is running ...");
                } else {
                    mClearInvalidServerAddressDisposable.dispose();
                    showHint();
                    item.setTitle(R.string.start_client);
                    mServerMenuItem.setEnabled(true);
                    mLANSocketService.stopClient();
                }
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void showServerList() {
        if (View.VISIBLE != rvServerList.getVisibility()) {
            rvServerList.setVisibility(View.VISIBLE);
            tvHint.setText("");
            tvHint.setVisibility(View.GONE);
        }
    }

    private void showHint() {
        if (View.VISIBLE != tvHint.getVisibility()) {
            mServerList.clear();
            mServerListAdapter.notifyDataSetChanged();
            rvServerList.setVisibility(View.GONE);
            tvHint.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onDestroy() {
        unbindService(mServiceConnection);
        super.onDestroy();
    }
}
