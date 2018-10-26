package com.abinbev.dsa.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.abinbev.dsa.R;
import com.abinbev.dsa.model.Account;
import com.abinbev.dsa.model.User;
import java.util.ArrayList;
import java.util.List;

public class AccountListAdapter extends BaseAdapter {


    public interface AccountClickHandler {
        void onAccountClick(String accountId);
    }

    private final List<Account> accounts;
    private final AccountClickHandler accountClickHandler;

    public AccountListAdapter(AccountClickHandler accountClickHandler) {
        super();
        accounts = new ArrayList<>();
        this.accountClickHandler = accountClickHandler;
    }

    @Override
    public int getCount() {
        return accounts.size();
    }

    @Override
    public Object getItem(int position) {
        return accounts.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            convertView = inflater.inflate(R.layout.account_list_item, parent, false);

            convertView.setTag(new ViewHolder(convertView));
        }

        final Account account = accounts.get(position);

        ViewHolder viewHolder = (ViewHolder) convertView.getTag();
        viewHolder.name.setText(account.getName());
        viewHolder.address.setText(getAddress(account));
        viewHolder.status.setText(account.getTranslatedNegotiationStatus());

        String userId = account.getOwnerId();
        if (userId != null) {
            User owner = User.getUserByUserId(userId);
            if (owner != null)
                viewHolder.owner.setText(owner.getName());
        }

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                accountClickHandler.onAccountClick(account.getId());
            }
        });

        return convertView;
    }

    private String getAddress(Account account) {
        String street = account.getStreet();
        String streetNumber = account.getStreetNumber();
        String neighborhood = account.getNeighborhood();
        String province = account.getProvince();

        return joinNonEmpty(", ", street, streetNumber, neighborhood, province);
    }

    private static String joinNonEmpty(String delimiter, String... items) {
        if (items == null || items.length < 1) return "";

        StringBuilder sb = new StringBuilder();
        for (String item : items) {
            if (!TextUtils.isEmpty(item)) {
                if (sb.length() > 0) {
                    sb.append(delimiter);
                }
                sb.append(item);
            }
        }

        return sb.toString();
    }

    public void setData(List<Account> acccounts) {
        this.accounts.addAll(acccounts);
        this.notifyDataSetChanged();
    }

    public void clearData() {
        this.accounts.clear();
        this.notifyDataSetChanged();
    }

    class ViewHolder {

        @Bind(R.id.txt_name)
        TextView name;

        @Bind(R.id.txt_address)
        TextView address;

        @Bind(R.id.txt_status)
        TextView status;

        @Bind(R.id.txt_owner)
        TextView owner;

        public ViewHolder(View convertView) {
            ButterKnife.bind(this, convertView);
        }

    }
}
