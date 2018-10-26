package com.abinbev.dsa.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.abinbev.dsa.R;
import com.abinbev.dsa.model.Account;
import com.abinbev.dsa.model.Order__c;
import com.abinbev.dsa.utils.AbInBevConstants;
import com.abinbev.dsa.utils.AppScheduler;
import com.abinbev.dsa.utils.DateUtils;
import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;
import com.salesforce.androidsyncengine.syncmanifest.Order;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

public class PedidoListAdapter extends RecyclerView.Adapter<PedidoListAdapter.DataObjectHolder> implements Filterable {

    private String TAG = getClass().getSimpleName();

    public interface OrderClickHandler {
        void onOrderClick(String orderId, String accountId);
    }

    public PedidoListAdapter(Context context, boolean isShowPocName) {
        super();
        orders = new ArrayList<>();
        this.context = context;
        this.isShowPocName = isShowPocName;
    }

    private boolean isShowPocName;
    List<Order__c> orders;
    OrderClickHandler orderClickHandler;
    CompositeOrderFilter compositeOrderFilter;
    Context context;

    public PedidoListAdapter(Context context) {
        super();
        orders = new ArrayList<>();
        this.context = context;
    }


    public static class DataObjectHolder extends RecyclerView.ViewHolder {
        @Nullable
        @Bind(R.id.card_view)
        CardView cardView;

        @Bind(R.id.pedido_number)
        TextView orderNumber;

        @Bind(R.id.pedido_type)
        TextView orderType;

        @Bind(R.id.pedido_source)
        TextView orderSource;

        @Bind(R.id.pedido_end_date)
        TextView orderEndDate;

        @Bind(R.id.pedido_begin_date)
        TextView orderBeginDate;

        @Bind(R.id.pedido_status)
        TextView orderStatus;

        @Bind(R.id.pedido_created_date)
        TextView orderCreationDate;

        @Bind(R.id.total)
        TextView total;

        @Bind(R.id.expand_layout)
        LinearLayout expandLayout;

        @Bind(R.id.table_layout_details)
        TableLayout detailsLayout;

        @Bind(R.id.hide_expand_label)
        TextView hideExpandTV;

        @Bind(R.id.hide_expand_img)
        ImageView hideExpandImg;

        @Bind(R.id.tr_poc_name)
        TableRow tr_poc_name;

        @Bind(R.id.poc_name)
        TextView pocName;

        public DataObjectHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        private void hideDetails(Context context) {
            hideExpandTV.setText(context.getResources().getString(R.string.show_more_details));
            hideExpandImg.setImageResource(R.drawable.ic_expand_more_black);
            detailsLayout.setVisibility(View.GONE);
        }

        public void showDetails(Context context) {
            hideExpandTV.setText(context.getResources().getString(R.string.hide_more_details));
            hideExpandImg.setImageResource(R.drawable.ic_expand_less_black);
            detailsLayout.setVisibility(View.VISIBLE);
        }
    }

    public void setPromotionClickHandler(OrderClickHandler promotionClickHandler) {
        this.orderClickHandler = promotionClickHandler;
    }

    @Override
    public PedidoListAdapter.DataObjectHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.orders_list_entry_view, parent, false);

        return new PedidoListAdapter.DataObjectHolder(view);
    }

    @Override
    public void onBindViewHolder(PedidoListAdapter.DataObjectHolder holder, int position) {
        Order__c order = orders.get(position);

        holder.hideDetails(context);

        holder.orderNumber.setText(order.getName());
        holder.orderType.setText(order.getTranslatedRecordTypeName());
        holder.orderStatus.setText(order.getTranslatedStatus());

        if (order.getStatus().equals(AbInBevConstants.PedidoStatus.STATUS_CANCELLED))
            holder.orderStatus.setTextColor(Color.RED);
        else if (order.getStatus().equals(AbInBevConstants.PedidoStatus.STATUS_CLOSED))
            holder.orderStatus.setTextColor(Color.parseColor("#00cc44"/*darker green*/));
        else
            holder.orderStatus.setTextColor(Color.parseColor("#ffd633"/*darker yellow*/));


        String createdDate = DateUtils.formatDateTimeShort(order.getCreatedDate());
        holder.orderCreationDate.setText(createdDate);
        holder.orderSource.setText(order.getSource());
        holder.orderEndDate.setText(DateUtils.formatDateTimeShort(order.getStringValueForKey(AbInBevConstants.PedidoFields.END_DATE)));
        holder.orderBeginDate.setText(DateUtils.formatDateTimeShort(order.getStringValueForKey(AbInBevConstants.PedidoFields.START_DATE)));

        holder.total.setText(order.getTotal().equals(Double.NaN) ? "" : DecimalFormat.getCurrencyInstance().format(order.getTotal()));
        holder.detailsLayout.setVisibility(View.GONE);

        holder.tr_poc_name.setVisibility(isShowPocName ? View.VISIBLE : View.GONE);
        fetchAccount(holder.pocName, order);

        holder.expandLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.hideExpandTV.getText().equals(context.getResources().getString(R.string.show_more_details)))
                    holder.showDetails(context);
                else
                    holder.hideDetails(context);
            }
        });

        DataObjectHolder h = (DataObjectHolder) holder;
        h.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (orderClickHandler != null)
                    if (position >= 0) {
                        Order__c o = (Order__c) orders.get(position);
                        orderClickHandler.onOrderClick(o.getId(), o.getAccountId());
                    }
            }

        });
    }

    private CompositeSubscription subscription = new CompositeSubscription();

    public void fetchAccount(TextView view, Order__c order) {
        subscription.add(Observable.create((subscriber) -> {
            subscriber.onNext(new Account(DataManagerFactory.getDataManager().exactQuery(AbInBevConstants.AbInBevObjects.ACCOUNT, "Id", order.getAccount())));
        }).subscribeOn(AppScheduler.background())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(account -> setAccountName((Account) account, view)
                        , error -> Log.e(TAG, "Error fetchAccount, ", error)));
    }

    private void setAccountName(Account account, TextView view) {
        view.setText(account == null ? "" : account.getName());
    }


    @Override
    public int getItemCount() {
        return this.orders.size();
    }


    public void setData(List<Order__c> orders, Date showDateSince) {
        this.orders.clear();
        for (Order__c o : orders) {
            if (o.getStartDate() == null || o.getStartDate().after(showDateSince))
                this.orders.add(o);
        }

        this.notifyDataSetChanged();
        compositeOrderFilter = new CompositeOrderFilter(this, this.orders);

    }

    protected void filterData(List<Order__c> pedidos) {
        if (pedidos != null) {
            this.orders.clear();
            this.orders.addAll(pedidos);
            this.notifyDataSetChanged();
        }
    }

    @Override
    public Filter getFilter() {
        return compositeOrderFilter;
    }

    public void onRecycle() {
        subscription.unsubscribe();
    }

    /*
    public void sortByOrderNumber(final boolean ascending) {
        Collections.sort(orders, new Comparator<Order__c>() {
            @Override
            public int compare(Order__c lhs, Order__c rhs) {
                if (ascending) {
                    if (lhs.getName() == null) return 1;
                    if (rhs.getName() == null) return -1;
                    return lhs.getName().compareTo(rhs.getName());
                } else {
                    if (lhs.getName() == null) return -1;
                    if (rhs.getName() == null) return 1;
                    return rhs.getName().compareTo(lhs.getName());
                }
            }
        });
        this.notifyDataSetChanged();
    }

    public void sortByType(final boolean ascending) {
        Collections.sort(orders, new Comparator<Order__c>() {
            @Override
            public int compare(Order__c lhs, Order__c rhs) {
                if (ascending) {
                    return lhs.getTranslatedRecordTypeName().compareTo(rhs.getTranslatedRecordTypeName());
                } else {
                    return rhs.getTranslatedRecordTypeName().compareTo(lhs.getTranslatedRecordTypeName());
                }
            }
        });
        this.notifyDataSetChanged();
    }

    public void sortByStatus(final boolean ascending) {
        Collections.sort(orders, new Comparator<Order__c>() {
            @Override
            public int compare(Order__c lhs, Order__c rhs) {
                if (ascending) {
                    return lhs.getTranslatedStatus().compareTo(rhs.getTranslatedStatus());
                } else {
                    return rhs.getTranslatedStatus().compareTo(lhs.getTranslatedStatus());
                }
            }
        });
        this.notifyDataSetChanged();
    }

    public void sortByCreateDate(final boolean ascending) {
        Collections.sort(orders, new Comparator<Order__c>() {
            @Override
            public int compare(Order__c lhs, Order__c rhs) {
                if (ascending) {
                        if (!TextUtils.isEmpty(lhs.getCreatedDate()) && !TextUtils.isEmpty(rhs.getCreatedDate())) {
                            return DateUtils.dateFromDateTimeString(lhs.getCreatedDate()).compareTo(DateUtils.dateFromDateTimeString(rhs.getCreatedDate()));
                        } else if (TextUtils.isEmpty(lhs.getCreatedDate()) && TextUtils.isEmpty(rhs.getCreatedDate())){
                            return 0;
                        } else if (TextUtils.isEmpty(lhs.getCreatedDate())) {
                            return -1;
                        } else {
                            return 1;
                        }

                } else {
                    if (!TextUtils.isEmpty(lhs.getCreatedDate()) && !TextUtils.isEmpty(rhs.getCreatedDate())) {
                        return DateUtils.dateFromDateTimeString(rhs.getCreatedDate()).compareTo(DateUtils.dateFromDateTimeString(lhs.getCreatedDate()));
                    } else if (TextUtils.isEmpty(lhs.getCreatedDate()) && TextUtils.isEmpty(rhs.getCreatedDate())){
                        return 0;
                    } else if (TextUtils.isEmpty(rhs.getCreatedDate())) {
                        return -1;
                    } else {
                        return 1;
                    }
                }
            }
        });
        this.notifyDataSetChanged();
    }

    public void sortByTotal(final boolean ascending) {
        Collections.sort(orders, new Comparator<Order__c>() {
            @Override
            public int compare(Order__c lhs, Order__c rhs) {
                if (ascending) {
                    return lhs.getTotal().compareTo(rhs.getTotal());
                } else {
                    return rhs.getTotal().compareTo(lhs.getTotal());
                }
            }
        });
        this.notifyDataSetChanged();
    }
*/
}
