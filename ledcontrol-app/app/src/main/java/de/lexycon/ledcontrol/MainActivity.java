package de.lexycon.ledcontrol;


import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorChangedListener;
import com.flask.colorpicker.builder.ColorWheelRendererBuilder;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements TabLayout.OnTabSelectedListener {
    private SeekBar sbBrightness;
    private Button btnSwitchOnOff, btnMode1, btnMode2, btnMode3, btnMode4, btnMode5;
    private com.flask.colorpicker.ColorPickerView colorPickerView;
    private ColorPickerView.WHEEL_TYPE mWheelStyle;

    private String mIPAddress;
    private int mPort;

    private Integer cDefaultColor;
    private Integer iDefaultBrightness;

    private UdpClientHandler udpClientHandler;
    private UDPClientThread udpClientThread;

    //This is our tablayout
    private TabLayout tabLayout;

    //This is our viewPager
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        udpClientHandler = new UdpClientHandler(this);
    }

    private void setViewMain() {
        setContentView(R.layout.activity_main);


        //Adding toolbar to the activity
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Initializing the tablayout
        tabLayout = (TabLayout) findViewById(R.id.tabLayout);

        //Adding the tabs using addTab() method
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.tab_title_first)));
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.tab_title_second)));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        //Initializing viewPager
        viewPager = (ViewPager) findViewById(R.id.pager);

        //Creating our pager adapter
        PagerAdapter adapter = new PagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount());

        //Adding adapter to pager
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        //Adding onTabSelectedListener to swipe views

        tabLayout.addOnTabSelectedListener(this);
        tabLayout.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {

                System.out.println("LayoutChanged");
                colorPickerView = (com.flask.colorpicker.ColorPickerView) findViewById(R.id.color_picker_view);
                sbBrightness = (SeekBar) findViewById(R.id.sbBrightness);
                btnSwitchOnOff = (Button) findViewById(R.id.buttonSwitchOnOff);
                btnMode1 = (Button) findViewById(R.id.buttonMode1);
                btnMode2 = (Button) findViewById(R.id.buttonMode2);
                btnMode3 = (Button) findViewById(R.id.buttonMode3);
                btnMode4 = (Button) findViewById(R.id.buttonMode4);
                btnMode5 = (Button) findViewById(R.id.buttonMode5);
                if (iDefaultBrightness != null) sbBrightness.setProgress(iDefaultBrightness);
                if (cDefaultColor != null) colorPickerView.setColor(cDefaultColor, false);
                if (mWheelStyle != null) colorPickerView.setRenderer(ColorWheelRendererBuilder.getRenderer(mWheelStyle));



                sbBrightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        int iProgress = progress;
                        if (iProgress < 5) iProgress = 5;
                        UDPSendBytes("B" + String.valueOf(iProgress));
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });

                colorPickerView.addOnColorChangedListener(new OnColorChangedListener() {
                    @Override
                    public void onColorChanged(int i) {
                        UDPSendBytes("C" + String.valueOf(Integer.toHexString(i).substring(2)));
                    }
                });

                btnSwitchOnOff.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        UDPSendBytes("0");
                        btnSwitchOnOff.setEnabled(false);

                        Timer buttonTimer = new Timer();
                        buttonTimer.schedule(new TimerTask() {

                            @Override
                            public void run() {
                                runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {
                                        btnSwitchOnOff.setEnabled(true);
                                    }
                                });
                            }
                        }, 3000);
                    }
                });

                btnMode1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        UDPSendBytes("2");
                    }
                });
                btnMode2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        UDPSendBytes("3");
                    }
                });
                btnMode3.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        UDPSendBytes("4");
                    }
                });
                btnMode4.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        UDPSendBytes("5");
                    }
                });
                btnMode5.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        UDPSendBytes("6");
                    }
                });
            }
        });

        if (!PreferenceManager.getDefaultSharedPreferences(this).contains(getString(R.string.pref_key_ipaddress))) {
            PreferenceManager.setDefaultValues(this, R.xml.preferences, true);
        }

    }

    private void UDPSendBytes(String strSend) {
        if (udpClientThread != null && udpClientThread.isAlive()) udpClientThread.interrupt();
        udpClientThread = new UDPClientThread(mIPAddress, mPort, udpClientHandler);
        udpClientThread.setBytes(strSend);
        udpClientThread.start();
    }


    private void updateValues(String values){
        if (values != null) {
            String[] aValues = values.substring(1).split(",");
            if (aValues.length == 2) {
                iDefaultBrightness = Integer.parseInt(aValues[0]);
                cDefaultColor = Color.parseColor("#" + aValues[1]);
            }
        }
        setViewMain();
    }

    private void readSettings() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        this.mIPAddress = settings.getString(getString(R.string.pref_key_ipaddress), "");
        this.mPort = Integer.valueOf(settings.getString(getString(R.string.pref_key_port), "20000"));
        switch (settings.getString(getString(R.string.pref_key_wheelstyle), "")) {
            case "FLOWER" :
                this.mWheelStyle = ColorPickerView.WHEEL_TYPE.FLOWER;
                break;
            case "CIRCLE": default:
                this.mWheelStyle = ColorPickerView.WHEEL_TYPE.CIRCLE;
                break;
        }
        if (this.colorPickerView != null) colorPickerView.setRenderer(ColorWheelRendererBuilder.getRenderer(mWheelStyle));
        switch (settings.getString(getString(R.string.pref_key_theme), "")) {
            case "LIGHT": default:
                setTheme(R.style.AppTheme_Light);
                break;
            case "DARK" :
                setTheme(R.style.AppTheme_Dark);
                break;
        }
    }

    @Override
    protected void onResume()
    {
        this.readSettings();
        setViewMain();
        UDPSendBytes("A");
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    public static class UdpClientHandler extends Handler {
        public static final int UPDATE_VALUES = 0;
        private MainActivity parent;

        public UdpClientHandler(MainActivity parent) {
            super();
            this.parent = parent;
        }

        @Override
        public void handleMessage(Message msg) {

            switch (msg.what){
                case UPDATE_VALUES:
                    parent.updateValues((String)msg.obj);
                    break;
                default:
                    super.handleMessage(msg);
            }

        }
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        viewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }
}
