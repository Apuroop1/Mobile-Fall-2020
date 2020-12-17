package com.team9.expensetracker.ui.accounts;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.team9.expensetracker.ExpenseTrackerApp;
import com.team9.expensetracker.R;
import com.team9.expensetracker.adapters.BaseRecyclerViewAdapter;
import com.team9.expensetracker.custom.BaseViewHolder;
import com.team9.expensetracker.entities.Account;

import java.util.List;

class AccountsAdapter extends BaseRecyclerViewAdapter<AccountsAdapter.ViewHolder> {
    private List<Account> mAccountsList;
    private int lastPosition = -1;
    private BaseViewHolder.RecyclerClickListener onRecyclerClickListener;

    public class ViewHolder extends BaseViewHolder {
        public TextView tvName;

        public ViewHolder(View view, BaseViewHolder.RecyclerClickListener onRecyclerClickListener) {
            super(view, onRecyclerClickListener);
            tvName = view.findViewById(R.id.tv_name);
            view.setOnClickListener(this);
        }
    }

    public AccountsAdapter(List<Account> mAccountsList, BaseViewHolder.RecyclerClickListener onRecyclerClickListener) {
        this.mAccountsList = mAccountsList;
        this.onRecyclerClickListener = onRecyclerClickListener;
    }

    @Override
    public AccountsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_account_item, parent, false);
        return new ViewHolder(view, onRecyclerClickListener);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Account account = mAccountsList.get(position);
        holder.tvName.setText(account.getName());
        holder.itemView.setTag(account);
        holder.itemView.setSelected(isSelected(position));

        if (position > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(ExpenseTrackerApp.getContext(), R.anim.push_left_in);
            holder.itemView.startAnimation(animation);
            lastPosition = position;
        }
    }

    @Override
    public int getItemCount() {
        return mAccountsList.size();
    }

    public void updateAccounts(List<Account> mAccountsList) {
        this.mAccountsList = mAccountsList;
        notifyDataSetChanged();
    }
}
