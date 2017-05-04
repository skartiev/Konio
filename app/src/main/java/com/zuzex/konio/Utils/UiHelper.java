package com.zuzex.konio.Utils;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by dgureev on 11/27/14.
 */
public class UiHelper {

    public static void showToast(Context context, CharSequence text) {
        if(context != null) {
            Toast toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
            toast.show();
        }
    }
}
