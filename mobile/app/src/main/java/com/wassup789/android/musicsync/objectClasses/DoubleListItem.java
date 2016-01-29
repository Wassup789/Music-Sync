package com.wassup789.android.musicsync.objectClasses;

import android.widget.Switch;

public class DoubleListItem{
    public String name;
    public boolean isDivider;
    public String title;
    public String subTitle;
    public boolean useSwitch;
    public Switch listSwitch = null;
    public int switchval = -1;

    public DoubleListItem(String _itemName, boolean _isDivider, String _title, String _subTitle, boolean _useSwitch){
        name = _itemName;
        isDivider = _isDivider;
        title = _title;
        subTitle = _subTitle;
        useSwitch = _useSwitch;
    }

    public void setSwitch(Switch _switch){
        listSwitch = _switch;
    }

    /**
     * NOTE: USE THIS BEFORE LISTVIEW INITIALIZATION
     *
     * @param isChecked
     */
    public DoubleListItem setSwitchValue(boolean isChecked){
        switchval = (isChecked == true ? 1 : 0);
        return this;
    }
}

