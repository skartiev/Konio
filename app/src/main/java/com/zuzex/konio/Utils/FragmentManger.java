package com.zuzex.konio.Utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import com.zuzex.konio.Fragments.LoginFragment;
import com.zuzex.konio.Fragments.MainFragment;
import com.zuzex.konio.Fragments.PreloaderFragment;
import com.zuzex.konio.api.HttpApi;

/**
 * Created by dgureev on 17.12.14.
 */
public class FragmentManger implements PreloaderFragment.ProgressListener{
    private static final String CURRENT_FRAGMENT = "CURRENT_FRAGMENT_TAG";
    private String currentFragmentTag;
    private FragmentActivity activity;
    private int container;
    private PreloaderFragment mPreloaderFragment;

    public FragmentManger(FragmentActivity activity, int container, Bundle savedInstanceState) {
        this.activity = activity;
        this.container = container;

        if(savedInstanceState != null) {
            currentFragmentTag = savedInstanceState.getString(CURRENT_FRAGMENT);
        }

        if(mPreloaderFragment != null) {
            if(!tryLaunchLastFragment()) {
                loadFragmentByTag(PreloaderFragment.TAG, true);
                return;
            }
        }

        if(isValidLastFragment()) {
            loadFragmentByTag(currentFragmentTag, false);
        } else {
            if (HttpApi.getInstance().isAuthorized) {
                loadFragmentByTag(MainFragment.TAG, false);
            } else {
                loadFragmentByTag(PreloaderFragment.TAG, false);
            }
        }
    }

    public void loadFragmentByTag(final String tag, boolean appendToBackStack) {
        final FragmentManager fm = activity.getSupportFragmentManager();
        Fragment tmpFragment = fm.findFragmentByTag(tag);
        if(tag.equals(PreloaderFragment.TAG)) {
            mPreloaderFragment = new PreloaderFragment();
            mPreloaderFragment.setProgressListener(this);
            mPreloaderFragment.startLoading();
            fm.beginTransaction()
                    .replace(container, mPreloaderFragment, tag)
                    .commit();
            return;
        } else if(tag.equals(MainFragment.TAG)) {
            if(tmpFragment == null) {
                tmpFragment = new MainFragment();
            }

        } else if(tag.equals(LoginFragment.TAG)) {
            if (tmpFragment == null) {
                tmpFragment = new LoginFragment();
            }
        }
        if(appendToBackStack) {
            fm.beginTransaction()
                    .replace(container, tmpFragment, tag)
                    .addToBackStack(tag)
                    .commit();
        } else {
            fm.beginTransaction()
                    .replace(container, tmpFragment, tag)
                    .commit();
        }
        currentFragmentTag = tag;
    }

    boolean isValidLastFragment() {
        if (currentFragmentTag == null || currentFragmentTag.isEmpty()) {
            return false;
        }
        return true;
    }

    public Bundle onSaveInstanceState(Bundle outState) {
        outState.putString(CURRENT_FRAGMENT, currentFragmentTag);
        return outState;
    }

    public boolean tryLaunchLastFragment() {
        if(isValidLastFragment()) {
            loadFragmentByTag(currentFragmentTag, true);
            return true;
        }
        return false;
    }

    @Override
    public void onCompletion(Boolean result) {
        if(result) {
            loadFragmentByTag(MainFragment.TAG, false);
        } else {
            loadFragmentByTag(LoginFragment.TAG, false);
        }



//        getSupportActionBar().show();
//        if(result) {
//            mMainFragment = (MainFragment) getSupportFragmentManager().findFragmentByTag(MainFragment.TAG);
//            if (mMainFragment == null) {
//                mMainFragment = new MainFragment();
//            }
//            getSupportFragmentManager().beginTransaction()
//                    .replace(R.id.container, mMainFragment, MainFragment.TAG)
//                    .remove(mPreloaderFragment)
//                    .commit();
//        } else {
//            mLoginFragment = (LoginFragment) getSupportFragmentManager().findFragmentByTag(LoginFragment.TAG);
//            if (mLoginFragment == null) {
//                mLoginFragment = new LoginFragment();
//            }
//            getSupportFragmentManager().beginTransaction()
//                    .replace(R.id.container, mLoginFragment, LoginFragment.TAG)
//                    .remove(mPreloaderFragment)
//                    .commit();
//        }
//        mPreloaderFragment = null;
    }

    @Override
    public void onProgressUpdate(int value) {
        mPreloaderFragment.setProgress(value);
    }

    private boolean checkCompletionStatus() {
        if (mPreloaderFragment != null && mPreloaderFragment.hasResult()) {
            onCompletion(mPreloaderFragment.getResult());
            mPreloaderFragment.setProgressListener(null);
            mPreloaderFragment = null;
            return true;
        }

        return false;
    }

    public void onStart() {
        if (mPreloaderFragment != null) {
            checkCompletionStatus();
        }
    }

    public void onStop() {
        if (mPreloaderFragment != null) {
            mPreloaderFragment.removeProgressListener();
        }
    }
}
