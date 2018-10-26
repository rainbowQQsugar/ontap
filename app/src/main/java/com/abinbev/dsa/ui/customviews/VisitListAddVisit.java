package com.abinbev.dsa.ui.customviews;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.FrameLayout;

import com.abinbev.dsa.R;
import com.abinbev.dsa.adapter.events.CompositeEventFilter;
import com.abinbev.dsa.adapter.events.EventAdapter;
import com.abinbev.dsa.model.Event;
import com.abinbev.dsa.ui.view.DividerItemDecoration;
import com.abinbev.dsa.ui.view.ScrollToTopLinearLayoutManager;
import com.abinbev.dsa.utils.ViewUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Jakub Stefanowski on 08.08.2017.
 */

public class VisitListAddVisit extends FrameLayout {

    private static final String TAG = "VisitListAddVisit";

    public interface OnAddVisitCloseClickedListener {
        void onAddVisitCloseClicked();
    }

    public interface OnVisitCreateClickedListener {
        void onVisitCreateClicked(Event event);
    }

    @Bind(R.id.editAddSearch)
    EditText editTextAdd;

    @Bind(R.id.view_visit_add_visit_recycler)
    RecyclerView recyclerView;

    EventAdapter adapter;

    LinearLayoutManager layoutManager;

    OnAddVisitCloseClickedListener onAddVisitCloseClickedListener;

    OnVisitCreateClickedListener onVisitCreateClickedListener;

    List<Event> inPlanVisits = Collections.emptyList();

    List<Event> outOfPlanVisits = Collections.emptyList();

    Filter.FilterListener filterListener = i -> {
//        layoutManager.smoothScrollToPosition(recyclerView, null, activatedPosition > INVALID_POSITION ? activatedPosition : 0);
    };


    public VisitListAddVisit(@NonNull Context context) {
        super(context);
        init(context);
    }

    public VisitListAddVisit(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public VisitListAddVisit(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(@NonNull Context context) {
        inflate(context, R.layout.view_visit_list_add_visit, this);
        ButterKnife.bind(this);

        ////////////////////////////////////////////////////////////////////////////////////////////
        layoutManager = new ScrollToTopLinearLayoutManager(context);

        ////////////////////////////////////////////////////////////////////////////////////////////
        String[] statusSortingOrder = getResources().getStringArray(R.array.visit_statuses);
        adapter = new EventAdapter(statusSortingOrder);
        adapter.hideItems(true);
        adapter.setIsTablet(getResources().getBoolean(R.bool.isTablet));
        adapter.setListener(new EventAdapter.EventListener() {

            @Override
            public void onCreateEventClick(Event event, int position) {
                if (onVisitCreateClickedListener != null) {
                    onVisitCreateClickedListener.onVisitCreateClicked(event);
                }
                adapter.removeSearchAt(position);
            }

            @Override
            public void onCloseClick() { }

            @Override
            public void onEventClick(Event event, int position) { }

            @Override
            public void onAccountDetailsClick(String accountId) { }

            @Override
            public void onDirectionsClick(Event event) { }

            @Override
            public void onCallClick(Event event) { }
        });

        ////////////////////////////////////////////////////////////////////////////////////////////
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.VERTICAL_LIST));
        recyclerView.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            int height = bottom - top;
            // only set the height of full-size, (more info) items to the largest we have measured
            if (height > adapter.getFullSizeHeight()) {
                adapter.setFullSizeHeight(height);
            }
        });

        ////////////////////////////////////////////////////////////////////////////////////////////
        editTextAdd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void afterTextChanged(Editable editable) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                mHandler.removeCallbacks(mFilterTask);
                mHandler.postDelayed(mFilterTask, DELAY_BEFORE_SEARCH);
            }
        });
        editTextAdd.setOnEditorActionListener((v, actionId, event) -> {
            boolean handled = false;
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                ViewUtils.closeKeyboard(editTextAdd);
                handled = true;
            }
            return handled;
        });
    }

    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (visibility == VISIBLE) {
            if (!TextUtils.isEmpty(editTextAdd.getText())) {
                adapter.getCompositeEventFilter()
                        .clearFilters()
                        .addFilter(new CompositeEventFilter.AccountSearchFilter())
                        .filter(editTextAdd.getText(), filterListener);
            }
            editTextAdd.requestFocus();
            ViewUtils.showKeyboard(editTextAdd);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mHandler.removeCallbacks(mFilterTask);
    }

    @OnClick(R.id.event_add_close)
    void onCloseClicked() {
        if (onAddVisitCloseClickedListener != null) {
            onAddVisitCloseClickedListener.onAddVisitCloseClicked();
        }
    }

    public void setOnAddVisitCloseClickedListener(OnAddVisitCloseClickedListener l) {
        this.onAddVisitCloseClickedListener = l;
    }

    public void setOnVisitCreateClickedListener(OnVisitCreateClickedListener l) {
        onVisitCreateClickedListener = l;
    }

    public void setInPlanVisits(List<Event> inPlanVisits) {
        this.inPlanVisits = inPlanVisits;
        updateVisits();
    }

    public void setOutOfPlanVisits(List<Event> outOfPlanVisits) {
        this.outOfPlanVisits = outOfPlanVisits;
        updateVisits();
    }

    private void updateVisits() {
        ArrayList<Event> allEvents = new ArrayList<>(inPlanVisits.size() + outOfPlanVisits.size());
        allEvents.addAll(inPlanVisits);
        allEvents.addAll(outOfPlanVisits);
        adapter.setEvents(allEvents, false);
    }

    private Handler mHandler = new Handler();
    private static final int DELAY_BEFORE_SEARCH = 1000; // 1 second

    Runnable mFilterTask = () -> {
            adapter.getCompositeEventFilter()
                    .clearFilters()
                    .addFilter(new CompositeEventFilter.AccountSearchFilter())
                    .filter(editTextAdd.getText().toString(), filterListener);
    };

}
