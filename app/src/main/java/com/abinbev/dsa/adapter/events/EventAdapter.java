package com.abinbev.dsa.adapter.events;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Paint;
import android.location.Location;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.abinbev.dsa.R;
import com.abinbev.dsa.fragments.EventListFragment;
import com.abinbev.dsa.model.Account;
import com.abinbev.dsa.model.AccountSearchResult;
import com.abinbev.dsa.model.Event;
import com.abinbev.dsa.model.EventGroup;
import com.abinbev.dsa.model.VisitState;
import com.abinbev.dsa.ui.view.CustomSwipeRevealLayout;
import com.abinbev.dsa.ui.view.MoreInfoView;
import com.abinbev.dsa.utils.DateUtils;
import com.abinbev.dsa.utils.collections.CompositeComparator;
import com.chauthai.swipereveallayout.ViewBinderHelper;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * @author Jason Harris (jason@akta.com)
 */
public class EventAdapter extends RecyclerView.Adapter<EventAdapter.BaseEventViewHolder> implements Filterable {

    public final int EVENT_ITEM = 0;
    public final int SELECTED_ITEM = 1;
    public final int SEARCH_ITEM = 2;
    private static final int DEFAULT_DISTANCE_UPDATE_THRESHOLD = 100;

    private List<EventListItem> searches;

    private List<Event> allEvents;

    private List<EventListItem> items;

    private int activatedPosition = EventListFragment.INVALID_POSITION;
    private int fullSizeHeight;

    private EventListener listener;
    private CompositeEventFilter compositeEventFilter;
    private boolean isTablet;
    private boolean hideItems;
    private LatLng currentLocation;
    private boolean sortAscending = true;

    private final ViewBinderHelper swipeViewHelper = new ViewBinderHelper();

    private List<String> statusSortingOrder;

    public void setIsTablet(boolean isTablet) {
        this.isTablet = isTablet;
    }

    public boolean isTablet() {
        return isTablet;
    }

    public interface EventListener {
        void onCloseClick();
        void onEventClick(Event event, int position);
        void onDirectionsClick(Event event);
        void onCallClick(Event event);
        void onCreateEventClick(Event event, int position);
        void onAccountDetailsClick(String accountId);
    }

    public EventAdapter(String[] statusSortingOrder) {
        this.items = Collections.emptyList();
        this.compositeEventFilter = new CompositeEventFilter(this, items);
        this.searches = Collections.emptyList();
        this.allEvents = Collections.emptyList();
        this.statusSortingOrder = Arrays.asList(statusSortingOrder);
    }
    public void setCallbacks(EventListFragment.Callbacks callbacks ){
        if(compositeEventFilter!=null){
            compositeEventFilter.setCallbacks(callbacks);
        }
    }
    public List<Event> getEvents() {
        return allEvents;
    }

    public void setSortAscending(boolean ascending) {
        this.sortAscending = ascending;
    }

    public void setEvents(List<Event> events, boolean aggregate) {
        allEvents = new ArrayList<>(events);

        List<EventListItem> newListItems = new ArrayList<>();

        if (aggregate) {
            HashMap<String, EventGroup> eventGroups = getAggregatedEventGroups(allEvents);
            for (EventGroup eg : eventGroups.values()) {
                EventListItem listItem = createListItemFrom(eg);
                if (listItem != null) {
                    newListItems.add(listItem);
                }
            }
        }
        else {
            for (Event event : allEvents) {
                EventListItem listItem = createListItemFrom(event);
                if (listItem != null) {
                    newListItems.add(listItem);
                }
            }
        }

        sortEvents(newListItems, sortAscending);
        items = Collections.unmodifiableList(newListItems);
        compositeEventFilter.setEventList(this.items);

        notifyDataSetChanged();
    }

    private EventListItem createListItemFrom(Event event) {
        if (event == null) return null;

        Float distance = measureDistance(currentLocation, event.getAccount().getLocation());
        int completedVisits = 0;
        int totalVisits = 0;
        int statusOrdinal = statusSortingOrder.indexOf(event.getNotTranslatedVisitState());
        return new EventListItem(event, distance, completedVisits, totalVisits, statusOrdinal);
    }

    private EventListItem createListItemFrom(EventGroup eg) {
        if (eg == null) return null;

        Event event = eg.getNextOpenEvent();
        if (event != null) {
            Float distance = measureDistance(currentLocation, event.getAccount().getLocation());
            int completedVisits = eg.getCompletedVisitsCount();
            int totalVisits = eg.getVisitsCount();
            int statusOrdinal = statusSortingOrder.indexOf(event.getNotTranslatedVisitState());
            return new EventListItem(event, distance, completedVisits, totalVisits, statusOrdinal);
        }
        else {
            return null;
        }
    }

    void setEventListItems(List<EventListItem> newItems) {
        sortEvents(newItems, sortAscending);
        items = Collections.unmodifiableList(newItems);

        List<Event> events = new ArrayList<>();
        for (EventListItem listItem : newItems) {
            events.add(listItem.event);
        }
        allEvents = Collections.unmodifiableList(events);
        notifyDataSetChanged();
    }

    private static Float measureDistance(LatLng currentLocation, LatLng targetLocation) {
        if (currentLocation == null || targetLocation == null) return null;

        float[] distance = new float[1];
        Location.distanceBetween(currentLocation.latitude, currentLocation.longitude,
                targetLocation.latitude, targetLocation.longitude, distance);

        return distance[0];
    }

    private HashMap<String, EventGroup> getAggregatedEventGroups(List<Event> events) {
        events = new ArrayList<>(events); // Safety copy.
        HashMap<String, EventGroup> eventGroups = new HashMap<>();

        for (Event e : events) {
            String id = e.getAccount().getId();

            EventGroup oldEventGroup = eventGroups.get(id);

            if (oldEventGroup == null) {
                EventGroup group = new EventGroup();
                group.events.add(e);
                group.setVisitsCount(1);
                if (e.getVisitState() == VisitState.completed)
                    group.setCompletedVisitsCount(1);
                eventGroups.put(id, group);
            } else {
                int oldVisitsCnt = oldEventGroup.getVisitsCount();
                oldEventGroup.setVisitsCount(oldVisitsCnt + 1);
                oldEventGroup.events.add(e);
                int oldCompletedVisitsCnt = oldEventGroup.getCompletedVisitsCount();
                if (e.getVisitState() == VisitState.completed)
                    oldEventGroup.setCompletedVisitsCount(oldCompletedVisitsCnt + 1);
                eventGroups.put(id, oldEventGroup);
            }
        }


        for (Map.Entry<String, EventGroup> entry : eventGroups.entrySet()) {
            EventGroup value = entry.getValue();
            value.sortByDate();
        }

        return eventGroups;
    }

    public void setLocation(LatLng location) {
        float distanceMeters = DEFAULT_DISTANCE_UPDATE_THRESHOLD;

        if (currentLocation != null && location != null) {
            float[] res = new float[1];
            Location.distanceBetween(currentLocation.latitude, currentLocation.longitude,
                    location.latitude, location.longitude, res);
            distanceMeters = res[0];
        }

        if (distanceMeters >= DEFAULT_DISTANCE_UPDATE_THRESHOLD) {
            currentLocation = location;
            List<EventListItem> newEventListItems = new ArrayList<>();
            for (EventListItem oldListItem : items) {
                Event event = oldListItem.event;
                Float distance = measureDistance(currentLocation, event.getAccount().getLocation());
                newEventListItems.add(new EventListItem(event, distance,
                        oldListItem.completedVisits, oldListItem.totalVisits,
                        oldListItem.statusOrdinal));
            }

            sortEvents(newEventListItems, sortAscending);
            items = Collections.unmodifiableList(newEventListItems);
            compositeEventFilter.setEventList(items);

            notifyDataSetChanged();
        }
    }

    public void hideItems(boolean hideItems) {
        this.hideItems = hideItems;
    }

    public void setListener(EventListener listener) {
        this.listener = listener;
    }

    public int getFullSizeHeight() {
        return fullSizeHeight;
    }

    public void setFullSizeHeight(int fullSizeHeight) {
        this.fullSizeHeight = fullSizeHeight;
    }

    public int getActivatedPosition() {
        return activatedPosition;
    }

    public void clearSearches() {
        searches = Collections.emptyList();
    }

    public void setSearches(List<AccountSearchResult> searches) {
        ArrayList<EventListItem> listItems = new ArrayList<>();
        for (AccountSearchResult result: searches) {
            listItems.add(new EventListItem(result, null, 0, 0, 0));
        }
        this.searches = Collections.unmodifiableList(listItems);
    }

    @Override
    public Filter getFilter() {
        return getCompositeEventFilter();
    }

    public CompositeEventFilter getCompositeEventFilter() {
        return compositeEventFilter;
    }

    public void setActivatedPosition(int activatedPosition) {
        this.activatedPosition = activatedPosition;
        notifyItemChanged(activatedPosition);
    }

    public int indexOf(Event event) {
        return items.indexOf(event);
    }

    @Override
    public long getItemId(int position) {
        return Long.valueOf(getItem(position).event.getId());
    }

    @Override
    public int getItemViewType(int position) {
        if (position == activatedPosition) {
            return SELECTED_ITEM;
        } else {
            if (position > searches.size() - 1) {
                return EVENT_ITEM;
            } else {
                return SEARCH_ITEM;
            }
        }
    }

    @Override
    public BaseEventViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View itemView;
        switch (viewType) {
            case SELECTED_ITEM:
                itemView = inflater.inflate(R.layout.more_info_item, viewGroup, false);
                itemView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, fullSizeHeight));
                itemView.setTranslationY(0);
                return new SelectedEventViewHolder(itemView);
            case SEARCH_ITEM:
                itemView = inflater.inflate(R.layout.search_account_item, viewGroup, false);
                return new AccountSearchViewHolder(itemView);
            default:
                itemView = LayoutInflater.from(viewGroup.getContext())
                                         .inflate(R.layout.event_item, viewGroup, false);
                return new EventViewHolder(itemView);
        }

    }

    @Override
    public void onBindViewHolder(BaseEventViewHolder holder, int position) {
        EventListItem eventListItem = getItem(position);
        Event event = eventListItem.event;
        Account account = event.getAccount();

        switch (getItemViewType(position)) {
            case SELECTED_ITEM:
                SelectedEventViewHolder viewHolder = (SelectedEventViewHolder) holder;
                viewHolder.moreInfoView.setEvent(event);
                break;
            case EVENT_ITEM:
                EventViewHolder eventViewHolder = (EventViewHolder) holder;
                swipeViewHelper.bind(eventViewHolder.swipeLayout, event.getId());
                setCompletedFields(eventListItem, eventViewHolder);
                break;
            case SEARCH_ITEM:
                AccountSearchViewHolder searchViewHolder = (AccountSearchViewHolder) holder;
                boolean hasEvent = ((AccountSearchResult) event).hasEvent();
                searchViewHolder.addEvent.setVisibility(hasEvent ? View.GONE : View.VISIBLE);
                break;
            default:
                break;
        }

        holder.name.setText(account.getName());
        holder.address.setText(getAddress(account));
        holder.itemView.setActivated(position == activatedPosition);
        if (holder.visitsCount != null) {
            holder.visitsCount.setText(eventListItem.completedVisits + " / " + eventListItem.totalVisits);

        }
        if (holder.date != null) {
            holder.date.setText(DateUtils.formatDateStringShort(account.getLastVisit()));
        }
        if (holder.endDate != null) {
            holder.endDate.setText(DateUtils.formatDateTimeShort(event.getEndDate()));
        }

        if (holder.distance != null) {
            if (eventListItem.distance == null) {
                holder.distance.setText("");
            }
            else {
                Resources resources = holder.itemView.getResources();
                float distanceKm = eventListItem.distance / 1000;
                holder.distance.setText(String.format(Locale.US,
                        "%.1f %s", distanceKm, resources.getString(R.string.kilometers)));
            }
        }
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

    @Override
    public int getItemCount() {
        return hideItems ? searches.size() : searches.size() + items.size();
    }

    public EventListItem getItem(int position) {
        if (position >= searches.size()) {
            return items.get(position - searches.size());
        } else {
            return searches.get(position);
        }
    }

    public void removeSearchAt(int position) {
        List<EventListItem> searchesCopy = new ArrayList<>(searches);
        searchesCopy.remove(position);
        searches = Collections.unmodifiableList(searchesCopy);
        notifyItemRemoved(position);
    }

    private void setCompletedFields(EventListItem eventListItem, EventViewHolder viewHolder) {
        Context context = viewHolder.itemView.getContext();

        if (eventListItem.completedVisits != 0 && eventListItem.completedVisits == eventListItem.totalVisits) {
            int lightGray = ContextCompat.getColor(context, R.color.light_gray);
            ButterKnife.apply(viewHolder.fields, CHANGE_TEXT_ALPHA, 0.6f);
            viewHolder.itemView.setBackgroundColor(lightGray);
        } else {
            TypedValue tv = new TypedValue();
            context.getTheme().resolveAttribute(android.R.attr.textColorPrimary, tv, true);
            int transparent = ContextCompat.getColor(context, android.R.color.transparent);
            ButterKnife.apply(viewHolder.fields, CHANGE_TEXT_ALPHA, 1f);
            viewHolder.itemView.setBackgroundColor(transparent);
        }
    }


    public class BaseEventViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.txtName)
        TextView name;

        @Bind(R.id.txtAddress)
        TextView address;

        @Nullable
        @Bind(R.id.txtDate)
        TextView date;

        @Nullable
        @Bind(R.id.txtDateLabel)
        TextView dateLabel;

        @Nullable
        @Bind(R.id.txtEndDate)
        TextView endDate;

        @Nullable
        @Bind(R.id.distanceTxt)
        TextView distance;

        @Nullable
        @Bind(R.id.visitsCountTxt)
        TextView visitsCount;

        List<TextView> fields = new ArrayList<>();

        public BaseEventViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            fields.add(name);
            fields.add(address);
            fields.add(distance);
            fields.add(visitsCount);
            if (date != null) fields.add(date);
            if (dateLabel != null) fields.add(dateLabel);
        }
    }

    public class AccountSearchViewHolder extends BaseEventViewHolder implements View.OnClickListener {

        @Bind(R.id.btnAddEvent)
        TextView addEvent;

        public AccountSearchViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            addEvent.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (listener != null) {
                int position = getAdapterPosition();
                if (position != -1) {
                    listener.onCreateEventClick(getItem(position).event, position);
                }
            }
        }
    }

    public class EventViewHolder extends BaseEventViewHolder implements View.OnClickListener {

        @Bind(R.id.action_call)
        View callButton;

        @Bind(R.id.action_directions)
        View directionsButton;

        @Bind(R.id.content_holder)
        View contentHolder;

        @Bind(R.id.swipe_layout)
        CustomSwipeRevealLayout swipeLayout;

        public EventViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            
            contentHolder.setOnClickListener(this);
            directionsButton.setOnClickListener(this);
            callButton.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (listener != null) {
                int position = getAdapterPosition();
                // -1 indicates NO_POSITION
                if (position != -1) {
                    if (view == callButton) {
                        listener.onCallClick(getItem(position).event);
                    }
                    else if (view == directionsButton) {
                        listener.onDirectionsClick(getItem(position).event);
                    }
                    else if (view == contentHolder) {
                        listener.onEventClick(getItem(position).event, position);
                    }
                }
            }
        }
    }

    private static void sortEvents(List<EventListItem> eventListItems, boolean sortAscending) {
        Comparator<EventListItem> distanceComparator = new DistanceComparator();
        if (!sortAscending) {
            distanceComparator = Collections.reverseOrder(distanceComparator);
        }

        Collections.sort(eventListItems, new CompositeComparator<>(new StatusComparator(), distanceComparator));
    }

    public class SelectedEventViewHolder extends BaseEventViewHolder implements View.OnClickListener {

        @Bind(R.id.more_info_view)
        MoreInfoView moreInfoView;
        @Bind(R.id.txtAccount)
        TextView account;
        @Bind(R.id.eventDirection)
        ImageView direction;
        @Bind(R.id.imgClose)
        ImageView close;
        @Bind(R.id.address_container)
        LinearLayout addressContainer;

        public SelectedEventViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            account.setOnClickListener(this);
            account.setPaintFlags(account.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            close.setOnClickListener(this);
            direction.setOnClickListener(this);
            addressContainer.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (listener == null) return;

            if (close == view) {
                listener.onCloseClick();
            }
            else if (direction == view) {
                int position = getAdapterPosition();
                if (position != -1) {
                    listener.onDirectionsClick(getItem(position).event);
                }
            }
            else if (addressContainer == view) {
                int position = getAdapterPosition();
                if (position != -1) {
                    listener.onDirectionsClick(getItem(position).event);
                }
            }
            else if (account == view) {
                int position = getAdapterPosition();
                if (position != -1) {
                    Event event = getItem(position).event;
                    String accountId = event.getAccount().getId();
                    listener.onAccountDetailsClick(accountId);
                }
            }
        }
    }

    private static final ButterKnife.Setter<View, Float> CHANGE_TEXT_ALPHA = (view, alpha, index) -> view.setAlpha(alpha);

}


