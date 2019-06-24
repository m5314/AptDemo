package com.moj.aptdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.moj.mojapi.BindView;
import com.moj.mojapi.MojKnife;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.tv)
    TextView mView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MojKnife.bind(this);

        mView.setText("bind success");
    }
}
