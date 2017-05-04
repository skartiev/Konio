package com.zuzex.konio.Fragments;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;

import com.badoo.mobile.util.WeakHandler;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.zuzex.konio.MainActivity;
import com.zuzex.konio.R;
import com.zuzex.konio.Utils.StyledFragment;
import com.zuzex.konio.Utils.UiHelper;
import com.zuzex.konio.api.BaseModel;
import com.zuzex.konio.api.HttpApi;

import org.json.JSONException;

import java.io.IOException;

/**
 * Created by dgureev on 11/19/14.
 */
public class LoginFragment extends StyledFragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    public static final String TAG = "LoginFragment";

    private EditText loginTextEdit;
    private EditText passwordTextEdit;
    private boolean isRemember = false;
    private WeakHandler handler;

    public LoginFragment() {
        handler = new WeakHandler();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_login, container, false);
        Button loginButton  = (Button) rootView.findViewById(R.id.login_button);
        Switch rememberMeSwitch = (Switch) rootView.findViewById(R.id.rememberMeSwitch);
        loginTextEdit = (EditText) rootView.findViewById(R.id.login_text);
        passwordTextEdit = (EditText) rootView.findViewById(R.id.login_password);
        loginTextEdit.setText("demo/demo@tswwh.com");
        passwordTextEdit.setText("d355363c0f4a057bb4f405af2ca3fe47");
        loginButton.setOnClickListener(this);
        rememberMeSwitch.setChecked(checkRemember());
        rememberMeSwitch.setOnCheckedChangeListener(this);
        updateBackground(rootView);
        return rootView;
    }

    boolean checkFields() {
        if(!loginTextEdit.getText().toString().isEmpty() && !passwordTextEdit.getText().toString().isEmpty()) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onClick(View v) {
        if(checkFields()) {
            Activity act = getActivity();
            login(loginTextEdit.getText().toString(), passwordTextEdit.getText().toString());
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if(isChecked) {
            isRemember = true;
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            SharedPreferences.Editor e = preferences.edit();
            e.putBoolean("isRemember", true);
            e.commit();
            saveCredentials(loginTextEdit.getText().toString(), passwordTextEdit.getText().toString());
        }
    }

    public void login(final String userName, final String password) {
        HttpApi.getInstance().login(userName, password, new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Context ct = getActivity();
                        UiHelper.showToast(ct, ct.getString(R.string.unable_to_login));
                    }
                });
            }

            @Override
            public void onResponse(Response response) throws IOException {
                String responseString = response.body().string();
                BaseModel answer = null;
                try {
                    answer = new BaseModel(responseString);
                    loadStyleFromBaseModel(answer);
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
                if(answer != null && answer.isSuccessStatus) {

                    Log.w(TAG, "LOGIN SUCCESS");
                    HttpApi.getInstance().isAuthorized = true;
                    MainFragment mMainFragment;
                    FragmentActivity activity = getActivity();
                    if(activity != null) {
                        mMainFragment = (MainFragment) activity.getSupportFragmentManager().findFragmentByTag(MainFragment.TAG);
                        if (mMainFragment == null) {
                            mMainFragment = new MainFragment();
                        }
                        getActivity().getSupportFragmentManager().beginTransaction()
                                .replace(R.id.container, mMainFragment, MainFragment.TAG)
                                .remove((LoginFragment) getActivity().getSupportFragmentManager().findFragmentByTag(LoginFragment.TAG))
                                .commit();
                    }
                }
            }
        });
    }

    private void saveCredentials(final String userName, final String password) {
        SharedPreferences userDetails = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor ed = userDetails.edit();
        ed.putString("userName", userName);
        ed.putString("password", password);
        ed.commit();
    }

    private boolean checkRemember() {
        SharedPreferences remember = PreferenceManager.getDefaultSharedPreferences(getActivity());
        return remember.getBoolean("isRemember", false);
    }

}
