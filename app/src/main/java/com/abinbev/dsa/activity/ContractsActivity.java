package com.abinbev.dsa.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import com.abinbev.dsa.R;
import com.abinbev.dsa.adapter.ContractsAdapter;
import com.abinbev.dsa.model.CN_PBO_Contract__c;
import com.abinbev.dsa.ui.presenter.ContractsPresenter;
import com.abinbev.dsa.ui.presenter.ContractsPresenter.ContractData;
import java.util.List;
import butterknife.Bind;

/**
 * Created by Jakub Stefanowski
 */
public class ContractsActivity extends AppBaseDrawerActivity implements ContractsPresenter.ViewModel {

    public static final String ARGS_ACCOUNT_ID = "account_id";
    @Bind(R.id.contract_names)
    TextView contractName;

    @Bind(R.id.recycler_view)
    RecyclerView recyclerView;

/*    @Bind(R.id.scrollview_contract_names)
    ScrollView scrollView;

    @Bind(R.id.linear_contract_names)
    LinearLayout linearLayout;*/

    private int screenHeight = -1;
    String accountId;

    ContractsAdapter adapter;

    ContractsPresenter presenter;
    private String TAG = this.getClass().getSimpleName();

    public static String DATE_DUE = "DATE_DUE";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle("");

        getScreenHeight();
        accountId = getIntent().getStringExtra(ARGS_ACCOUNT_ID);

        adapter = new ContractsAdapter();
        recyclerView.setAdapter(adapter);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        adapter.setOnItemClickedListener(this::openContractDetails);

        boolean isFirstStart = savedInstanceState == null;
        presenter = new ContractsPresenter(accountId, isFirstStart);

    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_contracts;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRefresh() {
        presenter.setViewModel(this);
        presenter.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        presenter.stop();
    }

    @Override /* ContractsPresenter.ViewModel */
    public void setData(List<ContractData> contracts) {
        adapter.setData(contracts);
    }

    @Override /* ContractsPresenter.ViewModel */
    public void showExpiringContracts(List<CN_PBO_Contract__c> contracts) {
        showExpiringContractsDialog(contracts);
    }

    private void showExpiringContractsDialog(List<CN_PBO_Contract__c> contracts) {
//        ArrayList<String> contractNames = new ArrayList<>();
        StringBuffer stringBuffer = null;

        if (contracts != null && contracts.size() > 0) {


            stringBuffer = new StringBuffer();
            for (int i = 0 ; i < contracts.size() ; i++) {
                CN_PBO_Contract__c c = contracts.get(i);

                if (i == 0) {
                    stringBuffer.append(c.getDateDue(DATE_DUE)+getResources().getString(R.string.due_with_days)+c.getName());
                } else if(i == contracts.size() -1) {
                    stringBuffer.append(" , "+c.getName());
                } else {
                    stringBuffer.append(" , "+c.getName());
                }
            }
        }

//        for (CN_PBO_Contract__c contract : contracts) {
//            contractNames.add(contract.getName());
//            stringBuffer.append(contract.getName()+",");
//        }

        if (stringBuffer == null)
            contractName.setVisibility(View.GONE);
        else {

            contractName.setText(stringBuffer.toString());
            contractName.setVisibility(View.VISIBLE);
        }

//        try {
//            linearLayout.post(new Runnable() {
//                @Override
//                public void run() {
//                    int height = 200;
//                    if (screenHeight > 0) {
//                        height = screenHeight/10;
//                    }
//                    Log.e(TAG, "height:"+height+" - "+linearLayout.getHeight()+" - "+screenHeight/10);
//                    if (linearLayout.getHeight() > height) {
//                        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) linearLayout.getLayoutParams();
//                        params.height = height;
//                        linearLayout.setLayoutParams(params);
//                    }
//                }
//            });
//        }catch (Exception e) {
//            Log.e(TAG, "---:"+e.getMessage());
//        }





//        new AlertDialog.Builder(this)
//                .setTitle(R.string.contract_expiring_contracts)
//                .setItems(contractNames.toArray(new String[contractNames.size()]), null)
//                .setPositiveButton(android.R.string.ok, null)
//                .create()
//                .show();
    }

    private void openContractDetails(CN_PBO_Contract__c contract) {
        startActivity(new Intent(this, ContractItemsActivity.class)
                .putExtra(ContractItemsActivity.ARGS_CONTRACT_ID, contract.getId())
                .putExtra(ContractItemsActivity.ARGS_CONTRACT_NAME, contract.getName()));
    }

    private void getScreenHeight() {

        try {

            WindowManager wm = (WindowManager) this.getSystemService(getApplicationContext().WINDOW_SERVICE);
            DisplayMetrics dm = new DisplayMetrics();
            wm.getDefaultDisplay().getMetrics(dm);
//            int width = dm.widthPixels;
            this.screenHeight = dm.heightPixels;
//            float density = dm.density;
//            int densityDpi = dm.densityDpi;
//            int screenWidth = (int) (width / density);
//            int screenHeight = (int) (height / density);

        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
