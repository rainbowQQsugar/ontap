package com.abinbev.dsa.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import com.abinbev.dsa.model.Material_Give__c;
import com.abinbev.dsa.ui.view.ProductAllView;
import com.abinbev.dsa.ui.view.ProductQuantityView;

import java.util.ArrayList;
import java.util.List;

public class ProductListAdapter2 extends RecyclerView.Adapter implements Filterable {

    public static final String VIEW_TYPE_COUNT = "count";
    public static final String VIEW_TYPE_ALL = "all";

    private final List<MaterialGiveAdapter.LineItem> lineItems;
    private CompositeProductFilter compositeProductFilter;
    private String recordTypeId;
    private String viewType;

    public ProductListAdapter2(String recordTypeId) {
        super();
        this.recordTypeId = recordTypeId;
        lineItems = new ArrayList<>();
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public void removeProduct(String productId) {
        compositeProductFilter.removeProduct(productId);

        MaterialGiveAdapter.LineItem lineItemToBeRemoved = null;
        for (MaterialGiveAdapter.LineItem lineItem : lineItems) {
            if (productId.equals(lineItem.product.getId())) {
                lineItemToBeRemoved = lineItem;
                break;
            }
        }
        int position = lineItems.indexOf(lineItemToBeRemoved);
        if (lineItems.remove(lineItemToBeRemoved)) {
            notifyItemRemoved(position);
        }
    }

    public void setData(List<Material_Give__c> products, String viewType) {
        this.lineItems.clear();

        for (Material_Give__c product : products) {
            this.lineItems.add(new MaterialGiveAdapter.LineItem(product, 0));
        }

        if ("Quantity".equalsIgnoreCase(viewType)) {
            this.viewType = VIEW_TYPE_COUNT;
        } else if ("All".equalsIgnoreCase(viewType)) {
            this.viewType = VIEW_TYPE_ALL;
        }

        if (compositeProductFilter == null) {
            compositeProductFilter = new CompositeProductFilter(this, this.lineItems);
        }

        this.notifyDataSetChanged();
    }

    protected void updateData(List<MaterialGiveAdapter.LineItem> lineItems) {
        this.lineItems.clear();
        this.lineItems.addAll(lineItems);
        this.notifyDataSetChanged();
    }

    @Override
    public Filter getFilter() {
        return compositeProductFilter;
    }

    //recycler view stuff here
    class ProductViewHolder extends RecyclerView.ViewHolder {
        View view;
        public ProductViewHolder(View itemView) {
            super(itemView);
            this.view = itemView;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        View convertView;
        if (VIEW_TYPE_ALL.equals(this.viewType)) {
            convertView = new ProductAllView(parent.getContext(), recordTypeId);
        } else {
            convertView = new ProductQuantityView(parent.getContext());
        }
        convertView.setLayoutParams(params);
        return new ProductViewHolder(convertView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        if (VIEW_TYPE_ALL.equals(viewType)) {
            ((ProductAllView) holder.itemView).setLineItem(lineItems.get(position));
        } else {
            ((ProductQuantityView) holder.itemView).setLineItem(lineItems.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return lineItems.size();
    }
}
