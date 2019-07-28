package me.lynnchurch.samples.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.lynnchurch.samples.R;
import me.lynnchurch.samples.bean.NetAddress;

public class ServerListAdapter extends RecyclerView.Adapter<ServerListAdapter.VH> {
    private List<NetAddress> mData;

    public ServerListAdapter(List<NetAddress> data) {
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
        NetAddress netAddress = mData.get(position);
        holder.tvServerInfo.setText(netAddress.toString());
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    class VH extends RecyclerView.ViewHolder {
        @BindView(R.id.tvServerInfo)
        TextView tvServerInfo;
        @BindView(R.id.btnStartSession)
        Button btnStartSession;

        public VH(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
