package com.team9.expensetracker.interfaces;

import android.content.Intent;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

public interface IFragmentListener {

    void replaceFragment(Fragment fragment, boolean addToBackStack);
    void replaceFragment(int containerId, Fragment fragment, boolean addToBackStack);
    void setResultWithData(int status, Intent intent);
    void setToolbar(Toolbar toolbar);
    void closeActivity();

}
