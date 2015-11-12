package com.wassup789.android.musicsync;

import android.Manifest;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.ImageView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.wassup789.android.musicsync.fragments.SettingsFragment;
import com.wassup789.android.musicsync.objectClasses.MediaInformation;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Array;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class BackgroundService extends Service {
    private final IBinder mBinder = new LocalBinder();
    public static final String mainDirectory = Environment.getExternalStorageDirectory() + "/musicsync";
    public static final String mediaDirectory = mainDirectory + "/files/";
    public static final String playlistFile = mainDirectory + "/default.m3u8";

    ResultReceiver resultReceiver;
    private SharedPreferences settings;

    public class LocalBinder extends Binder {
        BackgroundService getService() {
            return BackgroundService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.i("BackgroundService", "Service started");

        settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        File fileMainDirectory = new File(mainDirectory);
        Boolean test = true;
        if (!fileMainDirectory.exists())
            test = fileMainDirectory.mkdirs();
        File fileMediaDirectory = new File(mediaDirectory);
        if (!fileMediaDirectory.exists())
            fileMediaDirectory.mkdirs();

        /*SharedPreferences.Editor settingsEditor = settings.edit();
        if (!settings.contains("refreshToggle"))
            settingsEditor.putBoolean("refreshToggle", SettingsFragment.default_refreshToggle);
        if (!settings.contains("refreshInterval"))
            settingsEditor.putInt("refreshInterval", SettingsFragment.default_refreshInterval);
        settingsEditor.putString("backgroundServiceStatus", "Sleeping");*/
        sendMessage(100, "Sleeping");

        Boolean toStart = settings.getBoolean("refreshToggle", SettingsFragment.default_refreshToggle);
        Integer timeMinutes = settings.getInt("refreshInterval", SettingsFragment.default_refreshInterval);
        if (toStart && timeMinutes > 0)
            timer.scheduleAtFixedRate(timerTask, 1000, timeMinutes * 60 * 1000);
        if (timeMinutes < 1)
            Log.e("BackgroundService", "Invalid refresh interval, value is negative or zero.");
    }

    public void onDestroy() {
        SharedPreferences.Editor settingsEditor = settings.edit();
        sendMessage(100, "Killed");
        Log.i("BackgroundService", "Service killed");
    }

    private static Timer timer = new Timer();
    private static Boolean isRunning = false;

    private TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            update();
            sendMessage(100, "Sleeping");
            isRunning = false;
        }
    };

    public void update() {
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck == PackageManager.PERMISSION_DENIED) {
            timerTask.cancel();
            return;
        }

        if (!isRunning)
            isRunning = true;
        else
            return;
        sendMessage(100, "Gathering Information");
        try {
            URL url = new URL("http://wassup789.ga/scripts/musicsync/check.php");
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));

            String inputLine;
            String data = "";
            while ((inputLine = in.readLine()) != null)
                data = inputLine;

            Gson gson = new GsonBuilder().create();
            ArrayList<MediaInformation> output = gson.fromJson(data, new TypeToken<ArrayList<MediaInformation>>() {
            }.getType());
            for (int i = 0; i < output.size(); i++) {
                //Log.i("BackgroundService", output.get(i).name);
            }
            //Go through every file and remove duplicates and non-existing files
            File f = new File(mediaDirectory);
            File file[] = f.listFiles();
            ArrayList<File> filesRemove = new ArrayList<>(Arrays.asList(f.listFiles()));

            for (int i = 0; i < file.length; i++) {
                for (int ii = 0; ii < output.size(); ii++) {
                    //Removes files that exist
                    if (file[i].getName().equals(output.get(ii).name))
                        filesRemove.remove(file[i]);
                    //Remove files that are the exact size and name
                    if (file[i].getName().equals(output.get(ii).name) && file[i].length() == output.get(ii).size)
                        output.remove(ii);
                }
            }

            sendMessage(100, "Removing Files");
            //Remove files not in the original folder
            for (int i = 0; i < filesRemove.size(); i++) {
                Log.i("BackgroundService", "Deleting file: \"" + filesRemove.get(i).getName() + "\"");
                filesRemove.get(i).delete();
            }

            //Check if you need to download files
            if (output.size() == 0) {
                updatePlaylist();
                return;
            }

            //Reloop into array and download the files
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            int notificationID = 0;

            String failedDownloadString = "";
            int failedDownloads = 0;

            notificationManager.cancel(notificationID);
            notificationManager.cancel(notificationID + 1);

            Log.i("BackgroundService", "Start download of " + output.size() + " file(s)");
            sendMessage(100, "Downloading " + output.size() + " file(s)");

            for (int i = 0; i < output.size(); i++) {
                try {
                    sendMessage(101, "" + (output.size() - i - failedDownloads) + " (" + Math.round(((double) i / output.size()) * 100) + "% downloaded)");
                    sendMessage(102, output.get(i).name);
                    if (failedDownloads > 0)
                        failedDownloadString = "\n" + failedDownloads + " files failed to download";
                    NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                            .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_file_download_black_48dp))
                            .setSmallIcon(R.drawable.ic_file_download_black_48dp)
                            .setContentTitle("Downloading Music...")
                            .setContentText((i - failedDownloads) + "/" + (output.size() - failedDownloads) + " files downloaded")
                            .setStyle(new NotificationCompat.BigTextStyle()
                                            .bigText((i - failedDownloads) + "/" + (output.size() - failedDownloads) + " files downloaded" + failedDownloadString)
                                            .setSummaryText(output.get(i).name)
                            )
                            .setProgress(output.size(), i, false)
                            .setOngoing(true);

                    notificationManager.notify(notificationID, mBuilder.build());

                    Log.i("BackgroundService", "Downloading: \"" + output.get(i).name + "\"");

                    URL dUrl = new URL("http://wassup789.ga/scripts/musicsync/download.php?q=" + output.get(i).name_b64);
                    URLConnection connection = dUrl.openConnection();
                    connection.connect();

                    // download the file
                    InputStream inputs = new BufferedInputStream(connection.getInputStream());
                    OutputStream outputs = new FileOutputStream(mediaDirectory + output.get(i).name);

                    byte datas[] = new byte[1024];
                    int count;
                    while ((count = inputs.read(datas)) != -1)
                        outputs.write(datas, 0, count);

                    outputs.flush();
                    outputs.close();
                    inputs.close();
                } catch (FileNotFoundException e) {
                    Log.w("BackgroundService", "File: \"" + output.get(i).name + "\" failed to download");
                    Log.w("BackgroundService", "URL: " + "http://wassup789.ga/scripts/musicsync/download.php?q=" + output.get(i).name_b64);
                    failedDownloads++;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            sendMessage(101, "");

            if (failedDownloads > 0)
                failedDownloadString = "\n" + failedDownloads + " files failed to download";

            notificationManager.cancel(notificationID);
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_file_download_black_48dp))
                    .setSmallIcon(R.drawable.ic_file_download_black_48dp)
                    .setContentTitle("Download Complete")
                    .setContentText((output.size() - failedDownloads) + " files downloaded")
                    .setStyle(new NotificationCompat.BigTextStyle()
                                    .bigText((output.size() - failedDownloads) + " files downloaded" + failedDownloadString)
                    );
            notificationManager.notify(notificationID + 1, mBuilder.build());

            updatePlaylist();
        } catch (IOException e) {
            Log.e("BackgroundService", "An error has occurred whilst updating the media files.");
            e.printStackTrace();
        }
    }

    public void updatePlaylist() {
        /*try {
            Log.i("BackgroundService", "Generating playlist");
            File playlist = new File(playlistFile);

            FileOutputStream outputStream = new FileOutputStream(playlist, false);
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));

            bufferedWriter.write(mediaDirectory);
            bufferedWriter.close();
            outputStream.close();
            Log.i("BackgroundService", "Playlist generated");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return;*/

        sendMessage(100, "Updating Playlist");
        try {
            Log.i("BackgroundService", "Generating playlist");
            File f = new File(mediaDirectory);
            File mediaFiles[] = f.listFiles();

            File playlist = new File(playlistFile);

            FileOutputStream outputStream = new FileOutputStream(playlist, false);
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));

            bufferedWriter.write("#EXTM3U");
            bufferedWriter.newLine();
            bufferedWriter.newLine();

            for(int i = 0; i < mediaFiles.length; i++) {
                MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                mmr.setDataSource(mediaDirectory + mediaFiles[i].getName());
                String artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
                String title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
                String duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                mmr.release();

                bufferedWriter.write("#EXTINF:" + duration + ", " + artist + " - " + title);
                bufferedWriter.newLine();
                bufferedWriter.write((mediaDirectory + mediaFiles[i].getName()).toString());
                bufferedWriter.newLine();
                bufferedWriter.newLine();
            }
            bufferedWriter.close();
            outputStream.close();
            Log.i("BackgroundService", "Playlist generated");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("LocalService", "Received start id: " + startId + "; " + intent);
        if(intent != null)
            resultReceiver = intent.getParcelableExtra("receiver");
        return START_STICKY;
    }

    public void sendMessage(int resultCode, String value) {
        Bundle bundle = new Bundle();
        bundle.putString("data", value);
        if(resultReceiver != null)
            resultReceiver.send(resultCode, bundle);
    }
}