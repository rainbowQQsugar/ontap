package com.abinbev.dsa.ui.view.negotiation;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.abinbev.dsa.R;
import com.abinbev.dsa.adapter.PackagesAdapter;
import com.abinbev.dsa.model.Account;
import com.abinbev.dsa.model.Paquetes_por_segmento__c;
import com.abinbev.dsa.ui.presenter.PackagesPresenter;
import com.abinbev.dsa.ui.view.PagerHeaderView;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by wandersonblough on 12/10/15.
 */
public class PackagesView extends LinearLayout implements PackagesPresenter.ViewModel {

    @Bind(R.id.packages_pager)
    PackagesPager packagesPager;

    @Bind(R.id.page_header_view)
    PagerHeaderView pagerHeaderView;

    private PackagesPresenter packagesPresenter;
    private String customerName;
    private String accountId;

    public PackagesView(Context context) {
        this(context, null);
    }

    public PackagesView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PackagesView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflate(context, R.layout.negotiations_packages_view, this);
        ButterKnife.bind(this);
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
        customerName = Account.getById(accountId).getName();
        packagesPresenter = new PackagesPresenter(accountId);
        packagesPresenter.setViewModel(this);
        packagesPresenter.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (packagesPresenter != null) {
            packagesPresenter.stop();
        }
    }

    @Override
    public void setPackages(List<Paquetes_por_segmento__c> packages) {
        PackagesAdapter packagesAdapter = new PackagesAdapter(packages, customerName);
        packagesPager.setAdapter(packagesAdapter);
        packagesPager.setOffscreenPageLimit(packages.size());
        pagerHeaderView.initHeader(packagesPager, packages.size());
    }
}
