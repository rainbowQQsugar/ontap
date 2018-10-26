package com.abinbev.dsa.ui.view;

import android.content.Context;
import android.support.v7.app.AppCompatDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.abinbev.dsa.R;
import com.abinbev.dsa.adapter.CheckoutRulesAdapter;
import com.abinbev.dsa.model.checkoutRules.CheckoutRule;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A custom dialog show required tasks which needs to be done before check out
 */
public class CheckoutRulesDialog extends AppCompatDialog {

//    private static final String TAG = "TasksBeforeCheckoutDial";

    private CheckoutRulesAdapter listAdapter;

    @Bind(R.id.recycler_view)
    RecyclerView recyclerView;

    public CheckoutRulesDialog(Context context) {
        super(context, R.style.AppCompatAlertDialogStyle);

        setContentView(R.layout.dialog_tasks_before_checkout);
        ButterKnife.bind(this);

        initRequiredTasksList();
    }

    private void initRequiredTasksList() {
        listAdapter = new CheckoutRulesAdapter();
        listAdapter.setListener(this::dismiss);
        recyclerView.setAdapter(listAdapter);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
    }

    public void setCheckOutRules(List<CheckoutRule> checkOutRules) {
        listAdapter.setData(checkOutRules);
        listAdapter.notifyDataSetChanged();
    }

    @OnClick(R.id.confirm_button)
    public void confirmButtonClicked() {
        dismiss();
    }
}
