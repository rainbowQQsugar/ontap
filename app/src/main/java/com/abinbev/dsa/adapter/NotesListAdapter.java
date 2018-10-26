package com.abinbev.dsa.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.abinbev.dsa.R;
import com.abinbev.dsa.model.Note;
import com.abinbev.dsa.model.User;
import com.abinbev.dsa.utils.DateUtils;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;

public class NotesListAdapter extends BaseAdapter {

    public interface NoteClickHandler {
        void onNoteClick(String noteId);
    }

    private final Map<String, User> notesCreators;
    private final List<Note> notes;
    private final NoteClickHandler noteClickHandler;

    public NotesListAdapter(NoteClickHandler noteClickHandler) {
        super();
        this.notes = new ArrayList<>();
        this.notesCreators = new HashMap<>();
        this.noteClickHandler = noteClickHandler;
    }

    @Override
    public int getCount() {
        return notes.size();
    }

    @Override
    public Object getItem(int position) {
        return notes.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            convertView = inflater.inflate(R.layout.notes_list_item, parent, false);

            convertView.setTag(new ViewHolder(convertView));
        }

        final Note note = notes.get(position);
        final User user = notesCreators.get(note.getCreatedById());

        final String userName = user == null ? null : user.getName();
        final String pendientePlaceholder = parent.getContext().getString(R.string.pendiente);

        ViewHolder viewHolder = (ViewHolder) convertView.getTag();
        String createdDate = DateUtils.formatDateTimeShort(note.getCreatedDate());
        viewHolder.date.setText(TextUtils.isEmpty(createdDate) ? pendientePlaceholder : createdDate);
        viewHolder.title.setText(join(userName, note.getTitle(), " - "));
        viewHolder.comment.setText(note.getBody());

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noteClickHandler.onNoteClick(note.getId());
            }
        });

        return convertView;
    }

    private static String join(String a, String b, String delimiter) {
        if (TextUtils.isEmpty(a)) {
            return b;
        }
        else if (TextUtils.isEmpty(b)) {
            return a;
        }
        else {
            return a + delimiter + b;
        }
    }

    public void setData(List<Note> notes) {
        this.notes.clear();
        this.notes.addAll(notes);
        this.notifyDataSetChanged();
    }

    public void setData(List<Note> notes, Map<String, User> notesCreators) {
        this.notes.clear();
        this.notes.addAll(notes);
        this.notesCreators.clear();
        this.notesCreators.putAll(notesCreators);
        this.notifyDataSetChanged();
    }

   public void sortByDate(final boolean ascending) {
       Collections.sort(notes, new Comparator<Note>() {
           @Override
           public int compare(Note lhs, Note rhs) {
               if (ascending) {
                   if (!TextUtils.isEmpty(lhs.getCreatedDate()) && !TextUtils.isEmpty(rhs.getCreatedDate())) {
                       return DateUtils.dateFromDateTimeString(lhs.getCreatedDate())
                                       .compareTo(DateUtils.dateFromDateTimeString(rhs.getCreatedDate()));
                   } else if (TextUtils.isEmpty(lhs.getCreatedDate()) && TextUtils.isEmpty(rhs.getCreatedDate())) {
                       return 0;
                   } else if (TextUtils.isEmpty(lhs.getCreatedDate())) {
                       return -1;
                   } else {
                       return 1;
                   }

               } else {
                   if (!TextUtils.isEmpty(lhs.getCreatedDate()) && !TextUtils.isEmpty(rhs.getCreatedDate())) {
                       return DateUtils.dateFromDateTimeString(rhs.getCreatedDate())
                                       .compareTo(DateUtils.dateFromDateTimeString(lhs.getCreatedDate()));
                   } else if (TextUtils.isEmpty(lhs.getCreatedDate()) && TextUtils.isEmpty(rhs.getCreatedDate())) {
                       return 0;
                   } else if (TextUtils.isEmpty(rhs.getCreatedDate())) {
                       return -1;
                   } else {
                       return 1;
                   }
               }
           }
       });
       this.notifyDataSetChanged();
    }

    public void sortByTitle(final boolean ascending) {
        Collections.sort(notes, new Comparator<Note>() {
            @Override
            public int compare(Note lhs, Note rhs) {
                if (ascending) {
                    return lhs.getTitle().compareTo(rhs.getTitle());
                } else {
                    return rhs.getTitle().compareTo(lhs.getTitle());
                }
            }
        });
        this.notifyDataSetChanged();
    }

    public void sortByComment(final boolean ascending) {
        Collections.sort(notes, new Comparator<Note>() {
            @Override
            public int compare(Note lhs, Note rhs) {
                if (ascending) {
                    return lhs.getBody().compareTo(rhs.getBody());
                } else {
                    return rhs.getBody().compareTo(lhs.getBody());
                }
            }
        });
        this.notifyDataSetChanged();
    }


    class ViewHolder {

        @Bind(R.id.txt_date)
        TextView date;

        @Bind(R.id.txt_title)
        TextView title;

        @Bind(R.id.txt_comment)
        TextView comment;

        public ViewHolder(View convertView) {
            ButterKnife.bind(this, convertView);
        }

    }
}
