package com.abinbev.dsa.adapter;

import android.content.res.Configuration;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import butterknife.Bind;
import butterknife.ButterKnife;

import com.abinbev.dsa.ABInBevApp;
import com.abinbev.dsa.R;
import com.abinbev.dsa.model.Material_Give__c;
import com.abinbev.dsa.model.Order__c;
import com.abinbev.dsa.ui.view.EditOrderView;
import com.abinbev.dsa.utils.AbInBevConstants;
import com.salesforce.androidsyncengine.data.model.PicklistValue;

import java.util.Locale;

/**
 * @author Jason Harris (jason@akta.com)
 */
public class EditOrderAdapter extends RecyclerView.Adapter<EditOrderAdapter.MaterialGiveViewHolder> {

    public interface Listener {
        void onProductRemoved(int index, Material_Give__c product);

        void onProductUpdated();
    }
    private Order__c order;
    private Listener listener;

    public void setOrder(Order__c order) {
        this.order = order;
        notifyDataSetChanged();
    }

    @Override
    public MaterialGiveViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        EditOrderView itemView = new EditOrderView(parent.getContext());
        itemView.setLayoutParams(params);
        return new MaterialGiveViewHolder(itemView, new MyCustomEditTextListener());
    }

    @Override
    public int getItemCount() {
        return order == null || order.getProducts() == null ? 0  : order.getProducts().size();
    }

    private boolean isSFDCOrder(String source) {
        Configuration chinaConf = new Configuration(ABInBevApp.getAppContext().getResources().getConfiguration());
        chinaConf.setLocale(new Locale("zh"));

        Configuration enConf = new Configuration(ABInBevApp.getAppContext().getResources().getConfiguration());
        enConf.setLocale(new Locale("en"));

        return source.equals(ABInBevApp.getAppContext().createConfigurationContext(enConf).getResources().getString(R.string.order_source_sfdc))
                || source.equals(ABInBevApp.getAppContext().createConfigurationContext(chinaConf).getResources().getString(R.string.order_source_sfdc))
                || source.equals(ABInBevApp.getAppContext().createConfigurationContext(chinaConf).getResources().getString(R.string.order_source_sfdc_no_spaces));

    }

    @Override
    public void onBindViewHolder(final MaterialGiveViewHolder holder, final int index) {
        final Material_Give__c product = order.getProducts().get(index);
        EditOrderView editOrderView = (EditOrderView) holder.itemView;
        holder.myCustomEditTextListener.updatePosition(index);
        holder.productCount.removeTextChangedListener(holder.myCustomEditTextListener);
        editOrderView.setOrder(order, index);
        holder.productCount.addTextChangedListener(holder.myCustomEditTextListener);
        String source = order.getSource();
        boolean open = AbInBevConstants.PedidoStatus.STATUS_OPEN.equals(order.getStatus()) && (source.isEmpty() || isSFDCOrder(source));
        holder.actionOrder.setVisibility(open ? View.VISIBLE : View.INVISIBLE);
        holder.productCount.setEnabled(open);
        holder.spinnerQuantity.setEnabled(open);
        holder.spinnerReason.setEnabled(open);
        holder.actionOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (order.getProducts().size() == 1) {
                    Toast.makeText(holder.itemView.getContext(), R.string.minimum_order_message, Toast.LENGTH_SHORT).show();
                } else {
                    int position = order.getProducts().indexOf(product);
                    if (position > -1) {
                        order.removeProduct(position);
                        notifyItemRemoved(position);
                        if (listener != null) {
                            listener.onProductRemoved(position, product);
                        }
                    }
                }
            }
        });

        holder.spinnerReason.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                PicklistValue item = (PicklistValue) parent.getAdapter()
                        .getItem(position);
                order.setReason(index, item.getValue());
                if (listener != null) {
                    listener.onProductUpdated();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        holder.spinnerQuantity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                String item = (String) parent.getAdapter()
                        .getItem(position);
                order.setUnitOfMeasure(index, item);
                if (listener != null) {
                    listener.onProductUpdated();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public class MaterialGiveViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.spinnerReason)
        Spinner spinnerReason;

        @Bind(R.id.product_count)
        EditText productCount;

        @Bind(R.id.spinnerQuantity)
        Spinner spinnerQuantity;

        @Bind(R.id.order_action)
        ImageView actionOrder;


        public MyCustomEditTextListener myCustomEditTextListener;

        public MaterialGiveViewHolder(View itemView, MyCustomEditTextListener myCustomEditTextListener) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            this.myCustomEditTextListener = myCustomEditTextListener;
            this.productCount.addTextChangedListener(myCustomEditTextListener);
        }

    }

    // we make TextWatcher to be aware of the position it currently works with
    // this way, once a new item is attached in onBindViewHolder, it will
    // update current position MyCustomEditTextListener, reference to which is kept by ViewHolder
    private class MyCustomEditTextListener implements TextWatcher {
        private int position;

        public void updatePosition(int position) {
            this.position = position;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            int value = TextUtils.isEmpty(s) ? 0 : Integer.parseInt(s.toString());
            order.setQuantity(position, value);
            if (listener != null) {
                listener.onProductUpdated();
            }
        }
    }
}
