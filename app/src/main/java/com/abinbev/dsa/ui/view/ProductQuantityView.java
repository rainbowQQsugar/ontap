package com.abinbev.dsa.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.abinbev.dsa.R;
import com.abinbev.dsa.adapter.MaterialGiveAdapter;
import com.abinbev.dsa.ui.presenter.ProductQuantityPresenter;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnFocusChange;
import butterknife.OnTextChanged;

public class ProductQuantityView extends FrameLayout implements ProductQuantityPresenter.ViewModel {

    @Bind(R.id.product_name)
    TextView productName;

    @Bind(R.id.product_code)
    TextView productCodeTextView;

    @Bind(R.id.product_count)
    EditText productCount;

    @Bind(R.id.add_product)
    ImageView addProductButton;

    private ProductQuantityPresenter productQuantityPresenter;
    private OnClickListener addClickListener;

    public ProductQuantityView(Context context) {
        this(context, null);
    }

    public ProductQuantityView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ProductQuantityView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setup(context);
    }

    private void setup(Context context) {
        inflate(context, R.layout.product_quantity_list_entry, this);
        ButterKnife.bind(this);

        addClickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                productQuantityPresenter.addProduct();
            }
        };

        productQuantityPresenter = new ProductQuantityPresenter();
        productQuantityPresenter.setViewModel(this);
    }

    public void setLineItem(MaterialGiveAdapter.LineItem lineItem) {
        productQuantityPresenter.setViewModel(this);
        productQuantityPresenter.setLineItem(lineItem);

        productName.setText(lineItem.product.getName());
        productCodeTextView.setText(lineItem.product.getCode());
        productCount.setText(lineItem.quantity > 0 ? String.valueOf(lineItem.quantity) : "");
    }


    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        productQuantityPresenter.setViewModel(this);
        productQuantityPresenter.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        productQuantityPresenter.stop();
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

    private boolean validate() {
        return productQuantityPresenter.validate(productCount.getText().toString());
    }

    @OnTextChanged(value = R.id.product_count, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    @SuppressWarnings("unused")
    public void onProductCountEntered(CharSequence charSequence){
        if (validate()) {
            productQuantityPresenter.setProductCount(String.valueOf(charSequence));
        }
    }

    @OnFocusChange(R.id.product_count)
    @SuppressWarnings("unused")
    public void onFocusChange(View v, boolean hasFocus) {
        if(v.getId() == R.id.product_count && !hasFocus) {

            InputMethodManager imm =  (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }
}
