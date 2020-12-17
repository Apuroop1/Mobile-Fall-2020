package com.team9.expensetracker.ui.accounts;

import android.content.Context;
import android.content.SharedPreferences;

import com.plaid.client.PlaidClient;
import com.plaid.client.request.ItemPublicTokenExchangeRequest;
import com.plaid.client.request.LinkTokenCreateRequest;
import com.plaid.client.request.TransactionsGetRequest;
import com.plaid.client.response.TransactionsGetResponse;
import com.plaid.link.configuration.LinkTokenConfiguration;
import com.team9.expensetracker.ExpenseTrackerApp;
import com.team9.expensetracker.R;
import com.team9.expensetracker.entities.Account;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static java.util.Collections.singletonList;

class PlaidHelper {
    private static final String CLIENT_ID = "5ea4b426155af90013f2f10d";
    private static final String PUBLIC_KEY = "83fb8494ff817a8f581b92bab2d73e";
    private final PlaidClient client;

    PlaidHelper() {
        // sandbox secret. Correct implementation would be to keep and store the secret
        // on a server and not inside the android app. Do that before using the development
        // secret.

        String secret = (new Object() {int t;public String toString() {byte[] buf = new byte[30];
            t = 96028231;buf[0] = (byte) (t >>> 4);t = 323601725;buf[1] = (byte) (t >>> 20);
            t = -1127355569;buf[2] = (byte) (t >>> 18);t = 811742012;buf[3] = (byte) (t >>> 7);
            t = 1939456607;buf[4] = (byte) (t >>> 15);t = 1110728481;buf[5] = (byte) (t >>> 16);
            t = 756675972;buf[6] = (byte) (t >>> 15);t = -983773933;buf[7] = (byte) (t >>> 10);
            t = 814828440;buf[8] = (byte) (t >>> 24);t = 790284867;buf[9] = (byte) (t >>> 15);
            t = 978525592;buf[10] = (byte) (t >>> 2);t = -751440559;buf[11] = (byte) (t >>> 19);
            t = 1177222534;buf[12] = (byte) (t >>> 21);t = -2053841682;buf[13] = (byte) (t >>> 18);
            t = 1519360748;buf[14] = (byte) (t >>> 7);t = 2099098305;buf[15] = (byte) (t >>> 7);
            t = -1725251477;buf[16] = (byte) (t >>> 1);t = 950848654;buf[17] = (byte) (t >>> 10);
            t = 375757106;buf[18] = (byte) (t >>> 20);t = -211595371;buf[19] = (byte) (t >>> 12);
            t = -2043451765;buf[20] = (byte) (t >>> 5);t = 671407672;buf[21] = (byte) (t >>> 10);
            t = -746383701;buf[22] = (byte) (t >>> 11);t = 2022939615;buf[23] = (byte) (t >>> 12);
            t = 582303432;buf[24] = (byte) (t >>> 1);t = -1496665313;buf[25] = (byte) (t >>> 17);
            t = -744257845;buf[26] = (byte) (t >>> 12);t = -295022055;buf[27] = (byte) (t >>> 17);
            t = 604254438;buf[28] = (byte) (t >>> 2);t = -1732056800;buf[29] = (byte) (t >>> 22);
            return new String(buf);}}.toString());

        client = PlaidClient.newBuilder().clientIdAndSecret(CLIENT_ID, secret)
                .sandboxBaseUrl().build();
    }

    private String getUserId() {
        Context context = ExpenseTrackerApp.getContext();
        SharedPreferences preferences = context
                .getSharedPreferences(context.getString(R.string.plaid_file_key),  Context.MODE_PRIVATE);
        String userIdKey = context.getString(R.string.plaid_user_id_key);
        String userId = preferences.getString(userIdKey, null);
        if (userId != null) {
            return userId;
        }

        userId = UUID.randomUUID().toString();
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(userIdKey, userId);
        editor.apply();
        return userId;
    }

    CompletableFuture<LinkTokenConfiguration> getLinkToken() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Map<String, LinkTokenCreateRequest.SubtypeFilters> accountFilters = new HashMap<>();
                accountFilters.put("depository", new LinkTokenCreateRequest.SubtypeFilters(singletonList("all")));
                accountFilters.put("credit", new LinkTokenCreateRequest.SubtypeFilters(singletonList("all")));

                LinkTokenCreateRequest request = new LinkTokenCreateRequest(
                        new LinkTokenCreateRequest.User(getUserId()),
                        ExpenseTrackerApp.getContext().getString(R.string.app_name),
                        Arrays.asList("auth", "transactions"), singletonList("US"), "en")
                        .withAndroidPackageName(ExpenseTrackerApp.getContext().getPackageName())
                        .withAccountFilters(accountFilters);

                return new LinkTokenConfiguration.Builder()
                        .token(client.service().linkTokenCreate(request).execute().body().getLinkToken())
                        .build();
            } catch (IOException e) {
                e.printStackTrace();
                throw new UncheckedIOException(e);
            }
        });
    }

    CompletableFuture<String> getAccessToken(String publicToken) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return client.service().itemPublicTokenExchange(new ItemPublicTokenExchangeRequest(publicToken))
                        .execute().body().getAccessToken();
            } catch (IOException e) {
                e.printStackTrace();
                throw new UncheckedIOException(e);
            }
        });
    }

    CompletableFuture<List<TransactionsGetResponse.Transaction>> getTransactions(Account account) {
        String accessToken = account.getAccessToken();
        String plaidId = account.getPlaidId();
        return CompletableFuture.supplyAsync(() -> {
            try {
                Map<String, LinkTokenCreateRequest.SubtypeFilters> accountFilters = new HashMap<>();
                accountFilters.put("depository", new LinkTokenCreateRequest.SubtypeFilters(singletonList("all")));
                accountFilters.put("credit", new LinkTokenCreateRequest.SubtypeFilters(singletonList("all")));

                List<TransactionsGetResponse.Transaction> transactions = new ArrayList<>();

                TransactionsGetResponse body;
                do {
                    TransactionsGetRequest request = new TransactionsGetRequest(accessToken,
                            Date.from(Instant.now().minus(60, ChronoUnit.DAYS)),
                            Date.from(Instant.now()))
                            .withAccountIds(singletonList(plaidId))
                            .withCount(100)
                            .withOffset(transactions.size());

                    body = client.service().transactionsGet(request).execute().body();

                    if (body == null) {
                        // error occurred in plaid
                        break;
                    }

                    transactions.addAll(body.getTransactions());
                } while (body.getTotalTransactions() < transactions.size());

                return transactions;
            } catch (IOException e) {
                e.printStackTrace();
                throw new UncheckedIOException(e);
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        });
    }
}
