package com.team9.expensetracker.interfaces;

import android.view.ActionMode;
import android.view.View;

import androidx.annotation.DrawableRes;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.team9.expensetracker.ui.MainActivity;

import java.util.List;


public interface IMainActivityListener {

    void setMode(@MainActivity.NavigationMode int mode);
    void setTabs(List<String> tabList, TabLayout.OnTabSelectedListener onTabSelectedListener);
    void setFAB(@DrawableRes int drawableId, View.OnClickListener onClickListener);
    void setTitle(String title);
    void setPager(ViewPager vp, TabLayout.ViewPagerOnTabSelectedListener viewPagerOnTabSelectedListener);
    void setExpensesSummary(@IDateMode int dateMode);
    ActionMode setActionMode(ActionMode.Callback actionModeCallback);

}
