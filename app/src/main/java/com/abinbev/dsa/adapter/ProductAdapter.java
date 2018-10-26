package com.abinbev.dsa.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.abinbev.dsa.R;
import com.abinbev.dsa.model.Order_Item__c;
import com.abinbev.dsa.model.Product;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnTextChanged;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> implements Filterable {

    private List<Order_Item__c> orderItems = new ArrayList<>();
    private SearchFilter searchFilter = new SearchFilter(this);
    private OrderItemAddedListener orderItemAddedListener;

    public interface OrderItemAddedListener {
        void onOrderItemAdded(Order_Item__c orderItem);
    }


    public ProductAdapter(OrderItemAddedListener orderItemAddedListener) {
        this.orderItemAddedListener = orderItemAddedListener;
    }

    @Override
    public ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final ProductViewHolder productViewHolder = new ProductViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.product_item_view, parent, false));
        productViewHolder.orderAction.setOnClickListener(
                v -> {
                    int adapterPosition = productViewHolder.getAdapterPosition();

                    if (adapterPosition >= 0 && adapterPosition < orderItems.size()) {
                        Context context = productViewHolder.itemView.getContext();
                        Toast.makeText(context, context.getResources().getString(R.string.add_order_item_item_added), Toast.LENGTH_SHORT).show();
                        Order_Item__c orderItem = orderItems.get(adapterPosition);
                        orderItems.remove(adapterPosition);
                        searchFilter.removeOrderItem(orderItem);
                        notifyItemRemoved(adapterPosition);
                        orderItemAddedListener.onOrderItemAdded(orderItem);
                    }
                }
        );
        return productViewHolder;
    }

    @Override
    public void onBindViewHolder(ProductViewHolder holder, int position) {
        Order_Item__c orderItem = orderItems.get(position);
        Product product = orderItem.getProduct();
        holder.productName.setText(product.getProductName());
        holder.productCode.setText(TextUtils.isEmpty(orderItem.getProductExternalId())? orderItem.B2B_ProductID__c(): orderItem.getProductExternalId());
        holder.productUnit.setText(product.getProductUnit());
        holder.productCount.setText(orderItem.getQuantity() > 0 ? String.valueOf(orderItem.getQuantity()) : "");
    }

    @Override
    public int getItemCount() {
        return orderItems.size();
    }

    public void setOrderItems(List<Order_Item__c> orderItems) {
        this.orderItems.clear();
        this.orderItems.addAll(orderItems);
        searchFilter.setOrderItems(orderItems);
        notifyDataSetChanged();
    }

    void updateOrderItems(List<Order_Item__c> orderItems) {
        this.orderItems.clear();
        this.orderItems.addAll(orderItems);
        notifyDataSetChanged();
    }

    class ProductViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.product_name)
        TextView productName;

        @Bind(R.id.product_code)
        TextView productCode;

        @Bind(R.id.product_unit)
        TextView productUnit;

        @Bind(R.id.product_count)
        EditText productCount;

        @Bind(R.id.order_action)
        ImageView orderAction;

        public ProductViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @OnTextChanged(value = R.id.product_count, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
        void onCountChanged(CharSequence charSequence) {
            int adapterPosition = getAdapterPosition();

            if (adapterPosition >= 0 && adapterPosition < orderItems.size()) {

                Resources resources = itemView.getContext().getResources();
                if (charSequence.length() > 0 && Integer.valueOf(String.valueOf(charSequence)) > 0) {
                    orderItems.get(adapterPosition).setQuantity(Integer.valueOf(String.valueOf(charSequence)));
                    orderAction.setImageDrawable(resources.getDrawable(R.drawable.ic_add_box));
                    orderAction.setEnabled(true);
                } else {
                    orderItems.get(adapterPosition).setQuantity(0);
                    orderAction.setImageDrawable(resources.getDrawable(R.drawable.ic_add_box_outline));
                    orderAction.setEnabled(false);
                }
            }
        }
    }

    @Override
    public Filter getFilter() {
        return searchFilter;
    }


    /**
     * A filter capable of searching across {@link Product}s by
     * either product code or name.
     */
    private class SearchFilter extends Filter {

        ProductAdapter productAdapter;
        List<Order_Item__c> orderItems;

        SearchFilter(ProductAdapter productAdapter) {
            this.productAdapter = productAdapter;
            this.orderItems = new ArrayList<>();
        }

        void setOrderItems(List<Order_Item__c> orderItems) {
            this.orderItems.addAll(orderItems);
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Order_Item__c> filteredOrderItems = new ArrayList<>();
            String token = String.valueOf(constraint).toUpperCase();
            for (Order_Item__c orderItem : orderItems) {
                Product product = orderItem.getProduct();
                //note that we need to replace JS &nbsp(/u160) characters with SPACE (/u32) for matching
                if (product.getProductName().toUpperCase().replace("\u00A0", " ").contains(token)
                        || product.getProductCode().toUpperCase().replace("\u00A0", " ").contains(token)) {
                    filteredOrderItems.add(orderItem);
                }
            }

            FilterResults filterResults = new FilterResults();
            filterResults.count = filteredOrderItems.size();
            filterResults.values = filteredOrderItems;
            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            productAdapter.updateOrderItems((List<Order_Item__c>) results.values);
        }

        void removeOrderItem(Order_Item__c orderItem) {
            String productId = orderItem.getProduct().getId();
            for (int itemIndex = 0; itemIndex < orderItems.size(); itemIndex++) {
                if (orderItems.get(itemIndex).getProduct().getId().equals(productId)) {
                    orderItems.remove(itemIndex);
                    break;
                }
            }
        }
    }


}
