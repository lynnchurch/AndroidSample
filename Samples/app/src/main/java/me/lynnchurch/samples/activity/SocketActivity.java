package me.lynnchurch.samples.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.Group;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import me.lynnchurch.samples.R;
import me.lynnchurch.samples.adapter.ServerListAdapter;
import me.lynnchurch.samples.adapter.SocketMessageAdapter;
import me.lynnchurch.samples.bean.NetAddress;
import me.lynnchurch.samples.bean.ServerItem;
import me.lynnchurch.samples.bean.SocketMessage;
import me.lynnchurch.samples.event.SocketEvent;
import me.lynnchurch.samples.service.LanSocketService;
import me.lynnchurch.samples.utils.RxBus;

public class SocketActivity extends BaseActivity {
    private LanSocketService mLanSocketService;
    MenuItem mServerMenuItem;
    MenuItem mClientMenuItem;
    MenuItem mSessionMenuItem;
    @BindView(R.id.tvHint)
    TextView tvHint;
    @BindView(R.id.rvServerList)
    RecyclerView rvServerList;
    @BindView(R.id.rvMessage)
    RecyclerView rvMessage;
    @BindView(R.id.etInput)
    EditText etInput;
    @BindView(R.id.groupSession)
    Group groupSession;

    private List<ServerItem> mServerList = new ArrayList<>();
    private ServerListAdapter mServerListAdapter = new ServerListAdapter(mServerList);
    private boolean mIsServerRunning = false;
    private boolean mIsClientRunning = false;
    private List<SocketMessage> mSocketMessages = new ArrayList<>();
    private SocketMessageAdapter mSocketMessageAdapter = new SocketMessageAdapter(mSocketMessages);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle(getResources().getStringArray(R.array.titles)[3]);
        bindService(new Intent(this, LanSocketService.class), mServiceConnection, BIND_AUTO_CREATE);
        init();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_socket;
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mLanSocketService = ((LanSocketService.SocketBinder) service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    private void init() {
        initServerList();
        initMessageList();
        RxBus.getInstance().register(SocketEvent.class).compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(socketEvent -> {
                    switch (socketEvent.code) {
                        case SocketEvent.CODE_PLAIN:
                            tvHint.setText(socketEvent.msg);
                            break;
                        case SocketEvent.CODE_TCP_SERVER_ADDRESS:
                            if (groupSession.getVisibility() == View.VISIBLE || !mIsClientRunning) {
                                return;
                            }
                            showServerList();
                            NetAddress netAddress = new Gson().fromJson(new String(socketEvent.data), NetAddress.class);
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
                        case SocketEvent.CODE_TCP_SERVER_CONNECTED:
                        case SocketEvent.CODE_TCP_NEW_CLIENT_CONNECTED:
                            showSession();
                            break;
                        case SocketEvent.CODE_TCP_CLIENT_DEAD:
                            NetAddress netAddress1 = new Gson().fromJson(new String(socketEvent.data), NetAddress.class);
                            String clientDeadMsg = String.format(getString(R.string.client_dead), netAddress1.getIp() + ":" + netAddress1.getPort());
                            toast(clientDeadMsg, Toast.LENGTH_LONG);
                            break;
                        case SocketEvent.CODE_TCP_SERVER_DEAD:
                            toast(R.string.server_dead, Toast.LENGTH_LONG);
                            stopClient();
                            break;
                    }
                });
    }

    private void initServerList() {
        rvServerList.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        mServerListAdapter.setStartSessionListenner(netAddress -> {
            mLanSocketService.startTcpClient(netAddress.getIp(), netAddress.getPort());
        });
        rvServerList.setAdapter(mServerListAdapter);
        rvServerList.addItemDecoration(new DividerItemDecoration(this, RecyclerView.VERTICAL));
    }

    private void initMessageList() {
        rvMessage.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        rvMessage.setAdapter(mSocketMessageAdapter);

        RxBus.getInstance().register(SocketMessage.class).compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(socketMessage -> {
                    mSocketMessages.add(socketMessage);
                    mSocketMessageAdapter.notifyDataSetChanged();
                    rvMessage.scrollToPosition(mSocketMessages.size() - 1);
                });
    }

    private void clearInvalidServerAddress() {
        Observable.interval(3, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread())
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
                    mIsServerRunning = true;
                    item.setTitle(R.string.stop_server);
                    mClientMenuItem.setEnabled(false);
                    mLanSocketService.startServer();
                    tvHint.setText("server is running ...");
                } else {
                    stopServer();
                }
                break;
            case R.id.client:
                if (item.getTitle().equals(getString(R.string.start_client))) {
                    mIsClientRunning = true;
                    item.setTitle(R.string.stop_client);
                    mServerMenuItem.setEnabled(false);
                    mLanSocketService.startClient();
                    clearInvalidServerAddress();
                    tvHint.setText("client is running ...");
                } else {
                    stopClient();
                }
                break;
            case R.id.session:
                if (mIsServerRunning) {
                    stopServer();
                }
                if (mIsClientRunning) {
                    stopClient();
                }
                groupSession.setVisibility(View.INVISIBLE);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void stopServer() {
        mIsServerRunning = false;
        showHint();
        mServerMenuItem.setTitle(R.string.start_server);
        mClientMenuItem.setEnabled(true);
        mLanSocketService.stopServer();
        mSocketMessages.clear();
        mSocketMessageAdapter.notifyDataSetChanged();
    }

    private void stopClient() {
        mIsClientRunning = false;
        mClientMenuItem.setTitle(R.string.start_client);
        mServerMenuItem.setEnabled(true);
        mLanSocketService.stopClient();
        mSocketMessages.clear();
        mSocketMessageAdapter.notifyDataSetChanged();
        showHint();
    }

    private void showServerList() {
        if (View.VISIBLE != rvServerList.getVisibility()) {
            rvServerList.setVisibility(View.VISIBLE);
            tvHint.setText("");
            tvHint.setVisibility(View.INVISIBLE);
            groupSession.setVisibility(View.INVISIBLE);
            mServerMenuItem.setVisible(true);
            mClientMenuItem.setVisible(true);
            mSessionMenuItem.setVisible(false);
        }
    }

    private void showHint() {
        if (View.VISIBLE != tvHint.getVisibility()) {
            mServerList.clear();
            mServerListAdapter.notifyDataSetChanged();
            groupSession.setVisibility(View.INVISIBLE);
            rvServerList.setVisibility(View.INVISIBLE);
            tvHint.setVisibility(View.VISIBLE);
            mServerMenuItem.setVisible(true);
            mClientMenuItem.setVisible(true);
            mSessionMenuItem.setVisible(false);
        }
    }

    private void showSession() {
        if (View.VISIBLE != groupSession.getVisibility()) {
            mServerList.clear();
            mServerListAdapter.notifyDataSetChanged();
            rvServerList.setVisibility(View.INVISIBLE);
            tvHint.setVisibility(View.INVISIBLE);
            mServerMenuItem.setVisible(false);
            mClientMenuItem.setVisible(false);
            mSessionMenuItem.setVisible(true);
            groupSession.setVisibility(View.VISIBLE);
            if (mIsServerRunning) {
                etInput.setHint(R.string.please_send_to_client);
            }
            if (mIsClientRunning) {
                etInput.setHint(R.string.please_send_to_server);
            }
        }
    }

    @OnClick(R.id.btnSend)
    void sendMessage() {
        String text = etInput.getText().toString();
        if (mIsServerRunning) {
            mLanSocketService.serverSendTextMsg(text);
        } else if (mIsClientRunning) {
            mLanSocketService.clientSendTextMsg(text);
        }
        etInput.setText("");
    }

    @Override
    protected void onDestroy() {
        unbindService(mServiceConnection);
        super.onDestroy();
    }
}
