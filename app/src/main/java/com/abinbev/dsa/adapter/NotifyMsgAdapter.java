package com.abinbev.dsa.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.abinbev.dsa.R;
import com.abinbev.dsa.model.CN_Notification_Message__c;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Message adapter
 */
public class NotifyMsgAdapter extends BaseAdapter {

    private static String New_Task = "New Task";
    private static String System_Notification = "System Notification";
    private static String Contract_Renewal = "Contract Renewal";

    private DeleteMessageListener listener;

    public void setListener(DeleteMessageListener listener) {
        this.listener = listener;
    }

    public interface DeleteMessageListener {
        boolean onDeleteMessageListener(CN_Notification_Message__c message);
    }

    /**
     * Item type,int value. Must be incremented from 0.
     */

    public static final int TYPE_DEFAULT = -1;
    public static final int TYPE_TITLE = 0;

    public static final int TYPE_CONTENT = 1;

    public static final int TYPE_NEW_TASK = 0;

    public static final int TYPE_SYSTEM_NOTIFICATION = 1;

    public static final int TYPE_CONTRACT_RENEWAL = 2;

    /**
     * Item Type count
     */
    private static final int TYPE_ITEM_COUNT = 2;
    private Context context;
    private List<CN_Notification_Message__c> message__cs;
    private LayoutInflater inflater;
    private String TAG = this.getClass().getSimpleName();

    public NotifyMsgAdapter(Context context, List<CN_Notification_Message__c> message__cs) {

        this.message__cs = message__cs;
        this.context = context;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return message__cs == null ? 0 : message__cs.size();
    }

    @Override
    public CN_Notification_Message__c getItem(int position) {
        return message__cs.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position).getItemId();
    }

    @Override
    public int getViewTypeCount() {
        return TYPE_ITEM_COUNT;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolderHead viewHolderHead = null;
        ViewHolderContent viewHolder = null;

        try {
            switch (getItemViewType(position)) {

                case TYPE_TITLE:
                    if (viewHolderHead == null) {
                        convertView = inflater.inflate(R.layout.item_notify_message_title, parent, false);
                        viewHolderHead = new ViewHolderHead(convertView);
                        convertView.setTag(viewHolderHead);
                    } else {
                        viewHolderHead = (ViewHolderHead) convertView.getTag();
                    }
                    CN_Notification_Message__c message__c = (CN_Notification_Message__c) getItem(position);

                    if (message__c != null) {
                        viewHolderHead.typeTitle.setText(titleType(message__c.getCategory()));
                    }
                    break;
                case TYPE_CONTENT:
                    if (convertView == null) {
                        convertView = inflater.inflate(R.layout.item_notify_message_content, parent, false);
                        viewHolder = new ViewHolderContent(convertView);
                        convertView.setTag(viewHolder);
                    } else {

                        viewHolder = (ViewHolderContent) convertView.getTag();
                    }

                    CN_Notification_Message__c message = (CN_Notification_Message__c) getItem(position);

                    if (message != null) {

                        if (message.getTypeId() == TYPE_CONTENT) {
                            viewHolder.alarmClockLayout.setVisibility(View.GONE);
                        } else {
                            viewHolder.alarmClockLayout.setVisibility(View.VISIBLE);
                        }

                        viewHolder.title.setText("" + message.getTitle());
                        viewHolder.otherTitle.setText("" + message.getDescription());
                        viewHolder.timer.setText("" + message.getNotifyTimeFormat());
                        viewHolder.alarmClockTimer.setText("" + message.getDueDateFormat());
                        viewHolder.deleteButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                if (listener != null) {
                                    listener.onDeleteMessageListener(message);
                                }
                            }
                        });
                    }
                    break;
            }

        } catch (Exception e) {
            Log.e(TAG, "getView Error;" + e.getMessage());
        }
        return convertView;
    }

    static class ViewHolderContent {


        @Bind(R.id.timer)
        TextView timer;
        @Bind(R.id.title)
        TextView title;
        @Bind(R.id.other_title)
        TextView otherTitle;
        @Bind(R.id.alarm_clock)
        TextView alarmClock;
        @Bind(R.id.alarm_clock_timer)
        TextView alarmClockTimer;
        @Bind(R.id.alarm_clock_layout)
        RelativeLayout alarmClockLayout;
        @Bind(R.id.delete)
        ImageView delete;
        @Bind(R.id.delete_button)
        RelativeLayout deleteButton;


        public ViewHolderContent(View view) {

            ButterKnife.bind(this, view);

        }
    }

    static class ViewHolderHead {

        @Bind(R.id.type_title)
        TextView typeTitle;


        public ViewHolderHead(View view) {
            ButterKnife.bind(this, view);
        }
    }

    private String titleType(String typeName) {

        String result = "";
        if (!TextUtils.isEmpty(typeName)) {

            if (New_Task.equalsIgnoreCase(typeName)) {
                result = context.getResources().getString(R.string.notify_new_task);
            } else if (System_Notification.equalsIgnoreCase(typeName)) {
                result = context.getResources().getString(R.string.notify_system_notifycation);
            } else if (Contract_Renewal.equalsIgnoreCase(typeName)) {
                result = context.getResources().getString(R.string.notify_contract_renewal_);
            }

        }
        return result;
    }


}
