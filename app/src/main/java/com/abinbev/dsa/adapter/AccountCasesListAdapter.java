package com.abinbev.dsa.adapter;

import android.view.View;

import com.abinbev.dsa.R;


/**
 * Created by lukaszwalukiewicz on 29.12.2015.
 */
public class AccountCasesListAdapter extends AbstractCasesListAdapter {

    public AccountCasesListAdapter() {
        super();
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.case_list_item_view;
    }

    @Override
    protected ViewHolder createViewHolder(View view) {
        return new ViewHolder(view);
    }
}
