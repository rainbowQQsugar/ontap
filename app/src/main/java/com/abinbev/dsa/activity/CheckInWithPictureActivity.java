package com.abinbev.dsa.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.abinbev.dsa.R;
import com.abinbev.dsa.ui.presenter.CheckInWithPicturePresenter;
import com.abinbev.dsa.utils.AttachmentUtils;
import com.google.android.gms.maps.model.LatLng;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;

/**
 * Created by Jakub Stefanowski on 27.01.2017.
 */

public class CheckInWithPictureActivity extends Activity implements CheckInWithPicturePresenter.ViewModel {

    public static final String ARGS_MESSAGE = "message";
    public static final String ARGS_IS_COMMENT_REQUIRED = "is_comment_required";
    public static final String ARGS_EXTRAS = "extras";
    public static final String ARGS_CHECK_BOX_MESSAGE = "check_box_message";
    public static final String ARGS_CHECK_LOCATION = "check_location";

    public static final String RESULT_DESCRIPTION = "description";
    public static final String RESULT_PICTURE_URI = "picture_uri";
    public static final String RESULT_IS_CHECKBOX_CHECKED = "is_checkbox_checked";
    public static final String RESULT_LOCATION = "location";
    public static final String RESULT_EXTRAS = "extras";

    @Bind(R.id.positive)
    TextView positiveButton;

    @Bind(R.id.negative)
    TextView negativeButton;

    @Bind(R.id.message)
    TextView messageTextView;

    @Bind(R.id.error_message)
    TextView errorTextView;

    @Bind(R.id.image_view)
    ImageView imageView;

    @Bind(R.id.take_picture_button_big)
    Button takePictureButton;

    @Bind(R.id.check_box)
    CheckBox checkBox;

    CheckInWithPicturePresenter presenter;

    boolean isCommentRequired;

    Bundle additionalExtras;

    Callback picassoCallback = new Callback() {
        @Override
        public void onSuccess() {
            if (!isDestroyed()) {
                presenter.onImageLoaded();
            }
        }

        @Override
        public void onError() {
            if (!isDestroyed()) {
                presenter.onImageLoadFailed();
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_in_with_picture);
        ButterKnife.bind(this);

        additionalExtras = getIntent().getBundleExtra(ARGS_EXTRAS);
        isCommentRequired = getIntent().getBooleanExtra(ARGS_IS_COMMENT_REQUIRED, false);
        String message = getIntent().getStringExtra(ARGS_MESSAGE);
        String checkBoxMessage = getIntent().getStringExtra(ARGS_CHECK_BOX_MESSAGE);
        boolean checkLocation = !TextUtils.isEmpty(checkBoxMessage)
                && getIntent().getBooleanExtra(ARGS_CHECK_LOCATION, false);

        messageTextView.setText(message);
        positiveButton.setText(R.string.save);
        negativeButton.setText(R.string.cancel);

        if (TextUtils.isEmpty(checkBoxMessage)) {
            checkBox.setVisibility(View.GONE);
        }
        else {
            checkBox.setVisibility(View.VISIBLE);
            checkBox.setText(checkBoxMessage);
        }

        errorTextView.setVisibility(View.GONE);

        presenter = new CheckInWithPicturePresenter(isCommentRequired, checkLocation);
        presenter.loadState(savedInstanceState);
        presenter.setViewModel(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        presenter.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        presenter.stop();
    }

    @Override
    protected void onDestroy() {
        presenter.setViewModel(null);
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        presenter.saveState(outState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && AttachmentUtils.SELECT_PHOTO_REQUEST_CODE == requestCode) {
            Uri pictureUri = (data != null && data.getData() != null) ? data.getData() : AttachmentUtils.fileUri;
            presenter.setPictureUri(pictureUri);
        }
    }

    @Override /* CheckInWithPicturePresenter.ViewModel */
    public void setPicture(Uri uri) {
        if (imageView == null) return;

        if (uri != null) {
            Picasso.with(this)
                    .load(uri)
                    .fit()
                    .centerInside()
                    .into(imageView, picassoCallback);
        }
        else {
            Picasso.with(this)
                    .load(uri)
                    .into(imageView);
        }
    }

    @Override /* CheckInWithPicturePresenter.ViewModel */
    public void takePicture() {
        AttachmentUtils.takePhoto(this);
    }

    @Override /* CheckInWithPicturePresenter.ViewModel */
    public void showTakePictureButton(boolean show) {
        takePictureButton.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override /* CheckInWithPicturePresenter.ViewModel */
    public Context getContext() {
        return this;
    }

    @Override /* CheckInWithPicturePresenter.ViewModel */
    public void close() {
        finish();
    }

    @Override /* CheckInWithPicturePresenter.ViewModel */
    public void setSaveButtonEnabled(boolean enabled) {
        positiveButton.setEnabled(enabled);
    }

    @Override /* CheckInWithPicturePresenter.ViewModel */
    public void setResult(Uri pictureUri, String description, Location location) {
        LatLng latLng = location == null ? null : new LatLng(location.getLatitude(), location.getLongitude());
        Intent intent = new Intent()
                .putExtra(RESULT_EXTRAS, additionalExtras)
                .putExtra(RESULT_IS_CHECKBOX_CHECKED, checkBox.getVisibility() == View.VISIBLE ?
                        checkBox.isChecked() : null)
                .putExtra(RESULT_PICTURE_URI, pictureUri)
                .putExtra(RESULT_LOCATION, latLng)
                .putExtra(RESULT_DESCRIPTION, description);
        setResult(RESULT_OK, intent);
    }

    @Override /* CheckInWithPicturePresenter.ViewModel */
    public Activity getActivity() {
        return this;
    }

    @Override /* CheckInWithPicturePresenter.ViewModel */
    public void showWaitingForLocationMessage(boolean show) {
        if (show) {
            errorTextView.setText(R.string.please_wait_for_location);
            errorTextView.setVisibility(View.VISIBLE);
        }
        else {
            errorTextView.setVisibility(View.GONE);
        }
    }

    @OnClick(R.id.positive)
    void onSaveClicked() {
        presenter.onSaveClicked();
    }

    @OnClick(R.id.negative)
    void onCancelClicked() {
        presenter.onCancelClicked();
    }

    @OnClick(R.id.take_picture_button_big)
    void onPictureButtonClicked() {
        presenter.onTakePictureClicked();
    }

    @OnClick(R.id.check_box)
    void onCheckBoxClicked() {
        if (checkBox.isChecked()) {
            presenter.onCheckBoxChecked();
        }
        else {
            presenter.onCheckBoxUnchecked();
        }
    }

    @OnTextChanged(R.id.text_field)
    void onTextChanged(CharSequence text) {
        presenter.setDescriptionText(text);
    }
}
