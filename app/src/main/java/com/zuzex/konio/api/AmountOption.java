package com.zuzex.konio.api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dgureev on 19.12.14.
 */

public class AmountOption {

    public AmountOption(JSONObject amountOptionJson) {
        value = amountOptionJson.optString("value");
        label = amountOptionJson.optString("label");
    }

    public String value;
    public String label;

    public static List<AmountOption> CREATOR(JSONArray jsonOpions) {
        List<AmountOption> amountOptionList = new ArrayList<AmountOption>();
        if(jsonOpions != null && jsonOpions.length() > 0) {
            try {
                for (int i = 0; i < jsonOpions.length(); i++) {
                    AmountOption amountOptionItem = new AmountOption(jsonOpions.getJSONObject(i));
                    amountOptionList.add(amountOptionItem);
                }
            } catch(JSONException e) {
                e.printStackTrace();
            }

        }
        return amountOptionList;
    }
}

