package com.zuzex.konio;

import android.test.InstrumentationTestCase;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.zuzex.konio.api.AnnounceModel;
import com.zuzex.konio.api.BaseModel;
import com.zuzex.konio.api.HttpApi;
import com.zuzex.konio.api.PaymentModel;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by dgureev on 11/19/14.
 */
public class HttpApiTest extends InstrumentationTestCase {
    public HttpApiTest() {
        super();
    }

    @Override
    public void setUp() {

    }

    public void testApplicationLogin() throws Throwable{
        final CountDownLatch signal = new CountDownLatch(1);
        final String userName = "test";
        final String password = "test";

        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                HttpApi.getInstance().login(getInstrumentation().getContext(), userName, password,  new JsonHttpResponseHandler() {
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        assertEquals(statusCode, 200);
                        BaseModel answer = new BaseModel(response);
                        assertTrue(answer.isSuccessStatus);
                        signal.countDown();
                    }
                });
            }
        });

        try {
            signal.await(200, TimeUnit.SECONDS); // wait for callback
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertEquals(0, signal.getCount());
    }

    public void testStartPayment() throws Throwable {
        final CountDownLatch signal = new CountDownLatch(1);
        final int id  = 0;

        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                HttpApi.getInstance().startPayment(getInstrumentation().getContext(), id, new JsonHttpResponseHandler() {
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        assertEquals(statusCode, 200);
                        PaymentModel answer = null;
                        try {
                            answer = new PaymentModel(response);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        assertTrue(answer.isSuccessStatus);
                        assertEquals(answer.amount, 42.99);
                        assertEquals(answer.currency, "USD");
                        signal.countDown();
                    }
                });
            }
        });

        try {
            signal.await(200, TimeUnit.SECONDS); // wait for callback
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertEquals(0, signal.getCount());
    }

    public void testConfirmPayment() throws Throwable {
        final CountDownLatch signal = new CountDownLatch(1);
        final int id  = 0;

        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                HttpApi.getInstance().confirmPayment(getInstrumentation().getContext(), id, new JsonHttpResponseHandler() {
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        assertEquals(statusCode, 200);
                        BaseModel answer = new BaseModel(response);
                        assertTrue(answer.isSuccessStatus);
                        assertEquals(answer.message, "payment success");
                        signal.countDown();
                    }
                });
            }
        });

        try {
            signal.await(200, TimeUnit.SECONDS); // wait for callback
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertEquals(0, signal.getCount());
    }


    public void testLocationMessage_empty() throws Throwable {
        final CountDownLatch signal = new CountDownLatch(1);
        final String id  = "0";
        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                HttpApi.getInstance().sendDeviceUUID(getInstrumentation().getContext(), id, new JsonHttpResponseHandler() {
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        assertEquals(statusCode, 200);
                        AnnounceModel answer = new AnnounceModel(response);
                        assertTrue(answer.isSuccessStatus);
                        assertEquals(answer.url, null);
                        signal.countDown();
                    }
                });
            }
        });

        try {
            signal.await(200, TimeUnit.SECONDS); // wait for callback
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertEquals(0, signal.getCount());
    }

    public void testLocationMessageAndUrl() throws Throwable {
        final CountDownLatch signal = new CountDownLatch(1);
        final String id = "1";
        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                HttpApi.getInstance().sendDeviceUUID(getInstrumentation().getContext(), id, new JsonHttpResponseHandler() {
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        assertEquals(statusCode, 200);
                        AnnounceModel answer = new AnnounceModel(response);
                        assertTrue(answer.isSuccessStatus);
                        assertEquals(answer.url, "http://okay.jpg.to/");
                        signal.countDown();
                    }
                });
            }
        });

        try {
            signal.await(200, TimeUnit.SECONDS); // wait for callback
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertEquals(0, signal.getCount());
    }
}
