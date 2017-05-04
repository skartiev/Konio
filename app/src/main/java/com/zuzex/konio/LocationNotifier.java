package com.zuzex.konio;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Html;
import android.widget.RemoteViews;

import com.badoo.mobile.util.WeakHandler;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.zuzex.konio.Services.KonioBaseServiceHelper;
import com.zuzex.konio.Utils.UiHelper;
import com.zuzex.konio.api.AnnounceModel;
import com.zuzex.konio.api.HttpApi;

import org.json.JSONException;

import java.io.IOException;

/**
 * Created by dgureev on 11/27/14.
 */
public class LocationNotifier {
    public String mTitle = "" ;
    private Context mContext;
    private int id = 0;
    WeakHandler mHandler;
    AlertDialog alertDialog;
    NotificationManager mNotificationManager;
    private LocalBroadcastManager localAnnounceBroadcastManager;
    private BroadcastReceiver deviceAnnounceReceiver;

    public LocationNotifier(Context context, WeakHandler handler) {
        this.mContext = context;
        this.mHandler = handler;
        mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        mTitle = mContext.getApplicationContext().getPackageName();
        localAnnounceBroadcastManager = LocalBroadcastManager.getInstance(context);
        deviceAnnounceReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                AnnounceModel announceModel = intent.getParcelableExtra(AnnounceModel.TAG);
                showPushNotification(announceModel);
            }
        };
        localAnnounceBroadcastManager.registerReceiver(deviceAnnounceReceiver, new IntentFilter(KonioBaseServiceHelper.ANNOUNCE_INTENT_FILTER));
    }

    public void testNotify() {
        if(id > 2) {
            id = 0;
        }
        String ssdiName = "unknown";
        switch (id) {
            case 0: ssdiName = "abc123"; break;
            case 1: ssdiName = "tsww_ssid"; break;
        }
        HttpApi.getInstance().sendDeviceUUID(ssdiName, new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        UiHelper.showToast(mContext, mContext.getString(R.string.network_error));
                    }
                });
            }

            @Override
            public void onResponse(Response response) throws IOException {
                try {
                    final AnnounceModel announceModel = new AnnounceModel(response.body().string());
                    if (announceModel.isSuccessStatus) {
                        if(mContext != null) {
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if(mContext != null) {
                                        alertDialogNotify(announceModel);
                                    }
                                    showPushNotification(announceModel);
                                }
                            });
                        }

                    } else {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                UiHelper.showToast(mContext, mContext.getString(R.string.unknown_device));
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        id++;
    }

    public void showPushNotification(final AnnounceModel model) {
        if(model != null) {
            Intent targetIntent = new Intent(mContext, MainActivity.class);
            targetIntent.putExtra(AnnounceModel.TAG, model);
            int width = mContext.getResources().getDimensionPixelSize(android.R.dimen.notification_large_icon_width);
            int height = mContext.getResources().getDimensionPixelSize(android.R.dimen.notification_large_icon_height);
            Bitmap largeIcon = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_launcher), width, height, true);
            PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0, targetIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            long[] pattern = {100, 300, 100, 300};

            RemoteViews contentView = new RemoteViews(mContext.getPackageName(), R.layout.device_notification);
            contentView.setImageViewResource(R.id.image, R.drawable.ic_launcher);
            contentView.setTextViewText(R.id.title, mTitle);
            contentView.setTextViewText(R.id.text, Html.fromHtml(model.message));

            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(mContext)
                            .setLargeIcon(largeIcon)
                            .setSmallIcon(R.drawable.ic_launcher)
                            .setSound(alarmSound)
                            .setVibrate(pattern)
                            .setLights(Color.GREEN, 1000, 3000)
                            .setContent(contentView)
                            .setAutoCancel(true);
            mBuilder.setContentIntent(contentIntent);
            mNotificationManager.notify(0, mBuilder.build());
        }
    }

    public void alertDialogNotify(final AnnounceModel announceModel) {
        if(announceModel == null || announceModel.message == null) {
            return;
        }
        if (alertDialog != null) {
            alertDialog.dismiss();
        }
        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(mContext.getString(R.string.location_dialog_title));
        builder.setMessage(Html.fromHtml(announceModel.message));
        if (announceModel.isValudUrl()) {
            builder.setMessage(announceModel.url);
            builder.setPositiveButton(mContext.getString(R.string.open_in_browser), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(announceModel.url));
                    mContext.startActivity(browserIntent);
                }
            });
        } else {
            builder.setPositiveButton(mContext.getString(R.string.ok), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    alertDialog.dismiss();
                    alertDialog = null;
                }
            });
        }
        alertDialog = builder.create();
        alertDialog.show();
    }
}
