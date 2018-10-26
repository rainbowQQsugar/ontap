package com.abinbev.dsa.ui.view;

import android.app.DatePickerDialog;
import android.content.Context;
import android.support.v7.app.AppCompatDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.abinbev.dsa.R;
import com.abinbev.dsa.utils.AbInBevConstants;
import com.salesforce.androidsyncengine.data.model.PicklistValue;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A custom dialog to handle the filter criteria for {@link com.abinbev.dsa.model.Account}
 */
public class ProspectsFilterDialog extends AppCompatDialog {

    private static final int[] PROSPECT_FILTER_LABELS = {
            R.string.prospect_all_states,
            R.string.prospect_status_open,
            R.string.prospect_status_contacted,
            R.string.prospect_status_submitted,
            R.string.prospect_status_converted,
            R.string.prospect_status_rejected,
            R.string.prospect_status_unqualified

    };
    private static final String[] PROSPECT_FILTER_VALUES = {
            null,
            AbInBevConstants.ProspectStatus.OPEN,
            AbInBevConstants.ProspectStatus.CONTACTED,
            AbInBevConstants.ProspectStatus.SUBMITTED,
            AbInBevConstants.ProspectStatus.CONVERTED,
            AbInBevConstants.ProspectStatus.REJECTED,
            AbInBevConstants.ProspectStatus.UNQUALIFIED
    };

    @Bind(R.id.prospect_status)
    Spinner prospectStatusSpinner;
    ArrayAdapter<PicklistValue> prospectStatusAdapter;

    @Bind(R.id.prospect_creation_date)
    EditText prospectCreationDateEditText;

    private SimpleDateFormat dateFormat;

    private ProspectsFilterDialogListener listener;

    public interface ProspectsFilterDialogListener {
        void onDialogFilterClick(ProspectsFilterSelection prospectsFilterSelection);
    }

    public class ProspectsFilterSelection {
        public String prospectStatus = null;
        public Date prospectCreationDate = null;
    }

    public ProspectsFilterDialog(Context context, ProspectsFilterDialogListener listener) {
        super(context, R.style.AppCompatAlertDialogStyle);
        setContentView(R.layout.prospect_filter);
        ButterKnife.bind(this);
        this.listener = listener;
        setupProspectStatesSpinner();
        dateFormat = (SimpleDateFormat) android.text.format.DateFormat.getDateFormat(this.getContext());
    }

    private void setupProspectStatesSpinner() {
        List<PicklistValue> picklistValues = new ArrayList<>();
        for (int i = 0; i < PROSPECT_FILTER_LABELS.length; i++) {
            PicklistValue picklistValue = new PicklistValue();
            picklistValue.setLabel(getContext().getString(PROSPECT_FILTER_LABELS[i]));
            picklistValue.setValue(PROSPECT_FILTER_VALUES[i]);
            picklistValues.add(picklistValue);
        }
        prospectStatusAdapter = createAdapter(picklistValues);
        prospectStatusSpinner.setAdapter(prospectStatusAdapter);
        prospectStatusSpinner.setSelection(Adapter.NO_SELECTION, false);
    }

    @OnClick(R.id.prospect_creation_date)
    public void onProspectCreationDateClicked() {
        String fieldValue = prospectCreationDateEditText.getText().toString();
        Date date;
        try {
            date = dateFormat.parse(fieldValue);
        } catch (Exception e) {
            date = null;
        }
        int year, month, day;
        if (date != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            year = cal.get(Calendar.YEAR);
            month = cal.get(Calendar.MONTH);
            day = cal.get(Calendar.DAY_OF_MONTH);
        } else {
            Calendar today = Calendar.getInstance();
            year = today.get(Calendar.YEAR);
            month = today.get(Calendar.MONTH);
            day = today.get(Calendar.DAY_OF_MONTH);
        }
        DatePickerDialog datePickerDialog = new DatePickerDialog(this.getContext(), onDateListener, year, month, day);
        datePickerDialog.show();
    }

    private DatePickerDialog.OnDateSetListener onDateListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker datePicker, int year, int month, int day) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, day);
            prospectCreationDateEditText.setText(dateFormat.format(calendar.getTime()));
        }
    };

    private ArrayAdapter<PicklistValue> createAdapter(List<PicklistValue> picklistValues) {
        return new ArrayAdapter<PicklistValue>(getContext(),
                R.layout.twoline_spinner_item, android.R.id.text1, picklistValues) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                LayoutInflater inflater = LayoutInflater.from(parent.getContext());
                TextView textView = (TextView) inflater.inflate(R.layout.dropdown_text_item, parent, false);
                PicklistValue picklistValue = getItem(position);
                textView.setText(picklistValue.getLabel());
                return textView;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = convertView;
                if (view == null) {
                    LayoutInflater inflater = LayoutInflater.from(parent.getContext());
                    view = inflater.inflate(R.layout.twoline_spinner_item, parent, false);
                }
                PicklistValue picklistValue = getItem(position);
                ((TextView) view.findViewById(android.R.id.text1)).setText(picklistValue.getLabel());
                return view;
            }
        };
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @OnClick(R.id.clear)
    @SuppressWarnings("unused")
    void onClearClicked() {
        prospectStatusSpinner.setSelection(0, false);
        prospectCreationDateEditText.setText(null);
        ProspectsFilterSelection prospectsFilterSelection = new ProspectsFilterSelection();
        listener.onDialogFilterClick(prospectsFilterSelection);
        dismiss();
    }

    @OnClick(R.id.apply)
    @SuppressWarnings("unused")
    void onApplyClicked() {
        ProspectsFilterSelection prospectsFilterSelection = new ProspectsFilterSelection();
        prospectsFilterSelection.prospectStatus = ((PicklistValue) prospectStatusSpinner.getSelectedItem()).getValue();

        String fieldValue = prospectCreationDateEditText.getText().toString();
        Date date;
        try {
            date = dateFormat.parse(fieldValue);
        } catch (Exception e) {
            date = null;
        }
        prospectsFilterSelection.prospectCreationDate = date;

        listener.onDialogFilterClick(prospectsFilterSelection);
        dismiss();
    }


}
