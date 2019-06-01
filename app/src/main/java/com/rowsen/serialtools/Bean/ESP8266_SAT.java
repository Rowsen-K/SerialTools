package com.rowsen.serialtools.Bean;

public class ESP8266_SAT {
    public String ssid;
    public String psw;
    public String ip;
    public String port;
    public ESP8266_SAT(String ssid,String psw,String ip,String port){
        this.ssid = ssid;
        this.psw = psw;
        this.ip = ip;
        this.port = port;
    }
}
