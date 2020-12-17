package com.team9.expensetracker.entities;

import com.team9.expensetracker.utils.RealmManager;

import java.util.List;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

public class Account extends RealmObject implements IHaveId {
    @PrimaryKey
    @Required
    private String id;

    @Required
    private String plaidId;

    @Required
    private String name;

    @Required
    private String accessToken;

    public Account() {
    }

    public Account(String name, String plaidId, String accessToken) {
        this.name = name;
        this.plaidId = plaidId;
        this.accessToken = accessToken;
    }

    public static List<Account> getAccounts() {
        return RealmManager.getInstance().getRealmInstance().where(Account.class).findAll();
    }

    public static void saveNewAccount(Account account) {
        RealmManager.getInstance().save(account, Account.class);
    }

    public static void eraseAccounts(List<Account> accounts) {
        RealmManager.getInstance().delete(accounts);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPlaidId() {
        return plaidId;
    }

    public void setPlaidId(String plaidId) {
        this.plaidId = plaidId;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
}
