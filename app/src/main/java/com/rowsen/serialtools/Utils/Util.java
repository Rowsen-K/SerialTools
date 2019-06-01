package com.rowsen.serialtools.Utils;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;

import com.hoho.android.usbserial.util.SerialInputOutputManager;
import com.rowsen.serialtools.Bean.SerialPortSet;
import com.rowsen.serialtools.R;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.List;

import static java.lang.Thread.sleep;

public class Util {
    public static Position_ViewHold position_viewHold;
    public static TextView heading_viewHold;
    public static String s = "$GPRMC,060901.000,A,2348.8288,N,14037.5982,E,2.29,111.2,050319,,,A*54\n" +
            "$GPGGA,060901.000,2348.8288,N,14037.5982,E,1,04,1.0,3.0,M,,,,0000*07\n";/*+
            "$GPGLL,2348.8288,N,14037.5982,E,060901.000,A*3A\n" +
            "$GPRMC,060908.000,A,2348.8282,N,14037.6001,E,2.29,111.2,050319,,,A*56\n" +
            "$GPGGA,060908.000,2348.8282,N,14037.6001,E,1,04,1.0,3.0,M,,,,0000*05\n" +
            "$GPGLL,2348.8282,N,14037.6001,E,060908.000,A*38\n" +
            "$GPRMC,060916.000,A,2348.8275,N,14037.6021,E,2.29,111.2,050319,,,A*53\n" ;+
            "$GPGGA,060916.000,2348.8275,N,14037.6021,E,1,04,1.0,3.0,M,,,,0000*00\n" +
            "$GPGLL,2348.8275,N,14037.6021,E,060916.000,A*3D\n" +
            "$GPRMC,060924.000,A,2348.8268,N,14037.6040,E,2.29,111.2,050319,,,A*59\n" +
            "$GPGGA,060924.000,2348.8268,N,14037.6040,E,1,04,1.0,3.0,M,,,,0000*0A\n" +
            "$GPGLL,2348.8268,N,14037.6040,E,060924.000,A*37\n" +
            "$GPRMC,060931.000,A,2348.8261,N,14037.6060,E,2.29,111.2,050319,,,A*56\n" +
            "$GPGGA,060931.000,2348.8261,N,14037.6060,E,1,04,1.0,3.0,M,,,,0000*05\n" +
            "$GPGLL,2348.8261,N,14037.6060,E,060931.000,A*38\n" +
            "$GPRMC,060939.000,A,2348.8254,N,14037.6079,E,2.29,111.2,050319,,,A*50\n" +
            "$GPGGA,060939.000,2348.8254,N,14037.6079,E,1,04,1.0,3.0,M,,,,0000*03\n" +
            "$GPGLL,2348.8254,N,14037.6079,E,060939.000,A*3E\n" +
            "$GPRMC,060947.000,A,2348.8247,N,14037.6098,E,2.29,111.2,050319,,,A*54\n" +
            "$GPGGA,060947.000,2348.8247,N,14037.6098,E,1,04,1.0,3.0,M,,,,0000*07\n" +
            "$GPGLL,2348.8247,N,14037.6098,E,060947.000,A*3A\n" +
            "$GPRMC,060954.000,A,2348.8240,N,14037.6118,E,2.29,111.2,050319,,,A*58\n" +
            "$GPGGA,060954.000,2348.8240,N,14037.6118,E,1,04,1.0,3.0,M,,,,0000*0B\n" +
            "$GPGLL,2348.8240,N,14037.6118,E,060954.000,A*36\n" +
            "$GPRMC,061002.000,A,2348.8233,N,14037.6137,E,2.29,111.2,050319,,,A*5A\n" +
            "$GPGGA,061002.000,2348.8233,N,14037.6137,E,1,04,1.0,3.0,M,,,,0000*09\n" +
            "$GPGLL,2348.8233,N,14037.6137,E,061002.000,A*34\n" +
            "$GPRMC,061010.000,A,2348.8226,N,14037.6157,E,2.29,111.2,050319,,,A*5B\n" +
            "$GPGGA,061010.000,2348.8226,N,14037.6157,E,1,04,1.0,3.0,M,,,,0000*08\n" +
            "$GPGLL,2348.8226,N,14037.6157,E,061010.000,A*35\n" +
            "$GPRMC,061017.000,A,2348.8219,N,14037.6176,E,2.29,111.2,050319,,,A*53\n" +
            "$GPGGA,061017.000,2348.8219,N,14037.6176,E,1,04,1.0,3.0,M,,,,0000*00\n" +
            "$GPGLL,2348.8219,N,14037.6176,E,061017.000,A*3D\n" +
            "$GPRMC,061025.000,A,2348.8213,N,14037.6195,E,2.29,111.2,050319,,,A*55\n" +
            "$GPGGA,061025.000,2348.8213,N,14037.6195,E,1,04,1.0,3.0,M,,,,0000*06\n" +
            "$GPGLL,2348.8213,N,14037.6195,E,061025.000,A*3B\n" +
            "$GPRMC,061033.000,A,2348.8206,N,14037.6215,E,2.29,111.2,050319,,,A*5D\n" +
            "$GPGGA,061033.000,2348.8206,N,14037.6215,E,1,04,1.0,3.0,M,,,,0000*0E\n" +
            "$GPGLL,2348.8206,N,14037.6215,E,061033.000,A*33\n" +
            "$GPRMC,061040.000,A,2348.8199,N,14037.6234,E,2.29,111.2,050319,,,A*5F\n" +
            "$GPGGA,061040.000,2348.8199,N,14037.6234,E,1,04,1.0,3.0,M,,,,0000*0C\n" +
            "$GPGLL,2348.8199,N,14037.6234,E,061040.000,A*31\n" +
            "$GPRMC,061048.000,A,2348.8192,N,14037.6254,E,2.29,111.2,050319,,,A*5A\n" +
            "$GPGGA,061048.000,2348.8192,N,14037.6254,E,1,04,1.0,3.0,M,,,,0000*09\n" +
            "$GPGLL,2348.8192,N,14037.6254,E,061048.000,A*34\n" +
            "$GPRMC,061056.000,A,2348.8185,N,14037.6273,E,2.29,111.2,050319,,,A*56\n" +
            "$GPGGA,061056.000,2348.8185,N,14037.6273,E,1,04,1.0,3.0,M,,,,0000*05\n" +
            "$GPGLL,2348.8185,N,14037.6273,E,061056.000,A*38\n" +
            "$GPRMC,061103.000,A,2348.8178,N,14037.6292,E,2.29,111.2,050319,,,A*5A\n" +
            "$GPGGA,061103.000,2348.8178,N,14037.6292,E,1,04,1.0,3.0,M,,,,0000*09\n" +
            "$GPGLL,2348.8178,N,14037.6292,E,061103.000,A*34\n" +
            "$GPRMC,061111.000,A,2348.8171,N,14037.6312,E,2.29,111.2,050319,,,A*59\n" +
            "$GPGGA,061111.000,2348.8171,N,14037.6312,E,1,04,1.0,3.0,M,,,,0000*0A\n" +
            "$GPGLL,2348.8171,N,14037.6312,E,061111.000,A*37\n" +
            "$GPRMC,061118.000,A,2348.8164,N,14037.6331,E,2.29,111.2,050319,,,A*55\n" +
            "$GPGGA,061118.000,2348.8164,N,14037.6331,E,1,04,1.0,3.0,M,,,,0000*06\n" +
            "$GPGLL,2348.8164,N,14037.6331,E,061118.000,A*3B\n" +
            "$GPRMC,061126.000,A,2348.8157,N,14037.6351,E,2.29,111.2,050319,,,A*5E\n" +
            "$GPGGA,061126.000,2348.8157,N,14037.6351,E,1,04,1.0,3.0,M,,,,0000*0D\n" +
            "$GPGLL,2348.8157,N,14037.6351,E,061126.000,A*30\n" +
            "$GPRMC,061134.000,A,2348.8150,N,14037.6370,E,2.29,111.2,050319,,,A*59\n" +
            "$GPGGA,061134.000,2348.8150,N,14037.6370,E,1,04,1.0,3.0,M,,,,0000*0A\n" +
            "$GPGLL,2348.8150,N,14037.6370,E,061134.000,A*37\n" +
            "$GPRMC,061141.000,A,2348.8143,N,14037.6389,E,2.29,111.2,050319,,,A*5F\n";*/

    //position的解析dialog
    public static AlertDialog creat_analy_position_dialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("位置数据解析");
        builder.setCancelable(false);
        View v = View.inflate(context, R.layout.alert_position_analysis, null);
        if (position_viewHold == null)
            position_viewHold = new Position_ViewHold((TextView) v.findViewById(R.id.latitude), (TextView) v.findViewById(R.id.longitude), (TextView) v.findViewById(R.id.sog), (TextView) v.findViewById(R.id.cog), (TextView) v.findViewById(R.id.time));
        builder.setView(v);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        return builder.create();
    }

    //heading解析dialog
    public static AlertDialog creat_analy_heading_dialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("航向数据解析");
        builder.setCancelable(false);
        View v = View.inflate(context, R.layout.alert_heading_analysis, null);
        if (heading_viewHold == null)
            heading_viewHold = v.findViewById(R.id.heading);
        builder.setView(v);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        return builder.create();
    }

    //serial port设置dialog
    public static AlertDialog creat_set_port_dialog(Context context, final SharedPreferences set_sp, final Handler handler) {
        final SerialPortSet set;
        View v = View.inflate(context, R.layout.usb_set_alertdialog, null);
        final Spinner baudrate = v.findViewById(R.id.baudrate);
        final Integer[] baud = {4800, 9600, 38400, 74880, 115200};
        ArrayAdapter<Integer> baud_adapter = new ArrayAdapter(context, R.layout.support_simple_spinner_dropdown_item, baud);
        baudrate.setAdapter(baud_adapter);
        if (set_sp != null)
            baudrate.setSelection(search(baud, set_sp.getInt("baudrate", 4800)));

        final Spinner databit = v.findViewById(R.id.databit);
        final Integer[] data = {5, 6, 7, 8};
        ArrayAdapter<Integer> data_adapter = new ArrayAdapter(context, R.layout.support_simple_spinner_dropdown_item, data);
        databit.setAdapter(data_adapter);
        if (set_sp != null)
            databit.setSelection(search(data, set_sp.getInt("databit", 8)));
        else databit.setSelection(3);
        final Spinner stopbit = v.findViewById(R.id.stopbit);
        final Integer[] stop = {1, 2, 3};
        ArrayAdapter stop_adapter = new ArrayAdapter(context, R.layout.support_simple_spinner_dropdown_item, stop);
        stopbit.setAdapter(stop_adapter);
        if (set_sp != null)
            stopbit.setSelection(search(stop, set_sp.getInt("stopbit", 1)));

        final Spinner parity = v.findViewById(R.id.parity);
        final String[] p = {"NONE", "ODD", "EVEN", "MARK", "SPACE"};
        Integer[] p_map = {0, 1, 2, 3, 4};
        ArrayAdapter parity_adapter = new ArrayAdapter(context, R.layout.support_simple_spinner_dropdown_item, p);
        parity.setAdapter(parity_adapter);
        if (set_sp != null)
            parity.setSelection(search(p_map, set_sp.getInt("parity", 0)));
        //为该试图添加控件的行为
        //先构建一个窗口的初始化参数对象set
        if (set_sp != null)
            set = new SerialPortSet(set_sp.getInt("baudrate", 4800), set_sp.getInt("databit", 8), set_sp.getInt("stopbit", 1), set_sp.getInt("parity", 0));
        else set = new SerialPortSet();
        //设定一个spnner的选择监听函数
        Spinner.OnItemSelectedListener lis = new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                // ((TextView)view).setTextColor(getResources().getColor(R.color.colorPrimary));
                if (adapterView.getId() == baudrate.getId()) {
                    Log.e("命中", baud[i] + "");
                    set.baudrate = baud[i];
                } else if (adapterView.getId() == databit.getId())
                    set.databit = data[i];
                else if (adapterView.getId() == stopbit.getId())
                    set.stopbit = stop[i];
                else if (adapterView.getId() == parity.getId()) {
                    switch (p[i]) {
                        case "NONE":
                            set.parity = 0;
                            break;
                        case "ODD":
                            set.parity = 1;
                            break;
                        case "EVEN":
                            set.parity = 2;
                            break;
                        case "MARK":
                            set.parity = 3;
                            break;
                        case "SPACE":
                            set.parity = 4;
                            break;
                    }
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        };
        baudrate.setOnItemSelectedListener(lis);
        databit.setOnItemSelectedListener(lis);
        stopbit.setOnItemSelectedListener(lis);
        parity.setOnItemSelectedListener(lis);

        //构建一个弹窗用来配置窗口，将上述配置完毕的试图放入
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("串口参数设置");
        builder.setView(v);
        builder.setCancelable(false);
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Log.e("set", set.baudrate + " " + set.databit + " " + set.stopbit + " " + set.parity);
                if (set_sp != null)
                    set_sp.edit().putInt("baudrate", set.baudrate)
                            .putInt("databit", set.databit)
                            .putInt("stopbit", set.stopbit)
                            .putInt("parity", set.parity)
                            .commit();
                //iniPort();
                Message msg = new Message();
                msg.obj = set;
                msg.what = 7;
                handler.sendMessage(msg);
            }
        });
        return builder.create();
    }

    //数据接收区文本处理
    public static void textProcess(TextView content, String s) {
        content.append(s);
        if (content.getLineHeight() * (content.getLineCount() + 1) >= content.getHeight()) {
            content.scrollTo(0, (content.getLineCount() + 1) * content.getLineHeight() - content.getHeight());
        }
    }

    //模拟NMEA数据
    public static String simulator(String s) {
        int x = (int) s.charAt(0);
        for (int n = 1; n < s.length(); n++) {
            x ^= (int) s.charAt(n);
        }
        String r = Integer.toHexString(x);
        Log.e("result", r);
        if (r.length() == 1)
            return "$" + s + "*0" + r + "\n";
        else return "$" + s + "*" + r + "\n";
    }

    //查找索引位置函数(初始化串口时)
    public static int search(Integer[] arr, int para) {
        int k = -1;
        for (int n = 0; n < arr.length; n++) {
            if (para == arr[n]) {
                k = n;
                break;
            }
        }
        return k;
    }

    /*2016.6.11
     * 得到本机IP地址
     * */
    public static String getLocalIpAddress() {
        try {
            Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
            while (en.hasMoreElements()) {
                NetworkInterface nif = en.nextElement();
                Enumeration<InetAddress> enumIpAddr = nif.getInetAddresses();
                while (enumIpAddr.hasMoreElements()) {
                    InetAddress mInetAddress = enumIpAddr.nextElement();
                    if (!mInetAddress.isLoopbackAddress() && mInetAddress instanceof Inet4Address) {
                        return mInetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e("MyFeiGeActivity", "获取本地IP地址失败");
        }

        return null;
    }
}
