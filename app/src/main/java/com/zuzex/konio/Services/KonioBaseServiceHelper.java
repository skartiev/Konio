package com.zuzex.konio.Services;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;

/**
 * Created by dgureev on 15.12.14.
 */
public class KonioBaseServiceHelper {

    public final static String PARAM_PINTENT = "PARAM_PINTENT";

    //requset codes
    public static final int REQUEST_CODE_START_RANGING = 22;
    public static final int REQUEST_CODE_STOP_RINGING = 5;
    public static final int REQUEST_CODE_ENABLE_BLUETOOTH = 99;
    //result codes

    public static final int RESULT_STATUS_OK = 0;
    public static final int RESULT_STATUS_BLUETOTH_DISABLED = 11;

    public static final int RESULT_STATUS_PI_CANCELEXEPT = 3;
    public static final int RESULT_STATUS_REMOTE_EXCEPTION = 4;


    public static final String result = "RESULT";

    public final static String COMMAND_NAME = "COMMAND_NAME";

    public final static int COMMAND_START_RINGING = 14;
    public final static int COMMAND_FINISH_RINGING = 88;

    public static final String ANNOUNCE_INTENT_FILTER = "ANNOUNCE_INTENT_FILTER";
    public static final String ANNOUNCE_PARAM = "ANNOUNCE_PARAM";

    public static void startRanging(Activity activity) {
        Intent intent = new Intent(activity, KonioBaseService.class);
        PendingIntent pi = activity.createPendingResult(KonioBaseServiceHelper.REQUEST_CODE_START_RANGING, new Intent(), 0);
        intent.putExtra(KonioBaseServiceHelper.PARAM_PINTENT, pi);
        intent.putExtra(KonioBaseServiceHelper.COMMAND_NAME, KonioBaseServiceHelper.COMMAND_START_RINGING);
        activity.startService(intent);
    }
}
