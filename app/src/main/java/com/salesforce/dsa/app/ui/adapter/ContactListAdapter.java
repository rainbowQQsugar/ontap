package com.salesforce.dsa.app.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.salesforce.dsa.app.R;
import com.salesforce.dsa.data.model.Contact;

import java.util.LinkedList;
import java.util.List;

public class ContactListAdapter extends BaseAdapter {

    private static class ViewHolder {
        TextView name;
        TextView email;
        TextView account;
    }

    private List<Contact> listData;

    public ContactListAdapter(List<Contact> listData) {
        super();
        this.listData = new LinkedList<>(listData);
    }

    @Override
    public int getCount() {
        if (listData == null) {
            return 0;
        } else {
            return listData.size();
        }
    }

    @Override
    public Object getItem(int arg0) {

        if (listData == null)
            return null;
        else
            return listData.get(arg0);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

        ViewHolder holder = null;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.checkin_select_contact_list_item, parent, false);
            holder = new ViewHolder();
            holder.name = (TextView) convertView.findViewById(R.id.contactName);
            holder.email = (TextView) convertView.findViewById(R.id.contactEmail);
            holder.account = (TextView) convertView.findViewById(R.id.contactAccount);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Contact contact = (Contact) getItem(position);

        String name = contact.getName();
        String email = contact.getEmail();
        email = String.format("<%s>", email);
        String account = contact.getAccountName();

        holder.name.setText(name != null ? name : "");
        holder.email.setText(email != null ? email : "");
        holder.account.setText(account != null ? account : "");

        return convertView;
    }

    public void replaceItems(List<Contact> newItems) {
        listData.clear();
        listData.addAll(newItems);
        notifyDataSetChanged();
    }

}
