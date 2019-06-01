package com.rowsen.serialtools.Bean;

public class SerialPortSet {
    public int baudrate;
    public int databit;
    public int stopbit;
    public int parity;
    public SerialPortSet(){
        this.baudrate = 4800;
        this.databit = 8;
        this.stopbit = 1;
        this.parity = 0;
    }
   public SerialPortSet(int baudrate,int databit,int stopbit,int parity){
        this.baudrate = baudrate;
        this.databit = databit;
        this.stopbit = stopbit;
        this.parity = parity;
    }
}
