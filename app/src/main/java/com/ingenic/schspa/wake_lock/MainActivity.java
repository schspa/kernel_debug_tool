package com.ingenic.schspa.wake_lock;

import android.os.Bundle;
import android.os.PowerManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import android.view.Window;
import android.view.WindowManager;
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
    LinearLayout sysfs_ll;
    private final String dir = "/sys/module/kernel/parameters";
    private int screen_width;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        screen_width = this.getWindowManager().getDefaultDisplay().getWidth();

        mWakelock = ((PowerManager) this.getSystemService(POWER_SERVICE))
                .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "wake_lock_app");

        ToggleButton tg_screen = (ToggleButton) findViewById(R.id.tb_keepScreenOn);
        tg_screen.setChecked(false);
        tg_screen.setBackground(getResources().getDrawable(R.mipmap.ic_visibility_off_white));
        tg_screen.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    buttonView.setBackground(getResources().getDrawable(R.mipmap.ic_visibility_white));
                } else {
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    buttonView.setBackground(getResources().getDrawable(R.mipmap.ic_visibility_off_white));
                }
            }
        });
        ToggleButton wake_lock_switch = (ToggleButton) findViewById(R.id.tb_wakelock);
        wake_lock_switch.setBackground(getResources().getDrawable(R.mipmap.ic_lock_open_white));
        wake_lock_switch.setChecked(false);
        wake_lock_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    buttonView.setBackground(getResources().getDrawable(R.mipmap.ic_lock_outline_white));
                    mWakelock.acquire();
                } else {
                    buttonView.setBackground(getResources().getDrawable(R.mipmap.ic_lock_open_white));
                    mWakelock.release();
                }
                Snackbar.make(buttonView, "wack_lock:" + isChecked, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        sysfs_ll = (LinearLayout) findViewById(R.id.sysfs_ll);

        ToggleButton tb_showreadonly = (ToggleButton) findViewById(R.id.tb_rdwr);
        refresh(tb_showreadonly.isChecked());

        tb_showreadonly.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                refresh(isChecked);
            }
        });
    }

    private void refresh(boolean wronly) {
        sysfs_ll.removeAllViews();
        String[] str = sysfs.getfile(dir);

        for (int i = 0; i < str.length; i++) {
            SysfsInterfaceView v = new SysfsInterfaceView(getApplicationContext(), dir, str[i], screen_width);
            if (v.get_view() != null && (wronly || v.getwriteable()))
                sysfs_ll.addView(v.get_view());
        }
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
