package com.zuzex.konio.api;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;

import java.io.Serializable;

/**
 * Created by dgureev on 11/27/14.
 */
public class AnnounceModel extends BaseModel implements Parcelable {
    public static final String TAG = "AnnounceModel";

//    {"status":1,"message":"message action","data":{"message":"<em>Hello World<\/em>. Testing a <b>Response <span style=\"color:#440000\">Message<\/span><\/b>"}}

    public String url;
    public AnnounceModel(String strinResponse) throws JSONException{
        super(strinResponse);

        if(data != null) {
            url = data.optString("url");
            if(data.optString("message") != null) {
                message = data.optString("message");
            }
        }
    }

    public boolean isValudUrl() {
        if(url != null && !url.isEmpty()) {
            return true;
        }
        return false;
    }

    @Override
    public int describeContents() {
        return 1;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(url);
    }

    public AnnounceModel(Parcel in) {
        super(in);
        url = in.readString();
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public AnnounceModel createFromParcel(Parcel in) {
            return new AnnounceModel(in);
        }

        public AnnounceModel[] newArray(int size) {
            return new AnnounceModel[size];
        }
    };
}
