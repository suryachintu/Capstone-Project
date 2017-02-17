package com.surya.quakealert;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (savedInstanceState == null) {

            Bundle arguments = new Bundle();

            arguments.putString(getString(R.string.quake_extra),getIntent().getStringExtra(getString(R.string.quake_extra)));

            DetailActivityFragment fragment = new DetailActivityFragment();

            fragment.setArguments(arguments);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.quake_detail_container, fragment)
                    .commit();
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

}
