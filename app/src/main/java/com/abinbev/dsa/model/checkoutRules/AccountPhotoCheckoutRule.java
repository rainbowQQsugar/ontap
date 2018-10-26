package com.abinbev.dsa.model.checkoutRules;

import android.content.Context;
import android.content.Intent;

import com.abinbev.dsa.model.Account;
import com.abinbev.dsa.model.Attachment;

import java.io.File;

import static com.abinbev.dsa.utils.AzureUtils.getAzurePhotoFileForAccount;

/**
 * Created by Jakub Stefanowski on 04.08.2017.
 */

public class AccountPhotoCheckoutRule extends CheckoutRule {

    public static final String ACTION_TAKE_PHOTO = "com.abinbev.dsa.model.checkoutRules.AccountPhotoCheckoutRule.TAKE_PHOTO";

    private final Account account;

    AccountPhotoCheckoutRule(Account account) {
        this.account = account;
        setInfoForUser("Prospect requires a photo.");
    }

    @Override
    public boolean isFulfilled() {
        if (!account.isProspect()) return true;

        Attachment attachment = Attachment.getAccountPhotoAttachment(account.getId());
        return attachment != null || hasAzurePhoto(account.getId());
    }

    private boolean hasAzurePhoto(String accountId) {
        File azurePhotoForAccount = getAzurePhotoFileForAccount(accountId);
        return azurePhotoForAccount.exists();
    }

    @Override
    public void openScreen(Context context) {
        context.sendBroadcast(new Intent(ACTION_TAKE_PHOTO));
    }
}
