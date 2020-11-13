package com.team9.expensetracker.ui.help;

import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;

import com.team9.expensetracker.R;
import com.team9.expensetracker.ui.BaseActivity;

//import android.support.v7.widget.Toolbar;

public class HelpActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setToolbar(toolbar);
    }

}
