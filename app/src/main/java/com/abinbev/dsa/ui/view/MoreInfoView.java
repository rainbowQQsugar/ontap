package com.abinbev.dsa.ui.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.abinbev.dsa.R;
import com.abinbev.dsa.model.Account;
import com.abinbev.dsa.model.Attachment;
import com.abinbev.dsa.model.Contact;
import com.abinbev.dsa.model.Event;
import com.abinbev.dsa.model.User;
import com.abinbev.dsa.ui.presenter.MoreInfoPresenter;
import com.abinbev.dsa.utils.DateUtils;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * @author Jason Harris (jason@akta.com)
 */
public class MoreInfoView extends RelativeLayout implements MoreInfoPresenter.ViewModel {

    @Bind(R.id.imgHero)
    ImageView hero;
    @Bind(R.id.imgClose)
    ImageView close;
    @Bind(R.id.txtAccount)
    Button account;
    @Bind(R.id.txtContact)
    TextView contact;
    @Bind(R.id.imgMapMarker)
    ImageView pin;
    @Bind(R.id.imgPhone)
    ImageView imagePhone;
    @Bind(R.id.txtPhone)
    TextView phone;
    @Bind(R.id.txtNextVisit)
    TextView next;
    @Bind(R.id.txtLastVisit)
    TextView lastVisit;
    @Bind(R.id.txtOwner)
    TextView owner;
    @Bind(R.id.txtState)
    TextView state;
    @Bind(R.id.address_container)
    LinearLayout addressContainer;

    private MoreInfoPresenter presenter;
    private Uri imageUri;

    public MoreInfoView(Context context) {
        super(context);
        setUp(context);
    }

    public MoreInfoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setUp(context);
    }

    public MoreInfoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setUp(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public MoreInfoView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setUp(context);
    }

    private void setUp(Context context) {
        inflate(context, R.layout.merge_more_info, this);
        ButterKnife.bind(this);

        if (isInEditMode()) {
            return;
        }
        if (presenter == null) {
            presenter = new MoreInfoPresenter();
        }
        presenter.setViewModel(this);
        presenter.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        presenter.stop();
    }

    public void setEvent(Event event) {
        imageUri = null;
        Account account = event.getAccount();
        hero.setImageResource(R.drawable.default_account_background);
        contact.setText(null);
        lastVisit.setText(null);
        presenter.setViewModel(this);
        presenter.setEvent(event);
        presenter.getAccountPhoto(account.getId());
        if (account.getNextVisit() != null) {
            next.setText(account.getNextVisit().equals("null") ? "" : DateUtils.formatDateStringShort(account.getNextVisit()));
        } else {
            next.setText("");
        }
        state.setText(account.getAccountStatus());

        String phoneString = account.getFirstAvailablePhone();
        if (TextUtils.isEmpty(phoneString)) {
            phone.setVisibility(GONE);
            imagePhone.setVisibility(GONE);
        }
        else {
            phone.setVisibility(VISIBLE);
            imagePhone.setVisibility(VISIBLE);
            phone.setText(PhoneNumberUtils.formatNumber(phoneString));
        }
    }


    @Override
    public void setLastVisit(String date) {
        lastVisit.setText(DateUtils.formatDateStringShort(date));
    }

    @Override
    public void setPrimaryContact(Contact primaryContact) {
        contact.setText(primaryContact != null ? primaryContact.getName() : null);
    }

    @Override
    public void setOwner(User user) {
        if (user != null) {
            owner.setText(user.getName());
        }
    }

    @Override
    public void setAttachment(Attachment attachment, String accountId) {
        if (attachment != null) {
            this.imageUri = Uri.fromFile(new File(attachment.getFilePath(getContext(), accountId)));
        }
        setImageUri(imageUri);
    }

    public void setImageUri(Uri imageUri) {
        this.imageUri = imageUri;
        Picasso.with(getContext())
                .load(imageUri)
                .memoryPolicy(MemoryPolicy.NO_CACHE)
                .fit()
                .centerCrop()
                .placeholder(R.drawable.default_account_background)
                .error(R.drawable.default_account_background)
                .into(hero);
    }
}

