package com.abinbev.dsa.ui.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.abinbev.dsa.R;
import com.abinbev.dsa.activity.AccountDetailsActivity;
import com.abinbev.dsa.activity.AccountOverviewActivity;
import com.abinbev.dsa.activity.AddProductActivity;
import com.abinbev.dsa.activity.B2BOrderWebViewActivity;
import com.abinbev.dsa.activity.PedidoListActivity;
import com.abinbev.dsa.adapter.PedidosListPhoneAdapter;
import com.abinbev.dsa.model.Account;
import com.abinbev.dsa.model.Auth_Keys__c;
import com.abinbev.dsa.model.Order__c;
import com.abinbev.dsa.model.RecordType;
import com.abinbev.dsa.ui.customviews.ExpandedListView;
import com.abinbev.dsa.ui.presenter.B2BOrderPresenter;
import com.abinbev.dsa.ui.presenter.PedidoPresenter;
import com.abinbev.dsa.utils.AbInBevConstants;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PedidosView extends RelativeLayout implements PedidoPresenter.ViewModel, RefreshListener, PedidosListPhoneAdapter.PedidosItemClickHandler, B2BOrderPresenter.ViewModel {

    private static final String SHOW_ORDERS_SINCE = "showOrdersSince";

    @Nullable
    @Bind(R.id.pedidos_list)
    ExpandedListView pedidosListView;

    @Bind(R.id.no_pedidos)
    TextView emptyView;

    private PedidoPresenter pedidoPresenter;
    private Account account;

    public PedidosView(Context context) {
        this(context, null);
    }

    public PedidosView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PedidosView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setup(context);
    }

    private void setup(Context context) {
        inflate(context, R.layout.merge_pedidos_view, this);
        ButterKnife.bind(this);
    }

    public void setAccountId(String accountId) {
        if (pedidoPresenter == null) {
            pedidoPresenter = new PedidoPresenter(accountId);
        }
        pedidoPresenter.setViewModel(this);
        pedidoPresenter.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (pedidoPresenter != null) pedidoPresenter.stop();
        if (handler != null) handler.removeCallbacksAndMessages(null);
    }

    @Override
    public void setData(List<Order__c> pedidos) {
        if (pedidos.size() == 0) {
            emptyView.setVisibility(VISIBLE);
            pedidosListView.setVisibility(GONE);
        } else {
            emptyView.setVisibility(GONE);
            pedidosListView.setExpanded(true);
            pedidosListView.setVisibility(VISIBLE);
            PedidosListPhoneAdapter adapter = new PedidosListPhoneAdapter(pedidos, this);
            pedidosListView.setAdapter(adapter);
        }
    }

    @OnClick(R.id.view_all)
    @SuppressWarnings("unused")
    public void onViewAllClick() {
        //need to ensure we have an account id
        if (getContext() instanceof AccountOverviewActivity) {
            String accountId = ((AccountOverviewActivity) getContext()).accountId;
            Intent intent = new Intent(getContext(), PedidoListActivity.class);
            intent.putExtra(AccountDetailsActivity.ACCOUNT_ID_EXTRA, accountId);
            intent.putExtra(SHOW_ORDERS_SINCE, pedidoPresenter.getShowOrdersSince().getTime());
            getContext().startActivity(intent);
        }
    }

    @OnClick(R.id.pedido_label_add)
    @SuppressWarnings("unused")
    public void onAddOrderClick() {
        account = Account.getAccountForId(((AccountOverviewActivity) getContext()).accountId);
        if (account == null) return;
        String b2BRegistered = account.isB2BRegistered();
        if (!"true".equalsIgnoreCase(b2BRegistered)) {
            String defaultRecordType = RecordType.getDefaultRecordTypeId(getContext(), AbInBevConstants.AbInBevObjects.PEDIDO);
            pedidoPresenter.getNewOrderRecordType(defaultRecordType);
        } else {
            B2BOrderPresenter b2BOrderPresenter = new B2BOrderPresenter();
            b2BOrderPresenter.start();
            b2BOrderPresenter.setViewModel(this);
        }
    }

    @Override
    public void onRefresh() {
        if (pedidoPresenter != null) {
            pedidoPresenter.start();
        }
    }

    @Override
    public void goToCreateNewOrder(RecordType recordType) {
        if (getContext() instanceof AccountOverviewActivity) {
            String accountId = ((AccountOverviewActivity) getContext()).accountId;
            Intent intent = new Intent(getContext(), AddProductActivity.class);
            intent.putExtra(AddProductActivity.EXTRA_ORDER_TYPE_NAME, recordType.getName());
            intent.putExtra(AddProductActivity.EXTRA_ORDER_TYPE_ID, recordType.getId());
            intent.putExtra(AddProductActivity.EXTRA_ORDER_ID, AddProductActivity.NEW_ORDER);
            intent.putExtra(AddProductActivity.EXTRA_ACCOUNT_ID, accountId);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            getContext().startActivity(intent);
        }
    }


    @Override
    public void onPedidoClick(String pedidoId) {
        String accountId = ((AccountOverviewActivity) getContext()).accountId;
        pedidoPresenter.goToDetailView(getContext(), pedidoId, accountId);
    }

    @Override
    public void getAccountResult(B2BOrderPresenter.B2BParams params) {
        handler.obtainMessage(0, params).sendToTarget();
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            B2BOrderPresenter.B2BParams params = (B2BOrderPresenter.B2BParams) msg.obj;
            if (!params.rsp.equals("succ")) {
                Toast.makeText(getContext(), getContext().getString(R.string.net_error), Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(params.data.access_token)) {
                Toast.makeText(getContext(), params.data.message, Toast.LENGTH_SHORT).show();
                return;
            }
            new AlertDialog.Builder(getContext())
                    .setMessage(getContext().getString(R.string.skip_tips))
                    .setPositiveButton(getContext().getString(R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(getContext(), B2BOrderWebViewActivity.class);
                            //params.data.access_token
                            intent.putExtra(B2BOrderWebViewActivity.URL, Auth_Keys__c.getB2BHtmlUrl(params.data.access_token, account.getB2BCode()));
                            getContext().startActivity(intent);
                        }
                    }).show();
        }
    };
}
