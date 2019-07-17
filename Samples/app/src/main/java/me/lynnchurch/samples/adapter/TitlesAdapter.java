package me.lynnchurch.samples.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import me.lynnchurch.samples.R;

public class TitlesAdapter extends RecyclerView.Adapter<TitlesAdapter.VH> {
    private String[] data;
    private OnItemClickListener onItemClickListener;

    public TitlesAdapter(@NonNull String [] data) {
        this.data = data;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.titles_rv_item, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, final int position) {
        holder.tvTitle.setText(data[position]);
        if(null!=onItemClickListener){
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onItemClickListener.onItemClick(view, position);
                }
            });
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    onItemClickListener.onItemLongClick(view, position);
                    return true;
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return data.length;
    }

    class VH extends RecyclerView.ViewHolder {
        public final TextView tvTitle;

        public VH(View v) {
            super(v);
            tvTitle = v.findViewById(R.id.tvTitle);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
        void onItemLongClick(View view, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }
}
