package com.zuzex.konio.api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by dgureev on 11/27/14.
 */


public class PaymentModel extends BaseModel{
 ///////normal
//    {
//        "data": {
//        "amount": 40,
//                "id": "038db039759f0c32e0b75b782350c8ff",
//                "items": [],
//        "subject": "Testing !",
//                "pay_from": {
//            "credits": "Credits Account"
//        },
//        "recipient": "Demo Account",
//                "options": [],
//        "currency": "usd"
//    },
//        "message": "start payment",
//            "status": 1
//    }


    //////donation
//    {
//        "data": {
//        "amount": "",
//                "id": "3b47c16bc5556477eb0594c955e6fac8",
//                "pay_from": {
//            "credits": "Credits Account"
//        },
//        "subject": "Meow Meow payment",
//                "recipient": "Demo Account",
//                "options": [],
//        "currency": "usd"
//    },
//        "message": "start payment",
//            "status": 1
//    }


    ////////multiple options
//    {
//        "data": {
//        "amount": "",
//                "id": "4caf52e6f8ca7c7e7f30cc50f0f068fd",
//                "pay_from": {
//            "credits": "Credits Account"
//        },
//        "subject": "Meow Meow Multiple Options",
//                "recipient": "Demo Account",
//                "options": [
//        {
//            "value": 1,
//                "label": "Meow"
//        },
//        {
//            "value": 2,
//                "label": "woof"
//        },
//        {
//            "value": 3,
//                "label": "bark"
//        },
//        {
//            "value": 4,
//                "label": "qwaq"
//        },
//        {
//            "value": 5,
//                "label": "kurlique"
//        }
//        ],
//        "currency": "usd"
//    },
//        "message": "start payment",
//            "status": 1
//    }

    public enum PAY_TYPE {
        PAY_TYPE_NORMAL,
        PAY_TYPE_DONATION,
        PAY_TYPE_OPTIONS
    }

    public String getAmount() {
        return String.valueOf(amount);
    }

    public double amount;
    public String id;
    public JSONArray items;
    public String subject;
    public List<AmountOption> options;
    public String currency;
    public Map<String, String> payFrom;
    public String recipient;
    public PAY_TYPE payType = PAY_TYPE.PAY_TYPE_NORMAL;
    public String payFromSelected = "";

    public String getSelectedOption() {
        return selectedOption;
    }

    public String selectedOption = "";

    public PaymentModel(JSONObject jsonObject) throws JSONException{
        super(jsonObject);
        payFrom = new HashMap<>();
        id = data.optString("id");

        if(data.optString("amount").equals("")) {
            amount = 0;
        } else {
            amount = data.optDouble("amount");
        }
        items = data.optJSONArray("items");
        subject = data.optString("subject");
        currency = data.optString("currency");
        options = AmountOption.CREATOR(data.optJSONArray("options"));
        recipient = data.optString("recipient");

        JSONObject pay_tmp = data.optJSONObject("pay_from");
        if(pay_tmp != null) {
            Iterator<?> keys = pay_tmp.keys();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                if (pay_tmp.get(key) instanceof String) {
                    String value = pay_tmp.optString(key);
                    if (value == null) {
                        value = "";
                    }
                    payFrom.put(value, key);
                }
            }
        }
        setPayMentType();
    }

    private void setPayMentType() {
        if(data.optString("amount").equals("")) {
            payType = PAY_TYPE.PAY_TYPE_DONATION;
        }
        JSONArray options = data.optJSONArray("options");
        if(options != null && options.length() > 0) {
            payType = PAY_TYPE.PAY_TYPE_OPTIONS;
        }
    }
}
