package com.rowsen.serialtools;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.SerialInputOutputManager;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;
import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundButton;
import com.rowsen.serialtools.Bean.ESP8266_SAT;
import com.rowsen.serialtools.Bean.GGA;
import com.rowsen.serialtools.Bean.SerialPortSet;
import com.rowsen.serialtools.Utils.Position_ViewHold;
import com.rowsen.serialtools.Utils.Util;

import net.sf.marineapi.nmea.parser.SentenceFactory;
import net.sf.marineapi.nmea.sentence.GGASentence;
import net.sf.marineapi.nmea.sentence.HDGSentence;
import net.sf.marineapi.nmea.sentence.Sentence;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.dmoral.toasty.Toasty;

import static android.view.View.GONE;

public class UsbListActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {
    private static final String ACTION_USB_PERMISSION = "com.an.USB_PERMISSION";
    boolean nmea_mode = false;
    boolean at_mode = false;
    StringBuffer AT_result;
    boolean serialPort_change = false;
    UsbManager usbManager;
    UsbSerialPort serialPort;
    List<UsbSerialPort> availableDevice;
    SerialPortSet set;
    SerialInputOutputManager sio;
    StringBuilder sb;
    boolean open = false;
    int deviceNum = 0;
    Handler handler;
    BaseAdapter baseAdapter;
    AlertDialog analy_position_alert;
    AlertDialog analy_heading_alert;
    Position_ViewHold position_viewHold;
    TextView heading_viewHold;
    @BindView(R.id.info)
    TextView info;
    @BindView(R.id.usb_list)
    ListView usb_list;
    @BindView(R.id.content)
    TextView content;
    @BindView(R.id.sendContent)
    EditText sendContent;
    @BindView(R.id.send)
    Button send;
    @BindView(R.id.func)
    LinearLayout func;
    boolean AT_Result = false;
    boolean AT_Error = false;
    @BindView(R.id.normal)
    LinearLayout normal;
    @BindView(R.id.filter)
    QMUIRoundButton filter;
    @BindView(R.id.analysis)
    QMUIRoundButton analysis;
    @BindView(R.id.simulate)
    QMUIRoundButton simulate;
    @BindView(R.id.nmea)
    LinearLayout nmea;
    @BindView(R.id.auto)
    QMUIRoundButton auto;
    @BindView(R.id.manual)
    QMUIRoundButton manual;
    @BindView(R.id.at)
    LinearLayout at;
    @BindView(R.id.mode)
    QMUIRoundButton mode;
    @BindView(R.id.checkbox)
    CheckBox checkBox;

    QMUIDialog mode_select;
    QMUIDialog ESP_auto;
    QMUIDialog.EditTextDialogBuilder talkID_filter_builder;
    QMUIDialog analysis_select;
    String analysis_mode;
    String simulator_mode;
    QMUIDialog simulator_select;
    QMUIDialog simulator_setting;
    SharedPreferences set_sp;
    boolean GGA_START = false;
    boolean HDG_START = false;

    AlertDialog loading;
    usbReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usb_list);
        ButterKnife.bind(this);
        if (getSupportActionBar() != null)
            getSupportActionBar().hide();
        //注册自定义广播
        receiver = new usbReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_USB_PERMISSION);
        registerReceiver(receiver, intentFilter);
        loading = creatProgress();
        Window window = loading.getWindow();
        window.setBackgroundDrawable(new ColorDrawable(0));
        set_sp = getSharedPreferences("Portset", MODE_PRIVATE);
        AT_result = new StringBuffer();
        handler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 0:
                        //监控是否有新设备插入
                        if (usbManager.getDeviceList().size() != deviceNum) {
                            schedule_data();
                            baseAdapter.notifyDataSetChanged();
                        } else handler.sendEmptyMessageDelayed(0, 1000);
                        break;
                    case 1:
                        //nmea模式
                        String s = (String) msg.obj;
                        Log.e("content", s);
                        Sentence sentence = SentenceFactory.getInstance().createParser(s.substring(0, s.length() - 1));
                        Log.e("id", sentence.getSentenceId());
                        if (talkID_filter_builder != null)
                            Log.e("filter", (talkID_filter_builder.getEditText().getText()).toString());
                        /*NMEA模式下3个基本功能：
                        1.语句过滤功能：filter
                            2.解析功能：analysis
                            3.模拟功能：simulate*/
                        //过滤显示
                        if (talkID_filter_builder == null || talkID_filter_builder.getEditText() == null)
                            Util.textProcess(content, s);
                        else {
                            String talkID = (talkID_filter_builder.getEditText().getText()).toString();
                            if (!talkID.equals("") && talkID != null && talkID.equals(sentence.getSentenceId()))
                                Util.textProcess(content, s);
                            else if (talkID.equals("") || talkID == null)
                                Util.textProcess(content, s);
                        }
                        //解析到对话框，加一个解析状态的判断量
                        if (analysis_mode != null && sentence.getSentenceId().equals(analysis_mode)) {
                            //Log.e("Filter", s);
                            if ("GGA".equals(analysis_mode)) {
                                if (!analy_position_alert.isShowing())
                                    analy_position_alert.show();
                                if (position_viewHold == null)
                                    position_viewHold = Util.position_viewHold;
                                GGASentence GGA = (GGASentence) sentence;
                                position_viewHold.longitude.setText(GGA.getPosition().getLongitude() + "");
                                position_viewHold.latitude.setText(GGA.getPosition().getLatitude() + "");
                                position_viewHold.time.setText(GGA.getTime() + "");
                            }
                            if ("HDG".equals(analysis_mode)) {
                                if (!analy_heading_alert.isShowing())
                                    analy_heading_alert.show();
                                if (heading_viewHold == null)
                                    heading_viewHold = Util.heading_viewHold;
                                HDGSentence HDG = (HDGSentence) sentence;
                                heading_viewHold.setText(HDG.getDeviation() + "");
                            }
                        }
                        //模拟一个NMEA数据
                       /* if (simulator_mode != null && simulator_mode.equals("GGA")) {
                            //设计一个模拟数据的对话框,定义基准经纬度,时间,速度
                            simulator_setting.show();*/
                        //点击确定按钮之后开启一个新线程,依据基准数据,每秒写出一句数据
                        // }
                        break;
                    case 2:
                        send.performClick();
                        break;
                    case 3:
                        //处理AT数据的逻辑
                        String s1 = (String) msg.obj;
                        Util.textProcess(content, s1);
                        Log.e("content", s1);
                        //if (s1.contains("OK"))
                        //    AT_Result = true;
                        //else AT_Error = true;
                        break;
                    case 4:
                        Util.textProcess(content, "ESP8266-UDP_SAT模式自动配置完毕！\r\n已进入透传模式！波特率：" + set.baudrate);
                        //Toast.makeText(UsbListActivity.this, "ESP8266-UDP_SAT模式自动配置完毕！", Toast.LENGTH_SHORT).show();
                        Toasty.success(UsbListActivity.this, "ESP8266-UDP_SAT模式自动配置完毕！").show();
                        loading.dismiss();
                        break;
                    case 5:
                        //普通数据的处理逻辑
                        Util.textProcess(content, (String) msg.obj);
                        Log.e("content", (String) msg.obj);
                        break;
                    case 6:
                        set = (SerialPortSet) msg.obj;
                        iniPort(set);
                        Util.textProcess(content, "ESP8266串口配置修改完毕！波特率：" + set.baudrate + "\r\n");
                        //Toast.makeText(UsbListActivity.this, "ESP8266串口配置修改完毕！", Toast.LENGTH_SHORT).show();
                        Toasty.success(UsbListActivity.this, "ESP8266串口配置修改完毕！").show();
                        loading.dismiss();
                        break;
                    case 7:
                        set = (SerialPortSet) msg.obj;
                        Util.textProcess(content, "开始串口初始化！\r\n");
                        //Toast.makeText(UsbListActivity.this, "开始串口初始化！", Toast.LENGTH_SHORT).show();
                        Toasty.warning(UsbListActivity.this, "开始串口初始化！").show();
                        if (serialPort_change) {
                            ESP_set_port(set);
                            serialPort_change = !serialPort_change;
                        } else iniPort(set);
                        break;
                    case 8:
                        Util.textProcess(content, "ESP8266-UDP_SAT模式自动配置出现错误！\r\n");
                        //Toast.makeText(UsbListActivity.this, "ESP8266串口配置错误！", Toast.LENGTH_SHORT).show();
                        Toasty.error(UsbListActivity.this, "ESP8266串口配置错误！").show();
                        loading.dismiss();
                        break;
                    case 9:
                        Util.textProcess(content, "ESP8266-串口波特率修改出现错误！\r\n");
                        //Toast.makeText(UsbListActivity.this, "ESP8266串口波特率配置错误！", Toast.LENGTH_SHORT).show();
                        Toasty.error(UsbListActivity.this, "ESP8266串口波特率配置错误！").show();
                        loading.dismiss();
                        break;

                }

            }
        };
        content.setMovementMethod(ScrollingMovementMethod.getInstance());
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkBox.isChecked())
                    sio.writeAsync((sendContent.getText() + "\r\n").getBytes());
                else sio.writeAsync(sendContent.getText().toString().getBytes());
                Log.e("write", sendContent.getText().toString());
                sendContent.setText("");
            }
        });
        mode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mode_select != null) {
                    sio.stop();
                    mode_select.show();
                }
            }
        });
        filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                talkID_filter_builder = new QMUIDialog.EditTextDialogBuilder(UsbListActivity.this);
                talkID_filter_builder.setTitle("请输入要过滤的TalkID!");
                talkID_filter_builder.setPlaceholder("请输入要过滤的TalkID!");
                talkID_filter_builder.addAction(" 确定", new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        dialog.dismiss();
                    }
                });
                talkID_filter_builder.create().show();
            }
        });
        analysis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                analysis_select.show();
            }
        });
        simulate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                simulator_select.show();
            }
        });
        auto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ESP_auto.show();
            }
        });
        manual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                normal.setVisibility(View.VISIBLE);
                at.setVisibility(GONE);
            }
        });
        usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        deviceNum = usbManager.getDeviceList().size();
        availableDevice = new ArrayList<>();
        baseAdapter = new BaseAdapter() {
            @Override
            public int getCount() {
                return availableDevice.size();
            }

            @Override
            public Object getItem(int i) {
                return availableDevice.get(i);
            }

            @Override
            public long getItemId(int i) {
                return i;
            }

            @TargetApi(Build.VERSION_CODES.M)
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public View getView(int i, View view, ViewGroup viewGroup) {
                if (view == null)
                    view = View.inflate(UsbListActivity.this, R.layout.usb_list_item, null);
                TextView product_name = view.findViewById(R.id.product_name);
                TextView version = view.findViewById(R.id.version);
                TextView manufacturer = view.findViewById(R.id.manufacturer_name);
                TextView sn = view.findViewById(R.id.SN);
                TextView vendor = view.findViewById(R.id.vendor_id);
                TextView product = view.findViewById(R.id.product_id);
                UsbDevice usb = ((UsbSerialPort) getItem(i)).getDriver().getDevice();
                product_name.setText(usb.getProductName());
                version.setText("Ver:" + usb.getVersion());
                manufacturer.setText(usb.getManufacturerName());
                sn.setText("SN:" + usb.getSerialNumber());
                vendor.setText("VendorID:" + usb.getVendorId());
                product.setText("ProductID:" + usb.getProductId());
                return view;
            }
        };
        schedule_data();
        schedule_func();
        handler.sendEmptyMessageDelayed(0, 1000);
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(receiver);
        super.onDestroy();
    }

    //打开usb并初始化串口参数
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void iniPort(SerialPortSet set) {
        try {
            if (!open) {
                serialPort.open(usbManager.openDevice(serialPort.getDriver().getDevice()));
                open = true;
            } else sio.stop();
            serialPort.setParameters(set.baudrate, set.databit, set.stopbit, set.parity);
            func.setVisibility(View.VISIBLE);
            //Toast.makeText(this, "串口打开成功，参数：" + set.baudrate + " " + set.databit + " " + set.parity + " " + set.stopbit, Toast.LENGTH_SHORT).show();
            Toasty.success(this, "串口打开成功，参数：" + set.baudrate + " " + set.databit + " " + set.parity + " " + set.stopbit).show();
            content.append("串口初始化完毕！\r\n");
        } catch (IOException e) {
            e.printStackTrace();
            info.setText("USB串口初始化失败！");
        }
        if (sb == null) sb = new StringBuilder();
        sio = new SerialInputOutputManager(serialPort, new SerialInputOutputManager.Listener() {
            @Override
            public void onNewData(final byte[] data) {
                String s = null;
                s = new String(data);
                Log.e("data", s);
                //先判断模式，1表示nmea数据，3表示普通串口数据
                if (nmea_mode) {
                    Log.e("NMEA=true", "NMEA模式");
                    if (s.contains("\n")) {
                        Message msg = Message.obtain();
                        msg.what = 1;
                        msg.obj = sb + "\n";
                        Log.e("NmeaInfo", sb.toString());
                        handler.sendMessage(msg);
                        sb.delete(0, sb.length());
                    } else sb.append(s);
                } else if (at_mode) {
                    //AT模式
                    Log.e("AT=true", "AT模式" + s.contains("\r\n"));
                    //if (s.contains("\r\n")) {
                    Message msg = Message.obtain();
                    msg.what = 3;
                    msg.obj = s;
                    //Log.e("AT_Info", sb.toString());
                    handler.sendMessage(msg);
                    AT_result.append(s);
                    //sb.delete(0, sb.length());
                    //} else sb.append(s);
                } else {
                    //普通数据模式
                    Log.e("NORMAL=TRUE", s);
                    Message msg = Message.obtain();
                    msg.what = 5;
                    msg.obj = s;
                    handler.sendMessage(msg);
                }
            }

            @Override
            public void onRunError(Exception e) {

            }
        });
        func.setVisibility(View.VISIBLE);
        //创建模式对话框
        QMUIDialog.MenuDialogBuilder builder = new QMUIDialog.MenuDialogBuilder(this);
        builder.addItem("Normal", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                nmea_mode = false;
                at_mode = false;
                //Toast.makeText(UsbListActivity.this, "Normal模式", Toast.LENGTH_SHORT).show();
                Toasty.success(UsbListActivity.this, "Normal模式").show();
                content.setVisibility(View.VISIBLE);
                normal.setVisibility(View.VISIBLE);
                at.setVisibility(GONE);
                nmea.setVisibility(GONE);
                dialogInterface.dismiss();
                new Thread(sio).start();
            }
        });
        builder.addItem("NMEA数据", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                nmea_mode = true;
                at_mode = false;
                //Toast.makeText(UsbListActivity.this, "NMEA模式", Toast.LENGTH_SHORT).show();
                Toasty.success(UsbListActivity.this, "NMEA模式").show();
                content.setVisibility(View.VISIBLE);
                nmea.setVisibility(View.VISIBLE);
                at.setVisibility(GONE);
                normal.setVisibility(GONE);
                dialogInterface.dismiss();
                //创建Nmea模式下的解析语句选择对话框
                final QMUIDialog.MenuDialogBuilder NMEA_builder = new QMUIDialog.MenuDialogBuilder(UsbListActivity.this);
                NMEA_builder.addItem("GGA", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        analysis_mode = "GGA";
                        //Toast.makeText(UsbListActivity.this, "解析GGA", Toast.LENGTH_SHORT).show();
                        Toasty.success(UsbListActivity.this, "解析GGA").show();
                        dialogInterface.dismiss();
                        analy_position_alert = Util.creat_analy_position_dialog(UsbListActivity.this);
                    }
                });
                NMEA_builder.addItem("HDG", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        analysis_mode = "HDG";
                        //Toast.makeText(UsbListActivity.this, "解析HDG", Toast.LENGTH_SHORT).show();
                        Toasty.success(UsbListActivity.this, "解析HDG").show();
                        dialogInterface.dismiss();
                        analy_heading_alert = Util.creat_analy_heading_dialog(UsbListActivity.this);
                    }
                });
                analysis_select = NMEA_builder.create();
                //创建Nmea模式下的模拟语句选择对话框
                final QMUIDialog.MenuDialogBuilder NMEA_simulator = new QMUIDialog.MenuDialogBuilder(UsbListActivity.this);
                NMEA_simulator.addItem("GGA", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        simulator_mode = "GGA";
                        //Toast.makeText(UsbListActivity.this, "模拟GGA", Toast.LENGTH_SHORT).show();
                        Toasty.success(UsbListActivity.this, "模拟GGA").show();
                        dialogInterface.dismiss();
                        //  创建GGA基准数据对话框
                        QMUIDialog.CustomDialogBuilder GGA_builder = new QMUIDialog.CustomDialogBuilder(UsbListActivity.this);
                        GGA_builder.setTitle("GGA语句模拟基准数据");
                        GGA_builder.setLayout(R.layout.alert_simulator_setting);
                        GGA_builder.addAction("确定", new QMUIDialogAction.ActionListener() {
                            @Override
                            public void onClick(QMUIDialog dialog, int index) {
                                EditText para1 = dialog.findViewById(R.id.para1);
                                EditText para2 = dialog.findViewById(R.id.para2);
                                EditText para3 = dialog.findViewById(R.id.para3);
                                EditText para4 = dialog.findViewById(R.id.para4);
                                para3.setHint("请输入要模拟的速度！");
                                para4.setHint("请输入模拟时间的基准！");
                                final GGA gga = new GGA(para1.getText().toString(), para2.getText().toString(), para3.getText().toString(), para4.getText().toString());
                                //Toast.makeText(UsbListActivity.this, gga.toString(), Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                                new Thread() {
                                    @Override
                                    public void run() {
                                        super.run();
                                        String s = "GPGGA," + gga.time + "," + gga.lal + ",N," + gga.lon + ",E,1,04,1.0,3.0,M,,,,0000";
                                        sio.writeAsync(Util.simulator(s).getBytes());
                                        GGA_START = true;
                                        HDG_START = false;
                                        while (GGA_START) {
                                            SystemClock.sleep(990);
                                            //后期再完善经纬度依据速度和方向的模拟算法,此处仅做时间变化
                                            //s = "GPGGA," + (Double.parseDouble(gga.time) + n) + "," + gga.lal + ",N," + gga.lon + ",E,1,04,1.0,3.0,M,,,,0000";
                                            sio.writeAsync(Util.simulator(s).getBytes());
                                            //n++;
                                        }
                                    }
                                }.start();
                            }
                        });
                        simulator_setting = GGA_builder.create();
                        simulator_setting.show();
                    }
                });
                NMEA_simulator.addItem("HDG", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        simulator_mode = "HDG";
                        //Toast.makeText(UsbListActivity.this, "模拟HDG", Toast.LENGTH_SHORT).show();
                        Toasty.success(UsbListActivity.this, "模拟HDG").show();
                        dialogInterface.dismiss();
                    }
                });
                NMEA_simulator.addItem("停止模拟", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        GGA_START = false;
                        HDG_START = false;
                        //Toast.makeText(UsbListActivity.this, "模拟停止", Toast.LENGTH_SHORT).show();
                        Toasty.warning(UsbListActivity.this, "模拟停止").show();
                        dialogInterface.dismiss();
                    }
                });
                simulator_select = NMEA_simulator.create();
                new Thread(sio).start();
            }
        });
        builder.addItem("ESP8266", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                at_mode = true;
                nmea_mode = false;
                //Toast.makeText(UsbListActivity.this, "ESP8266模式", Toast.LENGTH_SHORT).show();
                Toasty.success(UsbListActivity.this, "ESP8266模式").show();
                content.setVisibility(View.VISIBLE);
                at.setVisibility(View.VISIBLE);
                normal.setVisibility(GONE);
                nmea.setVisibility(GONE);
                dialogInterface.dismiss();
                //创建AT模式下的几个常用自动模式对话框
                QMUIDialog.MenuDialogBuilder ESP_auto_builder = new QMUIDialog.MenuDialogBuilder(UsbListActivity.this);
                ESP_auto_builder.addItem("更改串口设置", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        serialPort_change = true;
                        Util.creat_set_port_dialog(UsbListActivity.this, null, handler).show();
                        //Toast.makeText(UsbListActivity.this, "AP模式", Toast.LENGTH_SHORT).show();
                    }
                });
                ESP_auto_builder.addItem("SAT_UDP_SERVER模式", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //Toast.makeText(UsbListActivity.this, "SAT_UDP_SERVER模式", Toast.LENGTH_SHORT).show();
                        Toasty.success(UsbListActivity.this, "SAT_UDP_SERVER模式").show();
                        dialogInterface.dismiss();
                        //创建一个ESP8266-SAT模式的配置对话框
                        QMUIDialog.CustomDialogBuilder ESP_setting = new QMUIDialog.CustomDialogBuilder(UsbListActivity.this);
                        ESP_setting.setLayout(R.layout.alert_esp8266_setting);
                        ESP_setting.addAction("确定", new QMUIDialogAction.ActionListener() {
                            @Override
                            public void onClick(QMUIDialog dialog, int index) {
                                EditText ssid = dialog.findViewById(R.id.ssid);
                                EditText psw = dialog.findViewById(R.id.psw);
                                EditText ip = dialog.findViewById(R.id.ip);
                                EditText port = dialog.findViewById(R.id.port);
                                ESP8266_SAT esp8266_sat = new ESP8266_SAT(ssid.getText().toString(), psw.getText().toString(), ip.getText().toString(), port.getText().toString());
                                dialog.dismiss();
                                ESP8266_UDP_SAT(esp8266_sat);
                            }
                        });
                        ESP_setting.create().show();
                    }
                });
                /*ESP_auto_builder.addItem("AP模式", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(UsbListActivity.this, "AP模式", Toast.LENGTH_SHORT).show();
                        dialogInterface.dismiss();
                    }
                });
                ESP_auto_builder.addItem("AP_SAT模式", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(UsbListActivity.this, "AP_SAT模式", Toast.LENGTH_SHORT).show();
                        dialogInterface.dismiss();
                    }
                });*/
                ESP_auto_builder.addItem("退出透传", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        sio.writeAsync("+++".getBytes());
                        //Toast.makeText(UsbListActivity.this, "退出透传", Toast.LENGTH_SHORT).show();
                        Toasty.warning(UsbListActivity.this, "退出透传").show();
                        dialogInterface.dismiss();
                    }
                });
                ESP_auto = ESP_auto_builder.create();
                new Thread(sio).start();
            }
        });
        mode_select = builder.create();
        mode_select.show();
        content.setVisibility(View.VISIBLE);
        mode.setVisibility(View.VISIBLE);
    }

    //弹窗配置串口参数
    /*public void setPort() {
        //先拿到保存在sp中的配置
        final SharedPreferences set_sp = getSharedPreferences("Portset", MODE_PRIVATE);
        //先配置一个试图的内容
        View v = View.inflate(UsbListActivity.this, R.layout.usb_set_alertdialog, null);
        final Spinner baudrate = v.findViewById(R.id.baudrate);
        final Integer[] baud = {4800, 9600, 38400, 74880, 115200};
        ArrayAdapter<Integer> baud_adapter = new ArrayAdapter(UsbListActivity.this, R.layout.support_simple_spinner_dropdown_item, baud);
        baudrate.setAdapter(baud_adapter);
        baudrate.setSelection(search(baud, set_sp.getInt("baudrate", 4800)));

        final Spinner databit = v.findViewById(R.id.databit);
        final Integer[] data = {5, 6, 7, 8};
        ArrayAdapter<Integer> data_adapter = new ArrayAdapter(UsbListActivity.this, R.layout.support_simple_spinner_dropdown_item, data);
        databit.setAdapter(data_adapter);
        databit.setSelection(search(data, set_sp.getInt("databit", 8)));

        final Spinner stopbit = v.findViewById(R.id.stopbit);
        final Integer[] stop = {1, 2, 3};
        ArrayAdapter stop_adapter = new ArrayAdapter(UsbListActivity.this, R.layout.support_simple_spinner_dropdown_item, stop);
        stopbit.setAdapter(stop_adapter);
        stopbit.setSelection(search(stop, set_sp.getInt("stopbit", 1)));

        final Spinner parity = v.findViewById(R.id.parity);
        final String[] p = {"NONE", "ODD", "EVEN", "MARK", "SPACE"};
        Integer[] p_map = {0, 1, 2, 3, 4};
        ArrayAdapter parity_adapter = new ArrayAdapter(UsbListActivity.this, R.layout.support_simple_spinner_dropdown_item, p);
        parity.setAdapter(parity_adapter);
        parity.setSelection(search(p_map, set_sp.getInt("parity", 0)));
        //为该试图添加控件的行为
        //先构建一个窗口的初始化参数对象set
        set = new SerialPortSet(set_sp.getInt("baudrate", 4800), set_sp.getInt("databit", 8), set_sp.getInt("stopbit", 1), set_sp.getInt("parity", 0));
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
        AlertDialog.Builder builder = new AlertDialog.Builder(UsbListActivity.this);
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
                set_sp.edit().putInt("baudrate", set.baudrate)
                        .putInt("databit", set.databit)
                        .putInt("stopbit", set.stopbit)
                        .putInt("parity", set.parity)
                        .commit();
                iniPort();
            }
        });
        builder.create().show();
    }*/


    //检测刷新usb在线流程
    //1.数据部分
    public boolean schedule_data() {
        //创建的时候初始化usblist，获取所有连接的usb设备
        HashMap<String, UsbDevice> map = usbManager.getDeviceList();
        /*/dev/bus/usb/001/002=UsbDevice[mName=/dev/bus/usb/001/002,mVendorId=1659,mProductId=8963,
                mManufacturerName=Prolific Technology Inc.,mProductName=USB-Serial Controller,mVersion=1.16,
                mSerialNumber=null,mConfigurations=[UsbConfiguration[mId=1,mName=null,mAttributes=128,mMaxPower=50*/
        if (map.isEmpty()) {
            info.setText("没有USB设备连接");
            info.setTextColor(getResources().getColor(R.color.colorAccent));
            availableDevice.clear();
            baseAdapter.notifyDataSetChanged();
            return false;
        } else {
            List<UsbSerialDriver> availableUsbDriver = UsbSerialProber.getDefaultProber().findAllDrivers(usbManager);
            for (UsbSerialDriver driver : availableUsbDriver) {
                for (UsbSerialPort port : driver.getPorts())
                    if (!availableDevice.contains(port))
                        availableDevice.add(port);
            }
            info.setText("已连接USB个数：" + map.size() + "  \n可用USB个数：" + availableDevice.size());
            info.setTextColor(getResources().getColor(R.color.colorPrimary));
            return true;
        }
    }

    //2.行为部分
    public void schedule_func() {
        usb_list.setAdapter(baseAdapter);
        usb_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                serialPort = (UsbSerialPort) baseAdapter.getItem(i);
                //判断是否有usb权限
                UsbDevice device = serialPort.getDriver().getDevice();
                if (usbManager.hasPermission(device)) {
                    //Toast.makeText(UsbListActivity.this, "已获取权限！", Toast.LENGTH_SHORT).show();
                    Toasty.success(UsbListActivity.this, "已获取权限！").show();
                    Util.creat_set_port_dialog(UsbListActivity.this, set_sp, handler).show();
                }
                //无权限请求usb权限
                else {
                    //Toast.makeText(UsbListActivity.this, "请先获取USB权限！", Toast.LENGTH_SHORT).show();
                    Toasty.error(UsbListActivity.this, "请先获取USB权限！").show();
                    usbManager.requestPermission(device, PendingIntent.getBroadcast(UsbListActivity.this, 0, new Intent(ACTION_USB_PERMISSION), 0));
                }
            }
        });
    }

    //抽取一个函数，传入参数为指令数组，搞几个模式的指令数组往里传
    //SAT模式下使用Client方式连接到主IP
    public void ESP8266_UDP_SAT(final ESP8266_SAT esp8266_sat) {
        loading.show();
        new Thread() {
            @Override
            public void run() {
                List<String> cmd = new LinkedList<String>();
                cmd.add("ATE1\r\n");
                cmd.add("AT+CWMODE=1\r\n");
                cmd.add("AT+CWJAP_DEF=\"" + esp8266_sat.ssid + "\",\"" + esp8266_sat.psw + "\"\r\n");
                cmd.add("AT+CIPSTART=\"UDP\",\"" + esp8266_sat.ip + "\"," + esp8266_sat.port + ",9000,0\r\n");
                cmd.add("AT+CIPMODE=1\r\n");
                cmd.add("AT+CIPSEND\r\n");
                if (CMD(cmd))
                    handler.sendEmptyMessage(4);
                else handler.sendEmptyMessage(8);
            }
        }.start();
    }

    public void ESP_set_port(final SerialPortSet set) {
        loading.show();
        new Thread() {
            @Override
            public void run() {
                String s = "AT+UART_CUR=" + set.baudrate + "," + set.databit + "," + set.stopbit + "," + set.parity + ",0\r\n";
                List<String> cmd = new LinkedList<>();
                cmd.add(s);
                if (CMD(cmd)) {
                    Message msg = Message.obtain();
                    msg.what = 6;
                    msg.obj = set;
                    handler.sendMessage(msg);
                } else handler.sendEmptyMessage(9);
            }
        }.start();
    }

    public boolean CMD(List<String> cmd) {
        String s = cmd.get(0);
        sio.writeAsync(s.getBytes());
        Log.e("cmd_write", s);
        if (!s.contains("AT+CWJAP_DEF=") && !s.contains("AT+CIPSTART")) {
            SystemClock.sleep(300);
        } else {
            SystemClock.sleep(18000);
        }
        Log.e("AT_result", AT_result.toString());
        if (AT_result.toString().contains("OK")) {
            AT_result.delete(0, AT_result.length());
            cmd.remove(0);
            if (cmd.size() > 0) {
                if (CMD(cmd))
                    return true;
                else return false;
            } else return true;
        } else return false;
    }

    AlertDialog creatProgress() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(R.layout.alert_loading);
        builder.setCancelable(false);
        return builder.create();
    }

    //广播接收器用于响应usb授权的结果
    class usbReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_USB_PERMISSION.equals(intent.getAction())) {
                boolean result = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false);
                //Log.e("result_permission", result + "");
                if (result)
                    Util.creat_set_port_dialog(UsbListActivity.this, set_sp, handler).show();
                else
                    //Toast.makeText(UsbListActivity.this, "请再次点击设备许可USB权限！", Toast.LENGTH_SHORT).show();
                    Toasty.error(UsbListActivity.this, "请再次点击设备许可USB权限！").show();
            }
            Log.e("Broadcast", "success");
        }
    }

}


//AP时使用Server方式等待客户端连接进来
   /* SERVER方式应该是不能使用透传功能的？？
ESP8266_AP() {
                sio.writeAsync("AT\r\n".getBytes());
                    sio.writeAsync("AT+CWMODE=2\r\n".getBytes());
                        sio.writeAsync("AT+CWSAP=\"ESP8266\",\"0123456789\",11,3\r\n".getBytes());
                            sio.writeAsync("AT+CIPMUX=1\r\n".getBytes());
                                sio.writeAsync("AT+CIPSERVER=1,5000\r\n".getBytes());
                                    sio.writeAsync("AT+CIPMODE=1\r\n".getBytes());
                                        sio.writeAsync("AT+CIPSEND\r\n".getBytes());
                                            handler.sendEmptyMessage(4);
             }

   AP_SAT() {
                    sio.writeAsync("AT+CWMODE=3\r\n".getBytes());
                        sio.writeAsync("AT+CWJAP_DEF=\"Rowsen-K\",\"rzcs0608\"\r\n".getBytes());
                            sio.writeAsync("AT+CIPSTART=\"TCP\",\"192.168.43.1\",9888\r\n".getBytes());
                                sio.writeAsync("AT+CIPMODE=1\r\n".getBytes());
                                    sio.writeAsync("AT+CIPSEND\r\n".getBytes());
                                        handler.sendEmptyMessage(4);
            }*/