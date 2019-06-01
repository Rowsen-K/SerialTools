package com.rowsen.serialtools.Utils;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rowsen.serialtools.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ContructFragment extends Fragment {
    View v;

    public ContructFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if(v == null){
            v = inflater.inflate(R.layout.fragment_contruct,container,false);
        }
        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ((ViewGroup) (v.getParent())).removeView(v);
    }
}
