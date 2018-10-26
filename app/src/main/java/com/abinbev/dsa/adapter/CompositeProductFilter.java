package com.abinbev.dsa.adapter;

import android.widget.Filter;

import com.abinbev.dsa.ABInBevApp;
import com.abinbev.dsa.R;
import com.abinbev.dsa.model.Material_Give__c;

import java.util.ArrayList;
import java.util.List;

public class CompositeProductFilter extends Filter {

    private final ProductListAdapter2 adapter;
    private final List<MaterialGiveAdapter.LineItem> lineItems;
    private final List<CompositeFilter> filters;

    private String selectedBrand;
    private String selectedCategory;
    private String searchText;

    private static String allValues;

    interface CompositeFilter {
        /**
         * Performs a filtering function on the provided product.
         * @param product - the product to be filtered
         * @return whether or not to include the product in the result set
         */
        boolean filter(Material_Give__c product);
    }

    public void setBrand(String brand) {
        this.selectedBrand = brand;
    }

    public void setCategory(String category) {
        this.selectedCategory = category;
    }

    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }

    public void clearFilters() {
        this.selectedBrand = allValues;
        this.selectedCategory = allValues;
    }

    public CompositeProductFilter(ProductListAdapter2 adapter, List<MaterialGiveAdapter.LineItem> lineItems) {
        super();
        this.adapter = adapter;
        this.lineItems = new ArrayList<>(lineItems);
        this.filters = new ArrayList<>();
        this.allValues = ABInBevApp.getAppContext().getString(R.string.all_values);

        initFilters();
    }

    public void removeProduct(String productId) {
        MaterialGiveAdapter.LineItem lineItemToBeRemoved = null;
        for (MaterialGiveAdapter.LineItem lineItem : lineItems) {
            if (productId.equals(lineItem.product.getId())) {
                lineItemToBeRemoved = lineItem;
                break;
            }
        }
        lineItems.remove(lineItemToBeRemoved);
    }

    private void initFilters() {
        this.filters.add(new BrandFilter());
        this.filters.add(new CategoryFilter());
        this.filters.add(new SearchFilter());

        this.selectedBrand = allValues;
        this.selectedCategory = allValues;
        this.searchText = "";
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        List<MaterialGiveAdapter.LineItem> filteredLineItems = new ArrayList<>();

        for (MaterialGiveAdapter.LineItem lineItem : lineItems) {
            boolean addProduct = false;

            //check filters
            for (CompositeFilter filter : filters) {
                if (filter.filter(lineItem.product)) {
                    addProduct = true;
                } else { //punt out and exclude
                    addProduct = false;
                    break;
                }
            }

            //if flag is true here, add product to result list
            if (addProduct) {
                filteredLineItems.add(lineItem);
            }
        }

        FilterResults results = new FilterResults();
        results.values = filteredLineItems;
        results.count = filteredLineItems.size();

        return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        adapter.updateData((List<MaterialGiveAdapter.LineItem>) results.values);
    }


    private class BrandFilter implements CompositeFilter {

        @Override
        public boolean filter(Material_Give__c product) {
            return allValues.equals(selectedBrand)
                    || product.getBrand().equals(selectedBrand);
        }
    }

    private class CategoryFilter implements CompositeFilter {

        @Override
        public boolean filter(Material_Give__c product) {
            return allValues.equals(selectedCategory)
                    || product.getCategory().equals(selectedCategory);
        }
    }

    private class SearchFilter implements CompositeFilter {

        @Override
        public boolean filter(Material_Give__c product) {
            return product.getName().toUpperCase().contains(searchText.toUpperCase())
                    || product.getCode().toUpperCase().contains(searchText.toUpperCase());
        }
    }

}
