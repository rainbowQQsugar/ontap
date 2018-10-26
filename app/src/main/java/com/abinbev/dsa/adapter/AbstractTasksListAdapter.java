package com.abinbev.dsa.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.abinbev.dsa.R;
import com.abinbev.dsa.model.Task;
import com.abinbev.dsa.utils.CollectionUtils;
import com.abinbev.dsa.utils.ContentUtils;
import com.abinbev.dsa.utils.DateUtils;
import com.android.internal.util.Predicate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by lukaszwalukiewicz on 12.01.2016.
 */
public abstract class AbstractTasksListAdapter extends BaseAdapter {
    List<Task> tasks;
    List<Task> totalTasks;
    List<Task> filterCompletedTasks;
    String currentSubject;
    String currentState;

    public AbstractTasksListAdapter() {
        super();
        this.tasks = new ArrayList<>();
        this.totalTasks = new ArrayList<>();
        this.currentSubject = null;
        this.currentState = null;
    }

    @Override
    public int getCount() {
        return tasks.size();
    }

    @Override
    public Task getItem(int position) {
        return tasks.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            convertView = inflater.inflate(getLayoutRes(), parent, false);
            convertView.setTag(createViewHolder(convertView));
        }

        final Task task = tasks.get(position);
        ViewHolder viewHolder = (ViewHolder) convertView.getTag();
        bind(task, viewHolder);

        return convertView;
    }

    protected abstract int getLayoutRes();

    protected abstract ViewHolder createViewHolder(View view);

    protected abstract void bind(Task task, ViewHolder vh);

    public void setData(List<Task> tasks) {
        this.totalTasks.clear();
        this.totalTasks.addAll(tasks);
        this.tasks = this.totalTasks;
        this.notifyDataSetChanged();
    }

//    protected List<Task> filterCompletedTasks() {
//        List<Task> leftList = new ArrayList<>();
//        leftList = tasks;
//        Collections.copy(leftList, tasks);
//        Iterator<Task> taskIterator = leftList.iterator();
//        while (taskIterator.hasNext()) {
//            if (taskIterator.next().getState().equals("Completed")) {
//                taskIterator.remove();
//            }
//        }
//        return leftList;
//    }

    public void sortBySubject(final boolean ascending) {
        Collections.sort(tasks, new Comparator<Task>() {
            @Override
            public int compare(Task lhs, Task rhs) {
                if (ascending) {
                    return lhs.getTranslatedSubject().compareTo(rhs.getTranslatedSubject());
                } else {
                    return rhs.getTranslatedSubject().compareTo(lhs.getTranslatedSubject());
                }
            }
        });
        this.notifyDataSetChanged();
    }

    public void sortByState(final boolean ascending) {
        Collections.sort(tasks, new Comparator<Task>() {
            @Override
            public int compare(Task lhs, Task rhs) {
                if (ascending) {
                    return lhs.getTranslatedState().compareTo(rhs.getTranslatedState());
                } else {
                    return rhs.getTranslatedState().compareTo(lhs.getTranslatedState());
                }
            }
        });
        this.notifyDataSetChanged();
    }

    public void sortByDueDate(final boolean ascending) {
        Collections.sort(tasks, new Comparator<Task>() {
            @Override
            public int compare(Task lhs, Task rhs) {
                if (ascending) {
                    if (!ContentUtils.isStringValid(lhs.getDueDate()) && !ContentUtils.isStringValid(rhs.getDueDate())) {
                        return 0;
                    } else if (!ContentUtils.isStringValid(lhs.getDueDate())) {
                        return -1;
                    } else if (!ContentUtils.isStringValid(rhs.getDueDate())) {
                        return 1;
                    }
                    return DateUtils.dateFromString(lhs.getDueDate()).compareTo(DateUtils.dateFromString(rhs.getDueDate()));
                } else {
                    if (!ContentUtils.isStringValid(lhs.getDueDate()) && !ContentUtils.isStringValid(lhs.getDueDate())) {
                        return 0;
                    } else if (!ContentUtils.isStringValid(rhs.getDueDate())) {
                        return -1;
                    } else if (!ContentUtils.isStringValid(lhs.getDueDate())) {
                        return 1;
                    }
                    return DateUtils.dateFromString(rhs.getDueDate()).compareTo(DateUtils.dateFromString(lhs.getDueDate()));
                }
            }
        });
        this.notifyDataSetChanged();
    }

    private void filterTasks(boolean isFilterStated) {
        Predicate<Task> predicate = new Predicate<Task>() {
            public boolean apply(Task task) {
                if (showAllValues() && isFilterStated) {
                    return !task.getState().equalsIgnoreCase("Completed");
                } else if (showAllValues()) {
                    return true;
                }
                if (showAllSubjects()) {
                    return task.getState().equalsIgnoreCase(currentState);
                }
                if (showAllStates()) {
                    return task.getSubject().equalsIgnoreCase(currentSubject);
                }

                return task.getSubject().equalsIgnoreCase(currentSubject) && task.getState().equalsIgnoreCase(currentState);
            }
        };
        this.tasks = CollectionUtils.filter(this.totalTasks, predicate);
    }

    public void filterBySubject(final String subject) {
        this.currentSubject = subject;
        filterTasks(false);
        this.notifyDataSetChanged();
    }

    public void filterExceptCompletedTasks() {
        filterTasks(true);
        this.notifyDataSetChanged();
    }

    public void filterByState(final String state) {
        this.currentState = state;
        if (state == null && totalTasks.size() > 20) {
            filterTasks(true);
        } else {
            filterTasks(false);
        }
        this.notifyDataSetChanged();
    }

    private boolean showAllValues() {
        return showAllSubjects() && showAllStates();
    }

    private boolean showAllSubjects() {
        return currentSubject == null;
    }

    private boolean showAllStates() {
        return currentState == null;
    }

    protected class ViewHolder {
        @Bind(R.id.subject)
        TextView subject;

        @Bind(R.id.dueDate)
        TextView dueDate;

        @Bind(R.id.state)
        TextView state;

        public ViewHolder(View convertView) {
            ButterKnife.bind(this, convertView);
        }
    }
}
