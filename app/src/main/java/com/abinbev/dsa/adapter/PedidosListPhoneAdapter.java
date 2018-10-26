package com.abinbev.dsa.adapter;

import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.abinbev.dsa.R;
import com.abinbev.dsa.model.Order__c;
import com.abinbev.dsa.utils.AbInBevConstants;
import com.abinbev.dsa.utils.DateUtils;

import java.text.DecimalFormat;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by usanaga on 3/31/16.
 */
public class PedidosListPhoneAdapter extends BaseAdapter {

    public interface PedidosItemClickHandler {
        void onPedidoClick(String pedidoId);
    }

    List<Order__c> pedidos;
    PedidosItemClickHandler pedidoClickHandler;

    public PedidosListPhoneAdapter(List<Order__c> pedidos, PedidosItemClickHandler pedidoClickHandler) {
        this.pedidos = pedidos;
        this.pedidoClickHandler = pedidoClickHandler;
    }

    @Override
    public int getCount() {
        if (pedidos.size() > 2) {
            return 2;
        }
        return pedidos.size();
    }

    @Override
    public Object getItem(int position) {
        return pedidos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            convertView = inflater.inflate(R.layout.pedido_row_view, parent, false);

            convertView.setTag(new ViewHolder(convertView));
        }

        final Order__c pedido = pedidos.get(position);
        final String pendientePlaceholder = parent.getContext().getString(R.string.pendiente);

        ViewHolder viewHolder = (ViewHolder) convertView.getTag();
        viewHolder.orderNumber.setText(TextUtils.isEmpty(pedido.getName()) ? pendientePlaceholder : pedido.getName() );
        viewHolder.orderType.setText(pedido.getTranslatedRecordTypeName());
        viewHolder.orderStatus.setText(pedido.getTranslatedStatus());

        if (pedido.getStatus().equals(AbInBevConstants.PedidoStatus.STATUS_CANCELLED))
            viewHolder.orderStatus.setTextColor(Color.RED);
        else if (pedido.getStatus().equals(AbInBevConstants.PedidoStatus.STATUS_CLOSED))
            viewHolder.orderStatus.setTextColor(Color.parseColor("#00cc44"/*darker green*/));
        else
            viewHolder.orderStatus.setTextColor(Color.parseColor("#ffd633"/*darker yellow*/));

        viewHolder.orderSource.setText(pedido.getSource());
        String startDate = pedido.getStartDate() == null ? "": DateUtils.DATE_STRING_FORMAT2.format(pedido.getStartDate());
        viewHolder.orderCreationDate.setText(startDate);
        viewHolder.totalAmount.setText(pedido.getTotal().equals(Double.NaN) ? "" : DecimalFormat.getCurrencyInstance().format(pedido.getTotal()));

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pedidoClickHandler.onPedidoClick(pedido.getId());
            }
        });

        return convertView;
    }

    class ViewHolder {

        @Bind(R.id.pedido_number)
        TextView orderNumber;

        @Bind(R.id.pedido_type)
        TextView orderType;

        @Bind(R.id.pedido_status)
        TextView orderStatus;

        @Bind(R.id.pedido_source)
        TextView orderSource;

        @Bind(R.id.pedido_created_date)
        TextView orderCreationDate;

        @Bind(R.id.total)
        TextView totalAmount;

        public ViewHolder(View convertView) {
            ButterKnife.bind(this, convertView);
        }


    }
}
