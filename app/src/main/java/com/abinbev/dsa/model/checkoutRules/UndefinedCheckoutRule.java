package com.abinbev.dsa.model.checkoutRules;

import android.content.Context;

/**
 * Created by Jakub Stefanowski on 04.08.2017.
 */

class UndefinedCheckoutRule extends CheckoutRule {

    UndefinedCheckoutRule() { }

    @Override
    public boolean isFulfilled() {
        return true;
    }

    @Override
    public void openScreen(Context context) {
        // empty
    }
}
