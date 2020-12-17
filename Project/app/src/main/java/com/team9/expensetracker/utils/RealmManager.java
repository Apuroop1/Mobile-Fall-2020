package com.team9.expensetracker.utils;

import com.team9.expensetracker.ExpenseTrackerApp;
import com.team9.expensetracker.entities.Category;
import com.team9.expensetracker.entities.Expense;
import com.team9.expensetracker.entities.IHaveId;
import com.team9.expensetracker.entities.Reminder;

import java.util.UUID;

import io.realm.AccountRealmProxy;
import io.realm.ExpenseRealmProxy;
import io.realm.FieldAttribute;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.RealmSchema;


public class RealmManager {

    private Realm realm;

    private static RealmManager ourInstance = new RealmManager();

    public static RealmManager getInstance() {
        return ourInstance;
    }

    public RealmManager(){
        Realm.init(ExpenseTrackerApp.getContext());

        Realm.setDefaultConfiguration(new RealmConfiguration.Builder()
                .schemaVersion(2) // increment when making db schema changes
                .migration((realm, prev, next) -> {
                    try {
                        RealmSchema schema = realm.getSchema();
                        if (prev < 1) {
                            schema.create(AccountRealmProxy.getTableName())
                                    .addField("id", String.class, FieldAttribute.PRIMARY_KEY, FieldAttribute.INDEXED, FieldAttribute.REQUIRED)
                                    .addField("plaidId", String.class, FieldAttribute.REQUIRED)
                                    .addField("name", String.class, FieldAttribute.REQUIRED)
                                    .addField("accessToken", String.class, FieldAttribute.REQUIRED);
                        }

                        if (prev < 2) {
                            schema.get(ExpenseRealmProxy.getTableName())
                                    .addField("plaidId", String.class);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }
                }).build());
        realm = Realm.getDefaultInstance();
    }

    public Realm getRealmInstance() {
        return realm;
    }

    public <E extends RealmObject> void update(final E object) {
        realm.executeTransaction(realm -> realm.copyToRealmOrUpdate(object));
    }

    public <E extends RealmObject> void update(final Iterable<E> object) {
        realm.executeTransaction(realm -> realm.copyToRealmOrUpdate(object));
    }

    public <E extends RealmObject & IHaveId> void save(final E object, final Class<E> clazz) {
        realm.executeTransaction(realm -> {
            checkDuplicateUUID(object, clazz);
            realm.copyToRealmOrUpdate(object);
        });
    }

    public <E extends RealmObject> void delete(final Iterable<E> objects){
        realm.executeTransaction(realm -> {
            if (objects == null) {
                return;
            }
            for (E object : objects) {
                if (object instanceof Category) {
                    Category category = (Category) object;
                    RealmResults<Expense> expenseList  = Expense.getExpensesPerCategory(category);
                    for (int i = expenseList.size()-1; i >= 0; i--) {
                        expenseList.get(i).deleteFromRealm();
                    }
                }
                object.deleteFromRealm();
            }
        });
    }

    public <E extends RealmObject> void delete(final E object){
        realm.executeTransaction(realm -> {
            if (object instanceof Category) {
                Category category = (Category) object;
                RealmResults<Expense> expenseList  = Expense.getExpensesPerCategory(category);
                for (int i = expenseList.size()-1; i >= 0; i--) {
                    expenseList.get(i).deleteFromRealm();
                }
            }
            object.deleteFromRealm();
        });
    }

    public <E extends RealmObject & IHaveId> RealmObject findById(Class<E> clazz, String id) {
        return realm.where(clazz).equalTo("id", id).findFirst();
    }

    public <E extends RealmObject & IHaveId>  void checkDuplicateUUID(E object, Class<E> clazz) {
        boolean repeated = true;
        while (repeated) {
            String id = UUID.randomUUID().toString();
            RealmObject realmObject = findById(clazz, id);
            if ( realmObject == null ) {
                ((IHaveId)object).setId(id);
                repeated = false;
            }
        }
    }

}
