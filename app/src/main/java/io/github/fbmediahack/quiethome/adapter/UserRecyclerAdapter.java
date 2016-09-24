package io.github.fbmediahack.quiethome.adapter;

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.matteo.firebase_recycleview.FirebaseRecyclerAdapter;
import com.google.firebase.database.Query;

import java.util.ArrayList;

import io.github.fbmediahack.quiethome.R;
import io.github.fbmediahack.quiethome.model.User;

public class UserRecyclerAdapter extends FirebaseRecyclerAdapter<UserRecyclerAdapter.ViewHolder, User> {
    public UserRecyclerAdapter(Query query, Class<User> itemClass) {
        super(query, itemClass);
    }

    public UserRecyclerAdapter(Query query, Class<User> itemClass, @Nullable ArrayList<User> items, @Nullable ArrayList<String> keys) {
        super(query, itemClass, items, keys);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.user_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        User item = getItem(position);
        holder.isSleeping.setText(item.isSleeping ? "Zzzzz" : "Awake");
        holder.userName.setText(item.displayName);
    }

    @Override
    protected void itemAdded(User item, String key, int position) {
    }

    @Override
    protected void itemChanged(User oldItem, User newItem, String key, int position) {

    }

    @Override
    protected void itemRemoved(User item, String key, int position) {

    }

    @Override
    protected void itemMoved(User item, String key, int oldPosition, int newPosition) {

    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView userName;
        TextView isSleeping;

        ViewHolder(View itemView) {
            super(itemView);

            userName = (TextView) itemView.findViewById(R.id.text_user_name);
            isSleeping = (TextView) itemView.findViewById(R.id.text_is_sleeping);
        }
    }
}
