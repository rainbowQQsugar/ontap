package com.abinbev.dsa.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.Bind;
import butterknife.OnClick;
import butterknife.OnItemSelected;
import butterknife.OnTextChanged;
import butterknife.OnTouch;
import com.abinbev.dsa.R;
import com.abinbev.dsa.adapter.ProductAdapter;
import com.abinbev.dsa.model.Order_Item__c;
import com.abinbev.dsa.ui.presenter.AddProductPresenter;
import com.abinbev.dsa.ui.view.ShoppingCart;
import com.abinbev.dsa.utils.AppPreferenceUtils;
import com.salesforce.androidsyncengine.datamanager.SyncUtils;
import java.util.List;

/**
 * This activity is responsible for adding products (via order items) to an order, both existing and new.
 */
public class AddProductActivity extends AppBaseActivity implements AddProductPresenter.ViewModel, ProductAdapter.OrderItemAddedListener {

    private static final String TAG = AddProductActivity.class.getSimpleName();
    public static final String EXTRA_ACCOUNT_ID = "account_id";
    public static final String EXTRA_ORDER_TYPE_ID = "order_type_id";
    public static final String EXTRA_ORDER_TYPE_NAME = "order_type";
    public static final String EXTRA_ORDER_ID = "order_id";
    public static final String NEW_ORDER = "new_order";
    public static final String EXTRA_NEGOTIATION_ID = "negotiation_id";

    String orderTypeId;
    String orderId;
    String accountId;
    ProductAdapter productListAdapter;
    AddProductPresenter addProductPresenter;

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.product_list)
    RecyclerView productList;

    @Bind(R.id.empty_view)
    TextView emptyView;

    @Bind(R.id.product_filter_container)
    RelativeLayout filterContainer;

    @Bind(R.id.brand_spinner)
    Spinner brandSpinner;

    @Bind(R.id.category_spinner)
    Spinner categorySpinner;

    @Bind(R.id.product_filter)
    ImageView productFilter;

    @Bind(R.id.second_column)
    @Nullable
    TextView secondColumnHeader;

    @Bind(R.id.imageShoppingCart)
    ShoppingCart shoppingCart;

    private boolean syncOnDestroy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        if (intent != null) {
            orderTypeId = intent.getStringExtra(EXTRA_ORDER_TYPE_ID);
            orderId = intent.getStringExtra(EXTRA_ORDER_ID);
            accountId = intent.getStringExtra(EXTRA_ACCOUNT_ID);
        } else {
            Log.w(TAG, "null intent");
        }

        setupToolbar();
        setupList();
        addProductPresenter = new AddProductPresenter(orderTypeId, orderId, accountId);
    }

    @Override
    protected void onResume() {
        super.onResume();
        addProductPresenter.setViewModel(this);
        addProductPresenter.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        addProductPresenter.stop();
    }

    @Override
    protected void onDestroy() {
        if (syncOnDestroy) {
            SyncUtils.TriggerRefresh(this);
            AppPreferenceUtils.putPedidoSyncRequired(this, false);
        }
        super.onDestroy();
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_add_product;
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.pedidos).toUpperCase());

        if (NEW_ORDER.equals(orderId)) {
            getSupportActionBar().setSubtitle(getString(R.string.nueva_venta).toUpperCase());
        } else {
            getSupportActionBar().setSubtitle(getString(R.string.añadir_a_venta).toUpperCase() + " " + orderId);
        }
    }

    private void setupList() {
        productListAdapter = new ProductAdapter(this);
        productList.setLayoutManager(new LinearLayoutManager(this));
        productList.setAdapter(productListAdapter);

//        productList.setEmptyView(emptyView);
    }

    @Override
    public void onOrderItemAdded(Order_Item__c orderItem) {
        addProductPresenter.addOrderItem(orderItem);
    }

    @Override
    public void setAllOrderItems(List<Order_Item__c> orderItems) {
        productListAdapter.setOrderItems(orderItems);
    }

    @Override //TODO: this should be pulled up into the AppBaseActivity
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (AppPreferenceUtils.getPedidoSyncRequired(this)) {
                syncOnDestroy = true;
            }
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.close)
    public void onCloseClicked() {
        filterContainer.setVisibility(View.GONE);
        productFilter.setImageDrawable(getResources().getDrawable(R.drawable.ic_filter_blue));
        clearFilters();
    }

    private void clearFilters() {
        categorySpinner.setSelection(0);
        brandSpinner.setSelection(0);
//        CompositeProductFilter compositeProductFilter = (CompositeProductFilter) productListAdapter.getFilter();
//        if (compositeProductFilter != null) {
//            compositeProductFilter.clearFilters();
//            compositeProductFilter.filter("");
//        }
    }

    @OnClick(R.id.product_filter)
    public void onProductFilterClicked() {
        if (filterContainer.getVisibility() == View.VISIBLE) {
            onCloseClicked();
        } else {
            productFilter.setImageDrawable(getResources().getDrawable(R.drawable.ic_filter_gray));
            filterContainer.setVisibility(View.VISIBLE);
        }
    }

    @OnTouch(R.id.searchProduct)
    public boolean onSearchTouched(View v) {
        v.setFocusable(true);
        v.setFocusableInTouchMode(true);
        return false;
    }

    @OnItemSelected(R.id.brand_spinner)
    public void onBrandSelected(AdapterView<?> parent, View view, int pos, long id) {
        String brand = (String) parent.getItemAtPosition(pos);
//        CompositeProductFilter compositeProductFilter = (CompositeProductFilter) productListAdapter.getFilter();
//        if (compositeProductFilter != null) {
//            compositeProductFilter.setBrand(brand);
//            compositeProductFilter.filter("");
//        }
    }

    @OnItemSelected(R.id.category_spinner)
    public void onCategorySelected(AdapterView<?> parent, View view, int pos, long id) {
        String category = (String) parent.getItemAtPosition(pos);
//        CompositeProductFilter compositeProductFilter = (CompositeProductFilter) productListAdapter.getFilter();
//        if (compositeProductFilter != null) {
//            compositeProductFilter.setCategory(category);
//            compositeProductFilter.filter("");
//        }
    }

    @OnTextChanged(R.id.searchProduct)
    public void onTextChanged(CharSequence text) {
        Filter filter = productListAdapter.getFilter();
        filter.filter(text);
    }

    @Override
    public void productAdded(int totalCount) {
        //all wee need to do here is update the shopping cart;
        shoppingCart.setCount(totalCount);
    }

    @Override
    public void orderCreated(String orderId) {
        this.orderId = orderId;
        if (!NEW_ORDER.equals(orderId) || !TextUtils.isEmpty(orderId)) {
            AppPreferenceUtils.putPedidoSyncRequired(this, true);
            Intent intent = new Intent(this, PedidoDetailActivity.class);
            intent.putExtra(AddProductActivity.EXTRA_ORDER_ID, orderId);
            intent.putExtra(AddProductActivity.EXTRA_ACCOUNT_ID, accountId);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            syncOnDestroy = false;
            finish();
        }
    }

    @OnClick(R.id.imageShoppingCart)
    public void onShoppingCartClick() {
        if (addProductPresenter.canCreate()) {
            addProductPresenter.createOrder(getResources().getString(R.string.order_source_sfdc));
        } else {
            Toast.makeText(this, getResources().getString(R.string.minimum_order_message), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        if (AppPreferenceUtils.getPedidoSyncRequired(this)) {
            syncOnDestroy = true;
        }
        super.onBackPressed();
    }

}
