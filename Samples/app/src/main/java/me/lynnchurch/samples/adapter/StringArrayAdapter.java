package me.lynnchurch.samples.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import me.lynnchurch.samples.R;

public class StringArrayAdapter extends RecyclerView.Adapter<StringArrayAdapter.VH> {
    private String[] mData;
    private OnItemClickListener mOnItemClickListener;

    public StringArrayAdapter(@NonNull String[] data) {
        mData = data;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.string_array_rv_item, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        TextView textView = (TextView) holder.itemView;
        textView.setText(mData[position]);
        if (null != mOnItemClickListener) {
            textView.setOnClickListener(v -> mOnItemClickListener.onItemClick((TextView) v, position));
        }
    }

    @Override
    public int getItemCount() {
        return mData.length;
    }

    class VH extends RecyclerView.ViewHolder {
        public VH(@NonNull View itemView) {
            super(itemView);
        }
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(TextView v, int position);
    }
}
