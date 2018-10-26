package com.abinbev.dsa.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Filter;

import com.abinbev.dsa.R;
import com.abinbev.dsa.adapter.ProductsForDistributionAdapterImpl;
import com.abinbev.dsa.model.Account;
import com.abinbev.dsa.model.Product;
import com.abinbev.dsa.ui.presenter.SelectProductForDistributionListPresenter;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnTextChanged;
import butterknife.OnTouch;

public class SelectProductForDistributionListActivity extends AppCompatActivity implements SelectProductForDistributionListPresenter.ViewModel {

    private static final String TAG = "SelectProductFoActivity";

    public static final String EXTRA_ACCOUNT_ID = "account_id";

    public static final int REQUEST_CODE_CREATE_DISTRIBUTION = 10;

    private String accountId;
    private ProductsForDistributionAdapterImpl productForDistributionAdapter;
    private SelectProductForDistributionListPresenter selectProductForDistributionListPresenter;

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.product_list)
    RecyclerView productList;

    @Bind(R.id.searchProduct)
    EditText searchField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_product_for_distribution_list);
        ButterKnife.bind(this);

        Intent intent = getIntent();
        if (intent != null) {
            accountId = intent.getStringExtra(EXTRA_ACCOUNT_ID);
        } else {
            Log.w(TAG, "null intent");
        }

        setupToolbar();
        setupList();
        selectProductForDistributionListPresenter = new SelectProductForDistributionListPresenter(accountId);
    }

    @Override
    protected void onResume() {
        super.onResume();
        selectProductForDistributionListPresenter.setViewModel(this);
        selectProductForDistributionListPresenter.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        selectProductForDistributionListPresenter.stop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_CREATE_DISTRIBUTION) {
            if (resultCode == RESULT_OK) {
                setResult(RESULT_OK);
                finish();
            }
        }
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(R.string.add_to_distribution_list_title);
        getSupportActionBar().setSubtitle(R.string.distribution_product);
    }

    private void setupList() {
        productForDistributionAdapter = new ProductsForDistributionAdapterImpl() {

            @Override
            public void onBindViewHolder(ViewHolderImpl vh, int position) {
                super.onBindViewHolder(vh, position);
                View button = vh.itemView.findViewById(R.id.product_for_distribution_add_button);
                button.setClickable(true);
                button.setOnClickListener(v -> {
                    final Product product = getStored_stored_data().get(position);
                    onProductClicked(product);
                });
            }
        };
        productList.setLayoutManager(new LinearLayoutManager(this));
        productList.setAdapter(productForDistributionAdapter);
    }

    void onProductClicked(Product product) {
        Intent intent = new Intent(this, AddProductToDistributionListActivity.class);
        intent.putExtra(AddProductToDistributionListActivity.ARGS_ACCOUNT_ID, accountId);
        intent.putExtra(AddProductToDistributionListActivity.ARGS_PRODUCT_ID, product.getId());
        startActivityForResult(intent, REQUEST_CODE_CREATE_DISTRIBUTION);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @OnTouch(R.id.searchProduct)
    public boolean onSearchTouched(View v) {
        v.setFocusable(true);
        v.setFocusableInTouchMode(true);
        return false;
    }

    @OnTextChanged(R.id.searchProduct)
    public void onTextChanged(CharSequence text) {
        Filter filter = productForDistributionAdapter.getFilter();
        filter.filter(text);
    }

    @Override
    public void onAccountLoaded(Account account) {
        productForDistributionAdapter.setChannel(account.getChannel());
        Filter filter = productForDistributionAdapter.getFilter();
        filter.filter(searchField.getText().toString());
    }
}
