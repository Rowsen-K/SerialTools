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
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.dmoral.toasty.Toasty;

public class TcpClientActivity extends AppCompatActivity {

    @BindView(R.id.host_ip)
    EditText hostIp;
    @BindView(R.id.listen_port)
    EditText listenPort;
    @BindView(R.id.connect)
    QMUIRoundButton connect;
    @BindView(R.id.content)
    TextView content;
    @BindView(R.id.clear)
    QMUIRoundButton clear;
    String ip;
    int port;
    Socket socket;
    byte[] data;
    InputStream in;
    boolean connect_state = false;
    Handler handler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_mode);
        ButterKnife.bind(this);
        if (getSupportActionBar() != null)
            getSupportActionBar().hide();
        content.setMovementMethod(ScrollingMovementMethod.getInstance());
        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch(msg.what){
                    case 0:
                        Util.textProcess(content,msg.obj+"");
                        break;
                }
            }
        };
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                content.setText("");
            }
        });
        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                connect.setEnabled(false);
                listenPort.setEnabled(false);
                if(!connect_state){
                ip = hostIp.getText().toString().trim();
                String s = listenPort.getText().toString().trim();
                port = Integer.parseInt(s);
                if (!TextUtils.isEmpty(s) && s.matches("\\d{4,5}") && port < 65535) {
                    new Thread() {
                        @Override
                        public void run() {
                            try {
                                socket = new Socket(InetAddress.getByName(ip),port);
                                in = socket.getInputStream();
                                connect_state = true;
                                int n = 0;
                                while ((n = in.read(data))>0) {
                                    Message msg = Message.obtain();
                                    msg.what = 0;
                                    msg.obj = new String(data, 0, n);
                                    handler.sendMessage(msg);
                                }
                            } catch (UnknownHostException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }.start();
                }
                else {
                    Toasty.error(TcpClientActivity.this, "端口范围1025-65536").show();
                    connect.setEnabled(true);
                    listenPort.setEnabled(true);
                }
            }
                else {
                connect_state = false;
                if (socket != null && socket.isConnected()) {
                    Log.e("tcp", "断开");
                    try {
                        socket.close();
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
