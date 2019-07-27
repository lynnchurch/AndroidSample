package me.lynnchurch.samples.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import butterknife.BindView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import me.lynnchurch.samples.R;
import me.lynnchurch.samples.event.SocketEvent;
import me.lynnchurch.samples.service.UDPSocketService;
import me.lynnchurch.samples.utils.RxBus;

public class SocketActivity extends BaseActivity {
    private UDPSocketService mUDPSocketService;
    MenuItem mServerMenuItem;
    MenuItem mClientMenuItem;
    @BindView(R.id.tvHint)
    TextView tvHint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle(getResources().getStringArray(R.array.titles)[3]);
        bindService(new Intent(this, UDPSocketService.class), mServiceConnection, BIND_AUTO_CREATE);
        init();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_socket;
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mUDPSocketService = ((UDPSocketService.SocketBinder) service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    private void init() {
        RxBus.getInstance().register(SocketEvent.class).compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(socketEvent -> {
                    tvHint.setText(socketEvent.msg);
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.socket, menu);
        mServerMenuItem = menu.getItem(0);
        mClientMenuItem = menu.getItem(1);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.server:
                if (item.getTitle().equals(getString(R.string.start_server))) {
                    item.setTitle(R.string.stop_server);
                    mClientMenuItem.setEnabled(false);
                    mUDPSocketService.startServer();
                    tvHint.setText("server is starting ...");
                } else {
                    item.setTitle(R.string.start_server);
                    mClientMenuItem.setEnabled(true);
                    mUDPSocketService.stopServer();
                }
                break;
            case R.id.client:
                if (item.getTitle().equals(getString(R.string.start_client))) {
                    item.setTitle(R.string.stop_client);
                    mServerMenuItem.setEnabled(false);
                    mUDPSocketService.startClient();
                    tvHint.setText("client is starting ...");
                } else {
                    item.setTitle(R.string.start_client);
                    mServerMenuItem.setEnabled(true);
                    mUDPSocketService.stopClient();
                }
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        unbindService(mServiceConnection);
        super.onDestroy();
    }
}
