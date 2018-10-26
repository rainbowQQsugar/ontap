package com.abinbev.dsa.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.abinbev.dsa.R;
import com.abinbev.dsa.adapter.MaterialGiveAdapter;
import com.abinbev.dsa.ui.presenter.ProductAllPresenter;
import com.salesforce.androidsyncengine.data.model.PicklistValue;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnFocusChange;
import butterknife.OnItemSelected;
import butterknife.OnTextChanged;

public class ProductAllView extends FrameLayout implements ProductAllPresenter.ViewModel {

    @Bind(R.id.product_name)
    TextView productName;

    @Bind(R.id.product_code)
    TextView productCodeTextView;

    @Bind(R.id.product_count)
    EditText productCount;

    @Bind(R.id.order_action)
    ImageView addProductButton;

    @Bind(R.id.spinnerReason)
    Spinner spinnerReason;

    @Bind(R.id.spinnerQuantity)
    Spinner spinnerQuantity;

    private ProductAllPresenter productAllPresenter;
    private OnClickListener addClickListener;
    private String recordTypeId;
    public ProductAllView(Context context, String recordTypeId) {
        this(context, null, recordTypeId);
    }

    public ProductAllView(Context context, AttributeSet attrs, String recordTypeId) {
        this(context, attrs, 0, recordTypeId);
    }

    public ProductAllView(Context context, AttributeSet attrs, int defStyleAttr, String recordTypeId) {
        super(context, attrs, defStyleAttr);
        this.recordTypeId = recordTypeId;
        setup(context);
    }

    private void setup(Context context) {
        inflate(context, R.layout.product_all_list_item, this);
        ButterKnife.bind(this);

        addClickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                productAllPresenter.addProduct(Double.valueOf(productCount.getText().toString()),
                        (PicklistValue) spinnerReason.getSelectedItem(), (String) spinnerQuantity.getSelectedItem());
            }
        };

        productAllPresenter = new ProductAllPresenter(recordTypeId);
    }

    public void setLineItem(MaterialGiveAdapter.LineItem lineItem) {
        productAllPresenter.setViewModel(this);
        productAllPresenter.setLineItem(lineItem);
        productAllPresenter.start();

        productName.setText(lineItem.product.getName());
        productCodeTextView.setText(lineItem.product.getCode());
        productCount.setText(lineItem.quantity > 0 ? String.valueOf(lineItem.quantity) : "");

        setPickList(lineItem.reason);
        setUnit(lineItem.unitOfMeasure);
    }

    private void setPickList(String reason) {
        SpinnerAdapter spinnerAdapter = spinnerReason.getAdapter();
        if (spinnerAdapter != null) {
            for (int index = 0; index < spinnerAdapter.getCount(); index++) {
                PicklistValue picklistValue = (PicklistValue) spinnerAdapter.getItem(index);
                if (picklistValue.getValue().equals(reason)) {
                    spinnerReason.setSelection(index);
                    break;
                }
            }
        }
    }

    private void setUnit(String unit) {
        SpinnerAdapter spinnerAdapter = spinnerQuantity.getAdapter();
        if (spinnerAdapter != null) {
            for (int index = 0; index < spinnerAdapter.getCount(); index++) {
                String quantity = (String) spinnerAdapter.getItem(index);
                if (quantity.equals(unit)) {
                    spinnerQuantity.setSelection(index);
                    break;
                }
            }
        }
    }

    private boolean validate() {
        return productAllPresenter.validate(productCount.getText().toString(), (PicklistValue) spinnerReason.getSelectedItem());
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        productAllPresenter.stop();
    }

    @Override
    public void updateImageButtonToAdd() {
        addProductButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_add_box));
        addProductButton.setOnClickListener(addClickListener);
    }

    @Override
    public void updateImageButtonToOutline() {
        addProductButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_add_box_outline));
        addProductButton.setOnClickListener(null);
    }

    @Override
    public void setReasonPicklist(List<PicklistValue> picklistValues, String selection) {
        ArrayAdapter<PicklistValue> adapter = new ArrayAdapter<PicklistValue>(getContext(),
                android.R.layout.simple_spinner_item, android.R.id.text1, picklistValues) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                return getDropDownView(position, convertView, parent);
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
        for (int index = 0; index < picklistValues.size(); index++) {
            if (picklistValues.get(index).getValue().equals(selection)) {
                spinnerReason.setSelection(index);
                break;
            }
        }
    }

    @OnTextChanged(value = R.id.product_count, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    @SuppressWarnings("unsused")
    public void onTextChanged(CharSequence charSequence) {
        if (validate()) {
            productAllPresenter.setProductCount(String.valueOf(charSequence));
        }

    }

    @OnItemSelected(R.id.spinnerReason)
    @SuppressWarnings("unsused")
    public void onReasonSelected(AdapterView<?> parent, View view, int position, long id){
        productAllPresenter.setReasonSelected((PicklistValue) spinnerReason.getItemAtPosition(position));
    }

    @OnItemSelected(R.id.spinnerQuantity)
    @SuppressWarnings("unsused")
    public void onQuantitySelected(AdapterView<?> parent, View view, int position, long id){
        productAllPresenter.setUnitOfMeasure((String) spinnerQuantity.getItemAtPosition(position));
    }

    @OnFocusChange(R.id.product_count)
    @SuppressWarnings("unused")
    public void onFocusChange(View v, boolean hasFocus) {
        if(v.getId() == R.id.product_count && !hasFocus) {
            InputMethodManager imm =  (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }

//    public ProductAllPresenter getProductAllPresenter() {
//        return productAllPresenter;
//    }
}
