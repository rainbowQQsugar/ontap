package com.abinbev.dsa.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import com.abinbev.dsa.model.Material_Give__c;
import com.abinbev.dsa.ui.view.ProductAllView;
import com.abinbev.dsa.ui.view.ProductQuantityView;

import java.util.ArrayList;
import java.util.List;

public class MaterialGiveAdapter extends BaseAdapter implements Filterable {

    public static final String VIEW_TYPE_COUNT = "count";
    public static final String VIEW_TYPE_ALL = "all";

    private final List<LineItem> lineItems;
    private CompositeProductFilter compositeProductFilter;

    private String viewType;

    public MaterialGiveAdapter() {
        super();
        lineItems = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return lineItems.size();
    }

    @Override
    public Object getItem(int position) {
        return lineItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final LineItem lineItem = lineItems.get(position);

        if (convertView == null) {
            if (VIEW_TYPE_ALL.equals(viewType)) {
                convertView = new ProductAllView(parent.getContext(), null);
            } else {
                convertView = new ProductQuantityView(parent.getContext());
            }
        }

        if (VIEW_TYPE_ALL.equals(viewType)) {
            initProductAllView(convertView, lineItem);
        } else {
            initProductQuantityView(convertView, lineItem);
        }
        return convertView;
    }

    private void initProductQuantityView(View convertView, LineItem lineItem) {
        ProductQuantityView productQuantityView = (ProductQuantityView) convertView;
        productQuantityView.setLineItem(lineItem);
    }

    private void initProductAllView(View convertView, LineItem lineItem) {
        ProductAllView view = (ProductAllView) convertView;
        view.setLineItem(lineItem);
    }

    public void removeProduct(String productId) {
        compositeProductFilter.removeProduct(productId);

        LineItem lineItemToBeRemoved = null;
        for (LineItem lineItem : lineItems) {
            if (productId.equals(lineItem.product.getId())) {
                lineItemToBeRemoved = lineItem;
                break;
            }
        }

        if (lineItems.remove(lineItemToBeRemoved)) {
            this.notifyDataSetChanged();
        }
    }

    public void setData(List<Material_Give__c> products, String viewType) {
        this.lineItems.clear();

        for (Material_Give__c product : products) {
            this.lineItems.add(new LineItem(product, 0));
        }

        if ("Quantity".equalsIgnoreCase(viewType)) {
            this.viewType = VIEW_TYPE_COUNT;
        } else if ("All".equalsIgnoreCase(viewType)) {
            this.viewType = VIEW_TYPE_ALL;
        }

        if (compositeProductFilter == null) {
//            compositeProductFilter = new CompositeProductFilter(this, this.lineItems);
        }

        this.notifyDataSetChanged();
    }

    protected void updateData(List<LineItem> lineItems) {
        this.lineItems.clear();
        this.lineItems.addAll(lineItems);
        this.notifyDataSetChanged();
    }

    @Override
    public Filter getFilter() {
        return compositeProductFilter;
    }

    public static class LineItem {
        public Material_Give__c product;
        public int quantity;
        public String reason;
        public String unitOfMeasure;

        public LineItem(Material_Give__c product, int quantity) {
            this.product = product;
            this.quantity = quantity;
        }
    }

}
