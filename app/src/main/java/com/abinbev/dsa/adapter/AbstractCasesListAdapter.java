package com.abinbev.dsa.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.abinbev.dsa.R;
import com.abinbev.dsa.model.Case;
import com.abinbev.dsa.utils.CollectionUtils;
import com.abinbev.dsa.utils.ContentUtils;
import com.abinbev.dsa.utils.DateUtils;
import com.android.internal.util.Predicate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


/**
 * Created by lukaszwalukiewicz on 29.12.2015.
 */
public abstract class AbstractCasesListAdapter extends BaseAdapter {
    protected List<Case> totalCases;
    protected List<Case> cases;
    protected String currentState;
    protected String currentRecordTypeId;

    public AbstractCasesListAdapter() {
        super();
        this.currentState = null;
        this.currentRecordTypeId = null;
        this.totalCases = new ArrayList<>();
        this.cases = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return cases.size();
    }

    @Override
    public Case getItem(int position) {
        return this.cases.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            convertView = inflater.inflate(getLayoutRes(), parent, false);
            convertView.setTag(createViewHolder(convertView));
        }

        ViewHolder holder = (ViewHolder) convertView.getTag();
        Case caso = cases.get(position);
        bind(holder, caso);

        return convertView;
    }

    protected abstract int getLayoutRes();

    protected abstract ViewHolder createViewHolder(View view);

    protected void bind(ViewHolder vh, Case caso) {
        vh.caseCode.setText(caso.getName());
        vh.caseRecordType.setText(caso.getTranslatedRecordName());
        vh.caseSLA1.setText(DateUtils.formatDateTimeShort(caso.getSLA1()));
        vh.caseState.setText(caso.getTranslatedStatus());
    }

    public void setData(List<Case> cases) {
        this.totalCases.clear();
        this.totalCases.addAll(cases);
        this.cases = this.totalCases;
        this.notifyDataSetChanged();
    }

    public void sortByName(final boolean ascending) {
        Collections.sort(cases, new Comparator<Case>() {
            @Override
            public int compare(Case lhs, Case rhs) {
                if (ascending) {
                    return lhs.getName().compareTo(rhs.getName());
                } else {
                    return rhs.getName().compareTo(lhs.getName());
                }
            }
        });
        this.notifyDataSetChanged();
    }

    public void sortByRecordName(final boolean ascending) {
        Collections.sort(cases, new Comparator<Case>() {
            @Override
            public int compare(Case lhs, Case rhs) {
                if (ascending) {
                    return lhs.getTranslatedRecordName().compareTo(rhs.getTranslatedRecordName());
                } else {
                    return rhs.getTranslatedRecordName().compareTo(lhs.getTranslatedRecordName());
                }
            }
        });
        this.notifyDataSetChanged();
    }

    public void sortBySLA1(final boolean ascending) {
        Collections.sort(cases, new Comparator<Case>() {
            @Override
            public int compare(Case lhs, Case rhs) {
                if (ascending) {
                    if (!ContentUtils.isStringValid(lhs.getSLA1()) && !ContentUtils.isStringValid(rhs.getSLA1())) {
                        return 0;
                    } else if (!ContentUtils.isStringValid(lhs.getSLA1())) {
                        return -1;
                    } else if (!ContentUtils.isStringValid(rhs.getSLA1())) {
                        return 1;
                    }
                    return DateUtils.dateFromString(lhs.getSLA1()).compareTo(DateUtils.dateFromString(rhs.getSLA1()));
                } else {
                    if (!ContentUtils.isStringValid(rhs.getSLA1()) && !ContentUtils.isStringValid(lhs.getSLA1())) {
                        return 0;
                    } else if (!ContentUtils.isStringValid(rhs.getSLA1())) {
                        return -1;
                    } else if (!ContentUtils.isStringValid(lhs.getSLA1())) {
                        return 1;
                    }
                    return DateUtils.dateFromString(rhs.getSLA1()).compareTo(DateUtils.dateFromString(lhs.getSLA1()));
                }
            }
        });
        this.notifyDataSetChanged();
    }

    public void sortByState(final boolean ascending) {
        Collections.sort(cases, new Comparator<Case>() {
            @Override
            public int compare(Case lhs, Case rhs) {
                if (ascending) {
                    return lhs.getTranslatedStatus().compareTo(rhs.getTranslatedStatus());
                } else {
                    return rhs.getTranslatedStatus().compareTo(lhs.getTranslatedStatus());
                }
            }
        });
        this.notifyDataSetChanged();
    }

    private void filterCases(){
        Predicate<Case> predicate = new Predicate<Case>() {
            public boolean apply(Case tmpCaso) {
                if (showAllValues()){
                    return true;
                }
                if (showAllStates()){
                    return tmpCaso.getRecordTypeId().equalsIgnoreCase(currentRecordTypeId);
                }
                if (showAllRecordNames()){
                    return tmpCaso.getStatus().equalsIgnoreCase(currentState);
                }

                return tmpCaso.getRecordTypeId().equalsIgnoreCase(currentRecordTypeId) && tmpCaso.getStatus().equalsIgnoreCase(currentState);
            }
        };
        this.cases = CollectionUtils.filter(this.totalCases, predicate);
    }

    private boolean showAllValues(){
        return showAllStates() && showAllRecordNames();
    }

    private boolean showAllStates(){
        return currentState == null;
    }

    private boolean showAllRecordNames(){
        return currentRecordTypeId == null;
    }

    public void filterByState(final String state){
        this.currentState = state;
        filterCases();
        this.notifyDataSetChanged();
    }

    public void filterByRecordTypeId(final String recordTypeId){
        this.currentRecordTypeId = recordTypeId;
        filterCases();
        this.notifyDataSetChanged();
    }

    protected static class ViewHolder {

        @Bind(R.id.case_code)
        public TextView caseCode;

        @Bind(R.id.case_recordType)
        public TextView caseRecordType;

        @Bind(R.id.case_sla1)
        public TextView caseSLA1;

        @Bind(R.id.case_state)
        public TextView caseState;

        public ViewHolder(View convertView) {
            ButterKnife.bind(this, convertView);
        }
    }
}
