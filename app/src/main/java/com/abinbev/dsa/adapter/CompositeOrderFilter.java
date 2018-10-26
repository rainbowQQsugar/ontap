package com.abinbev.dsa.adapter;

import android.text.TextUtils;
import android.widget.Filter;

import com.abinbev.dsa.ABInBevApp;
import com.abinbev.dsa.R;
import com.abinbev.dsa.model.Order_Item__c;
import com.abinbev.dsa.model.Order__c;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CompositeOrderFilter extends Filter {

    private final PedidoListAdapter adapter;
    private final List<Order__c> orders;
    private final List<CompositeFilter> filters;

    private String selectedOrderSource;
    private String selectedOrderStatus;
    private Date selectedOrderStartDate;
    private Date selectedOrderEndDate;
    private String selectedBrand;
    private String selectedSku;

    private static String allValues;

    interface CompositeFilter {
        /**
         * Performs a filtering function on the provided order.
         * @param order - the product to be filtered
         * @return whether or not to include the order in the result set
         */
        boolean filter(Order__c order);
    }

    public void setOrderSource(String orderSource) {
        this.selectedOrderSource = orderSource;
    }

    public void setOrderStatus(String orderStatus) {
        this.selectedOrderStatus = orderStatus;
    }

    public void setOrderStartDate(Date startDate) {
        this.selectedOrderStartDate = startDate;
    }

    public void setOrderEndDate(Date endDate) {
        this.selectedOrderEndDate = endDate;
    }

    public String getSelectedOrderSource() {
        return selectedOrderSource;
    }

    public String getSelectedOrderStatus() {
        return selectedOrderStatus;
    }

    public String getSelectedBrand() {
        return selectedBrand;
    }

    public String getSelectedSku() { return selectedSku; }

    public void setSelectedBrand(String brand) { this.selectedBrand = brand; }

    public void setSelectedSku(String sku) { this.selectedSku = sku; }

    public void clearFilters() {
        this.selectedOrderSource = allValues;
        this.selectedOrderStatus = allValues;
        this.selectedBrand = allValues;
        this.selectedSku = allValues;
    }

    public CompositeOrderFilter(PedidoListAdapter adapter, List<Order__c> orders) {
        super();
        this.adapter = adapter;
        this.orders = new ArrayList<>(orders);
        this.filters = new ArrayList<>();
        this.allValues = ABInBevApp.getAppContext().getString(R.string.all_values);
        initFilters();
    }

    private void initFilters() {
        this.filters.add(new OrderSourceFilter());
        this.filters.add(new OrderStatusFilter());
        this.filters.add(new OrderDateFilter());
        this.filters.add(new OrderBrandFilter());
        this.filters.add(new OrderSkuFilter());

        this.selectedOrderStatus = allValues;
        this.selectedOrderSource = allValues;
        this.selectedBrand = allValues;
        this.selectedSku = allValues;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        List<Order__c> filteredOrders = new ArrayList<>();

        for (Order__c order : orders) {
            boolean keepOrder = true;

            for (int i = 0; i < filters.size() && keepOrder; i++)
                keepOrder = filters.get(i).filter(order);

            if (keepOrder)
                filteredOrders.add(order);

        }

        FilterResults results = new FilterResults();
        results.values = filteredOrders;
        results.count = filteredOrders.size();
        return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        adapter.filterData((List<Order__c>) results.values);
    }

    private class OrderSourceFilter implements CompositeFilter {

        @Override
        public boolean filter(Order__c order) {
            return TextUtils.isEmpty(selectedOrderSource) || order.getSource().equalsIgnoreCase(selectedOrderSource)
                    || allValues.equals(selectedOrderSource);
        }
    }

    private class OrderStatusFilter implements CompositeFilter {

        @Override
        public boolean filter(Order__c order) {
            return allValues.equals(selectedOrderStatus)
                    || order.getStatus().equalsIgnoreCase(selectedOrderStatus);
        }
    }

    private class OrderDateFilter implements CompositeFilter {

        @Override
        public boolean filter(Order__c order) {
            if (selectedOrderEndDate == null && selectedOrderStartDate == null)
                return true;

            if (selectedOrderStartDate != null && selectedOrderEndDate != null) {
            Date start = order.getStartDate();
            Date end = order.getEndDate();

            if (start != null && end != null)
                return isBetween(start, selectedOrderStartDate, selectedOrderEndDate)
                        && isBetween(end, selectedOrderStartDate, selectedOrderEndDate);
            else if (start != null)
                return isBetween(start, selectedOrderStartDate, selectedOrderEndDate);
            else
                return isBetween(end, selectedOrderStartDate, selectedOrderEndDate);
        }

            if (selectedOrderStartDate != null) {
            Date dateToCompare = order.getStartDate() != null ? order.getStartDate() : order.getEndDate();
            return dateToCompare != null && !dateToCompare.before(selectedOrderStartDate);

        } else {
            Date dateToCompare = order.getEndDate() != null ? order.getEndDate() : order.getStartDate();
            return dateToCompare != null && !dateToCompare.after(selectedOrderEndDate);
        }
    }

        private boolean isBetween(Date date, Date start, Date end) {
            if (date == null)
                return false;

            if (date.before(start))
                return false;

            if (date.after(end))
                return false;

            return true;
        }
    }
    private class OrderBrandFilter implements CompositeFilter {

        @Override
        public boolean filter(Order__c order) {
            if (allValues.equals(selectedBrand)) {
                return true;
            }
            List<Order_Item__c> items = Order_Item__c.getAllOrderLineItemsByOrderId(order.getId());
            for (Order_Item__c i : items) {
                if (i.getProduct().getChBrand().equalsIgnoreCase(selectedBrand)){
                    return true;
                }
            }
            return false;
        }
    }

    private class OrderSkuFilter implements CompositeFilter {

        @Override
        public boolean filter(Order__c order) {
            if (allValues.equals(selectedSku)) {
                return true;
            }
            List<Order_Item__c> items = Order_Item__c.getAllOrderLineItemsByOrderId(order.getId());
            for (Order_Item__c i : items) {
                if (i.getProduct().getProductName().equalsIgnoreCase(selectedSku)){
                    return true;
                }
            }
            return false;
        }
    }

}
