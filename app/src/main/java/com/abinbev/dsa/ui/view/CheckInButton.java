package com.abinbev.dsa.ui.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.abinbev.dsa.R;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Jakub Stefanowski on 13.06.2017.
 */

public class CheckInButton extends LinearLayout {


    public interface OnCheckButtonClickedListener {

        void onCheckInClicked();
        void onCheckOutClicked();
    }


    public enum State { CHECK_IN, CHECK_OUT }
    private Resources resources;

    private String checkInText;

    private String checkInPromptText;
    private String checkOutText;

    private String checkOutPromptText;

    private State state;
    private boolean canCheckout = true;

    private OnCheckButtonClickedListener listener;

    @Bind(R.id.merge_check_in_button_button)
    Button checkInButton;

    @Bind(R.id.merge_check_in_button_prompt)
    TextView checkInPrompt;

    @Bind(R.id.merge_check_in_button_validation)
    TextView validationError;

    String validationErrorText;

    public CheckInButton(Context context) {
        super(context);
        init();
    }

    public CheckInButton(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CheckInButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public CheckInButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.merge_check_in_button, this);
        setOrientation(VERTICAL);
        ButterKnife.bind(this);
        state = State.CHECK_IN;
        // default strings
        checkInText = getResources().getString(R.string.check_in);
        checkOutText = getResources().getString(R.string.check_out);
        resources = getResources();
        setupView();
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);
        ss.state = state;
        ss.checkInText = checkInText;
        ss.checkOutText = checkOutText;
        ss.checkInPromptText = checkInPromptText;
        ss.checkOutPromptText = checkOutPromptText;
        return ss;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        this.state = ss.state;
        this.checkInText = ss.checkInText;
        this.checkOutText = ss.checkOutText;
        this.checkInPromptText = ss.checkInPromptText;
        this.checkOutPromptText = ss.checkOutPromptText;
        //TODO setup view
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        checkInButton.setEnabled(enabled);
        setupView();
    }

    public void setCheckInValues(@StringRes int checkInStringId, @StringRes int promptStringId) {
        checkInText = resources.getString(checkInStringId);
        checkInPromptText = resources.getString(promptStringId);
        setupView();
    }

    public void setCheckOutValues(@StringRes int checkOutStringId, @StringRes int promptStringId) {
        checkOutText = resources.getString(checkOutStringId);
        checkOutPromptText = resources.getString(promptStringId);
        setupView();
    }

    public void setState(State state) {
        if (this.state != state) {
            this.state = state;
            this.validationErrorText = null;
            setupView();
        }
    }

    public State getState() {
        return state;
    }

    public void setOnCheckButtonClickedListener(OnCheckButtonClickedListener listener) {
        this.listener = listener;
    }

    public void setCanCheckout(final boolean canCheckout) {
        this.canCheckout = canCheckout;
        setupView();
    }

    public void showValidationError(String message) {
        validationErrorText = message;
        setupView();
    }

    public void showValidationError(@StringRes int messageRes) {
        validationErrorText = getResources().getString(messageRes);
        setupView();
    }

    public void hideValidationError() {
        validationErrorText = null;
        setupView();
    }

    private void setupView() {
        if (!TextUtils.isEmpty(validationErrorText)) {
            validationError.setText(validationErrorText);
            validationError.setVisibility(VISIBLE);
            checkInPrompt.setVisibility(GONE);
        }
        else {
            checkInPrompt.setVisibility(VISIBLE);
            validationError.setVisibility(GONE);

            switch (state) {
                case CHECK_IN:
                    checkInButton.setText(checkInText);

                    if(isEnabled()){
                        checkInButton.setBackgroundResource(R.color.abi_green);
                    }
                    else{
                        checkInButton.setBackgroundResource(R.color.sab_gray);
                    }

                    checkInPrompt.setText(checkInPromptText);
                    break;

                case CHECK_OUT:
                    checkInButton.setText(checkOutText);

                    if(isEnabled()){
                        checkInButton.setBackgroundResource(R.color.abi_blue);
                    }else{
                        checkInButton.setBackgroundResource(R.color.sab_gray);
                    }

                    if (canCheckout) {
                        checkInButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                    } else {
                        Drawable exclamationMarkDrawable = getContext().getResources().getDrawable(R.drawable.ic_exclamation_mark_black);
                        int drawableSize = (int) getResources().getDimension(R.dimen.space2);
                        exclamationMarkDrawable.setBounds(0, 0, drawableSize, drawableSize);
                        checkInButton.setCompoundDrawables(null, null, exclamationMarkDrawable, null);
                    }
                    checkInPrompt.setText(checkOutPromptText);
                    break;
            }
        }
    }

    @OnClick(R.id.merge_check_in_button_button)
    void onCheckClicked() {
        if (listener == null) return;

        switch (state) {
            case CHECK_IN:
                listener.onCheckInClicked();
                break;

            case CHECK_OUT:
                listener.onCheckOutClicked();
                break;
        }
    }

    static class SavedState extends BaseSavedState {

        String checkInText;
        String checkOutText;

        String checkInPromptText;
        String checkOutPromptText;

        State state;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            checkInText = in.readString();
            checkOutText = in.readString();
            checkInPromptText = in.readString();
            checkOutPromptText = in.readString();
            state = State.valueOf(in.readString());
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeString(checkInText);
            out.writeString(checkOutText);
            out.writeString(checkInPromptText);
            out.writeString(checkOutPromptText);
            out.writeString(state.name());
        }

        public static final Parcelable.Creator<SavedState> CREATOR
                = new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }
}
