package com.schspa;

import android.content.Context;
import android.graphics.Color;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.ingenic.schspa.wake_lock.R;


/**
 * Created by schspa on 11/18/15.
 * each file have one this view
 */
public class SysfsInterfaceView {
    private final String TAG = "SysfsInterfaceView";
    private Context context;
    private TextView tv_name, tv_value;
    private ToggleButton sw;
    private String path;
    private String dir;
    private String filename;
    private SysfsInterface sysfs = new SysfsInterface();
    private int name_width;
    private boolean writeable;

    public SysfsInterfaceView(Context context, String dir, String name, int width) {
        this.context = context;
        this.dir = dir;
        this.filename = name;
        this.name_width = (width*4)/10;
        path = new String(dir+"/"+name);
    }

    public View get_view() {

        String ret = sysfs.read(path);
        View v = null;
        if (ret == null) {
            writeable = false;
        } else {
            Log.d(TAG, "ret = "+ret);
            switch (ret) {
                case "N":
                    v = BoolView(context, dir, filename, false);
                    break;
                case "Y":
                    v = BoolView(context, dir, filename, true);
                    break;
                default:
                    v = IntView(context, dir, filename, Integer.valueOf(ret));
                    break;
            }
            writeable = true;
        }
        return v;
    }
    public boolean getwriteable() {
        return sysfs.writeable(path);
    }

        public View BoolView(Context context, String dir, String filename, boolean initvalue) {
            LayoutInflater li = LayoutInflater.from(context);

            if (!getwriteable()) {
                View v = li.inflate(R.layout.value_view_rdonly, null);
                TextView name = (TextView) v.findViewById(R.id.name);
                TextView value = (TextView) v.findViewById(R.id.value);
                name.setText(filename);
                name.setMaxWidth(name_width);
                value.setText(String.valueOf(initvalue));
                return v;
            }

            View v = li.inflate(R.layout.boot_view_rdwr, null);
            TextView name = (TextView) v.findViewById(R.id.name);
            name.setMaxWidth(name_width);
            name.setText(filename);
            ToggleButton tg = (ToggleButton) v.findViewById(R.id.valueswitch);
            if (initvalue)
                tg.setChecked(true);
            else
                tg.setChecked(false);
            tg.setOnCheckedChangeListener(OnSwitch);
            return v;
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
                buttonView.setText(ret);
            }
        };

    private EditText ed_int_value;
    public View IntView(Context context, String dir, String filename, int initvalue) {
        LayoutInflater li = LayoutInflater.from(context);

        if (!getwriteable()) {
            View v = li.inflate(R.layout.value_view_rdonly, null);
            TextView name = (TextView) v.findViewById(R.id.name);
            name.setMaxWidth(name_width);
            TextView value = (TextView) v.findViewById(R.id.value);
            name.setText(filename);
            value.setText(String.valueOf(initvalue));
            return v;
        }
        View v = li.inflate(R.layout.int_view_rdwr, null);
        TextView name = (TextView) v.findViewById(R.id.name);
        name.setMaxWidth(name_width);
        name.setText(filename);
        ed_int_value = (EditText) v.findViewById(R.id.edittext);
        Button save_button = (Button) v.findViewById(R.id.bt_save);
        ed_int_value.setText(String.valueOf(initvalue));
        save_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sysfs.write(path, ed_int_value.getText().toString());
                ed_int_value.setText(sysfs.read(path));
            }
        });

        return v;
    }
}
