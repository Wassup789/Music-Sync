package com.wassup789.android.musicsync;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.service.notification.StatusBarNotification;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Base64;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.wassup789.android.musicsync.fragments.SettingsFragment;
import com.wassup789.android.musicsync.objectClasses.MediaInformation;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Timer;
import java.util.TimerTask;

public class BackgroundService extends Service {
    private final IBinder mBinder = new LocalBinder();
    public static final String mainDirectory = Environment.getExternalStorageDirectory() + "/MusicSync/";
    public static final String mediaDirectory = mainDirectory + "files/";
    public static final int notificationID = 0;
    public static final int notificationIDComplete = notificationID + 1;
    public static final int notificationIDPermMissing = notificationID + 2;
    public static int totalFilesDownloaded = 0;
    public static ArrayList<String> filesDownloaded = new ArrayList<String>();

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

        settings = getSharedPreferences("settings", Context.MODE_PRIVATE);

        File fileMainDirectory = new File(mainDirectory);
        if (!fileMainDirectory.exists())
            fileMainDirectory.mkdirs();
        File fileMediaDirectory = new File(mediaDirectory);
        if (!fileMediaDirectory.exists())
            fileMediaDirectory.mkdirs();

        sendMessage(100, "Sleeping");

        Boolean toStart = settings.getBoolean("refreshToggle", SettingsFragment.default_refreshToggle);
        Integer timeMinutes = settings.getInt("refreshInterval", SettingsFragment.default_refreshInterval);
        if (toStart && timeMinutes > 0)
            timer.scheduleAtFixedRate(timerTask, 1000, timeMinutes * 60 * 1000);
        if (timeMinutes < 1)
            Log.e("BackgroundService", "Invalid refresh interval, value is negative or zero.");
    }

    public void onDestroy() {
        sendMessage(100, "Killed");
        Log.i("BackgroundService", "Service killed");
    }

    private static Timer timer = new Timer();
    private static Boolean isRunning = false;

    private TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            //removeOldPlaylists();
            try {
                String[] playlists = new Gson().fromJson(settings.getString("playlists", SettingsFragment.default_playlists), String[].class);
                for (int i = 0; i < playlists.length; i++) {
                    if (playlists[i].equals(SettingsFragment.default_playlist))
                        break;
                    else
                        update(playlists[i]);
                }
                sendMessage(100, "Sleeping");
                isRunning = false;
            } catch (JsonParseException e) {
                e.printStackTrace();
            }
        }
    };

    public void removeOldPlaylists() {
        String[] playlists = new Gson().fromJson(settings.getString("playlists", SettingsFragment.default_playlists), String[].class);
        File f = new File(mediaDirectory);
        String[] directories = f.list(new FilenameFilter() {
            @Override
            public boolean accept(File current, String name) {
                return new File(current, name).isDirectory();
            }
        });
        if(playlists.length > 0) {
            for (int i = 0; i < directories.length; i++) {
                Boolean isValid = false;
                for (int ii = 0; ii < playlists.length; ii++) {
                    if (directories[i].equals(playlists[ii])) {
                        isValid = true;
                        break;
                    }
                }
                if (!isValid) {
                    File removeDir = new File(mediaDirectory + directories[i]);
                    File[] removeFiles = removeDir.listFiles();
                    for (int ii = 0; ii < removeFiles.length; ii++) {
                        removeFiles[ii].delete();
                    }
                    removeDir.delete();
                }
            }
        }
    }

    public void update(String playlistName) {
        String serverUrl = settings.getString("server", SettingsFragment.default_server);
        try {
            new URL(serverUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        if(!serverUrl.endsWith("/"))
            serverUrl += "/";
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck == PackageManager.PERMISSION_DENIED) {
            Log.w("BackgroundService", "Missing permission: WRITE_EXTERNAL_STORAGE; Update cancelled");
            timer.cancel();

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_file_download_black_48dp))
                    .setSmallIcon(R.drawable.ic_headset_white_48dp)
                    .setColor(Color.parseColor("#F57C00"))
                    .setContentTitle("Missing permission: storage")
                    .setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP), 0))
                    .setAutoCancel(true);
            if(!MainActivity.isActivityActiviated)
                notificationManager.notify(notificationIDPermMissing, mBuilder.build());
            return;
        }

        if (!isRunning)
            isRunning = true;
        else
            return;
        sendMessage(100, "Gathering Information");
        sendMessage(103, playlistName);
        try {
            URL verifyUrl = new URL(serverUrl + "verify.php?q=" + Base64.encodeToString(playlistName.getBytes(), Base64.DEFAULT));
            URLConnection urlConnection = verifyUrl.openConnection();
            urlConnection.setConnectTimeout(5000);
            urlConnection.connect();

            BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

            String inputLine;
            String data = "";
            while ((inputLine = in.readLine()) != null)
                data = inputLine;
            Gson gson = new GsonBuilder().create();
            Boolean[] doesPlaylistExist = gson.fromJson(data, Boolean[].class);
            if(!doesPlaylistExist[0]) {
                Log.i("BackgroundService", String.format("Playlist: \"%s\" does not exist, skipping playlist", playlistName));
                return;
            }

            URL getFilesUrl = new URL(serverUrl + "getfiles.php?q=" + Base64.encodeToString(playlistName.getBytes(), Base64.DEFAULT));
            urlConnection = getFilesUrl.openConnection();
            urlConnection.setConnectTimeout(5000);
            urlConnection.connect();
            in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

            String inputLine2;
            data = "";
            while ((inputLine2 = in.readLine()) != null)
                data = inputLine2;

            ArrayList<MediaInformation> output = gson.fromJson(data, new TypeToken<ArrayList<MediaInformation>>() {
            }.getType());

            //Go through every file and remove duplicates and non-existing files
            File f = new File(mediaDirectory + playlistName + "/");
            if(!f.exists())
                f.mkdirs();
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
                Log.i("BackgroundService", String.format("Deleting file: \"%s\"", filesRemove.get(i).getName()));
                filesRemove.get(i).delete();
            }

            //Check if you need to download files
            if (output.size() == 0) {
                updatePlaylist(playlistName);
                return;
            }

            //Reloop into array and download the files
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            String failedDownloadString = "";
            int failedDownloads = 0;

            notificationManager.cancel(notificationID);
            notificationManager.cancel(notificationIDComplete);
            notificationManager.cancel(notificationIDPermMissing);


            Log.i("BackgroundService", String.format("Start download of %d file(s)", output.size()));
            sendMessage(100, String.format("Downloading %d file(s)", output.size()));

            for (int i = 0; i < output.size(); i++) {
                try {
                    sendMessage(101, String.format("%d (%d%% downloaded)", (output.size() - i), (int) Math.round(((double) i / output.size()) * 100)));
                    sendMessage(102, output.get(i).name);
                    if (failedDownloads > 0)
                        failedDownloadString = String.format("\n%d files failed to download", failedDownloads);
                    NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                            .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_file_download_black_48dp))
                            .setSmallIcon(R.drawable.ic_headset_white_48dp)
                            .setColor(Color.parseColor("#F57C00"))
                            .setContentTitle("Downloading Music...")
                            .setContentText((i - failedDownloads) + "/" + (output.size() - failedDownloads) + " files downloaded")
                            .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(String.format("%d/%d files downloaded%s", (i - failedDownloads), (output.size() - failedDownloads), failedDownloadString))
                                .setSummaryText((Math.round(((i - failedDownloads + 0.0) / (output.size() - failedDownloads + 0.0)) * 1000.0) / 10.0) + "% downloaded")
                            )
                            .setProgress(output.size() - failedDownloads, i - failedDownloads, false)
                            .setOngoing(true)
                            .setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP).putExtra("showDownloaded", true), 0));
                    if(!MainActivity.isActivityActiviated)
                        notificationManager.notify(notificationID, mBuilder.build());

                    Log.i("BackgroundService", String.format("Downloading: \"%s\"", output.get(i).name));

                    URL downloadUrl = new URL(serverUrl + "download.php?q=" + output.get(i).name_b64);
                    URLConnection connection = downloadUrl.openConnection();
                    connection.setConnectTimeout(5000);
                    connection.connect();

                    // download the file
                    InputStream inputs = new BufferedInputStream(connection.getInputStream());
                    OutputStream outputs = new FileOutputStream(mediaDirectory + playlistName + "/" + output.get(i).name);

                    byte datas[] = new byte[1024];
                    int count;
                    while ((count = inputs.read(datas)) != -1)
                        outputs.write(datas, 0, count);

                    outputs.flush();
                    outputs.close();
                    inputs.close();

                    filesDownloaded.add(output.get(i).name);
                } catch (FileNotFoundException e) {
                    Log.w("BackgroundService", String.format("File: \"%s\" failed to download", output.get(i).name));
                    Log.w("BackgroundService", String.format("URL: %sdownload.php?q=%s", serverUrl, output.get(i).name_b64));
                    failedDownloads++;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            sendMessage(101, "");

            if (failedDownloads > 0)
                failedDownloadString = String.format("\n%d files failed to download", failedDownloads);

            notificationManager.cancel(notificationID);
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_file_download_black_48dp))
                    .setSmallIcon(R.drawable.ic_headset_white_48dp)
                    .setColor(Color.parseColor("#F57C00"))
                    .setContentTitle("Download Complete")
                    .setContentText(String.format("%d files downloaded", (totalFilesDownloaded + output.size() - failedDownloads)))
                    .setStyle(new NotificationCompat.BigTextStyle()
                                    .bigText(String.format("%d files downloaded%s", (totalFilesDownloaded + output.size() - failedDownloads), failedDownloadString))
                    )
                    .setDeleteIntent(createOnDismissedIntent(this, notificationIDComplete))
                    .setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP).putExtra("showDownloaded", true), 0))
                    .setAutoCancel(true);

            totalFilesDownloaded += output.size();
            if(!MainActivity.isActivityActiviated)
                notificationManager.notify(notificationIDComplete, mBuilder.build());

            sendMessage(100, "Updating Playlist");//This is put outside of class for static void reasons
            updatePlaylist(playlistName);
        } catch (IOException e) {
            Log.e("BackgroundService", "An error has occurred whilst updating the media files.");
            e.printStackTrace();
        }
    }

    private PendingIntent createOnDismissedIntent(Context context, int notificationId) {
        Intent intent = new Intent(context, NotificationDismissedReceiver.class);
        intent.putExtra("com.wassup789.android.musicsync.notificationId", notificationId);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), notificationId, intent, 0);
        return pendingIntent;
    }

    public static void updatePlaylist(String playlistName) {
        try {
            Log.i("BackgroundService", "Generating playlist");
            File f = new File(mediaDirectory + playlistName + "/");
            File[] mediaFiles = f.listFiles();
            /*Arrays.sort(mediaFiles, new Comparator<File>(){
                public int compare(File f1, File f2)
                {
                    BasicFileAttributes attr = Files.readAttributes(file, BasicFileAttributes.class);
                    return Long.compare(f1.lastModified(), f2.lastModified());
                }
            });*/
            Arrays.sort(mediaFiles, Collections.<File>reverseOrder());

            File playlist = new File(mainDirectory + playlistName + ".m3u8");

            FileOutputStream outputStream = new FileOutputStream(playlist, false);
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));

            bufferedWriter.write("#EXTM3U");
            bufferedWriter.newLine();
            bufferedWriter.newLine();

            for (int i = 0; i < mediaFiles.length; i++) {
                try {
                    MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                    mmr.setDataSource(mediaDirectory + playlistName + "/" + mediaFiles[i].getName());
                    String artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
                    if(artist == null)
                        artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST);
                    String title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
                    String duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                    mmr.release();

                    bufferedWriter.write(String.format("#EXTINF:%s, %s - %s", duration, artist, title));
                    bufferedWriter.newLine();
                    bufferedWriter.write((mediaDirectory + playlistName + "/" + mediaFiles[i].getName()).toString());
                    bufferedWriter.newLine();
                    bufferedWriter.newLine();
                } catch (RuntimeException e) {
                    Log.w(BackgroundService.class.getName(), String.format("Unable to retrieve MediaMetadata from \"%s\"", mediaFiles[i].getName()));
                    bufferedWriter.write((mediaDirectory + playlistName + "/" + mediaFiles[i].getName()).toString());
                    bufferedWriter.newLine();
                }
            }
            bufferedWriter.close();
            outputStream.close();
            Log.i("BackgroundService", String.format("Playlist: \"%s\" generated", playlistName));
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
