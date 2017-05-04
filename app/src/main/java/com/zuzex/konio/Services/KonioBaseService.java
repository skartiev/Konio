package com.zuzex.konio.Services;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;


/**
 * Created by dgureev on 15.12.14.
 */
public class KonioBaseService extends Service {
    private static final String TAG = "BeakonService";
    BeaconRangeListener beaconRangeListener;

    public void onCreate() {
        super.onCreate();
        beaconRangeListener = new BeaconRangeListener(this);
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        int command = intent.getIntExtra(KonioBaseServiceHelper.COMMAND_NAME, -1);
        PendingIntent pi = intent.getParcelableExtra(KonioBaseServiceHelper.PARAM_PINTENT);
        try {
            switch (command) {
                case (KonioBaseServiceHelper.COMMAND_START_RINGING): {
                    beaconRangeListener.startService(pi);
                    break;
                }
                case (KonioBaseServiceHelper.COMMAND_FINISH_RINGING): {
                    beaconRangeListener.stopService(pi, intent);
                    break;
                }
                default:
                    break;
            }
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        beaconRangeListener.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
