package com.schspa;

import android.content.Context;
import android.graphics.Color;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.ingenic.schspa.wake_lock.R;


/**
 * Created by schspa on 11/18/15.
 * each file have one this view
 */
public class SysfsInterfaceView extends LinearLayout {
    private final String TAG = "SysfsInterfaceView";
    private Context context;
    private TextView tv_name, tv_value;
    private ToggleButton sw;
    private String path;
    private SysfsInterface sysfs = new SysfsInterface();
    private boolean writeable;
    public SysfsInterfaceView(Context context, String dir, String name) {
        super(context);
        this.context = context;

        path = new String(dir+"/"+name);
        String ret = sysfs.read(path);
        if (ret == null) {
//            sw.setActivated(false);
            writeable = false;
        } else {
            Log.d(TAG, "ret = "+ret);
            switch (ret) {
                case "N":
                    BoolView(context, this, dir, name, false);
//                    sw.setChecked(false);
                    break;
                case "Y":
//                    sw.setChecked(true);
                    BoolView(context, this, dir, name, true);
                    break;
                default:
                    IntView(context, this, dir, name, Integer.valueOf(ret));
                    break;
            }
            writeable = true;
        }
    }

    public boolean getwriteable() {
        return sysfs.writeable(path);
    }

        public void BoolView(Context context, LinearLayout ll, String dir, String name, boolean initvalue) {
            ll.setOrientation(HORIZONTAL);
            tv_name = new TextView(context);
            tv_name.setText(name+":");
            tv_name.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
            ll.addView(tv_name);

            if (!getwriteable()) {
                TextView value = new TextView(context);
                value.setText(String.valueOf(initvalue));
                value.setTextColor(getResources().getColor(R.color.error_color));
                ll.addView(value);
                return ;
            }
            sw = new ToggleButton(context);
            if (initvalue)
                sw.setChecked(true);
            else
                sw.setChecked(false);
            sw.setOnCheckedChangeListener(OnSwitch);

            ll.addView(sw);
        }

        CompoundButton.OnCheckedChangeListener OnSwitch = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sysfs.write(path, isChecked? "Y": "N");
                String ret = sysfs.read(path);
                switch (ret) {
                    case "N":
                        buttonView.setChecked(false);
                        break;
                    case "Y":
                        buttonView.setChecked(true);
                        break;
                }
                sw.setText(ret);
            }
        };

    private EditText et;
    private Button save_button;
    public void IntView(Context context, LinearLayout ll, String dir, String name, int initvalue) {
        ll.setOrientation(HORIZONTAL);
        tv_name = new TextView(context);
        tv_name.setText(name+":");
        tv_name.setTextColor(getResources().getColor(R.color.colorPrimary));
        ll.addView(tv_name);

        if (!getwriteable()) {
            TextView value = new TextView(context);
            value.setText(String.valueOf(initvalue));
            value.setTextColor(getResources().getColor(R.color.colorAccent));
            ll.addView(value);
            return ;
        }
        et = new EditText(context);
        et.setText(String.valueOf(initvalue));
        et.setTextColor(Color.BLACK);

        et.setInputType(InputType.TYPE_CLASS_NUMBER
                | InputType.TYPE_NUMBER_FLAG_SIGNED);
        save_button = new Button(context);
        save_button.setText("Save");
        save_button.setTextColor(Color.BLACK);
        save_button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                sysfs.write(path, et.getText().toString());
                et.setText(sysfs.read(path));
            }
        });

        ll.addView(et);
        ll.addView(save_button);
    }
}
