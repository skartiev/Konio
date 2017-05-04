package com.zuzex.konio;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.badoo.mobile.util.WeakHandler;
import com.zuzex.konio.Services.KonioBaseServiceHelper;
import com.zuzex.konio.Utils.FragmentManger;
import com.zuzex.konio.Utils.UiHelper;
import com.zuzex.konio.api.AnnounceModel;

public class MainActivity extends ActionBarActivity {
    private static final String TAG = "MainActivity";
    private static final String USED = "USED_PARAMS";
    public boolean isVisible = false;
    public FragmentManger fragmentManger;
    private LocationNotifier mLocationNotifier;
    private LocalBroadcastManager localAnnounceBroadcastManager;
    private BroadcastReceiver deviceAnnounceReceiver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fragmentManger = new FragmentManger(this, R.id.container, savedInstanceState);
        setContentView(R.layout.activity_main);
        mLocationNotifier = new LocationNotifier(this, new WeakHandler());
        localAnnounceBroadcastManager = LocalBroadcastManager.getInstance(this);
        deviceAnnounceReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                AnnounceModel announceModel = intent.getParcelableExtra(AnnounceModel.TAG);
                if(isVisible) {
                    mLocationNotifier.alertDialogNotify(announceModel);
                }
            }
        };
        localAnnounceBroadcastManager.registerReceiver(deviceAnnounceReceiver, new IntentFilter(KonioBaseServiceHelper.ANNOUNCE_INTENT_FILTER));
    }

    @Override
    public void onResume() {
        super.onResume();
        isVisible = true;
        checkIntent(getIntent());
    }


    @Override
    public void onPause() {
        super.onPause();
        isVisible = false;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        fragmentManger.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            logout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        fragmentManger.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        fragmentManger.onStop();
    }

    private void logout() {
        SharedPreferences userDetails = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor ed = userDetails.edit();
        ed.putString("userName", "");
        ed.putString("password", "");
        ed.putBoolean("isRemember", false);
        ed.commit();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case KonioBaseServiceHelper.REQUEST_CODE_START_RANGING: {
                if(resultCode == KonioBaseServiceHelper.RESULT_STATUS_BLUETOTH_DISABLED) {
                    UiHelper.showToast(this, "Bluetooth is disabled");
                    final Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(intent, KonioBaseServiceHelper.REQUEST_CODE_ENABLE_BLUETOOTH);
                } else if(resultCode == KonioBaseServiceHelper.RESULT_STATUS_OK) {
                    UiHelper.showToast(this, "Ringing is on");
                } else {
                    unknownResultCode(resultCode);
                }
                break;
            }
            case KonioBaseServiceHelper.REQUEST_CODE_STOP_RINGING: {
                if(resultCode == KonioBaseServiceHelper.RESULT_STATUS_OK) {
                    UiHelper.showToast(this, "Bluetooth is disabled");
                } else {
                    unknownResultCode(resultCode);
                }
                break;
            }
            case KonioBaseServiceHelper.REQUEST_CODE_ENABLE_BLUETOOTH: {
                if(resultCode == Activity.RESULT_OK) {
                    UiHelper.showToast(this, "Ok, bluetooth enabled");
                    KonioBaseServiceHelper.startRanging(this);
                } else {
                    UiHelper.showToast(this, "Bluetooth is disabled");
                }
            }
        }
    }

    private void unknownResultCode(int result) {
        UiHelper.showToast(this, "Unknown code" + String.valueOf(result));
    }

    private void checkIntent(Intent intent) {
        try {
        AnnounceModel announceModel = intent.getParcelableExtra(AnnounceModel.TAG);
        boolean used = intent.getBooleanExtra(USED, false);
            if (isVisible && !used) {
                mLocationNotifier.alertDialogNotify(announceModel);
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        getIntent().removeExtra(AnnounceModel.TAG);
        getIntent().setAction("");
        getIntent().putExtra(USED, true);
    }

    public LocationNotifier getLocationNotifier() {
        return mLocationNotifier;
    }
}
