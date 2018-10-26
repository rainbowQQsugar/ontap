package com.abinbev.dsa.adapter;

import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.TextView;

import com.abinbev.dsa.R;
import com.abinbev.dsa.model.Event;
import com.abinbev.dsa.model.Task;
import com.abinbev.dsa.utils.DateUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by bduggirala
 */
public class EventPlanningListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    private static final int TYPE_HEADER = 0;
    private static final int TYPE_EVENT_ITEM = 1;
    private static final int TYPE_TASK_ITEM = 2;

    public interface CalendarEventListener {
        void onEventClick(Event event);

        void onTaskClick(Task task);

        void onDateSelected(Date newDate);
    }

    private final SimpleDateFormat dayOfMonthFormat;

    private final SimpleDateFormat dayOfWeekFormat;
    private final SimpleDateFormat dateFormat;

    private CalendarEventListener listener;

    List<Event> eventList;
    List<Task> taskList;

    public EventPlanningListAdapter() {
        super();
        eventList = new ArrayList<>();
        taskList = new ArrayList<>();
        dayOfMonthFormat = new SimpleDateFormat("d");
        dayOfWeekFormat = new SimpleDateFormat("EE");
        dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType == TYPE_EVENT_ITEM) {

            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View itemView = inflater.inflate(R.layout.event_planning_item_view, parent, false);
            return new EventViewHolder(itemView);

        } else if (viewType == TYPE_HEADER) {
            //inflate your layout and pass it to view holder
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View itemView = inflater.inflate(R.layout.event_planning_calendar_header, parent, false);
            return new CalendarViewHolder(itemView);
        } else if (viewType == TYPE_TASK_ITEM) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View itemView = inflater.inflate(R.layout.task_planning_item_view, parent, false);
            return new TaskViewHolder(itemView);
        }

        throw new RuntimeException("there is no type that matches the type " + viewType + " + make sure your using types correctly");

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof EventViewHolder) {
            final Event event = eventList.get(position - 1);
            Date activityDate = event.getStartDateTime();

//            if (activityDate != null) {
//                String day = dayOfMonthFormat.format(activityDate);
//                String dayOfWeek = dayOfWeekFormat.format(activityDate);
//                ((EventViewHolder) holder).eventDay.setText(day);
//                ((EventViewHolder) holder).eventDayOfWeek.setText(dayOfWeek);
//            }
            Resources resources = holder.itemView.getResources();
            if (isDailyVisit(event)) {
                ((EventViewHolder) holder).eventSubject.setText(event.getAccount().getName());
            } else {
                ((EventViewHolder) holder).eventSubject.setText(resources.getString(R.string.weekly_visit) + " - " + event.getAccount().getName());
            }


        } else if (holder instanceof CalendarViewHolder) {

        } else if (holder instanceof TaskViewHolder) {
            if (position > eventList.size()) {
                Task task = taskList.get(position - eventList.size() - 1);
                ((TaskViewHolder) holder).task.setText(task.getSubject() + " - " + task.getDueDate());
            }
        }
    }

    public List<Event> getEventList() {
        return eventList;
    }

    @Override
    public int getItemCount() {
        return eventList.size() + 1 + taskList.size();
    }

    public void setListener(CalendarEventListener listener) {
        this.listener = listener;
    }

    public Event getEventItem(int position) {
        return eventList.get(position - 1);
    }

    public Task getTaskItem(int position) {
        int taskPosition = position - eventList.size();
        return taskList.get(taskPosition - 1);
    }

    public void setEventData(List<Event> eventList) {
        this.eventList.clear();
        this.eventList.addAll(eventList);
        this.notifyDataSetChanged();
    }

    public void setTaskData(List<Task> taskList) {
        this.taskList.clear();
        this.taskList.addAll(taskList);
        this.notifyDataSetChanged();
    }

    public boolean isDailyVisit(Event event) {
        String startDate = dateFormat.format(event.getStartDateTime());
        String endDate = dateFormat.format(event.getEndDateTime());
        return startDate.equals(endDate);
    }

    @Override
    public int getItemViewType(int position) {
        if (isPositionHeader(position)) {
            return TYPE_HEADER;
        } else if (position <= eventList.size()) {
            return TYPE_EVENT_ITEM;
        } else {
            return TYPE_TASK_ITEM;
        }
    }

    private boolean isPositionHeader(int position) {
        return position == 0;
    }

    class EventViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

//        @Bind(R.id.event_day)
//        TextView eventDay;
//
//        @Bind(R.id.event_day_of_week)
//        TextView eventDayOfWeek;

        @Bind(R.id.event_subject)
        TextView eventSubject;

        public EventViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (listener != null) {
                int position = getAdapterPosition();
                // -1 indicates NO_POSITION
                if (position != -1) {
                    listener.onEventClick(getEventItem(position));
                }
            }
        }
    }

    class TaskViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

//        @Bind(R.id.event_day)
//        TextView eventDay;
//
//        @Bind(R.id.event_day_of_week)
//        TextView eventDayOfWeek;

        @Bind(R.id.task)
        TextView task;

        public TaskViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (listener != null) {
                int position = getAdapterPosition();
                // -1 indicates NO_POSITION
                if (position != -1) {
                    listener.onTaskClick(getTaskItem(position));
                }
            }
        }
    }

    class CalendarViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.calendarView)
        CalendarView calendarView;

        public CalendarViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);

            Long initialDate = calendarView.getDate();
            calendarView.setMinDate(initialDate);

            Calendar today = Calendar.getInstance();
            today.add(Calendar.MONTH, 3); // 3 months
            calendarView.setMaxDate(today.getTimeInMillis());

            calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
                public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                    Log.e("Babu", "in onSelectedDayChange");

                    month = month + 1;
                    String newDateString = year + "-" + month + "-" + dayOfMonth;
                    Date newDate = DateUtils.dateFromString(newDateString);
                    listener.onDateSelected(newDate);
                }
            });
        }
    }
}
