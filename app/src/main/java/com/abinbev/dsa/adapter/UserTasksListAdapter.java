package com.abinbev.dsa.adapter;

import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.TextView;

import com.abinbev.dsa.R;
import com.abinbev.dsa.model.Account;
import com.abinbev.dsa.model.Task;
import com.abinbev.dsa.utils.DateUtils;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import butterknife.Bind;

public class UserTasksListAdapter extends AbstractTasksListAdapter {

    public UserTasksListAdapter() {
        super();
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.user_task_list_entry_view;
    }

    @Override
    protected UserViewHolder createViewHolder(View view) {
        return new UserViewHolder(view);
    }

    @Override
    protected void bind(Task task, AbstractTasksListAdapter.ViewHolder vh) {
        UserViewHolder userVH = (UserViewHolder) vh;
        Account account = task.getAccount();
        userVH.subject.setText(task.getTranslatedSubject());
        userVH.dueDate.setText(task.getDueDate());
        userVH.state.setText(task.getTranslatedState());
        userVH.account.setText(account == null ? null : account.getName());
        if (isShowExpired(task.getDueDate())) {
            userVH.expired.setVisibility(View.VISIBLE);
            userVH.expired.setText(R.string.expired);
        } else if (isToBeExpired(task.getDueDate()) && !task.getState().equals("Completed")) {
            userVH.expired.setVisibility(View.VISIBLE);
            userVH.expired.setText(R.string.to_be_expired);
        } else {
            userVH.expired.setVisibility(View.GONE);
        }
        if (Task.STATE_COMPLETED.equals(task.getState())) {
            userVH.cardView.setBackgroundColor(userVH.cardView.getContext().getResources().getColor(R.color.sab_lightest_gray));
        } else {
            userVH.cardView.setBackgroundColor(userVH.cardView.getContext().getResources().getColor(R.color.sab_white));
        }
    }

    public void sortByAccount(final boolean ascending) {
        Collections.sort(tasks, new Comparator<Task>() {
            @Override
            public int compare(Task lhs, Task rhs) {
                String leftOwner = lhs.getOwnerName();
                String rightOwner = rhs.getOwnerName();

                if (leftOwner == null) {
                    leftOwner = "";
                }

                if (rightOwner == null) {
                    rightOwner = "";
                }

                if (ascending) {
                    return leftOwner.compareTo(rightOwner);
                } else {
                    return rightOwner.compareTo(leftOwner);
                }
            }
        });
        this.notifyDataSetChanged();
    }

    public boolean isShowExpired(String dueDateString) {
        String currentDateString = DateUtils.currentDateString();
        Date dueDate = DateUtils.dateFromString(dueDateString);
        Date currentDate = DateUtils.dateFromString(currentDateString);
        return dueDate.before(currentDate);
    }

    public boolean isToBeExpired(String dueDateString) {
        String currentDateString = DateUtils.currentDateString();
        Date dueDate = DateUtils.dateFromString(dueDateString);
        Date currentDate = DateUtils.dateFromString(currentDateString);
        Date twoDaysLaterDate = DateUtils.twoDaysLater();
        return ((dueDate.getTime() <= twoDaysLaterDate.getTime()) && (dueDate.getTime() >= currentDate.getTime()));
    }

    class UserViewHolder extends ViewHolder {

        @Bind(R.id.account)
        TextView account;

        @Bind(R.id.expired)
        TextView expired;

        @Bind(R.id.card_view)
        CardView cardView;

        public UserViewHolder(View convertView) {
            super(convertView);
        }
    }
}
