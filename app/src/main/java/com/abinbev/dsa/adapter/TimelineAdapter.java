package com.abinbev.dsa.adapter;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.abinbev.dsa.R;
import com.abinbev.dsa.model.Event;
import com.abinbev.dsa.model.Note;
import com.abinbev.dsa.utils.DateUtils;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

import io.mewa.adapterodactil.annotations.Adapt;
import io.mewa.adapterodactil.annotations.Data;
import io.mewa.adapterodactil.annotations.Row;
import io.mewa.adapterodactil.annotations.ViewType;

/**
 * Created by mewa on 6/21/17.
 */

@Adapt(layout = R.layout.timeline_item, viewGroup = R.id.timeline_item_container, type = TimelineAdapter.TimelineItem.class)
public abstract class TimelineAdapter<T extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<T> {

    public static final int NOTE_VIEW_TYPE = 0xbad;
    public static final int VISITS_VIEW_TYPE = 0xbaad;
    public static final int CHATTER_VIEW_TYPE = 0xbaaad;

    public static class ViewTyped extends TimelineAdapterImpl {
        @Override
        public int getItemViewType(int position) {
            return getStored_stored_data().get(position).getType();
        }
    }

    public interface TimelineItem<U> {
        String getId();

        int getType();

        long getTime();

        String getUser();

        U getItem();
    }

    @ViewType(NOTE_VIEW_TYPE)
    public static class NotesViewAdapter {

        @Row(num = 0, dataId = R.id.timeline_item_date)
        public static String date(TextView view, TimelineItem<Note> s) {
            String date = s.getItem().getCreatedDate();
            if (TextUtils.isEmpty(date)) { // not yet synced
                return String.format("<%s>", view.getContext().getResources().getString(R.string.pending).toLowerCase());
            }
            return DateUtils.formatDateStringShort(date);
        }

        @Row(num = 1, dataId = R.id.timeline_item_title)
        public static String owner(TextView view, TimelineItem<Note> s) {
            String user = s.getUser();
            view.setVisibility(user != null ? View.VISIBLE : View.GONE);
            return user;
        }

        @Row(num = 2, dataId = R.id.timeline_item_content)
        public static String content(TextView view, TimelineItem<Note> s) {
            return s.getItem().getBody();
        }

    }

    @ViewType(VISITS_VIEW_TYPE)
    public static class ChatterViewAdapter {

        @Row(num = 0, dataId = R.id.timeline_item_date)
        public static String date(TextView view, TimelineItem<Event> event) {
            String date = event.getItem().getCreatedDate();
            String controlDate = DateUtils.formatDateTimeShort(event.getItem().getControlStartDateTime());
            if (TextUtils.isEmpty(date)) { // not yet synced
                return String.format("%s <%s>", controlDate, view.getContext().getResources().getString(R.string.pending).toLowerCase());
            }
            return controlDate;
        }

        @Row(num = 1, dataId = R.id.timeline_item_title)
        public static String owner(TextView view, TimelineItem<Event> event) {
            return event.getUser();
        }

        @Row(num = 2, dataId = R.id.timeline_item_content)
        public static String content(TextView view, TimelineItem<Event> event) {
            Date start, end;
            try {
                start = DateUtils.SERVER_DATE_TIME_FORMAT.parse(event.getItem().getControlStartDateTime());
                end = new Date();

                if (event.getItem().getControlEndDateTime() != null) {
                    try {
                        end = DateUtils.SERVER_DATE_TIME_FORMAT.parse(event.getItem().getControlEndDateTime());
                    } catch (ParseException ignored) {
                    }
                }

                int minutes = (int) DateUtils.differenceDates(start, end, DateUtils.TimeUnits.MINUTES);
                String timeText = view.getContext().getResources().getQuantityString(R.plurals.minute, minutes, minutes);
                String comment = event.getItem().getCheckOutComment();

                return TextUtils.isEmpty(comment) ? timeText : timeText + '\n' + comment;
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return null;
        }

    }

    @Data
    public abstract void setData(List<TimelineItem> data);
}
