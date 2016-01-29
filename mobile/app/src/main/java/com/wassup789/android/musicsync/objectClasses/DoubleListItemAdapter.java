package com.wassup789.android.musicsync.objectClasses;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import com.wassup789.android.musicsync.R;

import java.util.ArrayList;

public class DoubleListItemAdapter {
    public static ListView getListView(Context context, ListView listview, final ArrayList<DoubleListItem> data, final CompoundButton.OnCheckedChangeListener onCheckedListener){
        ArrayAdapter adapter = new ArrayAdapter(context, R.layout.fragment_doublelistitem, R.id.listViewTitle, data) {
            @Override
            public View getView(final int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);

                LinearLayout listViewContent = (LinearLayout) view.findViewById(R.id.listViewContent);
                TextView listDivider = (TextView) view.findViewById(R.id.listViewDivider);
                TextView listTitle = (TextView) view.findViewById(R.id.listViewTitle);
                TextView listSubtitle = (TextView) view.findViewById(R.id.listViewSubtitle);
                Switch listSwitch = (Switch) view.findViewById(R.id.listViewSwitch);

                DoubleListItem item = data.get(position);
                if(data.get(position).isDivider){
                    listViewContent.setVisibility(View.GONE);
                    listSwitch.setVisibility(View.GONE);
                    listDivider.setText(item.title);
                }else{
                    listDivider.setVisibility(View.GONE);
                    listTitle.setText(item.title);

                    if(data.get(position).subTitle == null)
                        listSubtitle.setVisibility(View.GONE);
                    else
                        listSubtitle.setText(item.subTitle);

                    if(!data.get(position).useSwitch)
                        listSwitch.setVisibility(View.GONE);
                    else{
                        if(item.switchval != -1){
                            listSwitch.setChecked(item.switchval == 1 ? true : false);
                        }
                        item.setSwitch(listSwitch);
                        listSwitch.setText("" + position);
                        if(onCheckedListener != null)
                            listSwitch.setOnCheckedChangeListener(onCheckedListener);
                    }

                }
                return view;
            }
        };

        listview.setAdapter(adapter);
        listview.setDividerHeight(0);
        return listview;
    }
}
