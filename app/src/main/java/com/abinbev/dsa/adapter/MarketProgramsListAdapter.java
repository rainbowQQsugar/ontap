package com.abinbev.dsa.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.abinbev.dsa.R;
import com.abinbev.dsa.model.MarketProgram;
import com.abinbev.dsa.utils.ContentUtils;
import com.abinbev.dsa.utils.DateUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;


public class MarketProgramsListAdapter extends BaseAdapter{
    List<MarketProgram> marketPrograms;
    public MarketProgramsListAdapter() {
        super();
        marketPrograms = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return marketPrograms.size();
    }

    @Override
    public Object getItem(int position) {
        return marketPrograms.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            convertView = inflater.inflate(R.layout.market_program_list_entry_view, parent, false);
            convertView.setTag(new ViewHolder(convertView));
        }

        final MarketProgram marketProgram = marketPrograms.get(position);
        ViewHolder viewHolder = (ViewHolder) convertView.getTag();
        viewHolder.marketProgram.setText(marketProgram.getMarketProgram());
        viewHolder.marketProgramState.setText(marketProgram.getStatus());
        viewHolder.marketProgramStartDate.setText(DateUtils.formatDateStringShort(marketProgram.getStartDate()));
        viewHolder.marketProgramEndDate.setText(DateUtils.formatDateStringShort(marketProgram.getEndDate()));

        return convertView;
    }

    public void setData(List<MarketProgram> marketPrograms) {
        this.marketPrograms.clear();
        this.marketPrograms.addAll(marketPrograms);
        this.notifyDataSetChanged();
    }

    public void sortByMarketProgram(final boolean ascending) {
        Collections.sort(marketPrograms, new Comparator<MarketProgram>() {
            @Override
            public int compare(MarketProgram lhs, MarketProgram rhs) {
                if (ascending) {
                    return lhs.getMarketProgram().compareTo(rhs.getMarketProgram());
                } else {
                    return rhs.getMarketProgram().compareTo(lhs.getMarketProgram());
                }
            }
        });
        this.notifyDataSetChanged();
    }

    public void sortByState(final boolean ascending) {
        Collections.sort(marketPrograms, new Comparator<MarketProgram>() {
            @Override
            public int compare(MarketProgram lhs, MarketProgram rhs) {
                if (ascending) {
                    return lhs.getStatus().compareTo(rhs.getStatus());
                } else {
                    return rhs.getStatus().compareTo(lhs.getStatus());
                }
            }
        });
        this.notifyDataSetChanged();
    }

    public void sortByStartDate(final boolean ascending) {
        Collections.sort(marketPrograms, new Comparator<MarketProgram>() {
            @Override
            public int compare(MarketProgram lhs, MarketProgram rhs) {
                if (ascending) {
                    //null check these dates
                    if (!ContentUtils.isStringValid(lhs.getStartDate()) && !ContentUtils.isStringValid(rhs.getStartDate())) {
                        return 0;
                    } else if (!ContentUtils.isStringValid(lhs.getStartDate())) {
                        return -1;
                    } else if (!ContentUtils.isStringValid(rhs.getStartDate())) {
                        return 1;
                    }
                    return DateUtils.dateFromString(lhs.getStartDate()).compareTo(DateUtils.dateFromString(rhs.getStartDate()));
                } else {
                    //null check these dates
                    if (!ContentUtils.isStringValid(lhs.getStartDate()) && !ContentUtils.isStringValid(lhs.getStartDate())) {
                        return 0;
                    } else if (!ContentUtils.isStringValid(rhs.getStartDate())) {
                        return -1;
                    } else if (!ContentUtils.isStringValid(lhs.getStartDate())) {
                        return 1;
                    }
                    return DateUtils.dateFromString(rhs.getStartDate()).compareTo(DateUtils.dateFromString(lhs.getStartDate()));
                }
            }
        });
        this.notifyDataSetChanged();
    }

    public void sortByEndDate(final boolean ascending) {
        Collections.sort(marketPrograms, new Comparator<MarketProgram>() {
            @Override
            public int compare(MarketProgram lhs, MarketProgram rhs) {
                if (ascending) {
                    if (!ContentUtils.isStringValid(lhs.getEndDate()) && !ContentUtils.isStringValid(rhs.getEndDate())) {
                        return 0;
                    } else if (!ContentUtils.isStringValid(lhs.getEndDate())) {
                        return -1;
                    } else if (!ContentUtils.isStringValid(rhs.getEndDate())) {
                        return 1;
                    }
                    return DateUtils.dateFromString(lhs.getEndDate()).compareTo(DateUtils.dateFromString(rhs.getEndDate()));
                } else {
                    if (!ContentUtils.isStringValid(rhs.getEndDate()) && !ContentUtils.isStringValid(lhs.getEndDate())) {
                        return 0;
                    } else if (!ContentUtils.isStringValid(rhs.getEndDate())) {
                        return -1;
                    } else if (!ContentUtils.isStringValid(lhs.getEndDate())) {
                        return 1;
                    }
                    return DateUtils.dateFromString(rhs.getEndDate()).compareTo(DateUtils.dateFromString(lhs.getEndDate()));
                }
            }
        });
        this.notifyDataSetChanged();
    }

    class ViewHolder {

        @Bind(R.id.market_program)
        TextView marketProgram;

        @Bind(R.id.market_program_state)
        TextView marketProgramState;

        @Bind(R.id.market_program_start_date)
        TextView marketProgramStartDate;

        @Bind(R.id.market_program_end_date)
        TextView marketProgramEndDate;

        public ViewHolder(View convertView) {
            ButterKnife.bind(this, convertView);
        }
    }
}
