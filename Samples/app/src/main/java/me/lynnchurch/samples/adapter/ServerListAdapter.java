package me.lynnchurch.samples.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.lynnchurch.samples.R;
import me.lynnchurch.samples.bean.NetAddress;
import me.lynnchurch.samples.bean.ServerItem;

public class ServerListAdapter extends RecyclerView.Adapter<ServerListAdapter.VH> {
    private List<ServerItem> mData;
    private StartSessionListenner mStartSessionListenner;
    private SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public ServerListAdapter(List<ServerItem> data) {
        mData = data;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.server_list_rv_item, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        ServerItem serverItem = mData.get(position);
        NetAddress netAddress = serverItem.getNetAddress();
        holder.tvServerInfo.setText(netAddress.toString());
        holder.tvFoundTime.setText(mSimpleDateFormat.format(new Date(serverItem.getFoundTime())));
        holder.btnStartSession.setOnClickListener(v -> {
            if (null != mStartSessionListenner) {
                mStartSessionListenner.onStartSession(netAddress);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    class VH extends RecyclerView.ViewHolder {
        @BindView(R.id.tvServerInfo)
        TextView tvServerInfo;
        @BindView(R.id.tvFoundTime)
        TextView tvFoundTime;
        @BindView(R.id.btnStartSession)
        Button btnStartSession;

        public VH(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public void setStartSessionListenner(StartSessionListenner startSessionListenner) {
        mStartSessionListenner = startSessionListenner;
    }

    public interface StartSessionListenner {
        void onStartSession(NetAddress netAddress);
    }
}
