package com.moj.aptdemo;

import android.os.Bundle;
import android.widget.TextView;

import com.moj.mojapi.BindView;

/**
 * @author : moj
 * @date : 2019/6/24
 * @description :
 */
public class Main2Activity extends MainActivity{

    @BindView(R.id.tv)
    TextView v;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        v.setText("bind success  2");
    }
}
