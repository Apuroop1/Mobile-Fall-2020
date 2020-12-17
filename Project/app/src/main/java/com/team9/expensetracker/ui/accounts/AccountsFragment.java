package com.team9.expensetracker.ui.accounts;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.plaid.client.response.TransactionsGetResponse;
import com.plaid.link.Plaid;
import com.plaid.link.PlaidHandler;
import com.plaid.link.result.LinkAccount;
import com.plaid.link.result.LinkResultHandler;
import com.plaid.link.result.LinkSuccessMetadata;
import com.team9.expensetracker.R;
import com.team9.expensetracker.custom.BaseViewHolder;
import com.team9.expensetracker.custom.DefaultRecyclerViewItemDecorator;
import com.team9.expensetracker.entities.Account;
import com.team9.expensetracker.entities.Expense;
import com.team9.expensetracker.interfaces.IExpensesType;
import com.team9.expensetracker.ui.MainActivity;
import com.team9.expensetracker.ui.MainFragment;
import com.team9.expensetracker.utils.DialogManager;
import com.team9.expensetracker.utils.RealmManager;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import kotlin.Unit;


public class AccountsFragment extends MainFragment implements BaseViewHolder.RecyclerClickListener {
    private final PlaidHelper plaidHelper = new PlaidHelper();
    private CompletableFuture<PlaidHandler> plaidHandler;

    private RecyclerView rvAccounts;
    private List<Account> mAccountList;
    private AccountsAdapter mAccountsAdapter;
    private TextView tvEmpty;

    private ActionMode mActionMode;

    public static AccountsFragment newInstance() {
        return new AccountsFragment();
    }

    private void onAddNewAccount() {
        try {
            plaidHandler.get().open(this);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_accounts, container, false);
        plaidHandler = plaidHelper.getLinkToken().thenApply(linkToken ->
                Plaid.create(requireActivity().getApplication(), linkToken));
        rvAccounts = view.findViewById(R.id.rv_accounts);
        tvEmpty = view.findViewById(R.id.tv_empty);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mMainActivityListener.setMode(MainActivity.NAVIGATION_MODE_STANDARD);
        mMainActivityListener.setFAB(R.drawable.ic_add_white_48dp, v -> onAddNewAccount());
        mMainActivityListener.setTitle(getString(R.string.accounts));
        mAccountList = Account.getAccounts();
        mAccountsAdapter = new AccountsAdapter(mAccountList, this);
        rvAccounts.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvAccounts.setAdapter(mAccountsAdapter);
        rvAccounts.setHasFixedSize(true);
        rvAccounts.addItemDecoration(new DefaultRecyclerViewItemDecorator(getResources().getDimension(R.dimen.dimen_10dp)));
        reloadData();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        LinkResultHandler handler = new LinkResultHandler(success -> {
            LinkSuccessMetadata metadata = success.getMetadata();

            plaidHelper.getAccessToken(success.getPublicToken()).thenAccept(accessToken -> {
                requireActivity().runOnUiThread(() -> {
                    for(LinkAccount account : metadata.getAccounts()) {
                        Account.saveNewAccount(new Account( metadata.getInstitution().getName() +
                                ": " + account.getName(), account.getId(), accessToken));
                    }

                    reloadData();
                });
            });

            return Unit.INSTANCE;
        }, failure -> Unit.INSTANCE);

        if (handler.onActivityResult(requestCode, resultCode, data)) {
            return;
        }
    }

    private final ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.expenses_context_menu, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            if (item.getItemId() == R.id.delete){
                eraseAccounts();
                return true;
            }

            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mAccountsAdapter.clearSelection();
            mActionMode = null;
        }
    };

    private void eraseAccounts() {
        DialogManager.getInstance().createCustomAcceptDialog(getActivity(), getString(R.string.delete),
                getString(R.string.confirm_delete_items), getString(R.string.confirm),
                getString(R.string.cancel), (dialog, which) -> {
                    if (which == DialogInterface.BUTTON_POSITIVE) {
                        List<Account> accountsToDelete = new ArrayList<>();
                        for (int position : mAccountsAdapter.getSelectedItems()) {
                            accountsToDelete.add(mAccountList.get(position));
                        }
                        Account.eraseAccounts(accountsToDelete);
                    }
                    reloadData();
                    mActionMode.finish();
                    mActionMode = null;
                });
    }

    @Override
    public void onClick(RecyclerView.ViewHolder vh, int position) {
        Account account = (Account) vh.itemView.getTag();
        DialogManager.getInstance().createCustomAcceptDialog(requireActivity(),
            getString(R.string.synch_now_title), getString(R.string.synch_now),
                getString(R.string.synchronize), getString(R.string.cancel), (dialog, which) -> {
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    DialogManager.getInstance().showShortToast(getString(R.string.synchronizing));

                    plaidHelper.getTransactions(account).thenAccept(transactions -> {
                        requireActivity().runOnUiThread(() -> {
                            Set<String> existingPlaidIds = RealmManager.getInstance().getRealmInstance()
                                    .where(Expense.class).findAll().stream()
                                    .map(Expense::getPlaidId).collect(Collectors.toSet());

                            int added = 0;
                            for (TransactionsGetResponse.Transaction incoming : transactions.stream()
                                    .filter(t -> t.getAccountId().equals(account.getPlaidId()))
                                    .filter(t -> !existingPlaidIds.contains(t.getAccountId() + "/" + t.getTransactionId()))
                                    .collect(Collectors.toList())
                            ) {
                                RealmManager.getInstance().save(new Expense(incoming.getName(),
                                        Date.valueOf(incoming.getDate()),
                                        incoming.getAmount() > 0 ? IExpensesType.MODE_EXPENSES : IExpensesType.MODE_INCOME,
                                        null, (float)Math.abs(incoming.getAmount()),
                                        incoming.getAccountId() + "/" + incoming.getTransactionId()), Expense.class);
                                added++;
                            }

                            DialogManager.getInstance().showShortToast("Added " + added);
                        });
                    });
                }
            });
    }

    @Override
    public void onLongClick(RecyclerView.ViewHolder vh, int position) {
        if (mActionMode == null) {
            mActionMode = mMainActivityListener.setActionMode(mActionModeCallback);
        }
    }

    private void reloadData() {
        mAccountList = Account.getAccounts();
        tvEmpty.setVisibility(mAccountList.isEmpty() ? View.VISIBLE : View.GONE);
        mAccountsAdapter.updateAccounts(mAccountList);
    }
}
