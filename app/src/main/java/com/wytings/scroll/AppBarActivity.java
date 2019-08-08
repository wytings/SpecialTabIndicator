package com.wytings.scroll;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;

import com.wytings.adapter.MyAdapter;
import com.wytings.special.R;

/**
 * Created by Rex.Wei on 2019-08-08
 *
 * @author weiyuting
 */
public class AppBarActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_bar);

        final RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setAdapter(new MyAdapter(this, 50));

    }
}
