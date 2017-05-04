package com.zuzex.konio.Services;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.kontakt.sdk.android.configuration.ForceScanConfiguration;
import com.kontakt.sdk.android.configuration.MonitorPeriod;
import com.kontakt.sdk.android.connection.OnServiceBoundListener;
import com.kontakt.sdk.android.data.RssiCalculators;
import com.kontakt.sdk.android.device.Beacon;
import com.kontakt.sdk.android.device.Region;
import com.kontakt.sdk.android.manager.BeaconManager;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.zuzex.konio.api.AnnounceModel;
import com.zuzex.konio.api.HttpApi;

import org.json.JSONException;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by dgureev on 22.12.14.
 */

public class BeaconRangeListener implements BeaconManager.MonitoringListener {
    private final static String TAG = "BeaconListener";
    private Context mContext;
    private HttpApi mHttpApi;
    LocalBroadcastManager localBroadcastManager;
    private BeaconManager beaconManager;

    MonitorPeriod mMonitorPeriodMAX;
    Set<Region> defaultRegion;

    public BeaconRangeListener(Context context) {
        mContext = context;
        localBroadcastManager = LocalBroadcastManager.getInstance(context);
        mHttpApi = HttpApi.getInstance();
        defaultRegion = new HashSet<Region>();
        defaultRegion.add(Region.EVERYWHERE);
        beaconManager = BeaconManager.newInstance(mContext);
        beaconManager.registerMonitoringListener(this);
//        mMonitorPeriodMAX = new MonitorPeriod(10000, 300000);
        beaconManager.setRssiCalculator(RssiCalculators.newLimitedMeanRssiCalculator(5));
        beaconManager.setMonitorPeriod(MonitorPeriod.MINIMAL);
        beaconManager.setForceScanConfiguration(ForceScanConfiguration.DEFAULT);
        beaconManager.setDistanceSort(Beacon.DistanceSort.ASC);
    }

    public void startService(PendingIntent pi) throws PendingIntent.CanceledException {
        Intent result = new Intent();
        if(!beaconManager.isBluetoothEnabled()) {
            pi.send(mContext, KonioBaseServiceHelper.RESULT_STATUS_BLUETOTH_DISABLED, result);
        } else {
            connect(pi, result);
        }
    }

    public void connect(final PendingIntent pi, final Intent intent) throws PendingIntent.CanceledException {
        try {
            if(!beaconManager.isConnected()) {
                beaconManager.connect(new OnServiceBoundListener() {
                    @Override
                    public void onServiceBound() throws RemoteException {
                        beaconManager.startMonitoring(defaultRegion);
                        Log.w(TAG, "START RANGING");
                        try {
                            pi.send(mContext, KonioBaseServiceHelper.RESULT_STATUS_OK, intent);
                        } catch (PendingIntent.CanceledException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        } catch (RemoteException e) {
            e.printStackTrace();
            pi.send(mContext, KonioBaseServiceHelper.RESULT_STATUS_REMOTE_EXCEPTION, intent);
        }
    }

    public void stopService(PendingIntent pi, Intent intent) throws PendingIntent.CanceledException {
        if(beaconManager != null) {
            beaconManager.stopMonitoring();
            pi.send(mContext, KonioBaseServiceHelper.REQUEST_CODE_STOP_RINGING, intent);
        }
    }

    public void onDestroy() {
        beaconManager.disconnect();
        beaconManager = null;
    }

    @Override
    public void onMonitorStart() {
        Log.w(TAG, "START");
    }

    @Override
    public void onMonitorStop() {
        Log.w(TAG, "STOP");
    }

    @Override
    public void onBeaconsUpdated(Region region, List<Beacon> beacons) {
        Log.w(TAG, "UPD");
    }

    @Override
    public void onBeaconAppeared(Region region, Beacon beacon) {
        Log.w(TAG, "app");
        sendNotify(beacon);
    }

    @Override
    public void onRegionEntered(Region region) {
        Log.w(TAG, "ent");
    }

    @Override
    public void onRegionAbandoned(Region region) {
        Log.w(TAG, "ab");
    }

    public void sendNotify(Beacon beacon) {
        String UUID = beacon.getProximityUUID().toString();
        mHttpApi.sendDeviceUUID(UUID, new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                //todo handle failure request
            }

            @Override
            public void onResponse(Response response) throws IOException {
                try {
                    AnnounceModel announceModel = new AnnounceModel(response.body().string());
                    if (!announceModel.message.isEmpty()) {
                        Intent intent = new Intent(KonioBaseServiceHelper.ANNOUNCE_INTENT_FILTER);
                        intent.putExtra(AnnounceModel.TAG, announceModel);
                        localBroadcastManager.sendBroadcast(intent);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
