package com.abinbev.dsa.ui.presenter;

import android.content.Context;
import android.util.Log;

import com.abinbev.dsa.R;
import com.abinbev.dsa.model.Account;
import com.abinbev.dsa.utils.AbInBevConstants;
import com.abinbev.dsa.utils.AppScheduler;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Observable;

public class ProspectKPIDetailPresenter extends AbstractRxPresenter<ProspectKPIDetailPresenter.ViewModel> {

    private static final String TAG = ProspectKPIDetailPresenter.class.getSimpleName();
    private static final String[] PROSPECT_STATUS = {
            AbInBevConstants.ProspectStatus.OPEN,
            AbInBevConstants.ProspectStatus.CONTACTED,
            AbInBevConstants.ProspectStatus.SUBMITTED,
            AbInBevConstants.ProspectStatus.CONVERTED
    };
    private static final int[] PROSPECT_STATUS_NAME = {
            R.string.prospect_status_open,
            R.string.prospect_status_contacted,
            R.string.prospect_status_submitted,
            R.string.prospect_status_converted
    };

    private static final String INDEX = "index";
    private static final String COUNT = "count";
    private static final String NAME = "name";
    private static final String PERCENT = "percent";

    private Date startDate;
    private Date endDate;
    private Context context;

    public ProspectKPIDetailPresenter(Context context) {
        this.context = context;
    }

    public interface ViewModel {
        void clearData();

        void addChartData(int index, String label, Integer value);

        void addItemData(int index, String label);

        void presenterData();
    }

    @Override
    public void start() {
        super.start();
        loadData(startDate, endDate);
    }

    @Override
    public void stop() {
        super.stop();
    }

    public void loadData(Date startDate, Date endDate) {
        addSubscription(Observable.fromCallable(
                () -> {
                    List<Map<String, Object>> dataList = new ArrayList<>();
                    float totalCount = 0;
                    for (int i = 0; i < PROSPECT_STATUS.length; i++) {
                        String status = PROSPECT_STATUS[i];
                        int count = Account.getActiveProspectsCount(status, startDate, endDate);
                        if (count == 0) {
                            continue;
                        }
                        String name = context.getString(PROSPECT_STATUS_NAME[i]);
                        Map<String, Object> item = new HashMap<>();
                        item.put(COUNT, count);
                        item.put(NAME, name);
                        item.put(INDEX, i);
                        dataList.add(item);
                        totalCount += count;
                    }
                    for (Map<String, Object> item : dataList) {
                        Integer count = (Integer) item.get(COUNT);
                        item.put(PERCENT, count * 100.0f / totalCount);
                    }
                    return dataList;
                })
                .subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(
                        dataList -> {
                            this.startDate = startDate;
                            this.endDate = endDate;
                            if (viewModel() != null) {
                                viewModel().clearData();
                                for (int i = 0; i < dataList.size(); i++) {
                                    Map<String, Object> item = dataList.get(i);
                                    Integer index = (Integer) item.get(INDEX);
                                    Integer count = (Integer) item.get(COUNT);
                                    String name = (String) item.get(NAME);
                                    Float percent = (Float) item.get(PERCENT);
                                    viewModel().addChartData(index, String.format("%s [%.0f%%]", count, percent), count);
                                    viewModel().addItemData(index, name);
                                }
                                viewModel().presenterData();
                            }
                        },
                        error -> Log.e(TAG, "Error getting accounts count: ", error)
                ));
    }

    public void filterByDate(Date startDate, Date endDate) {
        loadData(startDate, endDate);
    }

    public void clear() {
        loadData(null, null);
    }


}