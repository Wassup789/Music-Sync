package com.wassup789.android.musicsync.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.gson.Gson;
import com.wassup789.android.musicsync.BackgroundService;
import com.wassup789.android.musicsync.R;
import com.wassup789.android.musicsync.objectClasses.DoubleListItem;
import com.wassup789.android.musicsync.objectClasses.DoubleListItemAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class FilesFragment extends Fragment {
    public boolean isViewingPlaylist = false;
    public boolean isViewingMetadata = false;
    public DoubleListItem currentPlaylist;

    public MaterialDialog metadataDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_files, container, false);

        view.setFocusableInTouchMode(true);
        view.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
                if(keyCode == KeyEvent.KEYCODE_BACK && isVisible())
                {
                    System.out.println(isViewingPlaylist);
                    if(isViewingPlaylist)
                        setListViewDefault(view);
                    else
                        return false;
                    return true;
                }
                return false;
            }
        });

        final Button toggleButton = (Button) view.findViewById(R.id.toggleButton);
        toggleButton.setVisibility(View.GONE);
        toggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isViewingMetadata)
                    setViewingMetadata(false, toggleButton, currentPlaylist);
                else
                    setViewingMetadata(true, toggleButton, currentPlaylist);
            }
        });

        setListViewDefault(view);
        return view;
    }

    public void setListViewDefault(View view){
        isViewingPlaylist = false;

        ListView filesList = (ListView) getActivity().findViewById(R.id.filesListView);
        if(filesList != null)
            filesList.setVisibility(View.GONE);
        TextView title = (TextView) getActivity().findViewById(R.id.title);
        if(title != null)
            title.setText("Playlists:");
        Button toggleButton = (Button) getActivity().findViewById(R.id.toggleButton);
        if(toggleButton != null)
            toggleButton.setVisibility(View.GONE);

        SharedPreferences settings = getActivity().getSharedPreferences("settings", Context.MODE_PRIVATE);
        ArrayList<DoubleListItem> data = new ArrayList<DoubleListItem>();
        String[] playlists = new Gson().fromJson(settings.getString("playlists", SettingsFragment.default_playlists), String[].class);
        for(int i = 0; i < playlists.length; i++){
            if(playlists[i].equals(SettingsFragment.default_playlist))
                break;
            else
                data.add(new DoubleListItem(playlists[i], false, playlists[i], null, false));
        }

        final ListView list = DoubleListItemAdapter.getListView(getContext(), (ListView) view.findViewById(R.id.playlistsListView), data, null);
        list.setVisibility(View.VISIBLE);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View view, int position, long l) {
                DoubleListItem item = (DoubleListItem) adapter.getItemAtPosition(position);
                currentPlaylist = item;

                list.setVisibility(View.GONE);

                TextView title = (TextView) getActivity().findViewById(R.id.title);
                title.setText(item.name.substring(0, 1).toUpperCase() + item.name.substring(1) + ":");

                Button toggleButton = (Button) getActivity().findViewById(R.id.toggleButton);
                toggleButton.setVisibility(View.VISIBLE);
                setViewingMetadata(false, toggleButton, item);
            }
        });
    }

    public void setViewingMetadata(boolean toView, Button button, DoubleListItem item) {
        isViewingPlaylist = true;

        if (toView) {
            metadataDialog = new MaterialDialog.Builder(getActivity())
                    .title("Loading Metadata...")
                    .content(R.string.dialog_loading)
                    .progress(true, 0)
                    .cancelable(false)
                    .show();
            new GetFileMetadataAsync().execute(item.name);

            button.setText("View Filename");
            isViewingMetadata = true;
        } else {

            File f = new File(BackgroundService.mediaDirectory + item.name + "/");
            ArrayList<DoubleListItem> filesData = new ArrayList<DoubleListItem>();
            if (!f.exists())
                return;
            File file[] = f.listFiles();
            filesData.add(new DoubleListItem("", true, String.format("File Path: %s", f.getPath()), null, false));
            filesData.add(new DoubleListItem("", true, String.format("Total Files: %d", file.length), null, false));
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            for (int i = 0; i < file.length; i++) {
                filesData.add(new DoubleListItem(file[i].getPath(), false, file[i].getName(), null, false));
            }

            button.setText("View Metadata");
            isViewingMetadata = false;

            ListView filesList = DoubleListItemAdapter.getListView(getContext(), (ListView) getActivity().findViewById(R.id.filesListView), filesData, null);
            filesList.setVisibility(View.VISIBLE);
            filesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapter, View view, int position, long l) {
                    DoubleListItem item = (DoubleListItem) adapter.getItemAtPosition(position);
                    playAudio(item.name);
                }
            });
        }
    }


    public class GetFileMetadataAsync extends AsyncTask<String, Void, Void> {
        protected Void doInBackground(String... strings) {
            getFileMetadata(strings[0]);
            return null;
        }

        protected void onProgressUpdate(Integer... progress) {}
        protected void onPostExecute(Long result) {}
    }

    public void getFileMetadata(String name){
        File f = new File(BackgroundService.mediaDirectory + name + "/");
        final ArrayList<DoubleListItem> filesData = new ArrayList<DoubleListItem>();
        if(!f.exists())
            return;
        File file[] = f.listFiles();
        filesData.add(new DoubleListItem("", true, String.format("File Path: %s", f.getPath()), null, false));
        filesData.add(new DoubleListItem("", true, String.format("Total Files: %d", file.length), null, false));
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        for (int i = 0; i < file.length; i++) {
            String artist = "";
            String title = "";
            try {
                mmr.setDataSource(file[i].getPath());
                artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
                if(artist == null)
                    artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST);
                title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            }catch (RuntimeException e){
                continue;
            }
            if((artist == null || artist == "") && artist != null)
                artist = file[i].getName();
            if((title == null || title == "") && artist != null){
                title = file[i].getName();
                artist = null;
            }
            if(artist == null && title == null)
                filesData.add(new DoubleListItem(file[i].getPath(), false, file[i].getName(), null, false));
            else
                filesData.add(new DoubleListItem(file[i].getPath(), false, title, "" + artist, false));
        }

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(!metadataDialog.isCancelled())
                    metadataDialog.cancel();

                ListView filesList = DoubleListItemAdapter.getListView(getContext(), (ListView) getActivity().findViewById(R.id.filesListView), filesData, null);
                filesList.setVisibility(View.VISIBLE);
                filesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapter, View view, int position, long l){
                        DoubleListItem item = (DoubleListItem) adapter.getItemAtPosition(position);
                        playAudio(item.name);
                    }
                });
            }
        });
    }

    public void playAudio(String filePath){
        Intent intent = new Intent();
        intent.setAction(android.content.Intent.ACTION_VIEW);
        File file = new File(filePath);
        if(file.isDirectory() && file.exists()) {
            intent.setDataAndType(Uri.fromFile(file), "audio/*");
            startActivity(intent);
        }
    }
}
