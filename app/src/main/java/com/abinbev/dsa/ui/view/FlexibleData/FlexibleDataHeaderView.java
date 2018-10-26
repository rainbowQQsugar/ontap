package com.abinbev.dsa.ui.view.FlexibleData;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.abinbev.dsa.R;
import com.abinbev.dsa.adapter.FlexibleData.FlexibleDataListAdapter;
import com.abinbev.dsa.adapter.FlexibleData.SortableHeaderDataHandler;
import com.abinbev.dsa.ui.view.SortableHeader;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by lukaszwalukiewicz on 08.01.2016.
 */
public class FlexibleDataHeaderView extends LinearLayout {
    @Bind({ R.id.flexibleDataNumber, R.id.flexibleDataValue})
    List<SortableHeader> sortableHeaders;
    private FlexibleDataListAdapter adapter;
    private String section;

    public void setSection(String section){
        this.section = section;
    }

    public void setupNumberHeader(SortableHeaderDataHandler sortableHeaderDataHandler){
        if (sortableHeaderDataHandler.isSorted()) {
            setNumberHeaderSortedAscending(sortableHeaderDataHandler.isSortedAscending());
        } else {
            resetNumberHeader();
        }
    }

    private void resetNumberHeader(){
        SortableHeader numberHeader = sortableHeaders.get(0);
        numberHeader.clearSortIndicator();
    }

    private void setNumberHeaderSortedAscending(boolean ascending){
        SortableHeader numberHeader = sortableHeaders.get(0);
        if (ascending){
            numberHeader.sortAscending();
        } else{
            numberHeader.sortDescending();
        }
    }

    public void setupValuerHeader(SortableHeaderDataHandler sortableHeaderDataHandler){
        if (sortableHeaderDataHandler.isSorted()) {
            setValueHeaderSortedAscending(sortableHeaderDataHandler.isSortedAscending());
        } else {
            resetValueHeader();
        }
    }

    private void resetValueHeader(){
        SortableHeader valueHeader = sortableHeaders.get(1);
        valueHeader.clearSortIndicator();
    }

    private void setValueHeaderSortedAscending(boolean ascending){
        SortableHeader valueHeader = sortableHeaders.get(1);
        if (ascending){
            valueHeader.sortAscending();
        } else{
            valueHeader.sortDescending();
        }
    }

    public FlexibleDataHeaderView(Context context, FlexibleDataListAdapter adapter) {
        this(context, null, adapter);
    }

    public FlexibleDataHeaderView(Context context, AttributeSet attrs, FlexibleDataListAdapter adapter) {
        this(context, attrs, 0, adapter);
    }

    public FlexibleDataHeaderView(Context context, AttributeSet attrs, int defStyleAttr, FlexibleDataListAdapter adapter) {
        super(context, attrs, defStyleAttr);
        inflate(context, R.layout.flexible_data_list_header_view, this);
        this.adapter = adapter;
        ButterKnife.bind(this);
    }

    @OnClick(R.id.flexibleDataNumber)
    public void onNameHeaderClicked(View view) {
        SortableHeader sortableHeader = (SortableHeader) view;
        adapter.sortByNumber(sortableHeader.toggleSortDirection(), section);
        clearSortOnAllOthers(sortableHeader.getId());
    }

    @OnClick(R.id.flexibleDataValue)
    public void onValueHeaderClicked(View view) {
        SortableHeader sortableHeader = (SortableHeader) view;
        adapter.sortByValue(sortableHeader.toggleSortDirection(), section);
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
