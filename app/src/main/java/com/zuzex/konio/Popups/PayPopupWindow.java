package com.zuzex.konio.Popups;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;

import com.badoo.mobile.util.WeakHandler;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.zuzex.konio.Adapters.AmountSpinnerAdapter;
import com.zuzex.konio.R;
import com.zuzex.konio.Utils.DecimalFilter;
import com.zuzex.konio.Utils.UiHelper;
import com.zuzex.konio.api.BaseModel;
import com.zuzex.konio.api.HttpApi;
import com.zuzex.konio.api.PaymentModel;

import org.json.JSONException;

import java.io.IOException;

/**
 * Created by dgureev on 18.12.14.
 */
public class PayPopupWindow extends PopupWindow {
    private Context mContext;
    private View popupPayView;
    EditText editAmountValue;
    WeakHandler handler;

    public PayPopupWindow(Context context, PaymentModel paymentModel, WeakHandler handler) {
        super(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        this.mContext = context;
        this.handler = handler;
        this.setFocusable(true);
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        popupPayView = layoutInflater.inflate(R.layout.pay_start_popup, null);
        showPayPopup(paymentModel);
        this.setContentView(popupPayView);
        showAtLocation(popupPayView, Gravity.CENTER, 0, 0);
    }

    public void showPayPopup(final PaymentModel payModel) {
        TextView subject = (TextView) popupPayView.findViewById(R.id.pay_popup_subject);
        subject.setText(payModel.subject);
        TextView message = (TextView) popupPayView.findViewById(R.id.pay_message);
        message.setText(payModel.message);

        TextView currency = (TextView) popupPayView.findViewById(R.id.pay_currency);
        currency.setText(payModel.currency);
        //normal
        switch (payModel.payType) {
            case PAY_TYPE_OPTIONS:
                TextView pay_currency = (TextView) popupPayView.findViewById(R.id.pay_currency);
                pay_currency.setVisibility(View.GONE);
                Spinner amountSpinner = (Spinner) popupPayView.findViewById(R.id.amount_spinner);
                amountSpinner.setVisibility(View.VISIBLE);
                AmountSpinnerAdapter amountSpinnerAdapter =
                        new AmountSpinnerAdapter(mContext, R.layout.amount_spinner_layout, payModel);
                amountSpinner.setAdapter(amountSpinnerAdapter);
                amountSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        payModel.selectedOption = payModel.options.get(position).value;
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
                break;
            case PAY_TYPE_NORMAL:
                currency.setVisibility(View.VISIBLE);
                TextView amountValue = (TextView) popupPayView.findViewById(R.id.pay_amount_value);
                amountValue.setVisibility(View.VISIBLE);
                amountValue.setText(String.valueOf(payModel.amount));
                break;
            case PAY_TYPE_DONATION:
                currency.setVisibility(View.VISIBLE);
                editAmountValue = (EditText) popupPayView.findViewById(R.id.pay_amount_editable);
                editAmountValue.setVisibility(View.VISIBLE);
                editAmountValue.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });
                editAmountValue.addTextChangedListener(new DecimalFilter(editAmountValue));
                break;
        }

        TextView recipientName = (TextView) popupPayView.findViewById(R.id.pay_recipient_name);
        recipientName.setText(payModel.recipient);

        Button payAcceptButton = (Button) popupPayView.findViewById(R.id.pay_button_accept);
        Button payCancelButton = (Button) popupPayView.findViewById(R.id.pay_button_cancel);

        final String[] accounts = payModel.payFrom.keySet().toArray(new String [0]);

        Spinner accouuntSpinner = (Spinner) popupPayView.findViewById(R.id.account_spinner);
        ArrayAdapter<String> spinnerAccountAdapter
                = new ArrayAdapter<String>(mContext, R.layout.spinner_item, accounts);
        spinnerAccountAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        accouuntSpinner.setAdapter(spinnerAccountAdapter);
        accouuntSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                payModel.payFromSelected = accounts[position];
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        payCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closePayPopup();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        UiHelper.showToast(mContext, mContext.getString(R.string.pay_cancel));
                        payModel.payFromSelected = "";
                    }
                });
            }
        });

        payAcceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(editAmountValue != null) {
                    String editAmountString = editAmountValue.getText().toString();
                    if(!editAmountString.isEmpty()) {
                        payModel.amount = Double.valueOf(editAmountString);
                    }
                }

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if(!payModel.payFromSelected.isEmpty()) {
                            confirmPayment(payModel);
                            closePayPopup();
                        } else {
                            if(mContext != null) {
                                UiHelper.showToast(mContext, "pay from not set");
                            }
                        }
                    }
                });
            }
        });
    }

    public void closePayPopup() {
            dismiss();
            popupPayView = null;
    }

    private void confirmPayment(PaymentModel payModel) {
        HttpApi.getInstance().confirmPayment(payModel, new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if(mContext != null) {
                            UiHelper.showToast(mContext, mContext.getString(R.string.network_error));
                        }
                    }
                });
            }

            @Override
            public void onResponse(final Response response) throws IOException {
                String responseString = null;
                responseString = response.body().string();
                try {
                    BaseModel answer = new BaseModel(responseString);
                    final String message = answer.message;
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            UiHelper.showToast(mContext, message);
                        }
                    });
                } catch (JSONException e) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            UiHelper.showToast(mContext, mContext.getString(R.string.data_error));
                        }
                    });
                }
            }
        });
    }
}
