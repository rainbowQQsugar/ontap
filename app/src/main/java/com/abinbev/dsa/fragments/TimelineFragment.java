package com.abinbev.dsa.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.abinbev.dsa.R;
import com.abinbev.dsa.activity.TimelineActivity;
import com.abinbev.dsa.adapter.TimelineAdapter;
import com.abinbev.dsa.model.Event;
import com.abinbev.dsa.model.Note;
import com.abinbev.dsa.model.User;
import com.abinbev.dsa.sync.DefaultSyncBroadcastReceiver;
import com.abinbev.dsa.ui.decorator.TimelineDecorator;
import com.abinbev.dsa.ui.presenter.TimelinePresenter;
import com.abinbev.dsa.utils.AppScheduler;
import com.abinbev.dsa.utils.DateUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Observable;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by Jakub Stefanowski on 26.01.2017.
 */

public class TimelineFragment extends Fragment implements TimelinePresenter.ViewModel {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String SHOW_VISITS = "show_visits";
    private static final String SHOW_NOTES = "show_notes";

    @Bind(R.id.timeline_recycler)
    RecyclerView timelineRecyclerView;

    private TimelineAdapter adapter;
    private LinearLayoutManager layoutManager;

    private TimelinePresenter timelinePresenter;

    private Note.NotesDetails notesDetails;
    private List<Event> events;

    private boolean showVisits;
    private boolean showNotes;

    private final CompositeSubscription subscriptions = new CompositeSubscription();

    public TimelineFragment() {
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static TimelineFragment newInstance(String accountId, boolean showVisits, boolean showNotes) {
        TimelineFragment fragment = new TimelineFragment();
        Bundle args = new Bundle();
        args.putBoolean(TimelineFragment.SHOW_VISITS, showVisits);
        args.putBoolean(TimelineFragment.SHOW_NOTES, showNotes);
        args.putString(TimelineActivity.ACCOUNT_ID_EXTRA, accountId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_timeline, container, false);
        ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        layoutManager = new LinearLayoutManager(getActivity());
        timelineRecyclerView.setLayoutManager(layoutManager);

        adapter = new TimelineAdapter.ViewTyped();
        timelineRecyclerView.setAdapter(adapter);

        if (getResources().getBoolean(R.bool.is10InchTablet)) {
            timelineRecyclerView.addItemDecoration(new TimelineDecorator(getActivity(), true));
        } else {
            timelineRecyclerView.addItemDecoration(new TimelineDecorator(getActivity(), false));
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Bundle args = getArguments();
        String accountId = args.getString(TimelineActivity.ACCOUNT_ID_EXTRA);
        showVisits = args.getBoolean(SHOW_VISITS);
        showNotes = args.getBoolean(SHOW_NOTES);
        if (timelinePresenter == null) {
            timelinePresenter = new TimelinePresenter(accountId, new DefaultSyncBroadcastReceiver());
        }
        timelinePresenter.start(this);
    }

    @Override
    public void onStop() {
        timelinePresenter.stop(this);
        subscriptions.clear();
        super.onStop();
    }

    @Override
    public synchronized void setEvents(List<Event> events) {
        this.events = events;
        updateAdapter(this.events, this.notesDetails);
    }

    @Override
    public synchronized void setNotes(Note.NotesDetails notesDetails) {
        this.notesDetails = notesDetails;
        updateAdapter(this.events, this.notesDetails);
    }

    private void updateAdapter(final List<Event> newEvents, final Note.NotesDetails newNotes) {
        subscriptions.clear();
        subscriptions.add(Observable.fromCallable(
                () -> {
                    int size = 0;
                    if (newEvents != null)
                        size += newEvents.size();

                    if (newNotes != null)
                        size += newNotes.notes.size();

                    List<TimelineAdapter.TimelineItem> data = new ArrayList<>(size);

                    updateNotes(data, newNotes);

                    updateVisits(data, newEvents);

                    Collections.sort(data, (o1, o2) -> {
                        int ret = Long.compare(o2.getTime(), o1.getTime());
                        if (ret == 0) {
                            String s1 = o1.getId();
                            String s2 = o1.getId();
                            return s1.compareTo(s2);
                        }
                        return ret;
                    });

                    return data;
                })
                .subscribeOn(Schedulers.computation())
                .observeOn(AppScheduler.main())
                .subscribe(timelineItems -> {
                    synchronized (adapter) {
                        adapter.setData(timelineItems);
                        adapter.notifyDataSetChanged();
                    }
                })
        );
    }

    private void updateVisits(List<TimelineAdapter.TimelineItem> data, List<Event> events) {
        if (showVisits && events != null) {
            for (int i = 0; i < events.size(); ++i) {
                final Event event = events.get(i);
                if (event.getCreatedDate() != null) {
                    Date itemDate = null;
                    final long time;

                    if (event.getControlStartDateTime() != null)
                        itemDate = DateUtils.dateFromDateTimeString(event.getControlStartDateTime());

                    // newly created events that haven't yet been synchronized have "" date
                    // for these parsing will fail, we'll display them at the top
                    if (itemDate == null) {
                        time = Long.MAX_VALUE;
                    } else {
                        time = itemDate.getTime();
                    }

                    final User user = User.getUserByUserId(event.getOwnerId());
                    final String username = user != null ? user.getName() : null;

                    data.add(new TimelineAdapter.TimelineItem<Event>() {
                        @Override
                        public String getId() {
                            return getItem().getId();
                        }

                        @Override
                        public int getType() {
                            return TimelineAdapter.VISITS_VIEW_TYPE;
                        }

                        @Override
                        public long getTime() {
                            return time;
                        }

                        @Override
                        public String getUser() {
                            return username;
                        }

                        @Override
                        public Event getItem() {
                            return event;
                        }
                    });
                }
            }
        }
    }

    private void updateNotes(List<TimelineAdapter.TimelineItem> data, Note.NotesDetails notesDetails) {
        if (showNotes && notesDetails != null) {
            for (int i = 0; i < notesDetails.notes.size(); i++) {
                final Note item = notesDetails.notes.get(i);
                final User user = notesDetails.users.get(item.getCreatedById());
                if (item.getCreatedDate() != null) {
                    Date itemDate = DateUtils.dateFromDateTimeString(item.getCreatedDate());
                    final long time;

                    // newly created events that haven't yet been synchronized have "" date
                    // for these parsing will fail, we'll display them at the top
                    if (itemDate == null) {
                        time = Long.MAX_VALUE;
                    } else {
                        time = itemDate.getTime();
                    }

                    data.add(new TimelineAdapter.TimelineItem<Note>() {

                        @Override
                        public String getId() {
                            return getItem().getId();
                        }

                        @Override
                        public int getType() {
                            return TimelineAdapter.NOTE_VIEW_TYPE;
                        }

                        @Override
                        public long getTime() {
                            return time;
                        }

                        @Override
                        public Note getItem() {
                            return item;
                        }

                        @Override
                        public String getUser() {
                            return user != null ? user.getName() : null;
                        }
                    });
                }
            }
        }
    }

}