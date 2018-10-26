package com.abinbev.dsa.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.abinbev.dsa.R;
import com.abinbev.dsa.model.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bduggirala on 5/12/16.
 */
public class SearchableAdapter extends BaseAdapter implements Filterable {

    private List<User> users = new ArrayList<User>();
    private Filter filter;
    private String search;

    public SearchableAdapter() {
        super();
    }

    public int getCount() {
        if (users.isEmpty()) return 1;
        return users.size();
    }

    public Object getItem(int position) {
        return users.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            convertView = inflater.inflate(android.R.layout.simple_list_item_2, null);

            holder = new ViewHolder();
            holder.text = (TextView) convertView.findViewById(android.R.id.text1);
            holder.text2 = (TextView) convertView.findViewById(android.R.id.text2);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (users.isEmpty()) {
            if(search == null || search.length() < 2) {
                holder.text.setText(R.string.enter_three_characters);

            } else {
                holder.text.setText(R.string.no_results);
            }
            holder.text2.setText(null);
            return convertView;
        }
        final User user = users.get(position);
        holder.text.setText(user.getName());
        holder.text2.setText(TextUtils.isEmpty(user.getEmail()) ? "" : user.getEmail());
        return convertView;
    }

    class ViewHolder {
        TextView text;
        TextView text2;
    }

    @Override
    public Filter getFilter() {
        if (filter == null) {
            filter = new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults filterResults = new FilterResults();
                    search = constraint.toString();
                    if (constraint != null) {
                        List<User> books = User.searchUsersbyName(constraint.toString());

                        filterResults.values = books;
                        filterResults.count = books.size();
                    }
                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    users = (List<User>) results.values;
                    notifyDataSetChanged();
                }
            };
        }
        return filter;
    }

    public void clearUsers() {
        users.clear();
    }

    public void resetSearch() {
        search = null;
    }
}
