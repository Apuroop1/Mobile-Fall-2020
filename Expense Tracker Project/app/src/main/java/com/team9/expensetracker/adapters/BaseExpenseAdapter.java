package com.team9.expensetracker.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.team9.expensetracker.ExpenseTrackerApp;
import com.team9.expensetracker.R;
import com.team9.expensetracker.custom.BaseViewHolder;
import com.team9.expensetracker.entities.Expense;
import com.team9.expensetracker.interfaces.IExpensesType;
import com.team9.expensetracker.utils.ExpensesManager;
import com.team9.expensetracker.utils.Util;

import java.util.List;

//import android.support.v4.view.ViewCompat;

//import android.support.v7.widget.RecyclerView;

public class BaseExpenseAdapter<VH extends RecyclerView.ViewHolder> extends BaseExpenseRecyclerViewAdapter<BaseExpenseAdapter.BaseExpenseViewHolder> {

    protected List<Expense> mExpensesList;
    protected int lastPosition = -1;
    protected int colorExpense;
    protected int colorIncome;
    protected String prefixExpense;
    protected String prefixIncome;
    private String titleTransitionName;

    protected BaseViewHolder.RecyclerClickListener onRecyclerClickListener;

    public BaseExpenseAdapter(Context context, BaseViewHolder.RecyclerClickListener onRecyclerClickListener) {
        this.mExpensesList = ExpensesManager.getInstance().getExpensesList();
        this.onRecyclerClickListener = onRecyclerClickListener;
        this.colorExpense = ExpenseTrackerApp.getContext().getResources().getColor(R.color.colorAccentRed);
        this.colorIncome = ExpenseTrackerApp.getContext().getResources().getColor(R.color.colorAccentGreen);
        this.prefixExpense = ExpenseTrackerApp.getContext().getResources().getString(R.string.expense_prefix);
        this.prefixIncome = ExpenseTrackerApp.getContext().getResources().getString(R.string.income_prefix);
        this.titleTransitionName = ExpenseTrackerApp.getContext().getString(R.string.tv_title_transition);
    }

    @Override
    public BaseExpenseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_expense_item, parent, false);
        return new BaseExpenseViewHolder(v, onRecyclerClickListener);
    }

    @Override
    public void onBindViewHolder(BaseExpenseViewHolder holder, int position) {
        holder.itemView.setSelected(isSelected(position));
        final Expense expense = mExpensesList.get(position);
        String prefix = "";
        switch (expense.getType()) {
            case IExpensesType.MODE_EXPENSES:
                holder.tvTotal.setTextColor(colorExpense);
                prefix = String.format(prefixExpense, Util.getFormattedCurrency(expense.getTotal()));
                break;
            case IExpensesType.MODE_INCOME:
                holder.tvTotal.setTextColor(colorIncome);
                prefix = String.format(prefixIncome, Util.getFormattedCurrency(expense.getTotal()));
                break;
        }
        if (expense.getCategory() != null)holder.tvCategory.setText(expense.getCategory().getName());
        if (expense.getDescription() != null && !expense.getDescription().isEmpty()) {
            holder.tvDescription.setText(expense.getDescription());
            holder.tvDescription.setVisibility(View.VISIBLE);
        } else {
            holder.tvDescription.setVisibility(View.GONE);
        }
        holder.tvTotal.setText(prefix);
        holder.itemView.setTag(expense);
        ViewCompat.setTransitionName(holder.tvTotal, titleTransitionName);
    }

    @Override
    public int getItemCount() {
        return mExpensesList.size();
    }

    public void updateExpenses(List<Expense> mExpensesList) {
        this.mExpensesList = mExpensesList;
        notifyDataSetChanged();
    }

    protected void setAnimation(BaseExpenseViewHolder holder, int position) {
        if (position > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(ExpenseTrackerApp.getContext(), R.anim.push_left_in);
            holder.itemView.startAnimation(animation);
            lastPosition = position;
        }
    }

    public static class BaseExpenseViewHolder extends BaseViewHolder {

        public TextView tvCategory;
        public TextView tvDescription;
        public TextView tvTotal;

        public BaseExpenseViewHolder(View v, RecyclerClickListener onRecyclerClickListener) {
            super(v, onRecyclerClickListener);
            tvCategory = (TextView)v.findViewById(R.id.tv_category);
            tvDescription = (TextView)v.findViewById(R.id.tv_description);
            tvTotal = (TextView)v.findViewById(R.id.tv_total);
        }

    }


}

