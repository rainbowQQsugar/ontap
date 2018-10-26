package com.abinbev.dsa.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.abinbev.dsa.R;
import com.abinbev.dsa.model.CN_Product_Negotiation__c;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class NegotiationListAdapter extends BaseAdapter {

    ArrayList<String> sortingOrder;

    public interface NegotiationClickHandler {
        void onNegotiationClick(String negotiationId);
    }

    private final List<CN_Product_Negotiation__c> negotiations;
    private final NegotiationClickHandler negotiationClickHandler;

    public NegotiationListAdapter(NegotiationClickHandler negotiationClickHandler) {
        super();
        this.negotiations = new ArrayList<>();
        this.negotiationClickHandler = negotiationClickHandler;
    }

    @Override
    public int getCount() {
        return negotiations.size();
    }

    @Override
    public Object getItem(int position) {
        return negotiations.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            convertView = inflater.inflate(R.layout.negotiation_list_entry_view, parent, false);

            convertView.setTag(new ViewHolder(convertView));
        }

        final CN_Product_Negotiation__c negotiation = negotiations.get(position);
        String id = TextUtils.isEmpty(negotiation.getNegotiationId()) ?
                parent.getContext().getString(R.string.pendiente) : negotiation.getNegotiationId();

        ViewHolder viewHolder = (ViewHolder) convertView.getTag();
        viewHolder.negotiationNumber.setText(id);
        viewHolder.status.setText(negotiation.getTranslatedStatus());
        viewHolder.type.setText(negotiation.getTranslatedRecordName());

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                negotiationClickHandler.onNegotiationClick(negotiation.getId());
            }
        });

        return convertView;
    }

    public void setSortingOrder(String[] order) {
        this.sortingOrder = new ArrayList<String>(Arrays.asList(order));
    }

    public void setData(List<CN_Product_Negotiation__c> negotiations) {
        this.negotiations.clear();
        if (negotiations != null)
            this.negotiations.addAll(negotiations);
        sortByStatus();
        this.notifyDataSetChanged();
    }

    public void sortByNegotiationNumber(final boolean ascending) {
        Collections.sort(negotiations, (lhs, rhs) -> {
            if (ascending) {
                return lhs.getId().compareTo(rhs.getId());
            } else {
                return rhs.getId().compareTo(lhs.getId());
            }
        });
        this.notifyDataSetChanged();
    }

    public void sortByStatus() {
        Collections.sort(negotiations, (lhs, rhs) -> sortingOrder.indexOf(lhs.getStatus()) < sortingOrder.indexOf(rhs.getStatus()) ? -1 :
                sortingOrder.indexOf(lhs.getStatus()) > sortingOrder.indexOf(rhs.getStatus()) ? 1 : 0);
    }

    public void sortByType(final boolean ascending) {
        Collections.sort(negotiations, (lhs, rhs) -> {
            if (ascending) {
                return lhs.getRecordTypeId().compareTo(rhs.getRecordTypeId());
            } else {
                return rhs.getRecordTypeId().compareTo(lhs.getRecordTypeId());
            }
        });
        this.notifyDataSetChanged();
    }


    class ViewHolder {

        @Bind(R.id.negotiation_number)
        TextView negotiationNumber;

        @Bind(R.id.status)
        TextView status;

        @Bind(R.id.type)
        TextView type;


        public ViewHolder(View convertView) {
            ButterKnife.bind(this, convertView);
        }

    }
}
