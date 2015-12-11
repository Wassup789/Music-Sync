package com.wassup789.android.musicsync.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.wassup789.android.musicsync.R;

import java.util.ArrayList;
import java.util.Arrays;

public class StatisticsFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_statistics, container, false);

        registerSpaceLeftGraph(view);

        return view;
    }

    public void registerSpaceLeftGraph(View view) {
        HorizontalBarChart spaceLeftChart = (HorizontalBarChart) view.findViewById(R.id.spaceLeftChart);

        spaceLeftChart.setDescription("");

        ArrayList<BarEntry> yVals = new ArrayList<BarEntry>();
        yVals.add(new BarEntry(new float[]{14, 2}, 0));

        BarDataSet set = new BarDataSet(yVals, "");
        set.setStackLabels(new String[] {"Space Used", "Space Left"});
        set.setColors(Arrays.asList(new Integer[]{Color.parseColor("#009688"), Color.parseColor("#CED7DB")}));

        ArrayList < BarDataSet > dataSets = new ArrayList<BarDataSet>();
        dataSets.add(set);

        BarData data = new BarData(new String[] {""}, dataSets);
        spaceLeftChart.setData(data);
        spaceLeftChart.setTouchEnabled(false);
        spaceLeftChart.setDrawGridBackground(false);
        spaceLeftChart.getAxisLeft().setEnabled(false);
        spaceLeftChart.getAxisRight().setEnabled(false);
        spaceLeftChart.invalidate();

        spaceLeftChart.setData(data);
    }
}
