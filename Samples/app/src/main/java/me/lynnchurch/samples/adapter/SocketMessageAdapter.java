package me.lynnchurch.samples.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import me.lynnchurch.samples.bean.SocketMessage;

public class SocketMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int ITEM_VIEW_TYPE_LEFT = 0;
    private static final int ITEM_VIEW_TYPE_RIGHT = 1;
    private SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private List<SocketMessage> mData;

    public SocketMessageAdapter(List<SocketMessage> data) {
        mData = data;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (ITEM_VIEW_TYPE_LEFT == viewType) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.socket_message_left_rv_item, parent, false);
            return new FromVH(view);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.socket_message_right_rv_item, parent, false);
            return new ToVH(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        SocketMessage socketMessage = mData.get(position);
        int itemViewType = getItemViewType(position);
        if (ITEM_VIEW_TYPE_LEFT == itemViewType) {
            FromVH fromVH = (FromVH) holder;
            NetAddress netAddress = socketMessage.getLeft();
            fromVH.tvFrom.setText(netAddress.getIp() + ":" + netAddress.getPort());
            fromVH.tvMsg.setText(socketMessage.getMsg());
            fromVH.tvTime.setText(mSimpleDateFormat.format(new Date(socketMessage.getTime())));
        } else {
            ToVH toVH = (ToVH) holder;
            NetAddress netAddress = socketMessage.getRight();
            toVH.tvFrom.setText(netAddress.getIp() + ":" + netAddress.getPort());
            toVH.tvMsg.setText(socketMessage.getMsg());
            toVH.tvTime.setText(mSimpleDateFormat.format(new Date(socketMessage.getTime())));
        }
    }

    @Override
    public int getItemViewType(int position) {
        SocketMessage socketMessage = mData.get(position);
        if (null == socketMessage.getLeft()) {
            return ITEM_VIEW_TYPE_RIGHT;
        } else {
            return ITEM_VIEW_TYPE_LEFT;
        }
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    class FromVH extends RecyclerView.ViewHolder {
        @BindView(R.id.tvFrom)
        TextView tvFrom;
        @BindView(R.id.tvMsg)
        TextView tvMsg;
        @BindView(R.id.tvTime)
        TextView tvTime;

        public FromVH(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    class ToVH extends RecyclerView.ViewHolder {
        @BindView(R.id.tvFrom)
        TextView tvFrom;
        @BindView(R.id.tvMsg)
        TextView tvMsg;
        @BindView(R.id.tvTime)
        TextView tvTime;

        public ToVH(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
