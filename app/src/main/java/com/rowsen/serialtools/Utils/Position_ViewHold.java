package com.rowsen.serialtools.Utils;

import android.widget.TextView;

//保存解析数据结果的UI
public class Position_ViewHold {
    public TextView latitude;
    public TextView longitude;
    public TextView sog;
    public TextView cog;
    public TextView time;

    public Position_ViewHold() {
    }

    Position_ViewHold(TextView latitude, TextView longitude, TextView sog, TextView cog, TextView time) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.sog = sog;
        this.cog = cog;
        this.time = time;
    }
}
