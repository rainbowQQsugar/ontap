package com.abinbev.dsa.adapter.events;


import android.text.TextUtils;
import android.widget.Filter;

import com.abinbev.dsa.fragments.EventListFragment;
import com.abinbev.dsa.model.Account;
import com.abinbev.dsa.model.AccountSearchResult;
import com.abinbev.dsa.model.Event;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

/**
 * @author Jason Harris (jason@akta.com)
 */
public class CompositeEventFilter extends Filter {

    private final EventAdapter adapter;

    private final List<ComponentFilter> filters;

    private List<EventListItem> eventList;
    private EventListFragment.Callbacks callbacks;

    interface ComponentFilter {
        Filter.FilterResults performFiltering(CharSequence constraint, Filter.FilterResults filterResults);
    }

    protected static class SearchFilterResults extends Filter.FilterResults {
        ArrayList<AccountSearchResult> searches;

        public SearchFilterResults() {
        }
    }

    public CompositeEventFilter(EventAdapter adapter, List<EventListItem> eventList) {
        super();
        this.adapter = adapter;
        this.eventList = new ArrayList<>(eventList);
        this.filters = new ArrayList<>();
    }

    public void setCallbacks(EventListFragment.Callbacks callbacks) {
        this.callbacks = callbacks;
    }

    public void setEventList(List<EventListItem> eventList) {
        this.eventList = new ArrayList<>(eventList);
    }

    public CompositeEventFilter addFilter(ComponentFilter filter) {
        filters.add(filter);
        return this;
    }

    public CompositeEventFilter clearFilters() {
        filters.clear();
        return this;
    }

    @Override
    protected Filter.FilterResults performFiltering(CharSequence constraint) {
        FilterResults results = new FilterResults();
        results.values = eventList;
        results.count = eventList.size();
        for (ComponentFilter filter : filters) {
            results = filter.performFiltering(constraint, results);
        }
        return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        // add search results if we have them
        if (results instanceof SearchFilterResults && ((SearchFilterResults) results).searches != null) {
            adapter.setSearches(((SearchFilterResults) results).searches);
        } else {
            adapter.clearSearches();
        }

        if (results != null && results.count > 0 || results.values != null) {
            adapter.setEventListItems((List<EventListItem>) results.values);
            if (callbacks != null) {
                final List<EventListItem> newItems = (List<EventListItem>) results.values;
                List<Event> events = new ArrayList<>();
                for (EventListItem listItem : newItems) {
                    events.add(listItem.event);
                }
                callbacks.itemsLoaded(events);
            }
        }
    }


    public static class VisitFilter implements ComponentFilter {
        private String state;

        public VisitFilter(String visitState) {
            state = visitState;
        }

        @Override
        public FilterResults performFiltering(CharSequence constraint, FilterResults filterResults) {
            if (TextUtils.isEmpty(state)) {
                return filterResults;
            }
            List<EventListItem> eventListItems = (List<EventListItem>) filterResults.values;
            ArrayList<EventListItem> matches = new ArrayList<>();
            for (EventListItem eventListItem : eventListItems) {
                Event event = eventListItem.event;
                if (state.equals(event.getVisitStateName())) {
                    matches.add(eventListItem);
                }
            }
            FilterResults results = new FilterResults();
            results.values = matches;
            results.count = matches.size();
            return results;
        }
    }


    public static class ChannelFilter implements ComponentFilter {
        private String channel;

        public ChannelFilter(String channel) {
            this.channel = channel;
        }

        @Override
        public FilterResults performFiltering(CharSequence constraint, FilterResults filterResults) {
            if (TextUtils.isEmpty(channel)) {
                return filterResults;
            }

            List<EventListItem> eventListItems = (List<EventListItem>) filterResults.values;
            ArrayList<EventListItem> matches = new ArrayList<>();
            for (EventListItem eventListItem : eventListItems) {
                Event event = eventListItem.event;
                if (channel.equals(event.getAccount().getChannel())) {
                    matches.add(eventListItem);
                }
            }
            FilterResults results = new FilterResults();
            results.values = matches;
            results.count = matches.size();
            return results;
        }
    }

    public static class POCNameFilter implements ComponentFilter {
        private String pocName;

        public POCNameFilter(String pocName) {
            this.pocName = pocName;
        }

        @Override
        public FilterResults performFiltering(CharSequence constraint, FilterResults filterResults) {
            if (TextUtils.isEmpty(pocName)) {
                return filterResults;
            }

            List<EventListItem> eventListItems = (List<EventListItem>) filterResults.values;
            ArrayList<EventListItem> matches = new ArrayList<>();
            for (EventListItem eventListItem : eventListItems) {
                Event event = eventListItem.event;
                if (event.getAccount().getName().toLowerCase().contains(pocName.toLowerCase())) {
                    matches.add(eventListItem);
                }
            }
            FilterResults results = new FilterResults();
            results.values = matches;
            results.count = matches.size();
            return results;
        }
    }


    public static class AccountSearchFilter implements ComponentFilter {

        @Override
        public FilterResults performFiltering(CharSequence constraint, FilterResults filterResults) {
            if (TextUtils.isEmpty(constraint)) {
                return filterResults;
            }
            Set<Account> accounts = new HashSet<>();
            if (filterResults != null && filterResults.values != null) {
                List<EventListItem> eventListItems = (List<EventListItem>) filterResults.values;
                for (EventListItem eventListItem : eventListItems) {
                    accounts.add(eventListItem.event.getAccount());
                }
            }
            ArrayList<AccountSearchResult> searchResults = new ArrayList<>();
            searchResults.addAll(AccountSearchResult.searchAccountsByNameOrId(constraint.toString()));
            ListIterator<AccountSearchResult> iterator = searchResults.listIterator();
            while (iterator.hasNext()) {
                AccountSearchResult result = iterator.next();
                // Those items are already added in current visit list. Wthout it they will be shown twice.
                String status = result.getAccount().getTranslatedProspectStatus();
                if (status.equals("Converted") || status.equals("Submitted") || status.equals("Rejected") || status.equals("Unqualified")) {
                    iterator.remove();
                }
                if (accounts.contains(result.getAccount())) {
                    iterator.remove();
                }
            }

            //filter out event items
            ArrayList<EventListItem> matches = new ArrayList<>();
            List<EventListItem> eventListItems = (List<EventListItem>) filterResults.values;
            String search = constraint.toString().toLowerCase();
            for (EventListItem eventListItem : eventListItems) {
                Event event = eventListItem.event;
                String name = event.getAccount().getName().toLowerCase();
                // TODO: investigate issue with missing field?
                String codigoDelCliente = event.getAccount().getCodigoDelCliente__c();
                String id = null;
                if (codigoDelCliente != null) id = codigoDelCliente.toLowerCase();
                if (name.contains(search) || (id != null && id.contains(search))) {
                    matches.add(eventListItem);
                }
            }

            SearchFilterResults results = new SearchFilterResults();
            results.searches = searchResults;
            results.values = matches;
            results.count = searchResults.size();
            return results;
        }
    }
}
