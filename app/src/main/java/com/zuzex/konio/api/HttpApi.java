package com.zuzex.konio.api;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;

import org.apache.http.auth.UsernamePasswordCredentials;

import javax.inject.Inject;

/**
 * Created by dgureev on 11/19/14.
 */
public class HttpApi {
    @Inject
    static DigestAuthenticator mDigestAuthenticator;
    private static String baseHost = "http://api.v1.dev.miretail.com.au";
    private static final int port = 80;

    private static volatile HttpApi instance;

    public boolean isAuthorized = false;

    private static OkHttpClient client = new OkHttpClient();

    private static OkHttpClient createAsyncClient() {
        OkHttpClient okHttpClient = new OkHttpClient();
        return okHttpClient;
    }

    public static HttpApi getInstance() {
        HttpApi localInstance = instance;
        if (localInstance == null) {
            synchronized (HttpApi.class) {
                client = createAsyncClient();
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new HttpApi();
                }
            }
        }
        return localInstance;
    }


    public static String getBaseHost() {
        return baseHost;
    }

    public static void post(String url, RequestBody body, Callback callback) {
        Request request = new Request.Builder()
                .url(baseHost+url)
                .post(body)
                .build();
        Call call = client.newCall(request);
        call.enqueue(callback);
    }

    public static void get(String url, Callback callback) {
        Request request = new Request.Builder()
                .url(baseHost+url)
                .build();
        Call call = client.newCall(request);
        call.enqueue(callback);
    }

    public void login(final String userName, final String password, Callback callback) {
        mDigestAuthenticator = new DigestAuthenticator(new UsernamePasswordCredentials(userName, password));
        client.setAuthenticator(mDigestAuthenticator);
        get("/login", callback);
    }

    public void startPayment(final String id, Callback callback) {
        RequestBody formBody = new FormEncodingBuilder()
                .add("id", id)
                .build();
        post("/pay/start", formBody, callback);
    }


    public void confirmPayment(PaymentModel payModel, Callback callback) {
        FormEncodingBuilder builder = new FormEncodingBuilder();
        builder.add("id", payModel.id);
        String payFrom = payModel.payFrom.get(payModel.payFromSelected);
        builder.add("pay_from", payFrom);
        switch (payModel.payType) {
            case PAY_TYPE_NORMAL:
                post("/pay/confirm", builder.build(), callback);
                break;
            case PAY_TYPE_DONATION:
                builder.add("amount", payModel.getAmount())
                    .build();
                post("/pay/confirm", builder.build(), callback);
                break;
            case PAY_TYPE_OPTIONS:
                builder.add("option", payModel.getSelectedOption());
                post("/pay/confirm", builder.build(), callback);
                break;
        }
    }

    public void sendDeviceUUID(final String id, Callback callback) {
        RequestBody formBody = new FormEncodingBuilder()
                .add("id", id)
                .build();
        post("/device/announce", formBody, callback);
    }
}