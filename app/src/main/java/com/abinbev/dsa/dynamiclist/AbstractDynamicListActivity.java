package com.abinbev.dsa.dynamiclist;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.StringRes;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import com.abinbev.dsa.R;
import com.abinbev.dsa.activity.AppBaseDrawerActivity;
import com.abinbev.dsa.utils.AppScheduler;
import com.salesforce.dsa.data.model.SFBaseObject;

import java.util.Arrays;
import java.util.List;

import butterknife.Bind;
import rx.Single;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class AbstractDynamicListActivity extends AppBaseDrawerActivity {

    private static final String TAG = "AbstractDynamicListActi";

    private DynamicListAdapter adapter;

    private String objectName;

    private List<String> fieldNames;

    private DynamicListItemRowBuilder itemRowBuilder;

    private DynamicListFieldValueBinder fieldValueBinder;

    private DynamicListFieldLabelProvider fieldLabelProvider;

    private DynamicListDataProvider dataProvider;

    @LayoutRes
    private int listItemResId = R.layout.list_item_dynamic_list;

    private boolean isInitialSetupCompleted;

    private CompositeSubscription subscriptions = new CompositeSubscription();

    @Bind(R.id.activity_dynamic_list_recycler_view)
    RecyclerView recyclerView;

    @Bind(R.id.title)
    TextView titleTextView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle("");

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
    }

    @Override
    protected void onStart() {
        if (!isInitialSetupCompleted) {
            setupView();
            isInitialSetupCompleted = true;
        }

        // TODO Load next pages
        requestData(0, 100);
        super.onStart();
    }

    @Override
    protected void onStop() {
        subscriptions.clear();
        super.onStop();
    }

    private void setupView() {
        if (adapter == null) {
            adapter = new DynamicListAdapter(fieldNames, listItemResId);
        }

        adapter.setOnItemClickListener(this::onItemClicked);

        if (itemRowBuilder == null) {
            itemRowBuilder = new SimpleItemRowBuilder(R.layout.list_item_dynamic_list_row);
        }
        adapter.setItemRowBuilder(itemRowBuilder);

        if (fieldValueBinder == null) {
            fieldValueBinder = new SimpleValueBinder();
        }
        adapter.setFieldValueBinder(fieldValueBinder);

        if (fieldLabelProvider != null) {
            // TODO allow loading in background.
            adapter.setFieldLabels(
                    fieldLabelProvider.getFieldLabels(this, objectName, fieldNames));
        }

        recyclerView.setAdapter(adapter);
    }

    private void requestData(int pageStart, int pageSize) {
        // Defensive copies.
        final DynamicListDataProvider currentDataProvider = dataProvider;
        final String currentObjectName = objectName;
        final List<String> currentFieldNames = fieldNames;

        if (currentDataProvider == null) {
            throw new IllegalStateException("You should call setDataProvider() in order to load" +
                    " any items.");
        }
        subscriptions.add(Single.fromCallable(
                () -> currentDataProvider.fetchRecords(currentObjectName, currentFieldNames, pageStart, pageSize))
                .subscribeOn(Schedulers.io())
                .observeOn(AppScheduler.main())
                .subscribe(this::setData, error -> Log.e(TAG, "onError: ", error)));
    }

    private void setData(List<SFBaseObject> objects) {
        adapter.setData(objects);
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_dynamic_list;
    }

    @Override
    public void onRefresh() {
        //TODO
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public String getObjectName() {
        return objectName;
    }

    public void setFieldNames(List<String> fieldNames) {
        this.fieldNames = fieldNames;
    }

    public void setFieldNames(String... fieldNames) {
        setFieldNames(Arrays.asList(fieldNames));
    }

    public List<String> getFieldNames() {
        return fieldNames;
    }

    public void setAdapter(DynamicListAdapter adapter) {
        this.adapter = adapter;
    }

    public void setItemRowBuilder(DynamicListItemRowBuilder itemRowBuilder) {
        this.itemRowBuilder = itemRowBuilder;
    }

    public void setFieldValueBinder(DynamicListFieldValueBinder fieldValueBinder) {
        this.fieldValueBinder = fieldValueBinder;
    }

    public void setFieldLabelProvider(DynamicListFieldLabelProvider fieldLabelProvider) {
        this.fieldLabelProvider = fieldLabelProvider;
    }

    public void setDataProvider(DynamicListDataProvider dataProvider) {
        this.dataProvider = dataProvider;
    }

    public DynamicListDataProvider getDataProvider() {
        return dataProvider;
    }

    public void setPageTitle(String text) {
        titleTextView.setText(text);
    }

    public void setPageTitle(@StringRes int stringRes) {
        titleTextView.setText(stringRes);
    }

    protected void onItemClicked(SFBaseObject item) { }
}