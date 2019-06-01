package com.rowsen.serialtools.Utils;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Toast;

import com.rowsen.serialtools.BluetoothActivity;
import com.rowsen.serialtools.R;
import com.rowsen.serialtools.UsbListActivity;
import com.rowsen.serialtools.WifiActivity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class ElecFragment extends Fragment {
    View v;
    Unbinder unbinder;
    @BindView(R.id.elec_header)
    ImageView elecHeader;
    @BindView(R.id.wifi)
    RadioButton wifi;
    @BindView(R.id.usb)
    RadioButton usb;
    @BindView(R.id.bluetooth)
    RadioButton bluetooth;
    @BindView(R.id.func4)
    RadioButton func4;

    public ElecFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (v == null) {
            v = inflater.inflate(R.layout.fragment_serial, container, false);
            unbinder = ButterKnife.bind(this, v);
            wifi.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(getContext(), WifiActivity.class));
                }
            });
            usb.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(getContext(),UsbListActivity.class));
                }
            });
            bluetooth.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(getContext(), BluetoothActivity.class));
                }
            });
        }
        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        //unbinder.unbind();
        ((ViewGroup) (v.getParent())).removeView(v);
    }
}
