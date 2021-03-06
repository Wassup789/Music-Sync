package com.wassup789.android.musicsync;

import android.Manifest;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.astuetz.PagerSlidingTabStrip;
import com.wassup789.android.musicsync.fragments.FilesFragment;
import com.wassup789.android.musicsync.fragments.SettingsFragment;
import com.wassup789.android.musicsync.fragments.StatusFragment;


public class MainActivity extends AppCompatActivity {
    PagerSlidingTabStrip tabs;
    ViewPager pager;
    public static MyResultReceiver resultReceiver;
    public static Boolean hasStartedBackground = false;
    public static boolean isActivityActivated = false;

    @Override
    public void onStart() {
        super.onStart();
        isActivityActivated = true;
    }

    @Override
    public void onStop() {
        super.onStop();
        isActivityActivated = false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (android.support.v7.widget.Toolbar) this.findViewById(R.id.toolbar);
        this.setSupportActionBar(toolbar);

        pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(new ViewPagerAdapter(getSupportFragmentManager()));
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            public void onPageScrollStateChanged(int state) {
            }

            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            public void onPageSelected(int position) {
            }
        });

        tabs = (PagerSlidingTabStrip) findViewById(R.id.tabHost);
        tabs.setViewPager(pager);

        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if(permissionCheck == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
            return;
        }

        resultReceiver = new MyResultReceiver(null);
        if(!hasStartedBackground) {
            Intent intent = new Intent(this, BackgroundService.class);
            intent.putExtra("receiver", resultReceiver);
            startService(intent);
            hasStartedBackground = true;
        }

        new CheckVersionAsync().execute();

        if(getIntent().hasExtra("showDownloaded") && getIntent().getBooleanExtra("showDownloaded", true) && BackgroundService.filesDownloaded.size() > 0) {
            new MaterialDialog.Builder(this)
                    .title(R.string.dialog_downloadedFiles_title)
                    .items(BackgroundService.filesDownloaded)
                    .show();
        }

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(BackgroundService.notificationID);
        notificationManager.cancel(BackgroundService.notificationIDComplete);
        notificationManager.cancel(BackgroundService.notificationIDPermMissing);
        BackgroundService.totalFilesDownloaded = 0;
        BackgroundService.filesDownloaded.clear();
    }
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 0: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(this, BackgroundService.class);
                    intent.putExtra("receiver", resultReceiver);
                    startService(intent);
                    hasStartedBackground = true;
                }else{
                    new MaterialDialog.Builder(this)
                            .title(getString(R.string.dialog_storage_title))
                            .content(getString(R.string.dialog_storage_desc))
                            .positiveText(getString(R.string.dialog_dismiss))
                            .icon(getDrawable(R.drawable.ic_folder_black_48dp))
                            .dismissListener(new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialog) {
                                    finish();
                                }
                            })
                            .show();
                    return;
                }
            }
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_about:
                new MaterialDialog.Builder(this)
                        .title(getString(R.string.dialog_about_title))
                        .positiveText(getString(R.string.dialog_dismiss))
                        .content(getString(R.string.dialog_about_desc))
                        .show();
                return true;
            /*case R.id.action_debug:

                return true;*/
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class ViewPagerAdapter extends FragmentStatePagerAdapter {

        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        public Fragment getItem(int num) {
            switch(num) {
                default:
                    return new SettingsFragment();
                case 1:
                    return new StatusFragment();
                case 2:
                    return new FilesFragment();
            }
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch(position) {
                default:
                    return "Settings";
                case 1:
                    return "Status";
                case 2:
                    return "Files";
            }
        }
    }

    class MyResultReceiver extends ResultReceiver
    {
        public MyResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, final Bundle resultData) {

            if(resultCode == BackgroundService.STATUS_UPDATE){//Update status information
                runOnUiThread( new Thread() {
                    public void run() {
                        String status = resultData.getString("data");
                        TextView statStatusValue = (TextView) findViewById(R.id.statStatusValue);
                        if (statStatusValue != null)
                            statStatusValue.setText(status);

                        TextView filesToDownloadLabel = (TextView) findViewById(R.id.filesToDownloadLabel);
                        TextView filesToDownloadValue = (TextView) findViewById(R.id.filesToDownloadValue);
                        TextView currentDownloadLabel = (TextView) findViewById(R.id.currentDownloadLabel);
                        TextView currentDownloadValue = (TextView) findViewById(R.id.currentDownloadValue);
                        TextView currentPlaylistLabel = (TextView) findViewById(R.id.currentPlaylistLabel);
                        TextView currentPlaylistValue = (TextView) findViewById(R.id.currentPlaylistValue);
                        if (status != null && !status.contains("Downloading")) {
                            if (filesToDownloadLabel != null)
                                filesToDownloadLabel.setVisibility(View.GONE);
                            if (filesToDownloadValue != null)
                                filesToDownloadValue.setVisibility(View.GONE);
                            if (currentDownloadLabel != null)
                                currentDownloadLabel.setVisibility(View.GONE);
                            if (currentDownloadValue != null)
                                currentDownloadValue.setVisibility(View.GONE);
                        } else {
                            if (filesToDownloadLabel != null)
                                filesToDownloadLabel.setVisibility(View.VISIBLE);
                            if (filesToDownloadValue != null)
                                filesToDownloadValue.setVisibility(View.VISIBLE);
                            if (currentDownloadLabel != null)
                                currentDownloadLabel.setVisibility(View.VISIBLE);
                            if (currentDownloadValue != null)
                                currentDownloadValue.setVisibility(View.VISIBLE);
                        }
                        if (status != null && (status.contains("Gathering Information") || status.contains("Downloading") || status.contains("Removing Files") || status.contains("Updating Playlist"))) {
                            if (currentPlaylistLabel != null)
                                currentPlaylistLabel.setVisibility(View.VISIBLE);
                            if (currentPlaylistValue != null)
                                currentPlaylistValue.setVisibility(View.VISIBLE);
                        } else {
                            if (currentPlaylistLabel != null)
                                currentPlaylistLabel.setVisibility(View.GONE);
                            if (currentPlaylistValue != null)
                                currentPlaylistValue.setVisibility(View.GONE);
                        }
                    }
                });
            }else if(resultCode == BackgroundService.STATUS_DOWNLOADINFO){//Set download information
                runOnUiThread( new Thread() {
                    public void run() {
                        String total = resultData.getString("data");
                        TextView filesToDownloadValue = (TextView) findViewById(R.id.filesToDownloadValue);
                        if(filesToDownloadValue != null)
                            filesToDownloadValue.setText(total);
                    }
                });
            }else if(resultCode == BackgroundService.STATUS_CURRENTDOWNLOADINFO){//Set current download information
                runOnUiThread( new Thread() {
                    public void run() {
                        String currentDownload = resultData.getString("data");
                        TextView currentDownloadValue = (TextView) findViewById(R.id.currentDownloadValue);
                        if(currentDownloadValue != null)
                            currentDownloadValue.setText(currentDownload);
                    }
                });
            }else if(resultCode == BackgroundService.STATUS_CURRENTPLAYLIST){//Set current playlist
                runOnUiThread( new Thread() {
                    public void run() {
                        String currentPlaylist = resultData.getString("data");
                        TextView currentPlaylistValue = (TextView) findViewById(R.id.currentPlaylistValue);
                        if(currentPlaylistValue != null)
                            currentPlaylistValue.setText(currentPlaylist);
                    }
                });
            }
        }
    }

    public class CheckVersionAsync extends AsyncTask<String, Void, Void> {
        protected Void doInBackground(String... strings) {
            if(!BackgroundService.checkVersion(MainActivity.this)){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new MaterialDialog.Builder(MainActivity.this)
                                .title(R.string.dialog_outdatedVersion_title)
                                .positiveText(getString(R.string.dialog_dismiss))
                                .content(Html.fromHtml(getString(R.string.dialog_outdatedVersion_desc)))
                                .show();
                    }
                });
            }
            return null;
        }

        protected void onProgressUpdate(Integer... progress) {}
        protected void onPostExecute(Long result) {}
    }
}
