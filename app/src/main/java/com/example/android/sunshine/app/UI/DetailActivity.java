package com.example.android.sunshine.app.UI;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;

import com.example.android.sunshine.app.R;
import com.example.android.sunshine.app.UI.*;

public class DetailActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {

            // 将数据存储到 Parcelable 中，封装到 Bundle 中
            Bundle arguments = new Bundle();
            arguments.putParcelable(com.example.android.sunshine.app.UI.DetailFragment.DETAIL_URI, getIntent().getData());

            // fragment 携带 Bundle 数据并加载在 Activity 中
            com.example.android.sunshine.app.UI.DetailFragment fragment = new com.example.android.sunshine.app.UI.DetailFragment();
            fragment.setArguments(arguments);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.weather_detail_container, fragment)
                    .commit();
        }
    }

}
