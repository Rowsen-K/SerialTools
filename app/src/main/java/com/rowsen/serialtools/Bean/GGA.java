package com.rowsen.serialtools.Bean;

import androidx.annotation.NonNull;

public class GGA {
    public String lal;
    public String lon;
    public String time;
    public String speed;
    public GGA(String lal,String lon,String speed,String time){
        this.lal = lal;
        this.lon = lon;
        this.time = time;
        this.speed = speed;
    }

    @NonNull
    @Override
    public String toString() {
        return "["+lal+","+lon+","+speed+","+time+"]";
    }
}
