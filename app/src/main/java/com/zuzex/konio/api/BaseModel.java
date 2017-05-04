package com.zuzex.konio.api;

import org.json.JSONException;
import org.json.JSONObject;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by dgureev on 11/26/14.
 */
public class BaseModel implements Parcelable{
    public boolean isSuccessStatus;
    public String message;
    JSONObject data;

    public JSONObject getData() {
        return data;
    }

    public BaseModel(String object) throws JSONException {
        this(new JSONObject(object));
    }

    public BaseModel(JSONObject jsonObject) {
        this.message = jsonObject.optString("message");
        this.isSuccessStatus = (jsonObject.optInt("status") == 1);
        this.data = jsonObject.optJSONObject("data");
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte)(isSuccessStatus ? 1 : 0));
        dest.writeString(message);
    }

    public BaseModel(Parcel in) {
        isSuccessStatus = in.readByte() != 0;
        message = in.readString();
    }

    public static Parcelable.Creator<BaseModel> CREATOR
            = new Parcelable.Creator<BaseModel>() {
        public BaseModel createFromParcel(Parcel in) {
            int description = in.readInt();
            switch (description) {
                case 1:
                    return new AnnounceModel(in);
                default:
                    return new BaseModel(in);
            }
        }

        public BaseModel[] newArray(int size) {
            return new BaseModel[size];
        }
    };
}
