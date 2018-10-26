package com.abinbev.dsa.ui.view.Opportunity;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.abinbev.dsa.R;
import com.abinbev.dsa.adapter.OpportunitiesListAdapter;
import com.abinbev.dsa.ui.view.SortableHeader;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by lukaszwalukiewicz on 05.01.2016.
 */
public class OpportunityHeaderView extends LinearLayout {
    @Bind({ R.id.opportunity_Name, R.id.opportunity_Real, R.id.opportunity_Ideal, R.id.opportunity_Opportunity})
    List<SortableHeader> sortableHeaders;

    OpportunitiesListAdapter adapter;

    @Bind(R.id.section_Name)
    public TextView sectionName;

    public OpportunityHeaderView(Context context, OpportunitiesListAdapter adapter) {
        this(context, null, adapter);
    }

    public OpportunityHeaderView(Context context, AttributeSet attrs, OpportunitiesListAdapter adapter) {
        this(context, attrs, 0, adapter);
    }

    public OpportunityHeaderView(Context context, AttributeSet attrs, int defStyleAttr, OpportunitiesListAdapter adapter) {
        super(context, attrs, defStyleAttr);
        inflate(context, R.layout.opportunity_header_view, this);
        this.adapter = adapter;
        ButterKnife.bind(this);
    }

    @OnClick(R.id.opportunity_Name)
    public void onOpportunityNameHeaderClicked(View view) {
        SortableHeader sortableHeader = (SortableHeader) view;
        adapter.sortByOpportunityName(sortableHeader.toggleSortDirection(), sectionName.getText().toString());
        clearSortOnAllOthers(sortableHeader.getId());
    }

    @OnClick(R.id.opportunity_Real)
    public void onOpportunityRealHeaderClicked(View view) {
        SortableHeader sortableHeader = (SortableHeader) view;
        adapter.sortByOpportunityReal(sortableHeader.toggleSortDirection(), sectionName.getText().toString());
        clearSortOnAllOthers(sortableHeader.getId());
    }

    @OnClick(R.id.opportunity_Ideal)
    public void onOpportunityIdealHeaderClicked(View view) {
        SortableHeader sortableHeader = (SortableHeader) view;
        adapter.sortByOpportunityIdeal(sortableHeader.toggleSortDirection(), sectionName.getText().toString());
        clearSortOnAllOthers(sortableHeader.getId());
    }

    @OnClick(R.id.opportunity_Opportunity)
    public void onOpportunityHeaderClicked(View view) {
        SortableHeader sortableHeader = (SortableHeader) view;
        adapter.sortByOpportunity(sortableHeader.toggleSortDirection(), sectionName.getText().toString());
        clearSortOnAllOthers(sortableHeader.getId());
    }

    /**
     * Clears the sort order on all headers in the group EXCEPT for the id of the view passed in
     *
     * @param sortableHeaderId - the id to NOT clear the sort on
     */
    private void clearSortOnAllOthers(int sortableHeaderId) {
        for (SortableHeader sortableHeader : sortableHeaders) {
            if (sortableHeader.getId() != sortableHeaderId) {
                sortableHeader.clearSortIndicator();
            }
        }
    }
}
