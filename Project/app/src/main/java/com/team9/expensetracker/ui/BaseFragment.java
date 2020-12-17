package com.team9.expensetracker.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.team9.expensetracker.R;
import com.team9.expensetracker.interfaces.IFragmentListener;


public class BaseFragment extends Fragment {

    protected IFragmentListener mFragmentListener;
    protected Toolbar mToolbar;

    public View onCreateFragmentView(@LayoutRes int layoutId, LayoutInflater inflater, ViewGroup container, boolean withToolbar) {
        if (!withToolbar) {
            return inflater.inflate(layoutId, container, false);
        }
        View viewWithToolbar = inflater.inflate(R.layout.fragment_base, container, false);
        ViewGroup llMainContainer = (ViewGroup) viewWithToolbar.findViewById(R.id.ll_container);
        View content = inflater.inflate(layoutId, container, false);
        llMainContainer.addView(content);

        mToolbar = (Toolbar) viewWithToolbar.findViewById(R.id.toolbar);
        mFragmentListener.setToolbar(mToolbar);
        return viewWithToolbar;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mFragmentListener = (IFragmentListener)context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mFragmentListener = null;
    }

    public void setTitle(String title) {
       if (getActivity() != null && getActivity() instanceof BaseActivity) {
           ((BaseActivity)getActivity()).getSupportActionBar().setTitle(title);
       }
    }
}
