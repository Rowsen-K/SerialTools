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

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.dmoral.toasty.Toasty;

public class TcpServerActivity extends AppCompatActivity {

    @BindView(R.id.host_ip)
    EditText hostIp;
    @BindView(R.id.listen_port)
    EditText listenPort;
    @BindView(R.id.connect)
    QMUIRoundButton connect;
    @BindView(R.id.content)
    TextView content;

    ServerSocket server;
    Socket socket;
    InputStream in;
    boolean connect_state = false;
    Handler handler;
    String remote_ip;

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
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 0:
                        Util.textProcess(content, "主机：" + msg.obj + "连进来了！\r\n");
                        break;
                    case 1:
                        Util.textProcess(content, "连接失败！错误信息：" + msg.obj);
                        break;
                    case 2:
                        Util.textProcess(content, remote_ip + ":" + msg.obj);
                        break;
                    case 3:
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
                                    server = new ServerSocket(port);
                                    socket = server.accept();
                                    in = socket.getInputStream();
                                    connect_state = true;
                                    connect.setText("断开");
                                    Message msg = Message.obtain();
                                    msg.what = 0;
                                    remote_ip = socket.getInetAddress().getHostAddress();
                                    msg.obj = remote_ip;
                                    handler.sendMessage(msg);
                                    byte[] data = new byte[1024];
                                    int n = 0;
                                    while (connect_state) {
                                        if ((n = in.read(data)) > 0) {
                                            Message msg_read = Message.obtain();
                                            msg_read.what = 2;
                                            msg_read.obj = new String(data, 0, n);
                                            handler.sendMessage(msg);
                                        }
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
                        Toasty.error(TcpServerActivity.this, "端口范围1025-65536").show();
                        connect.setEnabled(true);
                        listenPort.setEnabled(true);
                    }
                } else {
                    connect_state = false;
                    if (socket != null && socket.isConnected()) {
                        Log.e("tcp", "断开");
                        try {
                            socket.close();
                            server.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    connect.setText("连接");
                    connect.setEnabled(true);
                    listenPort.setEnabled(true);
                }
            }
        });
    }
}
