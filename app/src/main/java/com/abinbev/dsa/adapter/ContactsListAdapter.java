package com.abinbev.dsa.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.abinbev.dsa.R;
import com.abinbev.dsa.model.Contact;
import com.abinbev.dsa.utils.ContentUtils;
import com.abinbev.dsa.utils.DateUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by lukaszwalukiewicz on 18.01.2016.
 */
public class ContactsListAdapter extends BaseAdapter {
    private final List<Contact> contacts;

    public ContactsListAdapter() {
        super();
        contacts = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return contacts.size();
    }

    @Override
    public Object getItem(int position) {
        return contacts.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            convertView = inflater.inflate(R.layout.contact_list_item_view, parent, false);
            convertView.setTag(new ViewHolder(convertView));
        }

        final Contact contact = contacts.get(position);
        ViewHolder viewHolder = (ViewHolder) convertView.getTag();
        if (DateUtils.isBirthdayToday(contact.getBirthdate())) {
            viewHolder.contactName.setText(contact.getName());
            viewHolder.contactBirthdateInfo.setVisibility(View.VISIBLE);
        } else {
            viewHolder.contactName.setText(contact.getName());
            viewHolder.contactBirthdateInfo.setVisibility(View.GONE);
        }
        viewHolder.contactPhone.setText(contact.getPhone());
        viewHolder.contactFunction.setText(contact.getFunction());
        viewHolder.contactBirthdate.setText(DateUtils.formatDateStringShort(contact.getBirthdate()));
        return convertView;
    }

    public void setData(List<Contact> contacts) {
        this.contacts.clear();
        this.contacts.addAll(contacts);
        this.notifyDataSetChanged();
    }

    public void sortByName(final boolean ascending) {
        Collections.sort(contacts, new Comparator<Contact>() {
            @Override
            public int compare(Contact lhs, Contact rhs) {
                if (ascending) {
                    return lhs.getName().compareTo(rhs.getName());
                } else {
                    return rhs.getName().compareTo(lhs.getName());
                }
            }
        });
        this.notifyDataSetChanged();
    }

    public void sortByPhone(final boolean ascending) {
        Collections.sort(contacts, new Comparator<Contact>() {
            @Override
            public int compare(Contact lhs, Contact rhs) {
                if (ascending) {
                    return lhs.getPhone().compareTo(rhs.getPhone());
                } else {
                    return rhs.getPhone().compareTo(lhs.getPhone());
                }
            }
        });
        this.notifyDataSetChanged();
    }

    public void sortByFunction(final boolean ascending) {
        Collections.sort(contacts, new Comparator<Contact>() {
            @Override
            public int compare(Contact lhs, Contact rhs) {
                if (ascending) {
                    return lhs.getFunction().compareTo(rhs.getFunction());
                } else {
                    return rhs.getFunction().compareTo(lhs.getFunction());
                }
            }
        });
        this.notifyDataSetChanged();
    }

    public void sortByBirthDate(final boolean ascending) {
        Collections.sort(contacts, new Comparator<Contact>() {
            @Override
            public int compare(Contact lhs, Contact rhs) {
                if (ascending) {
                    if (!ContentUtils.isStringValid(lhs.getBirthdate()) && !ContentUtils.isStringValid(rhs.getBirthdate())) {
                        return 0;
                    }
                    else if (!ContentUtils.isStringValid(lhs.getBirthdate())) {
                        return -1;
                    } else if (!ContentUtils.isStringValid(rhs.getBirthdate())) {
                        return 1;
                    }

                    return DateUtils.dateFromString(lhs.getBirthdate()).compareTo(DateUtils.dateFromString(rhs.getBirthdate()));
                } else {
                    if (!ContentUtils.isStringValid(rhs.getBirthdate()) && !ContentUtils.isStringValid(lhs.getBirthdate())) {
                        return 0;
                    }
                    else if (!ContentUtils.isStringValid(rhs.getBirthdate())) {
                        return -1;
                    } else if (!ContentUtils.isStringValid(lhs.getBirthdate())) {
                        return 1;
                    }

                    return DateUtils.dateFromString(rhs.getBirthdate()).compareTo(DateUtils.dateFromString(lhs.getBirthdate()));
                }
            }
        });
        this.notifyDataSetChanged();
    }

    class ViewHolder {

        @Bind(R.id.contact_name)
        TextView contactName;

        @Bind(R.id.contact_Birthdate_info)
        TextView contactBirthdateInfo;

        @Bind(R.id.contact_phone)
        TextView contactPhone;

        @Bind(R.id.contact_function)
        TextView contactFunction;

        @Bind(R.id.contact_birthdate)
        TextView contactBirthdate;


        public ViewHolder(View convertView) {
            ButterKnife.bind(this, convertView);
        }
    }
}
