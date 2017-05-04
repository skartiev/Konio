package com.zuzex.konio.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcel;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.zuzex.konio.R;
import com.zuzex.konio.api.BaseModel;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by dgureev on 17.12.14.
 */
public class StyledFragment extends Fragment {
    private static int color = 0;
    private static String logoUrl = "";

    protected static final String STYLE_PREFERENCES_NAME = "style_preferences";

    protected static final String STYLE_LOGO = "style_logo";
    protected static final String STYLE_BACKGROUND_COLOR = "background_color";

    protected void updateBackground(View rootView) {
        rootView.setBackgroundColor(color);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if(logoUrl.isEmpty()) {
            Context ct = getActivity();
            if(ct != null) {
                SharedPreferences preferences = ct.getSharedPreferences(STYLE_PREFERENCES_NAME, Context.MODE_PRIVATE);
                logoUrl = preferences.getString(STYLE_LOGO, "");
                color = preferences.getInt(STYLE_BACKGROUND_COLOR, 0);
            }
        }
    }

    private void saveSettings() {
        Context ct = getActivity();
        if(ct != null) {
            SharedPreferences preferences = ct.getSharedPreferences(STYLE_PREFERENCES_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor ed = preferences.edit();
            ed.putString(STYLE_LOGO, logoUrl);
            ed.putInt(STYLE_BACKGROUND_COLOR, color);
            ed.commit();
        }
    }

    protected void updateLogo(ImageView imageView) {
        if(logoUrl != null && !logoUrl.isEmpty()) {
            Picasso.with(getActivity()).load(logoUrl).into(imageView);
        } else {
            Picasso.with(getActivity()).load(R.drawable.example_logo).into(imageView);
        }
    }

    public void loadStyleFromBaseModel(BaseModel baseModel) throws JSONException {
        JSONObject jsonObject = baseModel.getData();
        logoUrl = jsonObject.optString("logo");
        color = Color.parseColor(jsonObject.getString("background"));
        saveSettings();
    }

    private void loadDefaults() {
        color = Color.parseColor("#FFFFFF");
        logoUrl = "http://sefiani.com.au/wp-content/uploads/2011/02/EFTPOS_RGB_Large.gif";
    }
}
