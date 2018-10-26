package com.abinbev.dsa.activity;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.abinbev.dsa.R;
import com.abinbev.dsa.ui.presenter.InitialQueueActionDialogPresenter;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Jakub Stefanowski on 27.01.2017.
 */

public class InitialQueueActionDialogActivity extends Activity implements InitialQueueActionDialogPresenter.ViewModel {

    @Bind(R.id.positive)
    TextView positiveButton;

    @Bind(R.id.negative)
    TextView negativeButton;

    @Bind(R.id.message)
    TextView messageTextView;

    InitialQueueActionDialogPresenter presenter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog);
        ButterKnife.bind(this);

        messageTextView.setText(R.string.send_or_delete_queue);
        positiveButton.setText(R.string.send);
        negativeButton.setText(R.string.delete);

        presenter = new InitialQueueActionDialogPresenter(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        presenter.setViewModel(this);
        presenter.start();
    }

    @Override
    protected void onStop() {
        presenter.stop();
        super.onStop();
    }

    @OnClick(R.id.positive)
    void onSendClicked() {
        presenter.onSendClicked();
    }

    @OnClick(R.id.negative)
    void onDeleteClicked() {
        presenter.onDeleteClicked();
    }

    @Override
    public void close() {
        finish();
    }

    @Override
    public void showDeletingProgress() {
        negativeButton.setText("Deleting...");
        negativeButton.setEnabled(false);
    }
}
