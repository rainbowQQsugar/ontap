package com.abinbev.dsa.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.abinbev.dsa.R;
import com.salesforce.dsa.data.model.Address;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Jakub Stefanowski on 24.10.2017.
 */

public class AddressDialogFragment extends DialogFragment {

    private static final String ARGS_FIELD_NAME = "field_name";

    private static final String ARGS_ADDRESS = "address";

    public interface OnAddressSavedListener {
        void onAddressSaved(String fieldName, Address address);
    }

    @Bind(R.id.address_dialog_country)
    EditText countryEditText;

    @Bind(R.id.address_dialog_postal_code)
    EditText postalCodeEditText;

    @Bind(R.id.address_dialog_state)
    EditText stateEditText;

    @Bind(R.id.address_dialog_city)
    EditText cityEditText;

    @Bind(R.id.address_dialog_street)
    EditText streetEditText;

    String fieldName;

    Address address;

    OnAddressSavedListener onSavedListener;

    public static AddressDialogFragment createInstance(String fieldName, Address address) {
        AddressDialogFragment fragment = new AddressDialogFragment();

        Bundle args = new Bundle();
        args.putString(ARGS_FIELD_NAME, fieldName);
        args.putParcelable(ARGS_ADDRESS, address);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof OnAddressSavedListener) {
            onSavedListener = (OnAddressSavedListener) context;
        }
        else {
            throw new IllegalStateException("Parent Activity has to implement OnAddressSavedListener");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.Theme_AppCompat_Light_Dialog_Alert);
        Bundle args = getArguments();
        fieldName = args.getString(ARGS_FIELD_NAME);
        address = args.getParcelable(ARGS_ADDRESS);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_address_dialog, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        countryEditText.setText(address.getCountry());
        postalCodeEditText.setText(address.getPostalCode());
        stateEditText.setText(address.getState());
        cityEditText.setText(address.getCity());
        streetEditText.setText(address.getStreet());
    }

    @Override
    public void onDestroyView() {
        ButterKnife.unbind(this);
        super.onDestroyView();
    }

    @OnClick(R.id.address_dialog_cancel)
    public void onCancelClicked() {
        dismissAllowingStateLoss();
    }

    @OnClick(R.id.address_dialog_save)
    public void onSaveClicked() {
        updateAddressData();
        onSavedListener.onAddressSaved(fieldName, address);
        dismissAllowingStateLoss();
    }

    private void updateAddressData() {
        address.setCountry(countryEditText.getText().toString());
        address.setPostalCode(postalCodeEditText.getText().toString());
        address.setState(stateEditText.getText().toString());
        address.setCity(cityEditText.getText().toString());
        address.setStreet(streetEditText.getText().toString());
    }
}
