package com.rowsen.serialtools;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.qmuiteam.qmui.widget.QMUIAnimationListView;
import com.qmuiteam.qmui.widget.QMUIProgressBar;
import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundButton;
import com.rowsen.serialtools.Utils.Position_ViewHold;
import com.rowsen.serialtools.Utils.Util;

import net.sf.marineapi.nmea.event.SentenceEvent;
import net.sf.marineapi.nmea.event.SentenceListener;
import net.sf.marineapi.nmea.io.ExceptionListener;
import net.sf.marineapi.nmea.io.SentenceReader;
import net.sf.marineapi.nmea.parser.SentenceFactory;
import net.sf.marineapi.nmea.sentence.GGASentence;
import net.sf.marineapi.nmea.sentence.RMCSentence;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.dmoral.toasty.Toasty;

public class BluetoothActivity extends AppCompatActivity implements ExceptionListener {
    public static UUID SerialPortService = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    BluetoothAdapter badapter;
    BluetoothDevice[] devices;
    BroadcastReceiver receiver;
    BluetoothSocket socket;
    InputStream in;
    BufferedOutputStream out;
    BaseAdapter listAdapter;
    Handler handler;
    ViewHolder connectView = new ViewHolder();
    boolean readFlag = false;//读取标记
    boolean threadFinish = true;//线程结束标记
    SentenceReader reader;
    SentenceFactory fac = SentenceFactory.getInstance();
    ListView analy_list;
    String[] analy_func = {"GGA", "RMC", "HDT"};
    Position_ViewHold positionViewHold;
    AlertDialog analy_dialog;
    SentenceListener listener;
    boolean analysis_start = false;
    @BindView(R.id.bluetooth_list)
    QMUIAnimationListView bluetooth_list;
    @BindView(R.id.connectdevice)
    LinearLayout connectDevice;
    @BindView(R.id.selectDevice)
    QMUIRoundButton selectDevice;
    @BindView(R.id.read)
    QMUIRoundButton read;
    @BindView(R.id.content)
    TextView content;
    @BindView(R.id.sendContent)
    EditText sendContent;
    @BindView(R.id.send)
    QMUIRoundButton send;
    @BindView(R.id.analysis)
    QMUIRoundButton analysis;
    @BindView(R.id.func)
    LinearLayout func;
    @BindView(R.id.progressBar)
    QMUIProgressBar progressBar;
    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.analy_list)
    QMUIAnimationListView analyList;
    @BindView(R.id.drawer)
    DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);
        ButterKnife.bind(this);
        if (getSupportActionBar() != null)
            getSupportActionBar().hide();
        ArrayAdapter adapter = new ArrayAdapter(this, R.layout.item_analy_list, analy_func);
        analyList.setAdapter(adapter);
        analyList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.e("item", i + "");
                if (analy_dialog == null)
                    analy_dialog = Util.creat_analy_position_dialog(BluetoothActivity.this);
                analy_dialog.show();
                positionViewHold = Util.position_viewHold;
                drawerLayout.closeDrawer(Gravity.RIGHT);
                switch (i) {
                    case 0:
                        analysis(analy_func[i], 0);
                        break;
                    case 1:
                        analysis(analy_func[i], 1);
                        break;
                }

            }
        });
        selectDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (readFlag) {
                    readFlag = !readFlag;
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                reader.stop();
                func.setVisibility(View.GONE);
                bluetooth_list.setVisibility(View.VISIBLE);
            }
        });
        read.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                readFlag = !readFlag;
                if (readFlag) {
                    read.setText("停止读取");
                    content.setVisibility(View.VISIBLE);
                    try {
                        in = socket.getInputStream();
                    } catch (IOException e) {
                        e.printStackTrace();
                        //Toast.makeText(BluetoothActivity.this, "读取数据失败！\n请确认对方蓝牙已开启！", Toast.LENGTH_SHORT).show();
                        Toasty.error(BluetoothActivity.this, "读取数据失败！\n请确认对方蓝牙已开启！").show();
                    }
                    //拿到输入输出流之后直接开启一个读取线程
                    new Thread() {
                        @Override
                        public void run() {
                            super.run();
                            byte[] temp = new byte[128];
                            int n = 0;
                            while (readFlag) {
                                threadFinish = false;
                                try {
                                    n = in.read(temp);
                                    Log.e("读取到的字符数", n + "");
                                    Message msg = Message.obtain();
                                    msg.what = 0;
                                    msg.obj = new String(temp, 0, n);
                                    handler.sendMessage(msg);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    handler.sendEmptyMessage(1);
                                }
                            }
                            threadFinish = true;
                        }
                    }.start();
                } else {
                    read.setText("读取数据");
                }
            }
        });
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    out.write(sendContent.getText().toString().getBytes("GBK"));
                    out.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        content.setMovementMethod(ScrollingMovementMethod.getInstance());
        badapter = BluetoothAdapter.getDefaultAdapter();
        if (badapter == null) {
            //Toast.makeText(this, "该设备不支持蓝牙", Toast.LENGTH_SHORT).show();
            Toasty.error(this, "该设备不支持蓝牙！").show();
            this.finish();
        } else {
            if (badapter.isEnabled()) {
                devicesShow();
            } else {
                progressBar.setVisibility(View.VISIBLE);
                new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        if (badapter.enable())
                            badapter.startDiscovery();
                        else
                            handler.sendEmptyMessage(3);
                    }
                }.start();

            }
        }
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                switch (action) {
                    case BluetoothAdapter.ACTION_STATE_CHANGED:
                        int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 13);
                        Log.e("蓝牙通知", "状态" + state);
                        switch (state) {
                            case BluetoothAdapter.STATE_TURNING_ON:
                                //Toast.makeText(BluetoothActivity.this, "蓝牙正在打开，请稍后！", Toast.LENGTH_SHORT).show();
                                Toasty.warning(BluetoothActivity.this, "蓝牙正在打开，请稍后！").show();
                                break;
                            case BluetoothAdapter.STATE_ON:
                                //Toast.makeText(BluetoothActivity.this, "蓝牙已打开！", Toast.LENGTH_SHORT).show();
                                devicesShow();
                                progressBar.setVisibility(View.GONE);
                                break;
                            case BluetoothAdapter.STATE_TURNING_OFF:
                                //Toast.makeText(BluetoothActivity.this, "蓝牙正在关闭，即将退出当前页！", Toast.LENGTH_SHORT).show();
                                Toasty.warning(BluetoothActivity.this, "蓝牙正在关闭，即将退出当前页！").show();
                                break;
                            case BluetoothAdapter.STATE_OFF:
                                //Toast.makeText(BluetoothActivity.this, "蓝牙已关闭！", Toast.LENGTH_SHORT).show();
                                finish();
                                break;
                        }
                        break;
                    case BluetoothDevice.ACTION_ACL_CONNECTED:
                        Log.e("蓝牙通知", "连接成功！");
                        //获取发送通知的蓝牙设备对象
                        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        //连接成功就将list隐藏，并将连接的设备单显出来，并显示出数据接收窗口
                        bluetooth_list.setVisibility(View.GONE);
                        View v = View.inflate(BluetoothActivity.this, R.layout.bluetooth_device_item, null);
                        connectView.name = v.findViewById(R.id.name);
                        connectView.name.setText("设备名：" + device.getName());
                        connectView.state = v.findViewById(R.id.state);
                        connectView.state.setText("状态：" + device.getBondState() + "| 蓝牙连接成功");
                        connectView.state.setTextColor(getResources().getColor(R.color.colorPrimary));
                        connectView.mac = v.findViewById(R.id.mac);
                        connectView.mac.setText("MAC地址：" + device.getAddress());
                        connectDevice.removeAllViews();
                        connectDevice.addView(v);
                        func.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);
                        //analysis();
                        break;
                    case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                        Log.e("蓝牙通知", "连接断开！");
                        if (socket != null) {
                            try {
                                socket.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        connectView.state.setText("状态：蓝牙连接已断开！");
                        connectView.state.setTextColor(getResources().getColor(R.color.colorAccent));
                        break;
                }
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        registerReceiver(receiver, filter);
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 0:
                        //加入收到数据后的处理逻辑
                        content.setVisibility(View.VISIBLE);
                        String s = (String) msg.obj;
                        Util.textProcess(content, s);
                        break;
                    case 1:
                        //读取流故障时的情况
                        //Toast.makeText(BluetoothActivity.this, "读取数据异常！", Toast.LENGTH_SHORT).show();
                        Toasty.error(BluetoothActivity.this, "读取数据异常！").show();
                        break;
                    case 2:
                        //连接蓝牙的时候报错
                        //Toast.makeText(BluetoothActivity.this, "创建蓝牙串口服务失败！\n请确认对方蓝牙已打开！", Toast.LENGTH_SHORT).show();
                        Toasty.error(BluetoothActivity.this, "创建蓝牙串口服务失败！\n请确认对方蓝牙已打开！").show();
                        progressBar.setVisibility(View.GONE);
                        break;
                    case 3:
                        //Toast.makeText(BluetoothActivity.this, "蓝牙开启失败，即将退出当前页！", Toast.LENGTH_SHORT).show();
                        Toasty.error(BluetoothActivity.this, "蓝牙开启失败，即将退出当前页！").show();
                        finish();
                        break;
                    case 4:
                        //数据解析的结果返回了一个sentence
                        switch (msg.arg1) {
                            case 0:
                                GGASentence GGA = (GGASentence) msg.obj;
                                positionViewHold.longitude.setText(GGA.getPosition().getLongitude() + "");
                                positionViewHold.latitude.setText(GGA.getPosition().getLatitude() + "");
                                positionViewHold.time.setText(GGA.getTime() + "");
                                break;
                            case 1:
                                RMCSentence RMC = (RMCSentence) msg.obj;
                                positionViewHold.longitude.setText(RMC.getPosition().getLongitude() + "");
                                positionViewHold.latitude.setText(RMC.getPosition().getLatitude() + "");
                                positionViewHold.sog.setText(RMC.getSpeed() + "");
                                positionViewHold.cog.setText(RMC.getCourse() + "");
                                positionViewHold.time.setText(RMC.getTime() + "");
                                break;
                        }

                }
            }
        };
    }

    @Override
    public void onBackPressed() {
        unregisterReceiver(receiver);
        if (readFlag) {
            readFlag = !readFlag;
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (reader != null) reader.stop();
    }

    @Override
    protected void onDestroy() {
        onBackPressed();
        super.onDestroy();

    }

    //蓝牙设备列表展示
    public void devicesShow() {
        Set<BluetoothDevice> set = badapter.getBondedDevices();
        devices = new BluetoothDevice[set.size()];
        set.toArray(devices);
        //Log.e("devices", devices + "");
        listAdapter = new BaseAdapter() {
            @Override
            public int getCount() {
                return devices.length;
            }

            @Override
            public Object getItem(int i) {
                return devices[i];
            }

            @Override
            public long getItemId(int i) {
                return i;
            }

            @Override
            public View getView(int i, View view, ViewGroup viewGroup) {
                View v;
                if (view == null)
                    v = View.inflate(BluetoothActivity.this, R.layout.bluetooth_device_item, null);
                else v = view;
                TextView name = v.findViewById(R.id.name);
                TextView state = v.findViewById(R.id.state);
                TextView mac = v.findViewById(R.id.mac);
                name.setText("设备名：" + devices[i].getName());
                state.setText("状态：" + devices[i].getBondState());
                mac.setText("MAC地址：" + devices[i].getAddress());
                return v;
            }
        };
        bluetooth_list.setAdapter(listAdapter);
        bluetooth_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                progressBar.setVisibility(View.VISIBLE);
                final BluetoothDevice device = devices[i];
                new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        try {
                            socket = device.createRfcommSocketToServiceRecord(SerialPortService);
                            badapter.cancelDiscovery();
                            socket.connect();
                            out = new BufferedOutputStream(socket.getOutputStream());
                        } catch (IOException e) {
                            e.printStackTrace();
                            handler.sendEmptyMessage(2);
                        }
                    }
                }.start();
            }
        });
    }


    public void analysis(View view) {
        drawerLayout.openDrawer(Gravity.RIGHT, true);
    }

    @Override
    public void onException(Exception e) {
        Log.e("错误", e.getMessage());
    }

    static class ViewHolder {
        TextView name;
        TextView state;
        TextView mac;
    }

    //数据解析
    public void analysis(final String id, final int analy_type) {
        if (readFlag) {
            readFlag = !readFlag;
            read.setText("停止读取");
        }
        content.setVisibility(View.VISIBLE);
        new Thread() {
            @Override
            public void run() {
                super.run();
                while (!threadFinish) {
                }
                Log.e("开启解析", "start");
                if (in == null) {
                    try {
                        in = socket.getInputStream();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (reader == null) reader = new SentenceReader(in);
                reader.removeSentenceListener(listener);
                listener = new SentenceListener() {
                    @Override
                    public void readingPaused() {

                    }

                    @Override
                    public void readingStarted() {
                        analysis_start = true;
                    }

                    @Override
                    public void readingStopped() {

                    }

                    @Override
                    public void sentenceRead(SentenceEvent sentenceEvent) {
                        String s = sentenceEvent.getSentence().toSentence();
                        //Log.e("RAW",s);
                        //先将收到的事件送出更新UI
                        Message event = Message.obtain();
                        event.what = 0;
                        event.obj = s;
                        handler.sendMessage(event);
                        if (sentenceEvent.getSentence().getSentenceId().equals(id)) {
                            //Sentence sentence = fac.createParser(s);
                            Log.e(id, s);
                            Message parse = Message.obtain();
                            parse.what = 4;
                            parse.arg1 = analy_type;
                            parse.obj = sentenceEvent.getSentence();
                            handler.sendMessage(parse);
                        }
                    }
                };
                reader.addSentenceListener(listener);
                if (!analysis_start) reader.start();
            }
        }.start();

    }
}
