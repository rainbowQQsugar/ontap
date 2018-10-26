package com.abinbev.dsa.ui.presenter;

import android.content.Context;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;

public class CheckInWithPicturePresenter extends AbstractLocationAwarePresenter<CheckInWithPicturePresenter.ViewModel> {

//    private static final String TAG = CheckInWithPicturePresenter.class.getSimpleName();

    private static final int REQUIRED_ACCURACY = 80; //meters

    private static final String STATE_PICTURE_URI = "pictureUri";

    private static final String STATE_DESCRIPTION_TEXT = "descriptionText";

    private static final String STATE_LOCATION = "location";

    public interface ViewModel extends AbstractLocationAwarePresenter.LocationViewModel {
        void setPicture(Uri uri);

        void takePicture();

        void showTakePictureButton(boolean show);

        void showWaitingForLocationMessage(boolean show);

        void setSaveButtonEnabled(boolean enabled);

        Context getContext();

        void close();

        void setResult(Uri pictureUri, String description, Location location);
    }

    ViewModel viewModel;

    Uri pictureUri;

    CharSequence descriptionText;

    Location usersLocation;

    boolean isCommentRequired;

    boolean checkLocation;

    public CheckInWithPicturePresenter(boolean isCommentRequired, boolean checkLocation) {
        super();
        this.isCommentRequired = isCommentRequired;
        this.checkLocation = checkLocation;
    }

    @Override
    public void setViewModel(ViewModel viewModel) {
        this.viewModel = viewModel;
        super.setViewModel(viewModel);
        if (viewModel != null) {
            setupSaveButton();
        }
    }

    @Override
    public void start() {
        super.start();

        if (pictureUri != null) {
            viewModel.setPicture(pictureUri);
        }

        if (checkLocation) {
            startLocationUpdates();
        }
    }

    public void saveState(Bundle bundle) {
        bundle.putParcelable(STATE_PICTURE_URI, pictureUri);
        bundle.putCharSequence(STATE_DESCRIPTION_TEXT, descriptionText);
        bundle.putParcelable(STATE_LOCATION, usersLocation);
    }

    public void loadState(Bundle bundle) {
        if (bundle != null) {
            pictureUri = bundle.getParcelable(STATE_PICTURE_URI);
            descriptionText = bundle.getCharSequence(STATE_DESCRIPTION_TEXT);
            usersLocation = bundle.getParcelable(STATE_LOCATION);
        }
    }

    public void setPictureUri(Uri pictureUri) {
        this.pictureUri = pictureUri;
        viewModel.showTakePictureButton(pictureUri != null);
        viewModel.setPicture(pictureUri);
        setupSaveButton();
    }

    public void setDescriptionText(CharSequence text) {
        this.descriptionText = text;
        setupSaveButton();
    }

    public void onTakePictureClicked() {
        viewModel.takePicture();
    }

    public void onImageLoaded() {
        viewModel.showTakePictureButton(false);
    }

    public void onImageLoadFailed() {
        viewModel.showTakePictureButton(true);
    }

    public void onSaveClicked() {
        viewModel.setResult(pictureUri, String.valueOf(descriptionText), usersLocation);
        viewModel.close();
    }

    public void onCancelClicked() {
        viewModel.close();
    }

    public void onCheckBoxChecked() {
        if (checkLocation && usersLocation == null) {
            viewModel.showWaitingForLocationMessage(true);
        }
    }

    public void onCheckBoxUnchecked() {
        viewModel.showWaitingForLocationMessage(false);
    }

    private void setupSaveButton() {
        boolean hasPicture = pictureUri != null;
        boolean hasComment = !isCommentRequired || !TextUtils.isEmpty(descriptionText);
        viewModel.setSaveButtonEnabled(hasPicture && hasComment);
    }

    @Override
    public void onNewLocationReceived(Location location) {
        if (location.getAccuracy() != 0 && location.getAccuracy() <= REQUIRED_ACCURACY &&
                (usersLocation == null || location.getAccuracy() < usersLocation.getAccuracy())) {
            viewModel.showWaitingForLocationMessage(false);
            usersLocation = location;
        }
    }

    @Override
    public void onConnected() { }
}
