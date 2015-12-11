package com.wassup789.android.musicsync.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wassup789.android.musicsync.R;

import java.util.Timer;
import java.util.TimerTask;

public class StatusFragment extends Fragment{
    private static Timer timer = new Timer();
    SharedPreferences settings;

    public String backgroundServiceStatus = "Sleeping";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_status, container, false);

        return view;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
        timer.scheduleAtFixedRate(timerTask, 0, 1000);
    }

    private TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            backgroundServiceStatus = settings.getString("backgroundServiceStatus", "Unknown");
            //Log.i("StatisticsFragment", backgroundServiceStatus);

            //TextView statStatusValue = (TextView) getView().findViewById(R.id.statStatusValue);
            //statStatusValue.setText(backgroundServiceStatus);
            //mHandler.obtainMessage(1).sendToTarget();
        }
    };

    public Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            //TextView statStatusValue = (TextView) getView().findViewById(R.id.statStatusValue);
            //statStatusValue.setText(backgroundServiceStatus);
        }
    };
/*
    private class UpdaeteStatistics extends AsyncTask<URL, Integer, Long> {
        protected Long doInBackground(URL... urls) {
            int count = urls.length;
            long totalSize = 0;
            for (int i = 0; i < count; i++) {
                totalSize += Downloader.downloadFile(urls[i]);
                publishProgress((int) ((i / (float) count) * 100));
                // Escape early if cancel() is called
                if (isCancelled()) break;
            }
            return totalSize;
        }

        protected void onProgressUpdate(Integer... progress) {
            setProgressPercent(progress[0]);
        }

        protected void onPostExecute(Long result) {
            showDialog("Downloaded " + result + " bytes");
        }
    }*/
}
