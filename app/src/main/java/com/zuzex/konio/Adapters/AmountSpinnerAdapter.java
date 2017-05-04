package com.zuzex.konio.Adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.zuzex.konio.R;
import com.zuzex.konio.api.AmountOption;
import com.zuzex.konio.api.PaymentModel;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

/**
 * Created by dgureev on 19.12.14.
 */
public class AmountSpinnerAdapter extends ArrayAdapter<AmountOption> {

    private static class ViewHolder {
        TextView label;
        TextView value;
    }

    private  LayoutInflater inflater;
    private static String mCurrency;

    public AmountSpinnerAdapter(Context context, int resource, PaymentModel paymentModel) {
        super(context, resource, paymentModel.options);
        this.mCurrency = paymentModel.currency;
        inflater = (LayoutInflater) ((Activity) context).getLayoutInflater();
    }


    @Override public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getView(position, convertView, parent);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if(convertView == null) {
            viewHolder = new ViewHolder();
            convertView = inflater.inflate(R.layout.amount_spinner_layout, parent, false);
            viewHolder.label = (TextView) convertView.findViewById(R.id.amount_option_label);
            viewHolder.value = (TextView) convertView.findViewById(R.id.amount_option_value);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        AmountOption item = getItem(position);

        if(item != null) {
            viewHolder.label.setText(item.label);
            viewHolder.value.setText(formatAmountValue(item.value));
        }

        return convertView;
    }

    private static final String formatAmountValue(String amountValue) {
        String result;
        if(amountValue.length() > 2) {
            double value = Double.valueOf(amountValue)/100;
            DecimalFormat formatter = new DecimalFormat("##,###.00");
            formatter.setDecimalSeparatorAlwaysShown(true);
            formatter.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ENGLISH));
            result =  formatter.format(value);
        } else {
            result = String.format(Locale.ENGLISH, "%.2f", Double.valueOf(amountValue) / 100);
        }
        return mCurrency.toUpperCase()+" "+result;
    }
}
