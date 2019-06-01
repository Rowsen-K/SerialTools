package com.rowsen.serialtools;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundButton;
import com.rowsen.serialtools.Wifi_mode.TcpClientActivity;
import com.rowsen.serialtools.Wifi_mode.TcpServerActivity;
import com.rowsen.serialtools.Wifi_mode.UdpClientActivity;
import com.rowsen.serialtools.Wifi_mode.UdpServerActivity;

import butterknife.BindView;
import butterknife.ButterKnife;


public class WifiActivity extends AppCompatActivity implements View.OnClickListener {

    @BindView(R.id.udp_server)
    QMUIRoundButton udpServer;
    @BindView(R.id.udp_client)
    QMUIRoundButton udpClient;
    @BindView(R.id.tcp_server)
    QMUIRoundButton tcpServer;
    @BindView(R.id.tcp_client)
    QMUIRoundButton tcpClient;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi);
        ButterKnife.bind(this);
        if (getSupportActionBar() != null)
            getSupportActionBar().hide();
        udpServer.setOnClickListener(this);
        udpClient.setOnClickListener(this);
        tcpServer.setOnClickListener(this);
        tcpClient.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.udp_server:
                startActivity(new Intent(this, UdpServerActivity.class));
                break;
            case R.id.udp_client:
                startActivity(new Intent(this, UdpClientActivity.class));
                break;
            case R.id.tcp_server:
                startActivity(new Intent(this, TcpServerActivity.class));
                break;
            case R.id.tcp_client:
                startActivity(new Intent(this, TcpClientActivity.class));
                break;
            default:
                Log.e("view_id", view.getId() + "");
                break;
        }
    }
}
