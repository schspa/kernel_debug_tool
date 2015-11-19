package com.ingenic.schspa.wake_lock;

import android.os.Bundle;
import android.os.PowerManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.util.Log;
import android.widget.ToggleButton;

import com.schspa.SysfsInterface;
import com.schspa.SysfsInterfaceView;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "MainActivity";
    private SysfsInterface sysfs = new SysfsInterface();
    private PowerManager.WakeLock mWakelock;
    private ScrollView sysfswrapper;
    LinearLayout sysfs_ll;
    private final String dir = "/sys/module/kernel/parameters";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mWakelock = ((PowerManager)this.getSystemService(POWER_SERVICE))
                .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "wake_lock_app");

        ToggleButton wake_lock_switch = (ToggleButton) findViewById(R.id.tb_wakelock);
        wake_lock_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mWakelock.acquire();
                } else {
                    mWakelock.release();
                }
                Snackbar.make(buttonView, "wack_lock:" + isChecked, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        sysfswrapper = (ScrollView) findViewById(R.id.sysfswrapper);
        sysfs_ll = new LinearLayout(getApplicationContext());
        sysfs_ll.setOrientation(LinearLayout.VERTICAL);
        sysfswrapper.addView(sysfs_ll);

        String[] str = sysfs.getfile(dir);

        for (int i=0; i<str.length; i++) {
            Log.v(TAG, "get file:" + str[i]);
            SysfsInterfaceView v = new SysfsInterfaceView(getApplicationContext(), dir, str[i]);
            if (v != null && v.getwriteable())
                sysfs_ll.addView(v);
        }

        ToggleButton tb_showreadonly = (ToggleButton) findViewById(R.id.tb_rdwr);

        tb_showreadonly.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                String[] str = sysfs.getfile(dir);
                sysfs_ll.removeAllViews();
                if (isChecked) {
                    for (int i = 0; i < str.length; i++) {
                        Log.v(TAG, "get file:" + str[i]);
                        SysfsInterfaceView v = new SysfsInterfaceView(getApplicationContext(), dir, str[i]);
                        if (v != null)
                            sysfs_ll.addView(v);
                    }
                } else {
                    for (int i = 0; i < str.length; i++) {
                        Log.v(TAG, "get file:" + str[i]);
                        SysfsInterfaceView v = new SysfsInterfaceView(getApplicationContext(), dir, str[i]);
                        if (v != null && v.getwriteable())
                            sysfs_ll.addView(v);
                    }
                }
            }
        });
    }

    @Override
    protected void onStop() {
        if (mWakelock.isHeld())
            mWakelock.release();
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_exit) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
