package com.hoh.android.sunshyne;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.hoh.android.sunshyne.R;

/**
 * Created by funso on 5/16/15.
 */
public class ViewHolder {

    public final ImageView iconImageView;
    public final TextView dateTextView;
    public final TextView descTextView;
    public final TextView minTempTextView;
    public final TextView maxTempTextView;

    public ViewHolder(View view){
        iconImageView = (ImageView) view.findViewById(R.id.list_item_icon);
        dateTextView = (TextView) view.findViewById(R.id.list_item_date_textview);
        descTextView = (TextView) view.findViewById(R.id.list_item_forecast_textview);
        minTempTextView = (TextView) view.findViewById(R.id.list_item_low_textview);
        maxTempTextView = (TextView) view.findViewById(R.id.list_item_high_textview);
    }
}
