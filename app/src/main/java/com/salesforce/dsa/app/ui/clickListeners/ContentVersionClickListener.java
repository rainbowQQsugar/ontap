package com.salesforce.dsa.app.ui.clickListeners;

import android.view.View;
import android.widget.AdapterView;

import com.salesforce.dsa.app.utils.ContentUtils;
import com.salesforce.dsa.data.model.ContentVersion;

/**
 * @author nickc (nick.c@akta.com).
 */
public class ContentVersionClickListener implements AdapterView.OnItemClickListener {

    public ContentVersionClickListener() {
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Object object = parent.getAdapter().getItem(position);
        if (object instanceof ContentVersion) {
            ContentVersion cv = (ContentVersion) object;
//            ContentUtils.openContentVersion(parent.getContext(), cv);
        }
    }

}
