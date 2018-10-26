package com.salesforce.dsa.data.model;

import android.content.Context;

import com.salesforce.androidsdk.app.SalesforceSDKManager;
import com.salesforce.androidsdk.rest.ClientManager;
import com.salesforce.androidsdk.rest.RestClient;
import com.salesforce.androidsyncengine.datamanager.soql.QueryOp;
import com.salesforce.androidsyncengine.syncmanifest.FilterObject;
import com.salesforce.dsa.utils.DSAConstants.DSAObjects;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by usanaga on 10/4/15.
 */
public class User extends SFBaseObject {

    public User() {
        super(DSAObjects.USER);
    }

    @Override
    public List<FilterObject> getAdditionalFilters(Context context) {
        ClientManager clientManager = new ClientManager(context, SalesforceSDKManager.getInstance().getAccountType(),
                SalesforceSDKManager.getInstance().getLoginOptions(),
                SalesforceSDKManager.getInstance().shouldLogoutWhenTokenRevoked());
        RestClient client = clientManager.peekRestClient();

        FilterObject filterObject = new FilterObject();
        filterObject.setField("Id");
        filterObject.setValue(client.getClientInfo().userId);
        filterObject.setOp(QueryOp.eq);

        ArrayList<FilterObject> filterObjects = new ArrayList<FilterObject>();
        filterObjects.add(filterObject);
        return filterObjects;
    }
}
