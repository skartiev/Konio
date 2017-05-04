package com.zuzex.konio.Fragments;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.zuzex.konio.R;
import com.zuzex.konio.Utils.StyledFragment;
import com.zuzex.konio.api.BaseModel;
import com.zuzex.konio.api.HttpApi;

import org.apache.http.auth.UsernamePasswordCredentials;
import org.json.JSONException;

import java.io.IOException;

/**
 * Created by dgureev on 11/19/14.
 */
public class PreloaderFragment extends StyledFragment {

    public static final String TAG = "PreloaderFragment";
    private static int progress = 0;

    public interface ProgressListener {
        public void onCompletion(Boolean result);
        public void onProgressUpdate(int value);
    }

    private ProgressBar mProgressBar;
    private ProgressListener mProgressListener;
    private boolean mResult = false;
    private LoadingTask mTask;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        checkCredentials();
        setRetainInstance(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((ActionBarActivity) getActivity()).getSupportActionBar().hide();
    }

    public Boolean getResult() {
        return mResult;
    }

    public void setProgress(int progress) {
        if(mProgressBar != null) {
            mProgressBar.setProgress(progress);
        }
    }

    /**
     * Returns true if a result has already been calculated
     *
     * @return true if a result has already been calculated
     * @see #getResult()
     */
    public boolean hasResult() {
        return mResult;
    }

    /**
     * Removes the ProgressListener
     *
     * @see #setProgressListener(ProgressListener)
     */
    public void removeProgressListener() {
        mProgressListener = null;
    }

    /**
     * Sets the ProgressListener to be notified of updates
     *
     * @param listener ProgressListener to notify
     * @see #removeProgressListener()
     */
    public void setProgressListener(ProgressListener listener) {
        mProgressListener = listener;
    }

    /**
     * Starts loading the data
     */
    public void startLoading() {
        mTask = new LoadingTask();
        mTask.execute();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_preloader, container, false);
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.splash_progress);
        ImageView logoView = (ImageView) rootView.findViewById(R.id.preloader_logo);
        updateLogo(logoView);
        updateBackground(rootView);
        return rootView;
    }

    private class LoadingTask extends AsyncTask<Void, Integer, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                for (int i = progress; i < 100; i++) {
                    Thread.sleep(30);
                    progress = i;
                    this.publishProgress(i);
                }
            } catch (InterruptedException e) {
                return false;
            } finally {
                return mResult;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            mResult = result;
            mTask = null;
            if (mProgressListener != null) {
                mProgressListener.onCompletion(mResult);
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            if(mProgressListener != null) {
                mProgressListener.onProgressUpdate(values[0]);
            }
            setProgress(values[0]);
        }
    }

    private void checkCredentials() {
        SharedPreferences userDetails = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if(userDetails.getBoolean("isRemember", false)) {
            UsernamePasswordCredentials credentials = restoreCredentials();
            login(credentials.getUserName(), credentials.getPassword());
        }
    }

    private UsernamePasswordCredentials restoreCredentials() {
        String userName;
        String password;
        SharedPreferences userDetails = PreferenceManager.getDefaultSharedPreferences(getActivity());
        userName = userDetails.getString("userName", "");
        password = userDetails.getString("password", "");
        return new UsernamePasswordCredentials(userName, password);
    }

    public void login(final String userName, final String password) {
        HttpApi.getInstance().login(userName, password, new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                Log.w(TAG, "LOGIN FAILED" + request.toString());
                //TODO remove saved credentials
            }

            @Override
            public void onResponse(Response response) throws IOException {
                String responseString = response.body().string();
                BaseModel baseModel = null;
                try {
                    baseModel = new BaseModel(responseString);
                    loadStyleFromBaseModel(baseModel);
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
                if(baseModel != null && baseModel.isSuccessStatus) {
                    if(baseModel.isSuccessStatus) {
                        Log.w(TAG, "LOGIN SUCCESS" );
                        HttpApi.getInstance().isAuthorized = true;
                        mResult = true;
                    }
                }
            }
        });
    }
}
