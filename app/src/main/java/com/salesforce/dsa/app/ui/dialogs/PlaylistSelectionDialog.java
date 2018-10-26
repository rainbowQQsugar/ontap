package com.salesforce.dsa.app.ui.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.salesforce.dsa.app.R;
import com.salesforce.dsa.app.utils.DataUtils;
import com.salesforce.dsa.data.model.DSA_Playlist__c;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wandersonblough on 10/9/15.
 */
public class PlaylistSelectionDialog extends DialogFragment {

    public static final String ARGS_PLAYLIST_IDS = "args_playlist_ids";

    private List<String> selectedPlaylistIds;
    private PlayListAdapter playListAdapter;

    public PlaylistSelectionDialog() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            selectedPlaylistIds = getArguments().getStringArrayList(ARGS_PLAYLIST_IDS);
        }
        playListAdapter = new PlayListAdapter(DataUtils.fetchAllPlayLists(), selectedPlaylistIds);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());

        ListView listView = new ListView(getActivity());
        listView.setAdapter(playListAdapter);

        View addPlayListView = LayoutInflater.from(getActivity()).inflate(R.layout.add_playlist, listView, false);
        addPlayListView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCreatePlaylistDialog(view.getContext());
            }
        });

        listView.addFooterView(addPlayListView);

        dialogBuilder
                .setTitle(R.string.set_playlist)
                .setCancelable(true)
                .setPositiveButton(R.string.set, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //TODO Change Data
                    }
                })
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .setView(listView);

        return dialogBuilder.create();
    }

    public static PlaylistSelectionDialog instance(List<String> playlistIds) {
        PlaylistSelectionDialog selectionDialog = new PlaylistSelectionDialog();
        Bundle args = new Bundle();
        args.putStringArrayList(ARGS_PLAYLIST_IDS, (ArrayList<String>) playlistIds);
        selectionDialog.setArguments(args);
        return selectionDialog;
    }

    public void showCreatePlaylistDialog(final Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final View layout = LayoutInflater.from(context).inflate(R.layout.dialog_new_playlist, null);
        final EditText name = (EditText) layout.findViewById(R.id.edit_playlist_name);
        builder.setView(layout)
                .setTitle(R.string.create_new_playlist)
                .setPositiveButton(R.string.create, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        //TODO: Sync new playlist to server
                        Toast.makeText(context, "TODO: sync new playlist " + name.getText() + " to server", Toast.LENGTH_LONG)
                                .show();
                    }
                })
                .show();
    }

    public class PlayListAdapter extends BaseAdapter {

        List<DSA_Playlist__c> playlists;
        List<String> selectedPlaylistIds;

        public PlayListAdapter(List<DSA_Playlist__c> playlists, List<String> selectedPlaylistIds) {
            super();
            this.playlists = playlists;
            this.selectedPlaylistIds = selectedPlaylistIds;
        }

        @Override
        public int getCount() {
            return playlists == null ? 0 : playlists.size();
        }

        @Override
        public DSA_Playlist__c getItem(int position) {
            return playlists.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup viewGroup) {

            final DSA_Playlist__c playlist = getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.playlist_selector_item, viewGroup, false);
                convertView.setTag(new ViewHolder(convertView));
            }
            ViewHolder holder = (ViewHolder) convertView.getTag();
            holder.title.setText(playlist.getName());
            holder.check.setVisibility(selectedPlaylistIds.contains(playlist.getId()) ? View.VISIBLE : View.GONE);
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (selectedPlaylistIds.contains(playlist.getId())) {
                        selectedPlaylistIds.remove(playlist.getId());
                    } else {
                        selectedPlaylistIds.add(playlist.getId());
                    }
                    notifyDataSetChanged();
                }
            });
            return convertView;
        }

        public class ViewHolder {
            View view;
            ImageView check;
            TextView title;

            public ViewHolder(View view) {
                this.view = view;
                this.check = (ImageView) view.findViewById(R.id.check);
                this.title = (TextView) view.findViewById(R.id.title);
            }
        }
    }
}
