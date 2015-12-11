package com.wassup789.android.musicsync.fragments;

import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.hardware.ConsumerIrManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.wassup789.android.musicsync.BackgroundService;
import com.wassup789.android.musicsync.MainActivity;
import com.wassup789.android.musicsync.R;
import com.wassup789.android.musicsync.objectClasses.MediaInformation;
import com.wassup789.android.musicsync.objectClasses.PlaylistInformation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class SettingsFragment extends Fragment{

    public static final String default_server = "";
    public static final Boolean default_refreshToggle = true;
    public static final Integer default_refreshInterval = 5;// Refreshes every 5 minutes
    public static final String default_playlist = "\\/:*?_NULL_\"<>|";
    public static final String default_playlists = new Gson().toJson(new String[]{default_playlist});// Refreshes every 5 minutes

    public MaterialDialog playlistsDialog;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        registerGeneral(view);
        registerListeners(view);

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

        Boolean refreshToggle = settings.getBoolean("refreshToggle", default_refreshToggle);
        Switch refreshToggleSwitch = (Switch) view.findViewById(R.id.refreshToggleSwitch);
        refreshToggleSwitch.setChecked(refreshToggle);

        Integer refreshInterval = settings.getInt("refreshInterval", default_refreshInterval);
        TextView refreshIntervalValue = (TextView) view.findViewById(R.id.refreshIntervalValue);
        refreshIntervalValue.setText(refreshInterval.toString());
    }

    public void registerListeners(View view) {
        Button selectServerButton = (Button) view.findViewById(R.id.selectServerButton);
        selectServerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final SharedPreferences settings = getActivity().getSharedPreferences("settings", Context.MODE_PRIVATE);
                String serverUrl = settings.getString("server", default_server);
                new MaterialDialog.Builder(getActivity())
                    .title(R.string.dialog_selectServer_title)
                    .positiveText(R.string.dialog_done)
                        .inputType(InputType.TYPE_CLASS_TEXT)
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
                                Log.i("SettingsFragment", url);

                                SharedPreferences.Editor settingsEditor = settings.edit();
                                settingsEditor.putString("server", url);
                                settingsEditor.putString("playlists", default_playlists);
                                settingsEditor.commit();
                            }
                        })
                        .show();
            }
        });

        Button selectPlaylistsButton = (Button) view.findViewById(R.id.selectPlaylistsButton);
        selectPlaylistsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playlistsDialog = new MaterialDialog.Builder(getActivity())
                        .title(R.string.dialog_playlistsSelection_title)
                        .content(R.string.dialog_loading)
                        .progress(true, 0)
                        .show();

                new GetPlaylistSelection().execute();
            }
        });

        Switch refreshToggleSwitch = (Switch) view.findViewById(R.id.refreshToggleSwitch);
        refreshToggleSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                SharedPreferences settings = getActivity().getSharedPreferences("settings", Context.MODE_PRIVATE);
                SharedPreferences.Editor settingsEditor = settings.edit();
                settingsEditor.putBoolean("refreshToggle", isChecked);
                settingsEditor.commit();

                Intent intent = new Intent(getActivity(), BackgroundService.class);
                intent.putExtra("receiver", MainActivity.resultReceiver);

                getActivity().stopService(intent);
                NotificationManager notificationManager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(BackgroundService.notificationID);
                if (isChecked)
                    getActivity().startService(intent);
            }
        });

        Button refreshIntervalSaveButton = (Button) view.findViewById(R.id.refreshIntervalSaveButton);
        refreshIntervalSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText refreshIntervalValue = (EditText) getActivity().findViewById(R.id.refreshIntervalValue);
                String value = refreshIntervalValue.getText().toString();
                if (!value.matches("[-+]?\\d*\\.?\\d+"))//Is Numeric
                    return;
                Integer intVal = Integer.parseInt(value);
                SharedPreferences settings = getActivity().getSharedPreferences("settings", Context.MODE_PRIVATE);
                SharedPreferences.Editor settingsEditor = settings.edit();
                settingsEditor.putInt("refreshInterval", intVal);
                settingsEditor.commit();
                Toast.makeText(getActivity(), "Saved", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(getActivity(), BackgroundService.class);
                intent.putExtra("receiver", MainActivity.resultReceiver);

                getActivity().stopService(intent);
                NotificationManager notificationManager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(BackgroundService.notificationID);
                getActivity().startService(intent);
            }
        });

        Button forceRefreshButton = (Button) view.findViewById(R.id.forceRefreshButton);
        forceRefreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(), "Refreshing...", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(getActivity(), BackgroundService.class);
                intent.putExtra("receiver", MainActivity.resultReceiver);

                getActivity().stopService(intent);
                NotificationManager notificationManager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(BackgroundService.notificationID);
                getActivity().startService(intent);
            }
        });

        Button removeAllFilesButton = (Button) view.findViewById(R.id.removeAllFilesButton);
        removeAllFilesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
        });

    }

    public class GetPlaylistSelection extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... strings) {
            retrievePlaylistSelection();
            return null;
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(Long result) {
        }
    }

    public void retrievePlaylistSelection() {
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
                            .positiveText(R.string.dialog_dismiss)
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
}
