package com.abinbev.dsa.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.abinbev.dsa.R;
import com.abinbev.dsa.model.Comentario_caso_force__c;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class CommentAdapter extends RecyclerView.Adapter {

    private List<Comentario_caso_force__c> comments;

    public CommentAdapter() {
        super();
        this.comments= new ArrayList<>();
    }

    public void setData(List<Comentario_caso_force__c> comments) {
        this.comments.clear();
        this.comments.addAll(comments);
        this.notifyDataSetChanged();
        this.notifyItemRangeChanged(0, comments.size());
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @Bind(R.id.comment)
        public TextView comment;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_card, parent, false);
        itemView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((ViewHolder) holder).comment.setText(comments.get(position).getComment());
    }

    public void addComment(Comentario_caso_force__c comment) {
        comments.add(0, comment);
        notifyItemInserted(0);
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }
}