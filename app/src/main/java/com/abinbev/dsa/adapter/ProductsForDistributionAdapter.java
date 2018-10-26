package com.abinbev.dsa.adapter;

import android.support.v7.widget.RecyclerView;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.abinbev.dsa.R;
import com.abinbev.dsa.model.Product;

import java.util.List;

import io.mewa.adapterodactil.annotations.Adapt;
import io.mewa.adapterodactil.annotations.Data;
import io.mewa.adapterodactil.annotations.OverridePlugin;
import io.mewa.adapterodactil.annotations.Row;
import io.mewa.adapterodactil.annotations.ViewType;

@Adapt(layout = R.layout.product_for_distribution_item_view, viewGroup = R.id.product_for_distribution_root_view, type = Product.class)
public abstract class ProductsForDistributionAdapter<T extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<T> implements Filterable {

    public static final int PRODUCT_FOR_DISTRIBUTION_TYPE = 0;

    private SearchFilter searchFilter = new SearchFilter(this);

    private String channel;

    public void setChannel(String channel) {
        this.channel = channel;
    }

    @Data
    public abstract void setData(List<Product> data);

    @Override
    public Filter getFilter() {
        return searchFilter;
    }

    @ViewType(PRODUCT_FOR_DISTRIBUTION_TYPE)
    public static class ProductsForDistributionViewAdapter {

        @Row(num = 0, dataId = R.id.product_for_distribution_product_name)
        public static String productName(TextView view, Product product) {
            return product.getProductName();
        }

        @OverridePlugin
        @Row(num = 1, dataId = R.id.product_for_distribution_add_button)
        public static String addButton(ImageView view, Product product) {
            return "";
        }
    }

        /**
         * A filter capable of searching across {@link Product}s by product name.
         */
        public class SearchFilter extends Filter {

            ProductsForDistributionAdapter productAdapter;

            SearchFilter(ProductsForDistributionAdapter productsForDistributionAdapter) {
                this.productAdapter = productsForDistributionAdapter;
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String searchQuery = constraint == null ? null : String.valueOf(constraint);
                List<Product> filteredProductItems = Product.filter(searchQuery, channel);

                FilterResults filterResults = new FilterResults();
                filterResults.count = filteredProductItems.size();
                filterResults.values = filteredProductItems;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                List<Product> products = (List<Product>) results.values;
                productAdapter.setData(products);
                productAdapter.notifyDataSetChanged();
            }
        }
    }
