package com.salesforce.dsa.app.ui.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;


import com.salesforce.dsa.app.sync.CheckAccessFilePermissionTask;
import com.salesforce.dsa.data.model.CN_DSA_Folder__c;
import com.salesforce.dsa.data.model.CN_DSA__c;
import com.abinbev.dsa.model.CN_DSA_Azure_File__c;
import com.abinbev.dsa.R;
import com.salesforce.dsa.app.sync.DownloadDsaFileTask;
import com.salesforce.dsa.app.ui.activity.DigitalSalesAid;
import com.salesforce.dsa.app.ui.adapter.DsaTopIndicatorAdapter;
import com.salesforce.dsa.app.ui.adapter.FilesListAdapter;
import com.salesforce.dsa.app.utils.DataUtils;
import com.salesforce.dsa.async.ModelDataFetcherTask;
import com.salesforce.dsa.data.model.Category__c;
import com.salesforce.dsa.utils.CategoryUtils;
import com.salesforce.dsa.utils.DSAConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DsaFileFolderFragment extends Fragment implements FilesListAdapter.OnItemClickListener, DsaTopIndicatorAdapter.OnIndicatorClickListener, DownloadDsaFileTask.DownloadDsaFileCallBack, DigitalSalesAid.OnBackPressed {

    private CN_DSA__c activeConfig;
    private RecyclerView top_indicator, category_trees;
    private Map<Integer, String> topIndicators = new HashMap<>();
    private FilesListAdapter filesListAdapter;
    private DsaTopIndicatorAdapter dsaTopIndicatorAdapter;
    private int categoryLevel;
    private Map<Integer, List> categories = new HashMap<>();
    //    private List<Object> listData = new ArrayList<>();
    private String TAG = getClass().getSimpleName();
    private ArrayList<Category__c> visiableCategoies = new ArrayList<>();


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_dsa_folder, container, false);
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        String activeConfigId = getArguments().getString(DSAConstants.Constants.ACTIVE_CONFIG_ID);
        activeConfig = DataUtils.fetchMobileAppConfigForId(activeConfigId);
        DataUtils.fetchCategoryMobileConfig(activeConfig, catMobileConfigCallback);//get top level folders,don't puzzled by method name
        Log.e(TAG, "onActivityCreated:DsaFileFolderFragment ");
        top_indicator = (RecyclerView) getActivity().findViewById(R.id.top_indicator);
        top_indicator.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        category_trees = (RecyclerView) getActivity().findViewById(R.id.category_trees);
        category_trees.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));

        ((DigitalSalesAid) getActivity()).setDownloadDsaFileCallBack(this);
        ((DigitalSalesAid) getActivity()).setOnBackPressed(this);
    }

    private ModelDataFetcherTask.ModelDataFetcherCb<CN_DSA_Folder__c> catMobileConfigCallback = new ModelDataFetcherTask.ModelDataFetcherCb<CN_DSA_Folder__c>() {
        @Override
        public void onData(List<CN_DSA_Folder__c> data) {
            if (data.isEmpty()) return;
            DataUtils.checkAccessPermisson(data, new CheckAccessFilePermissionTask.OnCheckedCallBack<CN_DSA_Folder__c>() {
                @Override
                public void onCheckedCallBack(List<CN_DSA_Folder__c> list) {
                    if (!list.isEmpty()) {
                        categories.put(categoryLevel, list);
                        topIndicators.put(0, getActivity().getString(R.string.categories));
                        createList(list, topIndicators);
                    }
                }
            }, CN_DSA_Folder__c.class);
        }
    };

    private void createList(List<? extends Object> listData, Map<Integer, String> topIndicators) {
        if (filesListAdapter == null) {
            filesListAdapter = new FilesListAdapter(listData, getActivity());
            category_trees.setAdapter(filesListAdapter);
            filesListAdapter.setOnItemClickListener(this);
        } else {
            filesListAdapter.setDataList(listData);
            filesListAdapter.notifyDataSetChanged();
        }
        if (dsaTopIndicatorAdapter == null) {
            dsaTopIndicatorAdapter = new DsaTopIndicatorAdapter(topIndicators, getActivity());
            top_indicator.setAdapter(dsaTopIndicatorAdapter);
            dsaTopIndicatorAdapter.setOnIndicatorClickListener(this);
        } else {
            dsaTopIndicatorAdapter.setIndicators(topIndicators);
            dsaTopIndicatorAdapter.notifyDataSetChanged();
        }

    }


    @Override
    public void onItemClickListener(View view, int position) {
        Object object = filesListAdapter.getItem(position);
        CN_DSA_Folder__c category = null;
        List listData = new ArrayList<>();
        if (object instanceof CN_DSA_Folder__c) {
            category = (CN_DSA_Folder__c) object;
            CN_DSA_Folder__c finalCategory = category;
            DataUtils.checkAccessPermisson(CategoryUtils.getSubCategories(category.getId()), new CheckAccessFilePermissionTask.OnCheckedCallBack<CN_DSA_Folder__c>() {
                @Override
                public void onCheckedCallBack(List<CN_DSA_Folder__c> list) {
                    if (!list.isEmpty()) listData.addAll(list);
                    DataUtils.checkAccessPermisson(DataUtils.fetchAllDsaFileForCatetories(finalCategory.getId()), new CheckAccessFilePermissionTask.OnCheckedCallBack<CN_DSA_Azure_File__c>() {
                        @Override
                        public void onCheckedCallBack(List<CN_DSA_Azure_File__c> list) {
                            if (!list.isEmpty()) listData.addAll(list);
                            if (listData.size() == 0) {
                                Toast.makeText(getActivity(), getActivity().getString(R.string.empty_folder), Toast.LENGTH_SHORT).show();
                                return;
                            }
                            categoryLevel += 1;
                            categories.put(categoryLevel, listData);
                            topIndicators.put(categoryLevel, finalCategory.getName());
                            createList(listData, topIndicators);
                        }
                    }, CN_DSA_Azure_File__c.class);
                }
            }, CN_DSA_Folder__c.class);
        } else if (object instanceof CN_DSA_Azure_File__c) {
            ((DigitalSalesAid) getActivity()).downloadAndTriggrefresh((CN_DSA_Azure_File__c) object);
        }
    }


    @Override
    public void onIndicatorClickListener(View view, int position) {
        int size = topIndicators.size();
        if (dsaTopIndicatorAdapter != null) {
            for (int i = 0; i < size; i++) {
                if (i > position) {
                    topIndicators.remove(i);
                }
            }
            categoryLevel = position;
        }
        List files = categories.get(categoryLevel);
        createList(files, topIndicators);
    }


    @Override
    public void downloadResult(boolean isSucess) {
        if (filesListAdapter != null)
            filesListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onBackPressed() {
        if (categoryLevel == 0) {
            getActivity().finish();
            return;
        }
        topIndicators.remove(categoryLevel--);
        List files = categories.get(categoryLevel);
        createList(files, topIndicators);
    }
}
