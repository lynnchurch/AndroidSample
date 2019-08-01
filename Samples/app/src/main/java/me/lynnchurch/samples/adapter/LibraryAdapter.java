package me.lynnchurch.samples.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import me.lynnchurch.samples.bean.User;
import me.lynnchurch.samples.R;
import me.lynnchurch.samples.db.entity.Book;

public class LibraryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<Library> mData;
    private OnItemClickListener mOnItemClickListener;

    public LibraryAdapter(List<Library> data) {
        mData = data;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder;
        switch (viewType) {
            case Library.TYPE_BOOK:
                View bookView = LayoutInflater.from(parent.getContext()).inflate(R.layout.book_rv_item, parent, false);
                viewHolder = new BookViewHolder(bookView);
                break;
            case Library.TYPE_USER:
                View userView = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_rv_item, parent, false);
                viewHolder = new UserViewHolder(userView);
                break;
            default:
                viewHolder = null;
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        switch (getItemViewType(position)) {
            case Library.TYPE_BOOK:
                Book book = mData.get(position).book;
                BookViewHolder bookViewHolder = (BookViewHolder) holder;
                bookViewHolder.tvName.setText(book.getName());
                break;
            case Library.TYPE_USER:
                User user = mData.get(position).user;
                UserViewHolder userViewHolder = (UserViewHolder) holder;
                userViewHolder.tvName.setText(user.getName());
                userViewHolder.tvAge.setText("年龄：" + user.getAge());
                break;

        }
        if (null != mOnItemClickListener) {
            holder.itemView.setOnClickListener(v -> mOnItemClickListener.onItemClick(v, position));
            holder.itemView.setOnLongClickListener(v -> {
                mOnItemClickListener.onItemLongClick(v, position);
                return true;
            });
        }
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    @Override
    public int getItemViewType(int position) {
        return mData.get(position).type;
    }

    class BookViewHolder extends RecyclerView.ViewHolder {
        public TextView tvName;

        public BookViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = (TextView) itemView;
        }
    }

    class UserViewHolder extends RecyclerView.ViewHolder {
        public TextView tvName;
        public TextView tvAge;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvAge = itemView.findViewById(R.id.tvAge);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View v, int position);

        void onItemLongClick(View v, int position);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    public static class Library {
        public static final int TYPE_BOOK = 0;
        public static final int TYPE_USER = 1;
        public int type;
        public Book book;
        public User user;
    }
}
