package com.abinbev.dsa.ui.customviews;

import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Spinner;

import com.abinbev.dsa.R;
import com.abinbev.dsa.model.Event;
import com.abinbev.dsa.utils.AbInBevConstants;
import com.abinbev.dsa.utils.PicklistUtils;
import com.salesforce.androidsyncengine.data.model.PicklistValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Jakub Stefanowski on 08.08.2017.
 */

public class VisitListEventFilter extends FrameLayout {

    public interface OnCloseListener {
        void onEventFilterClose();
    }

    public interface OnFilterEventsListener {
        void onFilterEvents(String visitState, String channel, String pocName, boolean ascending);
    }

    private static class SpinnerItem {
        private final String value;
        private final String label;

        private SpinnerItem(String value, String label) {
            this.value = value;
            this.label = label;
        }

        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return label;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            SpinnerItem that = (SpinnerItem) o;

            return value != null ? value.equals(that.value) : that.value == null;

        }

        @Override
        public int hashCode() {
            return value != null ? value.hashCode() : 0;
        }
    }

    @Bind(R.id.visit_list_filter_close)
    ImageButton btnClose;

    @Bind(R.id.visit_list_filter_state_spinner)
    Spinner spinnerVisitState;

    @Bind(R.id.visit_list_filter_channel_spinner)
    Spinner spinnerChannel;

    @Bind(R.id.visit_list_filter_poc_name)
    EditText pocName;

    @Bind(R.id.visit_list_filter_distance_spinner)
    Spinner spinnerDstance;

    @Bind({
            R.id.visit_list_filter_state_spinner,
            R.id.visit_list_filter_channel_spinner,
            R.id.visit_list_filter_distance_spinner
    })
    List<Spinner> spinners;

    OnCloseListener onCloseListener;

    OnFilterEventsListener onFilterEventsListener;


    public VisitListEventFilter(@NonNull Context context) {
        super(context);
        init(context);
    }

    public VisitListEventFilter(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public VisitListEventFilter(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(@NonNull Context context) {
        inflate(context, R.layout.view_visit_list_event_filter, this);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.visit_list_filter_close)
    void onCloseClicked() {
        if (onCloseListener != null) {
            onCloseListener.onEventFilterClose();
        }
    }

    @OnClick(R.id.visit_list_filter_filter_button)
    void onApplyFilter() {
        if (onFilterEventsListener == null) return;

        SpinnerItem selectedVisitState = (SpinnerItem) spinnerVisitState.getSelectedItem();
        SpinnerItem selectedChannel = (SpinnerItem) spinnerChannel.getSelectedItem();
        SpinnerItem selectedDistance = (SpinnerItem) spinnerDstance.getSelectedItem();
        String poc = pocName.getText().toString();

        String visitStateName = selectedVisitState == null ? null : selectedVisitState.getValue();
        String channelName = selectedChannel == null ? null : selectedChannel.getValue();
        boolean ascending = selectedDistance == null
                || selectedDistance.getValue() == null
                || selectedDistance.getValue().equals(getResources().getString(R.string.distance_sort_ascending));

        onFilterEventsListener.onFilterEvents(visitStateName, channelName, poc, ascending);
        if (onCloseListener != null) {
            onCloseListener.onEventFilterClose();
        }
    }

    public void resetSelection() {
        //reset spinners to top item which is blank
        for (Spinner spinner : spinners) {
            spinner.setSelection(0);
        }
    }

    public void setOnCloseListener(OnCloseListener l) {
        onCloseListener = l;
    }

    public void setOnFilterEventsListener(OnFilterEventsListener l) {
        this.onFilterEventsListener = l;
    }

    private ArrayList<SpinnerItem> getPickListValuesByField(String objectName, String fieldName) {
        HashMap<String, List<PicklistValue>> values = PicklistUtils.getMetadataPicklistValues(objectName, fieldName);
        ArrayList<SpinnerItem> items = new ArrayList<>();
        List<PicklistValue> picklistValues = values.get(fieldName);

        if (picklistValues != null) {
            for (PicklistValue picklistValue : picklistValues) {
                items.add(new SpinnerItem(picklistValue.getValue(), picklistValue.getLabel()));
            }
        }

        return items;
    }

    public void setupFilters(List<Event> events) {
        HashMap<Integer, HashSet<SpinnerItem>> sets = new HashMap<>();
        for (Spinner spinner : spinners) {
            sets.put(spinner.getId(), new HashSet<>());
        }

        ArrayList<SpinnerItem> states = getPickListValuesByField(AbInBevConstants.AbInBevObjects.EVENT, AbInBevConstants.EventFields.ESTADO_DE_VISITA__C);
        sets.get(spinnerVisitState.getId()).addAll(states);

        String[] distanceValues = getResources().getStringArray(R.array.distance_sort);
        ArrayList<SpinnerItem> distanceItems = new ArrayList<>();
        for (String distanceValue : distanceValues) {
            distanceItems.add(new SpinnerItem(distanceValue, distanceValue));
        }
        sets.get(spinnerDstance.getId()).addAll(distanceItems);

        ArrayList<SpinnerItem> channels = getPickListValuesByField(AbInBevConstants.AbInBevObjects.ACCOUNT, AbInBevConstants.AccountFields.CHANNEL);
        sets.get(spinnerChannel.getId()).addAll(channels);

        for (Event event : events) {
            String visitStateName = event.getVisitStateName();
            if (!TextUtils.isEmpty(visitStateName)) {
                sets.get(spinnerVisitState.getId()).add(new SpinnerItem(visitStateName, visitStateName));
            }
        }

        for (Spinner spinner : spinners) {
            ArrayList<SpinnerItem> names = new ArrayList<>(sets.get(spinner.getId()));
            names.add(0, new SpinnerItem(null, ""));

            setSimpleArrayAdapter(spinner, names);
        }
    }

    private void setSimpleArrayAdapter(Spinner spinner, ArrayList<SpinnerItem> spinnerItems) {
        ArrayAdapter<SpinnerItem> adapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, android.R.id.text1, spinnerItems);
        adapter.setDropDownViewResource(R.layout.multiline_spinner_item);
        spinner.setAdapter(adapter);
    }
}
