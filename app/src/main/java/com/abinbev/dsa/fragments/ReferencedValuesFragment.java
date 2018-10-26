package com.abinbev.dsa.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.abinbev.dsa.R;
import com.abinbev.dsa.ui.presenter.ReferencedValuesPresenter;
import com.abinbev.dsa.ui.presenter.ReferencedValuesPresenter.ReferencedValue;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnTextChanged;

public class ReferencedValuesFragment extends DialogFragment implements
        ReferencedValuesPresenter.ViewModel, ReferencedValuesAdapter.OnItemSelectedListener {

    private static final String ARG_LABEL = "label";
    private static final String ARG_FIELD_NAME = "field_name";
    private static final String ARG_DISPLAY_FIELD_NAME = "display_field_name";
    private static final String ARG_REFERRED_OBJECT = "referred_object";
    private static final String ARG_LOOKUP_FILTER = "lookup_filter";

    @Bind(R.id.label)
    TextView labelTextView;

    @Bind(R.id.list)
    RecyclerView recyclerView;

    @Bind(R.id.search_field)
    EditText searchField;

    Handler handler = new Handler();

    OnReferencedItemSelected onItemSelectedListener;

    String referredObjectType;

    ReferencedValuesPresenter presenter;

    ReferencedValuesAdapter adapter;

    String fieldName;

    String displayFieldName;

    String lookupFilter;

    CharSequence label;

    Runnable searchRunnable = new Runnable() {
        @Override
        public void run() {
            presenter.getItems(searchField.getText().toString());
        }
    };


    public static ReferencedValuesFragment newInstance(CharSequence label, String fieldName,
                                                       String displayFieldName,
                                                       String referredObjectType,
                                                       String lookupFilter) {
        ReferencedValuesFragment fragment = new ReferencedValuesFragment();
        Bundle args = new Bundle();
        args.putCharSequence(ARG_LABEL, label);
        args.putString(ARG_FIELD_NAME, fieldName);
        args.putString(ARG_DISPLAY_FIELD_NAME, displayFieldName);
        args.putString(ARG_REFERRED_OBJECT, referredObjectType);
        args.putString(ARG_LOOKUP_FILTER, lookupFilter);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            referredObjectType = getArguments().getString(ARG_REFERRED_OBJECT);
            fieldName = getArguments().getString(ARG_FIELD_NAME);
            displayFieldName = getArguments().getString(ARG_DISPLAY_FIELD_NAME);
            label = getArguments().getCharSequence(ARG_LABEL);
            lookupFilter = getArguments().getString(ARG_LOOKUP_FILTER);
        }

        presenter = new ReferencedValuesPresenter(referredObjectType, displayFieldName, lookupFilter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_referenced_values, container, false);
        ButterKnife.bind(this, view);

        Context context = view.getContext();
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        adapter = new ReferencedValuesAdapter(this);
        recyclerView.setAdapter(adapter);

        labelTextView.setText(label);

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnReferencedItemSelected) {
            onItemSelectedListener = (OnReferencedItemSelected) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnReferencedItemSelected");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.start();
        presenter.setViewModel(this);
        presenter.getItems(null);
    }

    @Override
    public void onPause() {
        presenter.stop();
        handler.removeCallbacks(searchRunnable);
        super.onPause();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onItemSelectedListener = null;
    }

    @Override
    public void onDestroyView() {
        ButterKnife.unbind(this);
        super.onDestroyView();
    }

    @Override
    public void setItems(List<ReferencedValue> items) {
        adapter.setReferencedValues(items);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onItemSelected(ReferencedValue item) {
        onItemSelectedListener.onReferencedItemSelected(label, fieldName, item.getId(), item.getName());
        dismissAllowingStateLoss();
    }

    @OnTextChanged(R.id.search_field)
    public void onSearchTextChanged(CharSequence text) {
        handler.removeCallbacks(searchRunnable);
        handler.postDelayed(searchRunnable, 1000);
    }

    public interface OnReferencedItemSelected {
        void onReferencedItemSelected(CharSequence label, String fieldName, String itemId, String itemName);
    }
}
