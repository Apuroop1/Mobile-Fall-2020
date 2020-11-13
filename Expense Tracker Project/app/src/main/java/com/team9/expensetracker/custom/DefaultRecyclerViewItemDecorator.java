package com.team9.expensetracker.custom;

import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

//import android.support.v7.widget.RecyclerView;


public class DefaultRecyclerViewItemDecorator extends RecyclerView.ItemDecoration {

    private final float mVerticalSpaceHeight;

    public DefaultRecyclerViewItemDecorator(float mVerticalSpaceHeight) {
        this.mVerticalSpaceHeight = mVerticalSpaceHeight;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                               RecyclerView.State state) {
        outRect.bottom = (int) mVerticalSpaceHeight;
        if (parent.getChildAdapterPosition(view) == 0) {
            outRect.top = (int) mVerticalSpaceHeight;
        }
    }

}
