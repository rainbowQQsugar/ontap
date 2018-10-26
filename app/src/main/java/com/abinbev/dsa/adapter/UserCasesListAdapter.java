package com.abinbev.dsa.adapter;

import android.view.View;
import android.widget.TextView;

import com.abinbev.dsa.R;
import com.abinbev.dsa.model.Account;
import com.abinbev.dsa.model.Case;

import java.util.Collections;
import java.util.Comparator;

import butterknife.Bind;


/**
 * Created by lukaszwalukiewicz on 29.12.2015.
 */
public class UserCasesListAdapter extends AbstractCasesListAdapter {

    public UserCasesListAdapter() {
        super();
    }

    @Override
    protected void bind(ViewHolder vh, Case caso) {
        super.bind(vh, caso);

        UserViewHolder userVH = (UserViewHolder) vh;
        Account account = caso.getAccount();
        userVH.account.setText(account == null ? null : account.getName());
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.user_case_list_item_view;
    }

    @Override
    protected ViewHolder createViewHolder(View view) {
        return new UserViewHolder(view);
    }

    public void sortByAccount(final boolean ascending) {
        Collections.sort(cases, new Comparator<Case>() {
            @Override
            public int compare(Case lhs, Case rhs) {
                Account leftAccount = lhs.getAccount();
                Account rightAccount = rhs.getAccount();

                String leftAccountName = leftAccount == null ? null : leftAccount.getName();
                String rightAccountName = rightAccount == null ? null : rightAccount.getName();

                if (leftAccountName == null) {
                    leftAccountName = "";
                }
                if (rightAccountName == null) {
                    rightAccountName = "";
                }

                if (ascending) {
                    return leftAccountName.compareTo(rightAccountName);
                } else {
                    return rightAccountName.compareTo(leftAccountName);
                }
            }
        });
        this.notifyDataSetChanged();
    }

    public static class UserViewHolder extends ViewHolder {
        @Bind(R.id.case_account)
        public TextView account;

        public UserViewHolder(View convertView) {
            super(convertView);
        }
    }
}
