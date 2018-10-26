package com.abinbev.dsa.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.abinbev.dsa.R;
import com.abinbev.dsa.adapter.GiveGetFilter;
import com.abinbev.dsa.adapter.GiveGetSearchAdapter;
import com.abinbev.dsa.ui.presenter.GiveGetSearchPresenter;
import com.abinbev.dsa.ui.view.negotiation.Material__c;
import com.abinbev.dsa.ui.view.negotiation.NegotiationHelper;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;

/**
 * Created by wandersonblough on 1/30/16.
 */
public class GiveGetSearchFragment extends Fragment implements GiveGetSearchPresenter.ViewModel {

    private static final String ARGS_CURRENT_IDS = "args_current_ids";
    private static final String TAG = GiveGetSearchFragment.class.getSimpleName();

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.result_list)
    RecyclerView resultList;

    @Nullable
    @Bind(R.id.get_tab)
    TextView getTab;

    @Nullable
    @Bind(R.id.give_tab)
    TextView giveTab;

//    @Bind(R.id.search_field)
//    EditText searchField;

    List<String> currentIds;
    GiveGetSearchPresenter presenter;
    GiveGetSearchAdapter adapter;
    NegotiationHelper negotiationHelper;
    GiveGetFilter.Type type;

    public GiveGetSearchFragment() {

    }

    public static GiveGetSearchFragment newInstance(ArrayList<String> currentItems, GiveGetFilter.Type type) {
        GiveGetSearchFragment giveGetSearchFragment = new GiveGetSearchFragment();
        Bundle args = new Bundle();
        args.putStringArrayList(ARGS_CURRENT_IDS, currentItems);
        giveGetSearchFragment.setArguments(args);
        giveGetSearchFragment.type = type;
        return giveGetSearchFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentIds = getArguments().getStringArrayList(ARGS_CURRENT_IDS);

        presenter = new GiveGetSearchPresenter();
        presenter.setViewModel(this);
        presenter.start();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.give_get_search_activity, container, false);
        ButterKnife.bind(this, view);

        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.negotiations);
        if (type == GiveGetFilter.Type.GET)
            getSupportActionBar().setSubtitle(R.string.additional_gets);
        else if (type == GiveGetFilter.Type.GIVE)
            getSupportActionBar().setSubtitle(R.string.additional_gives);
        else
            getSupportActionBar().setSubtitle(R.string.additional_gives_gets);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        adapter = new GiveGetSearchAdapter();
        resultList.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        resultList.setAdapter(adapter);

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            negotiationHelper = (NegotiationHelper) getActivity();
        } catch (Exception e) {
            Log.e(TAG, "onAttach: ", e);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        adapter.updateSelectedIds(currentIds);
    }

    @Override
    public void onPause() {
        super.onPause();
        currentIds = adapter.getSelectedIds();
    }

    @Override
    public void onDestroy() {
        presenter.stop();
        super.onDestroy();
    }

    @Override
    public void setItems(List<Material__c> materialList) {
        adapter.setData(materialList);
        adapter.updateSelectedIds(currentIds);
        if (type == null) {
            if (giveTab != null) giveTab.performClick();
        } else if (adapter.getFilter() != null) {
            ((GiveGetFilter) adapter.getFilter()).setType(type);
            adapter.getFilter().filter("");
        }

    }

    @Nullable
    @OnClick(R.id.get_tab)
    public void showGets() {
        getTab.setActivated(true);
        giveTab.setActivated(false);
        if (adapter.getFilter() != null) {
            ((GiveGetFilter) adapter.getFilter()).setType(GiveGetFilter.Type.GET);
            adapter.getFilter().filter("");
        }
    }

    @Nullable
    @OnClick(R.id.give_tab)
    public void showGives() {
        getTab.setActivated(false);
        giveTab.setActivated(true);
        if (adapter.getFilter() != null) {
            ((GiveGetFilter) adapter.getFilter()).setType(GiveGetFilter.Type.GIVE);
            adapter.getFilter().filter("");
        }
    }

//    @OnTextChanged(R.id.search_field)
//    public void search(CharSequence searchTerm) {
//        // if data is not ready we cannot do anything with search
//        if (pedidoListAdapter.getFilter() != null) {
//            pedidoListAdapter.getFilter().filter(searchTerm);
//        }
//    }

    @OnClick(R.id.add_btn)
    public void addItems() {
        if (negotiationHelper != null) {
            negotiationHelper.updateItems(adapter.getSelectedItems());
        }
        getActivity().getSupportFragmentManager().popBackStack();
    }

    private ActionBar getSupportActionBar() {
        return ((AppCompatActivity) getActivity()).getSupportActionBar();
    }
}
