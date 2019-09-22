package com.wytings.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wytings.R;
import com.wytings.special.util.LogWrapper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rex.Wei on 2019-08-06
 *
 * @author weiyuting
 */
public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyHolder> {

    private List<String> modelList = new ArrayList<>();

    public MyAdapter(Context context, int count) {
        for (int i = 1; i <= count; i++) {
            modelList.add(context.getString(R.string.app_name) + i);
        }
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull final RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        // recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));
    }

    @Override
    @NonNull
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyHolder(LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        holder.titleText.setText(modelList.get(position));
        holder.itemView.setBackgroundColor(position % 2 == 0 ? Color.LTGRAY : Color.WHITE);
        //LogWrapper.d("MyAdapter - onBindViewHolder " + holder.titleText.getText());
    }

    @Override
    public int getItemCount() {
        return modelList.size();
    }

    static class MyHolder extends RecyclerView.ViewHolder {

        final TextView titleText;

        MyHolder(final View itemView) {
            super(itemView);
            this.titleText = itemView.findViewById(android.R.id.text1);
            this.titleText.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.colorAccent));
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    LogWrapper.d("MyAdapter - " + titleText.getText());
                }
            });
        }
    }
}


