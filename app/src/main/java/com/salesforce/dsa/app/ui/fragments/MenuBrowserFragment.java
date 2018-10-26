package com.salesforce.dsa.app.ui.fragments;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.abinbev.dsa.model.CN_DSA_Azure_File__c;
import com.salesforce.androidsyncengine.utils.Guava.GuavaUtils;
import com.salesforce.dsa.app.R;
import com.salesforce.dsa.app.ui.activity.DigitalSalesAid;
import com.salesforce.dsa.app.ui.adapter.MenuBrowserListAdapter;
import com.salesforce.dsa.app.utils.DataUtils;
import com.salesforce.dsa.async.ModelDataFetcherTask;
import com.salesforce.dsa.data.model.CN_DSA_Folder__c;
import com.salesforce.dsa.data.model.CategoryMobileConfig__c;
import com.salesforce.dsa.data.model.Category__c;
import com.salesforce.dsa.data.model.MobileAppConfig__c;
import com.salesforce.dsa.utils.CategoryUtils;
import com.salesforce.dsa.utils.DSAConstants;

import java.util.ArrayList;
import java.util.List;

public class MenuBrowserFragment extends Fragment {

    private LinearLayout linearLayout;
    private MobileAppConfig__c activeConfig;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.menu_browser, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        linearLayout = (LinearLayout) getActivity().findViewById(R.id.topLayout);

        String activeConfigId = getArguments().getString("Active_Config_Id");
//        activeConfig = DataUtils.fetchMobileAppConfigForId(activeConfigId);
        DataUtils.fetchCategoryMobileConfig(activeConfig, catMobileConfigCallback);

        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();

        HorizontalScrollView hsv = (HorizontalScrollView) getActivity().findViewById(R.id.scrollView);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if (prefs.getBoolean(DSAConstants.Constants.INTERNAL_MODE, false)) {
            hsv.setBackground(getResources().getDrawable(R.drawable.internal_mode_menu_browser));
        } else {
            hsv.setBackground(getResources().getDrawable(R.drawable.internal_mode_menu_browser_off));
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.menu_list_view).setIcon(R.drawable.ic_visual_browser);
    }

    private void createList(int listIndex, List<? extends Object> listData, String listTitle) {

        final HorizontalScrollView scrollView = (HorizontalScrollView) getActivity().findViewById(R.id.scrollView);

        MenuBrowserListAdapter adapter = new MenuBrowserListAdapter(listData);

        LayoutInflater inflater = getActivity().getLayoutInflater();
        RelativeLayout relativeLayout = (RelativeLayout) inflater.inflate(R.layout.menu_browser_list, null);

        int width = getResources().getDimensionPixelSize(R.dimen.menu_browser_list_width);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(width, LayoutParams.MATCH_PARENT);
        // relativeLayout.setLayoutParams();

        ListView listView = (ListView) relativeLayout.findViewById(android.R.id.list);

        // Assign pedidoListAdapter to ListView
        listView.setAdapter(adapter);
        ColorDrawable sage = new ColorDrawable(this.getResources().getColor(android.R.color.darker_gray));
        listView.setDivider(sage);
        listView.setDividerHeight(1);

        ListItemclickListener listener = new ListItemclickListener(listIndex);
        listView.setOnItemClickListener(listener);

        TextView headerView = (TextView) relativeLayout.findViewById(R.id.list_header);
        headerView.setText(listTitle);

        relativeLayout.setLayoutParams(layoutParams);
        linearLayout.addView(relativeLayout);

        // added view might be out of window
        // scroll to the right
        scrollView.postDelayed(new Runnable() {
            @Override
            public void run() {
                scrollView.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
            }
        }, 100);

    }

    private class ListItemclickListener implements OnItemClickListener {

        private final int listViewIndex;

        public ListItemclickListener(int listViewIndex) {
            this.listViewIndex = listViewIndex;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            int toRemove = linearLayout.getChildCount() - listViewIndex;
            if (toRemove > 0) {
                Log.i("ONCLICK", "Removing from: " + listViewIndex + " count: " + toRemove);
                linearLayout.removeViews(listViewIndex, toRemove);
            }
            Object object = parent.getAdapter().getItem(position);
            Category__c category = null;
            if (object instanceof Category__c) {
                category = (Category__c) object;
                List<Object> listData = new ArrayList<>();
                List<CN_DSA_Folder__c> categoriesList = CategoryUtils.getSubCategories(category.getId());
                List<CN_DSA_Azure_File__c> contentVersions = DataUtils.fetchAllDsaFileForCatetories(category.getId());
                if (categoriesList != null && !categoriesList.isEmpty()) {
                    listData.addAll(categoriesList);
                }

                if (contentVersions != null && !contentVersions.isEmpty()) {
                    listData.addAll(contentVersions);
                }
                if (listData != null && !listData.isEmpty()) {
                    createList(listViewIndex + 1, listData, category.getName());
                }
            } else if (object instanceof CN_DSA_Azure_File__c) {
                ((DigitalSalesAid) getActivity()).downloadAndTriggrefresh((CN_DSA_Azure_File__c) object);
            }
        }
    }

    private final ModelDataFetcherTask.ModelDataFetcherCb<Category__c> categoryCallback = new ModelDataFetcherTask.ModelDataFetcherCb<Category__c>() {
        @Override
        public void onData(List<Category__c> data) {
            List<String> allVisibleCategories = CategoryUtils.getAllCategories();
            ArrayList<Category__c> visiableCategoies = new ArrayList<>();
            for (int i = 0; i < data.size(); i++) {
                if (allVisibleCategories.contains(data.get(i).getId())) {
                    visiableCategoies.add(data.get(i));
                }
            }
            if (data != null && !data.isEmpty()) {
                createList(1, visiableCategoies, "Categories");
            }
        }
    };

    // create a callback to fetch CategoryMobileConfig objects
    private final ModelDataFetcherTask.ModelDataFetcherCb<CategoryMobileConfig__c> catMobileConfigCallback = new ModelDataFetcherTask.ModelDataFetcherCb<CategoryMobileConfig__c>() {
        @Override
        public void onData(List<CategoryMobileConfig__c> data) {
            // get all catMobileAppConfigIds
            List<String> catMobileAppConfigIds = GuavaUtils.transform(data, new GuavaUtils.Function<CategoryMobileConfig__c, String>() {
                public String apply(CategoryMobileConfig__c config) {
                    return config.getCategoryId__c();
                }
            });
            // send the ids to the category fetcher
            DataUtils.fetchTopLevelCategoriesForConfigs(catMobileAppConfigIds, categoryCallback);
        }
    };

}
