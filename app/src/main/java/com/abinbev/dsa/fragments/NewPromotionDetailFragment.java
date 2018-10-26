package com.abinbev.dsa.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.abinbev.dsa.R;
import com.abinbev.dsa.activity.AppBaseActivity;
import com.abinbev.dsa.bus.event.NegotiationEvent;
import com.abinbev.dsa.model.CN_Product_Negotiation__c;
import com.abinbev.dsa.model.Product;
import com.abinbev.dsa.ui.view.negotiation.PromotionHelper;
import com.abinbev.dsa.utils.AbInBevConstants;
import com.abinbev.dsa.utils.PicklistUtils;
import com.salesforce.androidsyncengine.data.model.PicklistValue;
import com.squareup.otto.Bus;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;


public class NewPromotionDetailFragment extends Fragment  {

    private static final String TAG = NewPromotionDetailFragment.class.getSimpleName();

    @Inject
    Bus eventBus;

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.save)
    FloatingActionButton submitButton;

    @Bind(R.id.submit_buttons)
    ViewGroup submitButtons;

    @Bind(R.id.scroll_view)
    ScrollView scrollView;

    @Bind(R.id.promotion_type)
    Spinner promotionType;

    @Bind(R.id.promotion_category)
    Spinner categories;

    @Bind(R.id.products)
    Spinner productList;

    @Bind(R.id.product_brand)
    TextView productBrand;

    @Bind(R.id.product_uom)
    TextView productUom;

    @Bind(R.id.description_editable)
    EditText description;

    private View mainLayout;
    private PromotionHelper promotionHelper;
    private List<Product> products;
    CN_Product_Negotiation__c negotiation = null;
    ArrayList<String> pocCategories = new ArrayList<String>();
    ArrayList<String> consumerCategories = new ArrayList<String>();
    ArrayList<String> promotionTypes = new ArrayList<String>();

    public NewPromotionDetailFragment() {
        super();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mainLayout = inflater.inflate(R.layout.fragment_new_promotion_details, container, false);
        ((AppBaseActivity) getActivity()).getAppComponent().inject(this);
        ButterKnife.bind(this, mainLayout);

        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.negociaciones);
        getSupportActionBar().setSubtitle(R.string.new_negotiation);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        getPickListValues();

        setPromotionType();
        setPromotionTypeListener();
        setProductsListener();
        setCategoryListener();

        updateSubmitButton();

        return mainLayout;
    }


    private void setCategoryListener() {
        categories.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String selectedCategory = parentView.getItemAtPosition(position).toString();
                eventBus.post(new NegotiationEvent.UpdatePromotionCategory(selectedCategory));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {}

        });
    }

    private void setProductsListener() {
        productList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String selectedProduct = parentView.getItemAtPosition(position).toString();
                Product p = getProductByName(selectedProduct);
                if (p != null) {
                    productBrand.setText(p.getChBrand());
                    productUom.setText(p.getProductUnit());
                    eventBus.post(new NegotiationEvent.UpdatePromotionProductId(p.getId()));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {}

        });
    }

    private Product getProductByName(String selectedProduct) {
        Product product = null;
        for (Product p : products) {
            if (p.getProductShortName().equals(selectedProduct)) {
                product = p;
                break;
            }
        }
        return product;
    }

    private void setPromotionTypeListener() {
        promotionType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String selectedType = parentView.getItemAtPosition(position).toString();
                setCategory(selectedType);
                eventBus.post(new NegotiationEvent.UpdatePromotionType(selectedType));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {}

        });
    }

    private void getPickListValues() {

        HashMap<String, List<PicklistValue>> values = PicklistUtils.getMetadataPicklistValues(
                AbInBevConstants.AbInBevObjects.PRODUCT_NEGOTIATIONS, AbInBevConstants.ProductNegotiationFields.CATEGORY,
                AbInBevConstants.ProductNegotiationFields.PROMOTION_TYPE);

        for (Map.Entry<String, List<PicklistValue>> entry : values.entrySet()) {
            String key = entry.getKey();
            List<PicklistValue> vals = entry.getValue();

            if (key.equals(AbInBevConstants.ProductNegotiationFields.PROMOTION_TYPE)) {
                for (PicklistValue pv : vals)
                    promotionTypes.add(pv.getValue());
            } else if (key.equals(AbInBevConstants.ProductNegotiationFields.CATEGORY)) {
                for (PicklistValue pv : vals) {
                    String s = pv.getValue();
                    if (!(s.equals(getResources().getString(R.string.promotion_category_item_fixed_case)) ||
                            s.equals(getResources().getString(R.string.promotion_category_item_laddered_rebate))))
                        consumerCategories.add(s);
                    if (!s.startsWith(getResources().getString(R.string.promotion_category_item_beer)))
                        pocCategories.add(s);
                }
            }
        }
    }

    private void setCategory(String type) {
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item,
                type.equals(getResources().getString(R.string.poc_promotion)) ? pocCategories : consumerCategories);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categories.setAdapter(spinnerArrayAdapter);
        setCategorySelection();
    }

    private void setPromotionType() {
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item,
                promotionTypes);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        promotionType.setAdapter(spinnerArrayAdapter);
        setPromotionTypeSelection();
    }


    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            promotionHelper = (PromotionHelper) getActivity();
        } catch (Exception e) {
            Log.e(TAG, "onAttach: ", e);
        }
    }

    @OnClick(R.id.save)
    public void submit() {

        String[] buttonTitles = new String[]{getResources().getString(R.string.save_and_send)};

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("")
                .setItems(buttonTitles, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        promotionHelper.saveSubmitNegotiation();
                    }
                });
        builder.create().show();
    }


    public void updateSubmitButton() {
        boolean hide = description.getText().toString().isEmpty();
        submitButton.setVisibility(hide ? View.GONE : View.VISIBLE);
    }


    private ActionBar getSupportActionBar() {
        return ((AppCompatActivity) getActivity()).getSupportActionBar();
    }

    public void setProducts(List<Product> products) {
        this.products = products;
        ArrayList<String> productsArray = new ArrayList<String>();

        for (Product p : products)
            productsArray.add(p.getProductShortName());

        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, productsArray);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        productList.setAdapter(spinnerArrayAdapter);
        setProductSelection();
    }

    @OnTextChanged(R.id.description_editable)
    void onDescriptionTextChanged(CharSequence txt, int start, int count, int after) {
        updateSubmitButton();
        eventBus.post(new NegotiationEvent.UpdatePromotionDescription(txt.toString()));
    }

    public void setNew() {
        getSupportActionBar().setSubtitle(R.string.new_negotiation);
    }

    public void setFields(CN_Product_Negotiation__c negotiation) {

        this.negotiation = negotiation;

        if (negotiation != null) {
            promotionType.setSelection(promotionTypes.indexOf(negotiation.getType()));
            description.setText(negotiation.getDescription());
        }

        setCategorySelection();
        setPromotionTypeSelection();
        setProductSelection();

    }

    private void setCategorySelection() {
        if (negotiation != null) {
            ArrayList<String> categoryTypesArray = negotiation.getType().equals(getResources().getString(R.string.poc_promotion)) ? pocCategories
                    : consumerCategories;
            categories.setSelection(categoryTypesArray.indexOf(negotiation.getCategory()));
        }
    }

    private void setPromotionTypeSelection() {
        if (negotiation != null) {
            promotionType.setSelection(promotionTypes.indexOf(negotiation.getType()));
        }
    }

    private void setProductSelection() {
        SpinnerAdapter adapter =  productList.getAdapter();
        if (adapter != null && negotiation != null) {
            for (int i = 0; i < adapter.getCount(); i++) {
                if (adapter.getItem(i).equals(negotiation.getProductName())) {
                    productList.setSelection(i);
                    break;
                }
            }
        }
    }
}
