package com.abinbev.dsa.ui.view;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.abinbev.dsa.R;

import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ProspectGPSField extends LinearLayout {

    @Bind(R.id.field_name)
    TextView fieldTextView;

    @Bind(R.id.field_value)
    TextView gpsTextView;

    public ProspectGPSField(Context context) {
        super(context);
        inflate(context, R.layout.prospect_gps_field, this);
        ButterKnife.bind(this);
        setLatLng(Double.NaN,Double.NaN);
    }

    public String getFieldName() {
        return fieldTextView.getText().toString();
    }

    public void setLatLng(double lat, double lng) {
        gpsTextView.setText(String.format("%f,%f", lat, lng));
    }

}
