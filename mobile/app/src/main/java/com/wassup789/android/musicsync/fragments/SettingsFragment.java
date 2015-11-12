package com.wassup789.android.musicsync.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.hardware.ConsumerIrManager;
import android.os.Bundle;
import android.text.Editable;
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
import com.wassup789.android.musicsync.BackgroundService;
import com.wassup789.android.musicsync.MainActivity;
import com.wassup789.android.musicsync.R;

import java.io.File;

public class SettingsFragment extends Fragment{

    public static final Boolean default_refreshToggle = true;
    public static final Integer default_refreshInterval = 5;// Refreshes every 5 minutes

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        registerGeneral(view);
        registerListeners(view);

        return view;
    }
    
    public void registerGeneral(View view){
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor settingsEditor = settings.edit();
        if(!settings.contains("refreshToggle"))
            settingsEditor.putBoolean("refreshToggle", default_refreshToggle);
        if(!settings.contains("refreshInterval"))
            settingsEditor.putInt("refreshInterval", default_refreshInterval);

        Boolean refreshToggle = settings.getBoolean("refreshToggle", default_refreshToggle);
        Switch refreshToggleSwitch = (Switch) view.findViewById(R.id.refreshToggleSwitch);
        refreshToggleSwitch.setChecked(refreshToggle);

        Integer refreshInterval = settings.getInt("refreshInterval", default_refreshInterval);
        TextView refreshIntervalValue = (TextView) view.findViewById(R.id.refreshIntervalValue);
        refreshIntervalValue.setText((CharSequence) refreshInterval.toString());
    }

    public void registerListeners(View view) {
        Switch refreshToggleSwitch = (Switch) view.findViewById(R.id.refreshToggleSwitch);
        refreshToggleSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
                SharedPreferences.Editor settingsEditor = settings.edit();
                settingsEditor.putBoolean("refreshToggle", isChecked);

                Intent intent = new Intent(getActivity(), BackgroundService.class);
                intent.putExtra("receiver", MainActivity.resultReceiver);

                getActivity().stopService(intent);
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
                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
                SharedPreferences.Editor settingsEditor = settings.edit();
                settingsEditor.putInt("refreshInterval", intVal);
                Toast.makeText(getActivity(), "Saved", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(getActivity(), BackgroundService.class);
                intent.putExtra("receiver", MainActivity.resultReceiver);

                getActivity().stopService(intent);
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
                getActivity().startService(intent);
            }
        });

        Button removeAllFilesButton = (Button) view.findViewById(R.id.removeAllFilesButton);
        removeAllFilesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new MaterialDialog.Builder(getActivity())
                        .title(getString(R.string.dialog_removefiles_title))
                        .content(getString(R.string.dialog_removefiles_desc))
                        .negativeText(getString(R.string.dialog_delete))//Inversion, negative = delete
                        .positiveText(getString(R.string.dialog_cancel))//Positive = cancel
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
}
