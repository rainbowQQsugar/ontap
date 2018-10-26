package com.abinbev.dsa.ui.view;

import android.content.Context;
import android.support.v7.app.AppCompatDialog;
import android.widget.EditText;

import com.abinbev.dsa.R;
import com.abinbev.dsa.ui.presenter.ProspectKPIDetailFilterDialogPresenter;
import com.abinbev.dsa.utils.DateUtils;

import java.util.Date;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A custom dialog to handle the filter criteria for {@link com.abinbev.dsa.model.Account}
 */
public class ProspectKPIDetailFilterDialog extends AppCompatDialog implements ProspectKPIDetailFilterDialogPresenter.ViewModel {

    @Bind(R.id.prospect_start_date)
    EditText prospectStartDateEditText;

    @Bind(R.id.prospect_end_date)
    EditText prospectEndDateEditText;

    private ProspectsFilterDialogListener listener;
    private ProspectsKPIDetailFilterSelection selection;
    protected ProspectKPIDetailFilterDialogPresenter presenter;

    @Override
    public void setStartDate(Date date, String formatDate) {
        selection.startDate = date;
        prospectStartDateEditText.setText(formatDate);
    }

    @Override
    public void setEndDate(Date date, String formatDate) {
        selection.endDate = date;
        prospectEndDateEditText.setText(formatDate);
    }


    public interface ProspectsFilterDialogListener {
        void onDialogFilterClick(ProspectsKPIDetailFilterSelection prospectsKPIDetailFilterSelection);
    }

    public class ProspectsKPIDetailFilterSelection {
        public Date startDate = null;
        public Date endDate = null;
    }

    public ProspectKPIDetailFilterDialog(Context context, ProspectsFilterDialogListener listener) {
        super(context, R.style.AppCompatAlertDialogStyle);
        setContentView(R.layout.prospect_kpi_detail_filter);
        ButterKnife.bind(this);
        this.listener = listener;
        selection = new ProspectsKPIDetailFilterSelection();
        presenter = new ProspectKPIDetailFilterDialogPresenter(context);
        presenter.setViewModel(this);
        presenter.start();
    }

    @OnClick(R.id.prospect_start_date)
    public void onProspectFilterStartDateClicked() {
        presenter.selectStartDate(selection.startDate);
    }

    @OnClick(R.id.prospect_end_date)
    public void onProspectFilterEndDateClicked() {
        presenter.selectEndDate(selection.endDate);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @OnClick(R.id.clear)
    @SuppressWarnings("unused")
    void onClearClicked() {
        presenter.clear();
        listener.onDialogFilterClick(selection);
        dismiss();
    }

    @OnClick(R.id.apply)
    @SuppressWarnings("unused")
    void onApplyClicked() {
        listener.onDialogFilterClick(selection);
        dismiss();
    }


}
