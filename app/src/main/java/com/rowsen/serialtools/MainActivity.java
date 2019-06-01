package com.rowsen.serialtools;

import android.os.Bundle;
import android.util.Log;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.qmuiteam.qmui.widget.QMUIViewPager;
import com.rowsen.serialtools.Utils.ContructFragment;
import com.rowsen.serialtools.Utils.ElecFragment;
import com.rowsen.serialtools.Utils.GearFragment;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {


    @BindView(R.id.vp)
    QMUIViewPager vp;
    @BindView(R.id.elec)
    RadioButton elec;
    @BindView(R.id.gear)
    RadioButton gear;
    @BindView(R.id.contruct)
    RadioButton contruct;
    @BindView(R.id.group)
    RadioGroup group;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        if (getSupportActionBar() != null)
            getSupportActionBar().hide();
        final List<Fragment> list = new ArrayList();
        list.add(new ElecFragment());
        list.add(new GearFragment());
        list.add(new ContructFragment());
        FragmentPagerAdapter adapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            @NonNull
            @Override
            public Fragment getItem(int position) {
                return list.get(position);
            }

            @Override
            public int getCount() {
                return list.size();
            }
        };
        vp.setEnableLoop(true);
        vp.setSwipeable(true);
        vp.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                //Log.e("position",position+"");
                switch (position) {
                    case 0:
                        group.check(R.id.elec);
                        elec.setBackgroundColor(getResources().getColor(R.color.qmui_btn_blue_bg));
                        gear.setBackgroundColor(getResources().getColor(R.color.qmui_config_color_white));
                        contruct.setBackgroundColor(getResources().getColor(R.color.qmui_config_color_white));
                        break;
                    case 1:
                        group.check(R.id.gear);
                        gear.setBackgroundColor(getResources().getColor(R.color.qmui_btn_blue_bg));
                        elec.setBackgroundColor(getResources().getColor(R.color.qmui_config_color_white));
                        contruct.setBackgroundColor(getResources().getColor(R.color.qmui_config_color_white));
                        break;
                    case 2:
                        group.check(R.id.contruct);
                        contruct.setBackgroundColor(getResources().getColor(R.color.qmui_btn_blue_bg));
                        gear.setBackgroundColor(getResources().getColor(R.color.qmui_config_color_white));
                        elec.setBackgroundColor(getResources().getColor(R.color.qmui_config_color_white));
                        break;
                }

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        vp.setAdapter(adapter);
        group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                //Log.e("group", i + "");
                switch (i) {
                    case R.id.elec:
                        vp.setCurrentItem(0);
                        break;
                    case R.id.gear:
                        vp.setCurrentItem(1);
                        break;
                    case R.id.contruct:
                        vp.setCurrentItem(2);
                        break;
                }
            }
        });
        elec.setBackgroundColor(getResources().getColor(R.color.qmui_btn_blue_bg));
    }

    @Override
    protected void onStart() {
        super.onStart();

    }
}
