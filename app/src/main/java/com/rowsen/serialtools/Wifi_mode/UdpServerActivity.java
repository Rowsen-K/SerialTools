package com.rowsen.serialtools.Wifi_mode;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundButton;
import com.rowsen.serialtools.R;
import com.rowsen.serialtools.Utils.Util;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.dmoral.toasty.Toasty;

public class UdpServerActivity extends AppCompatActivity {

    @BindView(R.id.host_ip)
    TextView hostIp;
    @BindView(R.id.listen_port)
    EditText listenPort;
    @BindView(R.id.connect)
    QMUIRoundButton connect;
    @BindView(R.id.content)
    TextView content;
    @BindView(R.id.clear)
    QMUIRoundButton clear;
    private DatagramSocket datagramSocket;
    byte[] buf;
    private DatagramPacket data;
    Handler handler;
    private SocketAddress ad;

    boolean connect_state = false;

    /*待完善：
1.直接获取本地ip显示；bingo
2.断开不能释放端口，必须退出app再进入才能释放；
3.
*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_mode);
        ButterKnife.bind(this);
        if (getSupportActionBar() != null)
            getSupportActionBar().hide();
        hostIp.setText(Util.getLocalIpAddress());
        hostIp.setEnabled(false);
        content.setMovementMethod(ScrollingMovementMethod.getInstance());
        buf = new byte[1024];
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 0:
                        Util.textProcess(content, ad + "连进来了！\r\n");
                        break;
                    case 1:
                        Util.textProcess(content, msg.obj + "\r\n");
                        break;
                    case 2:
                        Util.textProcess(content, msg.obj + "\r\n");
                        break;
                }
            }
        };
        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                connect.setEnabled(false);
                listenPort.setEnabled(false);
                if (!connect_state) {
                    String s = listenPort.getText().toString().trim();
                    final int port = Integer.parseInt(s);
                    if (!TextUtils.isEmpty(s) && s.matches("\\d{4,5}") && port < 65535) {
                        new Thread() {
                            @Override
                            public void run() {
                                super.run();
                                try {
                                    datagramSocket = new DatagramSocket(port);
                                    ad = datagramSocket.getRemoteSocketAddress();
                                    if (ad != null)
                                        handler.sendEmptyMessage(0);
                                    data = new DatagramPacket(buf, 1024);
                                    connect_state = true;
                                    connect.setText("断开");
                                    while (connect_state) {
                                        datagramSocket.receive(data);
                                        Message msg = Message.obtain();
                                        msg.what = 1;
                                        msg.obj = new String(data.getData(), data.getOffset(), data.getLength());
                                        handler.sendMessage(msg);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    Message msg = Message.obtain();
                                    msg.what = 1;
                                    msg.obj = e.toString();
                                    handler.sendMessage(msg);
                                }
                            }
                        }.start();
                    } else {
                        //Toast.makeText(UdpServerActivity.this, "端口范围1025-65536", Toast.LENGTH_SHORT).show();
                        Toasty.error(UdpServerActivity.this, "端口范围1025-65536").show();
                        connect.setEnabled(true);
                        listenPort.setEnabled(true);
                    }
                } else {
                    connect_state = false;
                    if (datagramSocket != null && datagramSocket.isConnected()) {
                        Log.e("udp", "断开");
                        datagramSocket.disconnect();
                        //datagramSocket.close();
                    }
                    connect.setText("连接");
                    connect.setEnabled(true);
                    listenPort.setEnabled(true);
                }
            }
        });
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                content.setText("");
            }
        });
    }
}
