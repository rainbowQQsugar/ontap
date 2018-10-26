package com.abinbev.dsa.adapter;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.abinbev.dsa.R;
import com.abinbev.dsa.model.Order_Item__c;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class OrderItemAdapter extends RecyclerView.Adapter<OrderItemAdapter.ViewHolder> {

    private boolean itemsCanBeDeleted;
    private OrderItemRemovedListener orderItemRemovedListener;
    List<Order_Item__c> orderItems = new ArrayList<>();

    public interface OrderItemRemovedListener {
        void orderItemRemoved(Order_Item__c orderItem);
    }

    public OrderItemAdapter(OrderItemRemovedListener orderItemRemovedListener) {
        this.orderItemRemovedListener = orderItemRemovedListener;
    }

    public void setItemsCanBeDeleted(boolean itemsCanBeDeleted) {
        this.itemsCanBeDeleted = itemsCanBeDeleted;
        this.notifyDataSetChanged(); //cause view holders to redraw
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final ViewHolder viewHolder = new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.order_item_view, parent, false));
        viewHolder.orderAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int adapterPosition = viewHolder.getAdapterPosition();
                Order_Item__c orderItem = orderItems.get(adapterPosition);
                orderItems.remove(adapterPosition);
                notifyItemRemoved(adapterPosition);
                orderItemRemovedListener.orderItemRemoved(orderItem);
            }
        });
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Order_Item__c orderItem = orderItems.get(position);
        holder.productName.setText(TextUtils.isEmpty(orderItem.getProductDesc()) ? orderItem.getProductName() : orderItem.getProductDesc());
        holder.productCode.setText(TextUtils.isEmpty(orderItem.getProductExternalId())? orderItem.B2B_ProductID__c(): orderItem.getProductExternalId());
        holder.quantity.setText(orderItem.getQuantity() + " " + ((orderItem.getProductUnitOfMeasure() == null || "null".equalsIgnoreCase(orderItem.getProductUnitOfMeasure())) ? "" : orderItem.getProductUnitOfMeasure()));
        holder.orderAction.setVisibility(itemsCanBeDeleted ? View.VISIBLE : View.GONE);
    }

    @Override
    public int getItemCount() {
        return orderItems.size();
    }

    public void setOrderItems(List<Order_Item__c> orderItems){
        this.orderItems.clear();
        this.orderItems.addAll(orderItems);
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.product_name)
        TextView productName;

        @Bind(R.id.product_code)
        TextView productCode;

        @Bind(R.id.quantity)
        TextView quantity;

        @Bind(R.id.order_action)
        ImageView orderAction;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

}


