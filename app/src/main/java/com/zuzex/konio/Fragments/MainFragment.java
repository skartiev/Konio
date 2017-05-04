package com.zuzex.konio.Fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.badoo.mobile.util.WeakHandler;
import com.layar.sdk.LayarSDK;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.picasso.Picasso;
import com.zuzex.konio.Camera.CameraActivity;
import com.zuzex.konio.LocationNotifier;
import com.zuzex.konio.MainActivity;
import com.zuzex.konio.Popups.PayPopupWindow;
import com.zuzex.konio.R;
import com.zuzex.konio.Services.KonioBaseServiceHelper;
import com.zuzex.konio.Utils.StyledFragment;
import com.zuzex.konio.Utils.UiHelper;
import com.zuzex.konio.api.BaseModel;
import com.zuzex.konio.api.HttpApi;
import com.zuzex.konio.api.PaymentModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class MainFragment extends StyledFragment implements View.OnClickListener {
    public static final String TAG = "MainFragment";
    public static final int GET_STRING_RESULT = 0;
    private WeakHandler mHandler;
    private PayPopupWindow payPopupWindow;
    public MainFragment() {

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mHandler = new WeakHandler();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        Button scanButton = (Button) rootView.findViewById(R.id.scan_button);
        Button testNotify = (Button) rootView.findViewById(R.id.test_notify);
        Button showPopup = (Button) rootView.findViewById(R.id.test_pay_popup);
        Button toggleRing = (Button) rootView.findViewById(R.id.toggle_ringing);
        Button layarButton = (Button) rootView.findViewById(R.id.layar_button);
        ImageView imageView = (ImageView) rootView.findViewById(R.id.logoImage);
        BaseModel baseModel = null;


        toggleRing.setOnClickListener(this);
        showPopup.setOnClickListener(this);
        testNotify.setOnClickListener(this);
        scanButton.setOnClickListener(this);
        layarButton.setOnClickListener(this);
        logoChange(imageView);
        updateBackground(rootView);
        return rootView;
    }

    private void logoChange(ImageView imageView) {
        Context ct = getActivity();
        SharedPreferences preferences = ct.getSharedPreferences(STYLE_PREFERENCES_NAME, Context.MODE_PRIVATE);
        String logoUrl = preferences.getString(STYLE_LOGO, "");
        Picasso.with(getActivity()).load(logoUrl).into(imageView);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.scan_button:
                Intent intent = new Intent(getActivity(), CameraActivity.class);
                startActivityForResult(intent, GET_STRING_RESULT);
                break;
            case R.id.test_notify:
                LocationNotifier locationNotifier = ((MainActivity) getActivity()).getLocationNotifier();
                if(locationNotifier != null) {
                    locationNotifier.testNotify();
                }

                break;
            case R.id.test_pay_popup:
                showPayPopup();
                break;
            case R.id.toggle_ringing:
                KonioBaseServiceHelper.startRanging(getActivity());
                break;
            case R.id.layar_button:
                String oauthKey = "JPaBOxsVwTmSEGvh";
                String oauthSecret = "SEPeTubyDjoRsUHhwcmVBCKZAdNlzgiF";

                LayarSDK.initialize(getActivity(), oauthKey, oauthSecret);
                LayarSDK.startLayarActivity(getActivity());
                break;
        }
    }

    private String getPayId(String qrCodeString) {
        final String baseHost = "http://pos.dev.miretail.com.au/Request/View/";
        if(qrCodeString != null && !qrCodeString.isEmpty() && qrCodeString.contains(baseHost)) {
            String result = qrCodeString.replace(baseHost, "");
            return result;
        }
        return "";
    }

    private void sendScannedData(String result) {
        try {
            if (result != null && !result.isEmpty()) {
                HttpApi.getInstance().startPayment(result, new Callback() {
                    @Override
                    public void onFailure(Request request, IOException e) {
                        UiHelper.showToast(getActivity(), getString(R.string.network_error));
                    }

                    @Override
                    public void onResponse(final Response response) throws IOException {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    final PaymentModel paymentModel = new PaymentModel(new JSONObject(response.body().string()));
                                    if (paymentModel.isSuccessStatus) {
                                        payPopupWindow = new PayPopupWindow(getActivity(), paymentModel, mHandler);
                                    } else {
                                        mHandler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                UiHelper.showToast(getActivity(), paymentModel.message);
                                            }
                                        });
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                });
            } else {
                UiHelper.showToast(getActivity(), getString(R.string.qr_code_not_valid));
            }
        }catch (IllegalArgumentException e) {
            UiHelper.showToast(getActivity(), getString(R.string.qr_code_not_valid));
        }
    }

    public void showPayPopup() {
        String testPayModel =
                "   {\n" +
                        "        \"data\": {\n" +
                        "        \"amount\": \"\",\n" +
                        "                \"id\": \"4caf52e6f8ca7c7e7f30cc50f0f068fd\",\n" +
                        "                \"pay_from\": {\n" +
                        "            \"credits\": \"Credits Account\"\n" +
                        "        },\n" +
                        "        \"subject\": \"Meow Meow Multiple Options\",\n" +
                        "                \"recipient\": \"Demo Account\",\n" +
                        "                \"options\": [\n" +
                        "        {\n" +
                        "            \"value\": 1,\n" +
                        "                \"label\": \"Meow\"\n" +
                        "        },\n" +
                        "        {\n" +
                        "            \"value\": 2,\n" +
                        "                \"label\": \"woof\"\n" +
                        "        },\n" +
                        "        {\n" +
                        "            \"value\": 3,\n" +
                        "                \"label\": \"bark\"\n" +
                        "        },\n" +
                        "        {\n" +
                        "            \"value\": 4,\n" +
                        "                \"label\": \"qwaq\"\n" +
                        "        },\n" +
                        "        {\n" +
                        "            \"value\": 5,\n" +
                        "                \"label\": \"kurlique\"\n" +
                        "        }\n" +
                        "        ],\n" +
                        "        \"currency\": \"usd\"\n" +
                        "    },\n" +
                        "        \"message\": \"start payment\",\n" +
                        "            \"status\": 1\n" +
                        "    }";
        try {
           PaymentModel paymentModel = new PaymentModel(new JSONObject(testPayModel));
            payPopupWindow = new PayPopupWindow(getActivity(), paymentModel, mHandler);
        } catch (JSONException e) {
            UiHelper.showToast(getActivity(), "failed show example");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
            case Activity.RESULT_OK: {
                if (requestCode == GET_STRING_RESULT) {
                    String resultString = data.getStringExtra(CameraActivity.RESULT);
                    String payId = getPayId(resultString);
                    if (!payId.isEmpty()) {
                        sendScannedData(payId);
                    } else {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                UiHelper.showToast(getActivity(), getString(R.string.qr_code_not_valid));
                            }
                        });
                    }
                }
                break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
