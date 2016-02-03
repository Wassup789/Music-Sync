package com.wassup789.android.musicsync.fragments;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.wassup789.android.musicsync.BackgroundService;
import com.wassup789.android.musicsync.MainActivity;
import com.wassup789.android.musicsync.R;
import com.wassup789.android.musicsync.objectClasses.DoubleListItem;
import com.wassup789.android.musicsync.objectClasses.DoubleListItemAdapter;
import com.wassup789.android.musicsync.objectClasses.PlaylistInformation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class SettingsFragment extends Fragment {

    public static final String default_server = "";
    public static final Boolean default_refreshToggle = true;
    public static final Integer default_refreshInterval = 5;// Refreshes every 5 minutes
    public static final String default_playlist = "\\/:*?_NULL_\"<>|";
    public static final String default_playlists = new Gson().toJson(new String[]{default_playlist});// Refreshes every 5 minutes

    public MaterialDialog playlistsDialog;
    public MaterialDialog oldPlaylistsDialog;

    private boolean fromListViewChecked = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        registerGeneral(view);

        SharedPreferences settings = getActivity().getSharedPreferences("settings", Context.MODE_PRIVATE);

        final ArrayList<DoubleListItem> data = new ArrayList<DoubleListItem>();
        data.add(new DoubleListItem("divider_general", true,  "General", null, false));
        data.add(new DoubleListItem("toggleMusicSync", false, getString(R.string.enableMusicSync_label), null, true).setSwitchValue(settings.getBoolean("refreshToggle", default_refreshToggle)));
        data.add(new DoubleListItem("editRefreshInterval", false, getString(R.string.refreshToggle_label), getString(R.string.refreshToggle_desc), false));
        data.add(new DoubleListItem("divider_serverSettings", true,  "Server Settings", null, false));
        data.add(new DoubleListItem("selectServer", false, getString(R.string.selectServer_label), null, false));
        data.add(new DoubleListItem("editPlaylists", false, getString(R.string.editPlaylists_label), getString(R.string.editPlaylists_desc), false ));
        data.add(new DoubleListItem("forceRefresh", false, getString(R.string.forceRefresh_label), null, false));
        data.add(new DoubleListItem("divider_remove", true,  "Cleanup", null, false));
        data.add(new DoubleListItem("removeOldPlaylists", false, getString(R.string.removeOldPlaylistFiles_label), getString(R.string.removeOldPlaylistFiles_desc), false));
        data.add(new DoubleListItem("removeAllFiles", false, getString(R.string.removeAllFiles_label), getString(R.string.removeAllFiles_desc), false));

        ListView list = DoubleListItemAdapter.getListView(getContext(), (ListView) view.findViewById(R.id.settingsListView), data, new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                int position = Integer.parseInt(compoundButton.getText().toString());
                if (!fromListViewChecked) {
                    switch (data.get(position).name) {
                        case "toggleMusicSync":
                            setMusicSync(!isChecked);
                            break;
                    }
                } else
                    fromListViewChecked = false;
            }
        });
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View view, int position, long l) {
                DoubleListItem item = (DoubleListItem) adapter.getItemAtPosition(position);

                switch(item.name){
                    case "toggleMusicSync":
                        if (item.listSwitch != null) {
                            setMusicSync(!item.listSwitch.isChecked());
                            fromListViewChecked = true;
                            item.listSwitch.setChecked(!item.listSwitch.isChecked());
                        }else
                            Toast.makeText(getActivity(), "Unable to save setting", Toast.LENGTH_LONG).show();
                        break;
                    case "editRefreshInterval":
                        setRefreshInterval();
                        break;
                    case "selectServer":
                        displayServerSelection();
                        break;
                    case "editPlaylists":
                        playlistsDialog = new MaterialDialog.Builder(getActivity())
                                .title(R.string.dialog_playlistsSelection_title)
                                .content(R.string.dialog_loading)
                                .progress(true, 0)
                                .show();

                        new GetPlaylistSelection().execute();
                        break;
                    case "forceRefresh":
                        forceRefresh();
                        break;
                    case "removeOldPlaylists":
                        oldPlaylistsDialog = new MaterialDialog.Builder(getActivity())
                                .title(R.string.dialog_removingoldplaylists)
                                .content(R.string.dialog_wait)
                                .progress(true, 0)
                                .show();

                        new RemoveOldPlaylistsAsync().execute();
                        break;
                    case "removeAllFiles":
                        removeAllFiles();
                        break;
                }
            }
        });
        return view;
    }

    public void registerGeneral(View view){
        SharedPreferences settings = getActivity().getSharedPreferences("settings", Context.MODE_PRIVATE);
        SharedPreferences.Editor settingsEditor = settings.edit();
        if(!settings.contains("server"))
            settingsEditor.putString("server", default_server);
        if(!settings.contains("refreshToggle"))
            settingsEditor.putBoolean("refreshToggle", default_refreshToggle);
        if(!settings.contains("refreshInterval"))
            settingsEditor.putInt("refreshInterval", default_refreshInterval);
        if(!settings.contains("playlists"))
            settingsEditor.putString("playlists", default_playlists);
        settingsEditor.commit();
    }

    public void setMusicSync(boolean value){
        SharedPreferences settings = getActivity().getSharedPreferences("settings", Context.MODE_PRIVATE);
        SharedPreferences.Editor settingsEditor = settings.edit();
        settingsEditor.putBoolean("refreshToggle", value);
        settingsEditor.commit();

        Intent intent = new Intent(getActivity(), BackgroundService.class);
        intent.putExtra("receiver", MainActivity.resultReceiver);

        getActivity().stopService(intent);
        NotificationManager notificationManager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(BackgroundService.notificationID);
        if (value)
            getActivity().startService(intent);
        Toast.makeText(getActivity(), "Saved", Toast.LENGTH_SHORT).show();
    }

    public void setRefreshInterval(){
        SharedPreferences settings = getActivity().getSharedPreferences("settings", Context.MODE_PRIVATE);
        new MaterialDialog.Builder(getContext())
                .title(R.string.dialog_setrefreshinterval)
                .content(R.string.dialog_setrefreshinterval_content)
                .inputType(InputType.TYPE_CLASS_NUMBER)
                .inputRange(1, 10)
                .positiveText(R.string.dialog_save)
                .input(getString(R.string.dialog_setrefreshinterval_default), "" + settings.getInt("refreshInterval", default_refreshInterval), false, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog dialog, CharSequence input) {
                        int value = Integer.parseInt(input.toString());

                        SharedPreferences settings = getActivity().getSharedPreferences("settings", Context.MODE_PRIVATE);
                        SharedPreferences.Editor settingsEditor = settings.edit();
                        settingsEditor.putInt("refreshInterval", value);
                        settingsEditor.commit();
                        Toast.makeText(getActivity(), "Saved", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(getActivity(), BackgroundService.class);
                        intent.putExtra("receiver", MainActivity.resultReceiver);

                        getActivity().stopService(intent);
                        NotificationManager notificationManager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
                        notificationManager.cancel(BackgroundService.notificationID);
                        getActivity().startService(intent);
                    }
                }).show();
    }

    public void displayServerSelection(){
        final SharedPreferences settings = getActivity().getSharedPreferences("settings", Context.MODE_PRIVATE);
        String serverUrl = settings.getString("server", default_server);
        new MaterialDialog.Builder(getActivity())
                .title(R.string.dialog_selectServer_title)
                .positiveText(R.string.dialog_save)
                .inputType(InputType.TYPE_TEXT_VARIATION_URI)
                .input(getString(R.string.dialog_selectServer_hint), serverUrl, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog dialog, CharSequence input) {
                        try {
                            new URL(input.toString());
                        } catch (MalformedURLException e) {
                            Toast.makeText(getActivity(), "Invalid URL", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        String url = input.toString();
                        if(!url.toLowerCase().startsWith("http")  && !url.toLowerCase().startsWith("https")) {
                            Toast.makeText(getActivity(), "Invalid URL, only http and https are supported", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if(!url.endsWith("/"))
                            url += "/";
                        Log.i("SettingsFragment", String.format("Set server to: %s", url));

                        SharedPreferences.Editor settingsEditor = settings.edit();
                        settingsEditor.putString("server", url);
                        settingsEditor.putString("playlists", default_playlists);
                        settingsEditor.commit();
                    }
                })
                .show();
    }

    public class GetPlaylistSelection extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... strings) {
            retrievePlaylistSelection();
            return null;
        }

        protected void onProgressUpdate(Integer... progress) {}
        protected void onPostExecute(Long result) {}
    }

    public void retrievePlaylistSelection() {
        long startTime = System.nanoTime();
        SharedPreferences settings = getActivity().getSharedPreferences("settings", Context.MODE_PRIVATE);
        String serverUrl = settings.getString("server", SettingsFragment.default_server);
        if(!serverUrl.endsWith("/"))
            serverUrl += "/";
        try {
            URL url = new URL(serverUrl + "playlists.php");
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));

            String inputLine;
            String data = "";
            while ((inputLine = in.readLine()) != null)
                data = inputLine;

            Gson gson = new GsonBuilder().create();

            String[] savedPlaylists = gson.fromJson(settings.getString("playlists", default_playlists), String[].class);
            final ArrayList<Integer> playlistsToSelect = new ArrayList<Integer>();

            ArrayList<PlaylistInformation> output = gson.fromJson(data, new TypeToken<ArrayList<PlaylistInformation>>() {}.getType());
            final String[] playlists = new String[output.size()];
            for(int i = 0; i < output.size(); i++) {
                playlists[i] = output.get(i).name;
                for(int ii = 0; ii < savedPlaylists.length; ii++){
                    if(output.get(i).name.equals(savedPlaylists[ii])){
                        playlistsToSelect.add(i);
                        break;
                    }
                }
            }

            long stopTime = System.nanoTime();

            if(stopTime - startTime < 4e8) {
                try {
                    Thread.sleep(400);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            Integer[] playlistsToSelectFinal = new Integer[playlistsToSelect.size()];
            playlistsToSelectFinal = playlistsToSelect.toArray(playlistsToSelectFinal);
            final Integer[] finalPlaylistsToSelectFinal = playlistsToSelectFinal;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(!playlistsDialog.isCancelled())
                        playlistsDialog.cancel();

                    new MaterialDialog.Builder(getActivity())
                            .title(R.string.dialog_playlistsSelection_title)
                            .items(playlists)
                            .itemsCallbackMultiChoice(finalPlaylistsToSelectFinal, new MaterialDialog.ListCallbackMultiChoice() {
                                @Override
                                public boolean onSelection(MaterialDialog dialog, Integer[] which, CharSequence[] text) {
                                    String[] list = new String[which.length];
                                    for (int i = 0; i < which.length; i++) {
                                        list[i] = text[i].toString();
                                    }
                                    SharedPreferences settings = getActivity().getSharedPreferences("settings", Context.MODE_PRIVATE);
                                    SharedPreferences.Editor settingsEditor = settings.edit();
                                    settingsEditor.putString("playlists", new Gson().toJson(list));
                                    settingsEditor.commit();
                                    return true;
                                }
                            })
                            .positiveText(R.string.dialog_save)
                            .show();
                }
            });
        } catch (FileNotFoundException e) {
            Log.e("SettingsFragment", "Could not reach server URL specified");
            if(!playlistsDialog.isCancelled())
                playlistsDialog.cancel();
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getActivity(), "Could not reach server", Toast.LENGTH_SHORT).show();
                }
            });
        }catch(IOException e) {
            Log.e("SettingsFragment", "An error has occurred whilst retrieving playlists.");
            e.printStackTrace();
        }
    }

    public void forceRefresh(){
        Toast.makeText(getActivity(), "Refreshing...", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(getActivity(), BackgroundService.class);
        intent.putExtra("receiver", MainActivity.resultReceiver);

        getActivity().stopService(intent);
        NotificationManager notificationManager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(BackgroundService.notificationID);
        getActivity().startService(intent);
    }

    public class RemoveOldPlaylistsAsync extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... strings) {
            removeOldPlaylists();
            return null;
        }

        protected void onProgressUpdate(Integer... progress) {}
        protected void onPostExecute(Long result) {}
    }

    public void removeOldPlaylists(){
        long startTime = System.nanoTime();
        SharedPreferences settings = getActivity().getSharedPreferences("settings", Context.MODE_PRIVATE);
        String[] playlists = new Gson().fromJson(settings.getString("playlists", SettingsFragment.default_playlists), String[].class);
        File f = new File(BackgroundService.mediaDirectory);
        String[] directories = f.list(new FilenameFilter() {
            @Override
            public boolean accept(File current, String name) {
                return new File(current, name).isDirectory();
            }
        });
        int playlistsDeleted = 0;
        double totalSizeDeleted = 0;
        for (int i = 0; i < directories.length; i++) {
            Boolean isValid = false;
            for (int ii = 0; ii < playlists.length; ii++) {
                if (directories[i].equals(playlists[ii])) {
                    isValid = true;
                    break;
                }
            }
            if (!isValid) {
                File removeDir = new File(BackgroundService.mediaDirectory + directories[i]);
                File[] removeFiles = removeDir.listFiles();
                for (int j = 0; j < removeFiles.length; j++) {
                    double fileSize = Math.round(removeFiles[j].length() / 1000000.0 * 10) / 10.0;
                    totalSizeDeleted += fileSize;
                    removeFiles[j].delete();
                }
                removeDir.delete();
                playlistsDeleted++;
            }
        }

        String fileSuffix = "MB";
        if(totalSizeDeleted/1000 >= 1) {
            totalSizeDeleted = Math.round(totalSizeDeleted / 1000.0 * 10) / 10.0;
            fileSuffix = "GB";
        }else
            totalSizeDeleted = Math.round(totalSizeDeleted * 10) / 10.0;

        long stopTime = System.nanoTime();

        if(stopTime - startTime < 5e8) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        final int finalPlaylistsDeleted = playlistsDeleted;
        final double finalTotalSizeDeleted = totalSizeDeleted;
        final String finalFileSuffix = fileSuffix;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!oldPlaylistsDialog.isCancelled())
                    oldPlaylistsDialog.cancel();

                new MaterialDialog.Builder(getActivity())
                        .title(R.string.dialog_removingoldplaylists)
                        .content(String.format("Removed %d playlist(s)\nRemoved %s%s", finalPlaylistsDeleted, finalTotalSizeDeleted, finalFileSuffix))
                        .positiveText(R.string.dialog_done)
                        .show();
            }
        });
    }

    public void removeAllFiles(){
        new MaterialDialog.Builder(getActivity())
                .title(R.string.dialog_removefiles_title)
                .content(R.string.dialog_removefiles_desc)
                .negativeText(R.string.dialog_delete)//Inversion, negative = delete
                .positiveText(R.string.dialog_cancel)//Positive = cancel
                .icon(getActivity().getDrawable(R.drawable.ic_delete_black_48dp))
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(MaterialDialog dialog, DialogAction which) {
                        File f = new File(BackgroundService.mediaDirectory);
                        File[] files = f.listFiles();
                        for (int i = 0; i < files.length; i++)
                            files[i].delete();
                        Toast.makeText(getActivity(), "Removed all media files", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }
    
}