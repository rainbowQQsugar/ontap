package com.abinbev.dsa.adapter;

import android.text.TextUtils;
import android.widget.Filter;

import com.abinbev.dsa.ABInBevApp;
import com.abinbev.dsa.R;
import com.abinbev.dsa.model.SalesVolume;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Diana Błaszczyk on 28/10/17.
 */

public class CompositeSalesVolumeFilter extends Filter  {

    private final SalesVolumeListAdapter adapter;
    private final List<SalesVolume> salesVolumes;
    private final List<CompositeSalesVolumeFilter.CompositeFilter> filters;

    private String selectedOrderSource;
    private String selectedOrderStatus;
    private Date selectedOrderStartDate;
    private Date selectedOrderEndDate;
    private String selectedBrand;
    private String selectedSku;

    private static String allValues;

    public void setOrderStatus(String orderStatus) {
        this.selectedOrderStatus = orderStatus;
    }

    public void setOrderSource(String orderSource) {
        this.selectedOrderSource = orderSource;
    }

    public String getSelectedOrderSource() {
        return selectedOrderSource;
    }

    public String getSelectedOrderStatus() {
        return selectedOrderStatus;
    }

    interface CompositeFilter {
        /**
         * Performs a filtering function on the provided order.
         * @param sv - the product to be filtered
         * @return whether or not to include the order in the result set
         */
        boolean filter(SalesVolume sv);
    }

    public void setStartDate(Date startDate) {
        this.selectedOrderStartDate = startDate;
    }

    public void setEndDate(Date endDate) {
        this.selectedOrderEndDate = endDate;
    }

    public String getSelectedBrand() {
        return selectedBrand;
    }

    public String getSelectedSku() { return selectedSku; }

    public void setSelectedBrand(String brand) { this.selectedBrand = brand; }

    public void setSelectedSku(String sku) { this.selectedSku = sku; }

    public void clearFilters() {
        this.selectedBrand = allValues;
        this.selectedSku = allValues;
    }

    public CompositeSalesVolumeFilter(SalesVolumeListAdapter adapter, List<SalesVolume> svs) {
        super();
        this.adapter = adapter;
        this.salesVolumes = new ArrayList<>(svs);
        this.filters = new ArrayList<>();
        this.allValues = ABInBevApp.getAppContext().getString(R.string.all_values);

        initFilters();
    }

    private void initFilters() {
        this.filters.add(new CompositeSalesVolumeFilter.SalesVolumeDateFilter());
        this.filters.add(new CompositeSalesVolumeFilter.SalesVolumeBrandFilter());
        this.filters.add(new CompositeSalesVolumeFilter.SalesVolumeSkuFilter());
        this.filters.add(new OrderSourceFilter());
        this.filters.add(new OrderStatusFilter());

        this.selectedBrand = allValues;
        this.selectedSku = allValues;
        this.selectedOrderStatus = allValues;
        this.selectedOrderSource = allValues;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {

        List<SalesVolume> filteredOrders = new ArrayList<>();

        for (SalesVolume sv : salesVolumes) {
            boolean keepSv = true;

            for (int i = 0; i < filters.size() && keepSv; i++)
                keepSv = filters.get(i).filter(sv);

            if (keepSv)
                filteredOrders.add(sv);
        }

        FilterResults results = new FilterResults();
        results.values = filteredOrders;
        results.count = filteredOrders.size();
        return results;

    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        adapter.filterData((List<SalesVolume>) results.values);
    }


    private class SalesVolumeBrandFilter implements CompositeSalesVolumeFilter.CompositeFilter {

        @Override
        public boolean filter(SalesVolume sv) {
            return allValues.equals(selectedBrand)
                    || sv.getProductBrand().equalsIgnoreCase(selectedBrand);
        }
    }

    private class SalesVolumeSkuFilter implements CompositeSalesVolumeFilter.CompositeFilter {

        @Override
        public boolean filter(SalesVolume sv) {
            return allValues.equals(selectedSku)
                    || sv.getProductSKU().equalsIgnoreCase(selectedSku);
        }
    }

    private class SalesVolumeDateFilter implements CompositeSalesVolumeFilter.CompositeFilter {

        @Override
        public boolean filter(SalesVolume sv) {
            if (selectedOrderEndDate == null && selectedOrderStartDate == null)
                return true;

            if (selectedOrderStartDate != null && selectedOrderEndDate != null) {
                Date start = sv.getStartDate();
                Date end = sv.getEndDate();

                if (start != null && end != null)
                    return isBetween(start, selectedOrderStartDate, selectedOrderEndDate)
                            && isBetween(end, selectedOrderStartDate, selectedOrderEndDate);
                else if (start != null)
                    return isBetween(start, selectedOrderStartDate, selectedOrderEndDate);
                else
                    return isBetween(end, selectedOrderStartDate, selectedOrderEndDate);
            }

            if (selectedOrderStartDate != null) {
                Date dateToCompare = sv.getStartDate() != null ? sv.getStartDate() : sv.getEndDate();
                return dateToCompare != null && !dateToCompare.before(selectedOrderStartDate);

            } else {
                Date dateToCompare = sv.getEndDate() != null ? sv.getEndDate() : sv.getStartDate();
                return dateToCompare != null && !dateToCompare.after(selectedOrderEndDate);
            }
        }

        private boolean isBetween(Date date, Date start, Date end) {
            if (date == null)
                return false;

            if (start != null && date.before(start))
                return false;

            if (end != null && date.after(end))
                return false;

            return true;
        }
    }
    private class OrderSourceFilter implements CompositeFilter {

        @Override
        public boolean filter(SalesVolume sv) {
            return TextUtils.isEmpty(selectedOrderSource) || sv.getOrderSource().equalsIgnoreCase(selectedOrderSource)
                    || allValues.equals(selectedOrderSource);
        }
    }

    private class OrderStatusFilter implements CompositeFilter {

        @Override
        public boolean filter(SalesVolume sv) {
            return allValues.equals(selectedOrderStatus)
                    || sv.getOrderStatus().equalsIgnoreCase(selectedOrderStatus);
        }
    }
}
