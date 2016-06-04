package com.wassup789.android.musicsync.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
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
import android.widget.FrameLayout;
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
                if(keyCode == KeyEvent.KEYCODE_BACK && isVisible()){
                    if(isViewingPlaylist) {
                        FrameLayout loadingContainer = (FrameLayout) getActivity().findViewById(R.id.loadingContainer);
                        loadingContainer.setVisibility(View.VISIBLE);
                        new SetListViewDefaultAsync().execute();
                    }else
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
                if (isViewingMetadata)
                    setViewingMetadata(false, toggleButton, currentPlaylist);
                else
                    setViewingMetadata(true, toggleButton, currentPlaylist);
            }
        });

        new SetListViewDefaultAsync().execute();
        return view;
    }


    public class SetListViewDefaultAsync extends AsyncTask<String, Void, Void> {
        protected Void doInBackground(String... strings) {
            setListViewDefault();
            return null;
        }

        protected void onProgressUpdate(Integer... progress) {}
        protected void onPostExecute(Long result) {}
    }

    public void setListViewDefault() {
        isViewingPlaylist = false;


        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ListView filesList = (ListView) getActivity().findViewById(R.id.filesListView);
                if (filesList != null)
                    filesList.setVisibility(View.GONE);
                TextView title = (TextView) getActivity().findViewById(R.id.title);
                if (title != null)
                    title.setText("Playlists:");
                Button toggleButton = (Button) getActivity().findViewById(R.id.toggleButton);
                if (toggleButton != null)
                    toggleButton.setVisibility(View.GONE);
            }
        });

        SharedPreferences settings = getActivity().getSharedPreferences("settings", Context.MODE_PRIVATE);
        ArrayList<DoubleListItem> data = new ArrayList<DoubleListItem>();
        String[] playlists = new Gson().fromJson(settings.getString("playlists", SettingsFragment.default_playlists), String[].class);
        for (int i = 0; i < playlists.length; i++) {
            if (playlists[i].equals(SettingsFragment.default_playlist))
                break;
            else {
                File f = new File(BackgroundService.mediaDirectory + playlists[i]);
                if (!f.exists() && !f.isDirectory())
                    data.add(new DoubleListItem(playlists[i], false, playlists[i], String.format("Total Files: %d | Total Size: %s%s", 0, "0.0", "MB"), false));
                else {
                    double totalSize = 0;
                    File[] files = f.listFiles();
                    for (int j = 0; j < files.length; j++) {
                        double fileSize = Math.round(files[j].length() / 1000000.0 * 10) / 10.0;
                        totalSize += fileSize;
                    }
                    String fileSuffix = "MB";
                    if (totalSize / 1000 > 0) {
                        totalSize = Math.round(totalSize / 1000.0 * 10) / 10.0;
                        fileSuffix = "GB";
                    } else
                        totalSize = Math.round(totalSize * 10) / 10.0;

                    data.add(new DoubleListItem(playlists[i], false, playlists[i], String.format("Total Files: %d | Total Size: %s%s", files.length, totalSize, fileSuffix), false));
                }
            }
        }

        final ArrayList<DoubleListItem> dataFinal = data;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                FrameLayout loadingContainer = (FrameLayout) getActivity().findViewById(R.id.loadingContainer);
                loadingContainer.setVisibility(View.GONE);

                final ListView list = DoubleListItemAdapter.getListView(getContext(), (ListView) getActivity().findViewById(R.id.playlistsListView), dataFinal, null);
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
        });
    }

    public void setViewingMetadata(boolean toView, Button button, DoubleListItem item) {
        isViewingPlaylist = true;

        if (toView) {
            metadataDialog = new MaterialDialog.Builder(getActivity())
                    .title("Loading Metadata...")
                    .content(R.string.dialog_wait)
                    .progress(true, 0)
                    .cancelable(false)
                    .show();
            new GetFileMetadataAsync().execute(item.name);

            button.setText("View Filename");
            isViewingMetadata = true;
        } else {

            File f = new File(BackgroundService.mediaDirectory + item.name + "/");
            ArrayList<DoubleListItem> filesData = new ArrayList<DoubleListItem>();
            if (!f.exists()) {
                filesData.add(new DoubleListItem("divider_filepath", true, String.format("File Path: %s/", f.getPath()), null, false).setTextColor(Color.parseColor("#777777")));
                filesData.add(new DoubleListItem("divider_totalfiles", true, String.format("Total Files: %d", 0), null, false).setTextColor(Color.parseColor("#777777")));
                filesData.add(2, new DoubleListItem("divider_totalsize", true, String.format("Total Size: %s%s", "0.0", "MB"), null, false).setTextColor(Color.parseColor("#777777")));
                filesData.add(new DoubleListItem("nofilesfound", false, "No Files Found", null, false));

                ListView filesList = DoubleListItemAdapter.getListView(getContext(), (ListView) getActivity().findViewById(R.id.filesListView), filesData, null);
                filesList.setVisibility(View.VISIBLE);
                filesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapter, View view, int position, long l) {
                        DoubleListItem item = (DoubleListItem) adapter.getItemAtPosition(position);
                        playAudio(item.name);
                    }
                });
                return;
            }
            File[] files = f.listFiles();
            filesData.add(new DoubleListItem("divider_filepath", true, String.format("File Path: %s/", f.getPath()), null, false).setTextColor(Color.parseColor("#777777")));
            filesData.add(new DoubleListItem("divider_totalfiles", true, String.format("Total Files: %d", files.length), null, false).setTextColor(Color.parseColor("#777777")));

            if(files.length == 0)
                filesData.add(new DoubleListItem("nofilesfound", false, "No Files Found", null, false));

            double totalSize = 0;
            for (int i = 0; i < files.length; i++) {
                double fileSize = Math.round(files[i].length() / 1000000.0 * 10) / 10.0;
                totalSize += fileSize;
                filesData.add(new DoubleListItem(files[i].getPath(), false, files[i].getName(), String.format("%sMB", fileSize), false));
            }
            String fileSuffix = "MB";
            if(totalSize/1000 >= 0) {
                totalSize = Math.round(totalSize / 1000.0 * 10) / 10.0;
                fileSuffix = "GB";
            }else
                totalSize = Math.round(totalSize * 10) / 10.0;

            filesData.add(2, new DoubleListItem("divider_totalsize", true, String.format("Total Size: %s%s", totalSize, fileSuffix), null, false).setTextColor(Color.parseColor("#777777")));

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
        if(!f.exists()) {
            filesData.add(new DoubleListItem("divider_filepath", true, String.format("File Path: %s/", f.getPath()), null, false).setTextColor(Color.parseColor("#777777")));
            filesData.add(new DoubleListItem("divider_totalfiles", true, String.format("Total Files: %d", 0), null, false).setTextColor(Color.parseColor("#777777")));
            filesData.add(2, new DoubleListItem("divider_totalsize", true, String.format("Total Size: %s%s", "0.0", "MB"), null, false).setTextColor(Color.parseColor("#777777")));

            filesData.add(new DoubleListItem("nofilesfound", false, "No Files Found", null, false));
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (!metadataDialog.isCancelled())
                        metadataDialog.cancel();

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
            });
            return;
        }
        File[] files = f.listFiles();
        filesData.add(new DoubleListItem("divider_filepath", true, String.format("File Path: %s/", f.getPath()), null, false).setTextColor(Color.parseColor("#777777")));
        filesData.add(new DoubleListItem("divider_totalfiles", true, String.format("Total Files: %d", files.length), null, false).setTextColor(Color.parseColor("#777777")));
        if(files.length == 0)
            filesData.add(new DoubleListItem("nofilesfound", false, "No Files Found", null, false));

        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        double totalSize = 0;
        for (int i = 0; i < files.length; i++) {
            String artist = "";
            String title = "";
            int duration = -1;
            int bitrate = -1;
            String time = "??:??";
            double fileSize = Math.round(files[i].length() / 1000000.0 * 10) / 10.0;
            totalSize += fileSize;
            try {
                mmr.setDataSource(files[i].getPath());
                artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
                if(artist == null)
                    artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST);
                title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
                duration = (int)Math.floor(Integer.parseInt(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)) / 1000);
                bitrate = (int)Math.floor(Integer.parseInt(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)) / 1000);
            }catch (RuntimeException e){}

            if(title == null || title.equals("")){
                artist = null;
                title = files[i].getName();
            }

            if(duration != -1){
                String hours = "" + (int) Math.floor(duration / 3600);
                String minutes = "" + (int) Math.floor((duration - (Integer.parseInt(hours) * 3600)) / 60);
                String seconds = "" + (duration - (Integer.parseInt(hours) * 3600) - (Integer.parseInt(minutes) * 60));
                if (Integer.parseInt(hours) < 10) {
                    hours = "0" + hours;
                }
                if (Integer.parseInt(minutes) < 10) {
                    minutes = "0" + minutes;
                }
                if (Integer.parseInt(seconds) < 10) {
                    seconds = "0" + seconds;
                }
                if (Integer.parseInt(hours) == 0)
                    time = minutes + ":" + seconds;
                else
                    time = hours + ":" + minutes + ":" + seconds;
            }

            filesData.add(new DoubleListItem(files[i].getPath(), false, String.format("%s%s", (artist == null) ? "" : artist + " - ", title), String.format("%s - %skbps - %sMB", time, (bitrate == -1 ? "???" : bitrate), fileSize), false));
        }
        String fileSuffix = "MB";
        if(totalSize/1000 >= 0) {
            totalSize = Math.round(totalSize / 1000.0 * 10) / 10.0;
            fileSuffix = "GB";
        }else
            totalSize = Math.round(totalSize * 10) / 10.0;

        filesData.add(2, new DoubleListItem("divider_totalsize", true, String.format("Total Size: %s%s", totalSize, fileSuffix), null, false).setTextColor(Color.parseColor("#777777")));

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!metadataDialog.isCancelled())
                    metadataDialog.cancel();

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
        });
    }

    public void playAudio(String filePath){
        Intent intent = new Intent();
        intent.setAction(android.content.Intent.ACTION_VIEW);
        File file = new File(filePath);
        if(!file.isDirectory() && file.exists()) {
            intent.setDataAndType(Uri.fromFile(file), "audio/*");
            startActivity(intent);
        }
    }
}
