package com.abinbev.dsa.adapter;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.abinbev.dsa.R;
import com.abinbev.dsa.model.CN_SPR__c;
import com.abinbev.dsa.utils.DateUtils;

import java.util.List;

import io.mewa.adapterodactil.annotations.Adapt;
import io.mewa.adapterodactil.annotations.Data;
import io.mewa.adapterodactil.annotations.Row;
import io.mewa.adapterodactil.annotations.ViewType;

/**
 * Created by Adam Chodera on 10.07.2017.
 */

@Adapt(layout = R.layout.spr_item, viewGroup = R.id.spr_item_container, type = CN_SPR__c.class)
public abstract class SprAdapter<T extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<T> {

    public static final int SPR_INFO_TYPE = 0;

    public static class ViewTyped extends SprAdapterImpl {
        @Override
        public int getItemViewType(int position) {
            return SPR_INFO_TYPE;
        }
    }

    @ViewType(SPR_INFO_TYPE)
    public static class SprViewAdapter {

        @Row(num = 0, dataId = R.id.spr_item_work_date)
        public static String workDate(TextView view, CN_SPR__c s) {
            String date = s.getWorkDate();
            if (TextUtils.isEmpty(date)) { // not yet synced
                return String.format("<%s>", view.getContext().getResources().getString(R.string.pending).toLowerCase());
            }
            return DateUtils.formatDateStringShort(date);
        }

        @Row(num = 1, dataId = R.id.spr_item_name)
        public static String name(TextView view, CN_SPR__c s) {
            String name = s.getName();
            view.setVisibility(name != null ? View.VISIBLE : View.GONE);
            return name;
        }

        @Row(num = 2, dataId = R.id.spr_item_working_hour)
        public static String workingHours(TextView view, CN_SPR__c s) {
            String workingFrom = s.getWorkingFrom();
            view.setVisibility(workingFrom != null ? View.VISIBLE : View.GONE);
            String workingTo = s.getWorkingTo();
            final String workingHours = DateUtils.formatDateHoursMinutesAMPM(workingFrom) + " - " + DateUtils.formatDateHoursMinutesAMPM(workingTo);
            final String label = view.getContext().getResources().getString(R.string.spr_working_hours);
            return label + " " + workingHours;
        }

        @Row(num = 3, dataId = R.id.spr_item_contact_phone)
        public static String contactPhone(TextView view, CN_SPR__c s) {
            String contactPhone = s.getContactPhone();
            view.setVisibility(contactPhone != null ? View.VISIBLE : View.GONE);

            final String label = view.getContext().getResources().getString(R.string.spr_contact_phone);
            return label + " " + contactPhone;
        }

        @Row(num = 4, dataId = R.id.spr_item_temporary_promo)
        public static String isTemporaryPromo(TextView view, CN_SPR__c s) {
            boolean isPromo = s.isTemporaryPromo();
            view.setVisibility(View.VISIBLE);

            final String label = view.getContext().getResources().getString(R.string.spr_temporary_promo);
            return label + " " + isPromo;
        }
    }

    @Data
    public abstract void setData(List<CN_SPR__c> data);
}
