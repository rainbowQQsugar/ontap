package com.abinbev.dsa.ui.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.abinbev.dsa.R;
import com.abinbev.dsa.model.Material_Give__c;
import com.abinbev.dsa.model.Order__c;
import com.salesforce.androidsyncengine.data.model.PicklistValue;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * @author Jason Harris (jason@akta.com)
 */
public class EditOrderView extends FrameLayout implements View.OnClickListener {

    @Bind(R.id.product_name)
    TextView productName;

    @Bind(R.id.product_code)
    TextView productCode;

    @Bind(R.id.product_count)
    EditText productCount;

    @Bind(R.id.measure)
    TextView measure;

    @Bind(R.id.order_action)
    ImageView actionOrder;

    @Bind(R.id.spinnerReason)
    Spinner spinnerReason;

    @Bind(R.id.spinnerQuantity)
    Spinner spinnerQuantity;

    private Order__c order;
    private int index = -1;

    public EditOrderView(Context context) {
        super(context);
        setUp(context);
    }

    public EditOrderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setUp(context);
    }

    public EditOrderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setUp(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public EditOrderView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setUp(context);
    }

    private void setUp(Context context) {
        inflate(context, R.layout.edit_order_item, this);
        ButterKnife.bind(this);

        if (isInEditMode()) {
            return;
        }

        actionOrder.setOnClickListener(this);
    }

    public void setOrder(Order__c order, int index) {
        this.order = order;
        this.index = index;

        if ("Quantity".equals(order.getOrderTypeProductTypeMapping__c().getLineItems())) {
            measure.setVisibility(View.VISIBLE);
            productCode.setVisibility(View.VISIBLE);
            spinnerReason.setVisibility(View.GONE);
            spinnerQuantity.setVisibility(View.GONE);
        } else {
            measure.setVisibility(View.GONE);
            productCode.setVisibility(View.GONE);
            spinnerReason.setVisibility(View.VISIBLE);
            spinnerQuantity.setVisibility(View.VISIBLE);
        }

        setupSpinner(order, index);

        List<Material_Give__c> products = order.getProducts();
        if (products != null) {
            setProduct(products.get(index));
        }

        String unit = order.getUnitOfMeasureByIndex(index);
        setupQuantity(unit);

        productCount.setText(order.getQuantityByIndex(index));
    }

    private void setupQuantity(String unit) {
        for (int i = 0; i < spinnerQuantity.getAdapter().getCount(); i++) {
            if (spinnerQuantity.getAdapter().getItem(i).equals(unit)) {
                spinnerQuantity.setSelection(i, false);
            }
        }
    }

    private void setReason(Order__c order, int index) {
        for (int i = 0; i <  spinnerReason.getAdapter().getCount(); i++) {
            PicklistValue value = (PicklistValue) spinnerReason.getAdapter().getItem(i);
            if (value.getValue() != null && value.getValue().equals(order.getMotiveByIndex(index))) {
                spinnerReason.setSelection(i, false);
                return;
            }
        }
        spinnerReason.setSelection(Adapter.NO_SELECTION, false);
    }

    private void setProduct(Material_Give__c product) {
        productName.setText(product.getName());
        productCode.setText(product.getCode());
    }

    private void setupSpinner(Order__c order, int index) {
        ArrayAdapter<PicklistValue> adapter = new ArrayAdapter<PicklistValue>(getContext(),
                android.R.layout.simple_spinner_item, android.R.id.text1, order.getReasonPicklist()) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                LayoutInflater inflater = LayoutInflater.from(parent.getContext());
                PicklistValue picklistValue = getItem(position);
                TextView textView = (TextView) inflater.inflate(R.layout.dropdown_text_item, parent, false);
                textView.setText(picklistValue.getLabel());
                return textView;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = convertView;
                if (view == null) {
                    LayoutInflater inflater = LayoutInflater.from(parent.getContext());
                    view = inflater.inflate(R.layout.reason_dropdown, parent, false);
                }
                PicklistValue picklistValue = getItem(position);
                ((TextView) view.findViewById(android.R.id.text1)).setText(picklistValue.getLabel());
                return view;
            }
        };
        adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spinnerReason.setAdapter(adapter);
        setReason(order, index);
    }

    @Override
    public void onClick(View v) {
        Toast.makeText(getContext(), "pressed minus! ", Toast.LENGTH_LONG).show();
    }

}