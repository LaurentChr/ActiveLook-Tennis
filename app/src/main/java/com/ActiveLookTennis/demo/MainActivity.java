package com.ActiveLookTennis.demo;

import static java.lang.Math.min;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.activelook.activelooksdk.Glasses;
import com.activelook.activelooksdk.types.Rotation;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private Glasses connectedGlasses;

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch sensorSwitch;
    private TextView largeText;
    private SeekBar luminanceSeekBar;

    String nameA="Player A", nameB="Player B", shortNameA=" A ", shortNameB=" B ";
    String PointA="0", GameA1="0", GameA2="0", GameA3="0", GameA4="0", GameA5="0";
    String PointB="0", GameB1="0", GameB2="0", GameB3="0", GameB4="0", GameB5="0";
    Integer set=1, setA=0, setB=0, TBA=0, TBB=0, matchSet=2;
    Boolean tieBreak=false, matchWin=false;
    @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
    private final Handler clockHandler = new Handler(), gestHandler = new Handler();
    private Runnable clockRunnable, gestRunnable;
    final int[] gest = {0}, gestTimer = {0};
    EditText Aname, Bname;
    TextView TieBreakTV;
    Button Apoint, Bpoint, A1game, B1game, A2game, B2game, A3game, B3game, A4game, B4game, A5game, B5game, Set35;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*
         * Check location permission (needed for BLE scan)
         */
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.BLUETOOTH_CONNECT,Manifest.permission.BLUETOOTH_SCAN}, 0);

        if (savedInstanceState != null && ((DemoApp) this.getApplication()).isConnected()) {
            this.connectedGlasses = savedInstanceState.getParcelable("connectedGlasses");
            this.connectedGlasses.setOnDisconnected(glasses -> {
                glasses.disconnect();
                MainActivity.this.disconnect();
            });
        }

        setContentView(R.layout.activity_scrolling);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Toolbar toolbar = findViewById(R.id.toolbar);

        largeText = findViewById(R.id.largeText);
        luminanceSeekBar = findViewById(R.id.luminanceSeekBar);
        sensorSwitch = findViewById(R.id.sensorSwitch);
        Apoint = findViewById(R.id.PointA);
        Bpoint = findViewById(R.id.PointB);
        A1game = findViewById(R.id.GameA1);
        B1game = findViewById(R.id.GameB1);
        A2game = findViewById(R.id.GameA2);
        B2game = findViewById(R.id.GameB2);
        A3game = findViewById(R.id.GameA3);
        B3game = findViewById(R.id.GameB3);
        A4game = findViewById(R.id.GameA4);
        B4game = findViewById(R.id.GameB4);
        A5game = findViewById(R.id.GameA5);
        B5game = findViewById(R.id.GameB5);
        TieBreakTV = findViewById(R.id.TieBreak);
        Set35 = findViewById(R.id.Set35);
        BatteryManager bm = (BatteryManager) getSystemService(BATTERY_SERVICE);
        int batLevel = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY); // phone battery level

        setSupportActionBar(toolbar);
        CollapsingToolbarLayout toolBarLayout = findViewById(R.id.toolbar_layout);
        toolBarLayout.setTitle(getTitle());

        clockRunnable = new Runnable() {
            @SuppressLint("DefaultLocale")
            @Override
            public void run() {
                if (connectedGlasses != null) {
                    String clock = sdf.format(new Date());
                    connectedGlasses.battery(r1 -> {
                        connectedGlasses.cfgSet("ALooK");
// if the battery level is less than 25%, the icon 1 of a low level battery is used, otherwise the icon 0 is used
                        if(r1<25){ connectedGlasses.imgDisplay((byte) 1, (short) 275, (short) 224);
                            connectedGlasses.txt(new Point(268,254), Rotation.TOP_LR, (byte) 1, (byte) 0x0F,
                                    String.format("%d", r1) + "% / " + String.format("%d", batLevel) + "%  ");
                            connectedGlasses.txt(new Point(100,254), Rotation.TOP_LR, (byte) 1, (byte) 0x0F, clock);
                        } else {connectedGlasses.imgDisplay((byte) 0, (short) 275, (short) 224);
                            connectedGlasses.txt(new Point(268,254), Rotation.TOP_LR, (byte) 1, (byte) 0x0F,
                                    String.format("%d", r1) + "% / " + String.format("%d", batLevel) + "%  ");
                            connectedGlasses.txt(new Point(100,254), Rotation.TOP_LR, (byte) 1, (byte) 0x0F, clock);
                        }
                    });//Glasses Battery
                    clockHandler.postDelayed(this,60000); }
            }   };
        clockHandler.removeCallbacks(clockRunnable);
        clockHandler.postDelayed(clockRunnable,60000); // on redemande toutes les 60 000ms

        gestRunnable = new Runnable() {
            @Override
            public void run() {
                    if (gest[0]==1) {gestTimer[0]++;}
                    if (gestTimer[0]==7 && gest[0]==1) { gestTimer[0]=0; gest[0]=0;
                        MainActivity.this.Point(1,1);} // simple gesture
                    if (gest[0]==2) { gestTimer[0]=0; gest[0]=0;
                        MainActivity.this.Point(2,1);} // double gesture
                    gestHandler.postDelayed(this,200);
            }
        };
        gestHandler.removeCallbacks(gestRunnable);
        gestHandler.postDelayed(gestRunnable,200); // on redemande toutes les 1000ms

        this.updateVisibility();
        this.bindActions();
    }

    @SuppressLint("DefaultLocale")
    private void updateVisibility() {
        final Glasses g = this.connectedGlasses;
        BatteryManager bm = (BatteryManager) getSystemService(BATTERY_SERVICE);
        int batLevel = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY); // phone battery level
        String clock = sdf.format(new Date());

        if (g == null) {
            this.findViewById(R.id.connected_content).setVisibility(View.GONE);
            this.findViewById(R.id.disconnected_content).setVisibility(View.VISIBLE);
        } else {
            this.findViewById(R.id.connected_content).setVisibility(View.VISIBLE);
            this.findViewById(R.id.disconnected_content).setVisibility(View.GONE);
            g.clear();
            g.gesture(true);
            g.subscribeToSensorInterfaceNotifications(() -> { gest[0]++; });
            g.battery(r1 -> {
                connectedGlasses.cfgSet("ALooK");
// if the battery level is less than 25%, the icon 1 of a low level battery is used, otherwise the icon 0 is used
                if(r1<25){ connectedGlasses.imgDisplay((byte) 1, (short) 275, (short) 224);
                    connectedGlasses.txt(new Point(268,254), Rotation.TOP_LR, (byte) 1, (byte) 0x0F,
                            String.format("%d", r1) + "% / " + String.format("%d", batLevel) + "%  ");
                    connectedGlasses.txt(new Point(100,254), Rotation.TOP_LR, (byte) 1, (byte) 0x0F, clock);
                } else {connectedGlasses.imgDisplay((byte) 0, (short) 275, (short) 224);
                    connectedGlasses.txt(new Point(268,254), Rotation.TOP_LR, (byte) 1, (byte) 0x0F,
                            String.format("%d", r1) + "% / " + String.format("%d", batLevel) + "%  ");
                    connectedGlasses.txt(new Point(100,254), Rotation.TOP_LR, (byte) 1, (byte) 0x0F, clock);
                }
            });//Glasses Battery
            g.txt(new Point(290, 200), Rotation.TOP_LR, (byte) 1, (byte) 0x0F, nameA.substring(0, min(12, nameA.length())));
            g.txt(new Point(140, 200), Rotation.TOP_LR, (byte) 1, (byte) 0x0F, nameB.substring(0, min(12, nameB.length())));
            g.txt(new Point(230, 175), Rotation.TOP_LR, (byte) 3, (byte) 0x0F, PointA+"  ");
            g.txt(new Point(130, 175), Rotation.TOP_LR, (byte) 3, (byte) 0x0F, PointB+"  ");
            g.txt(new Point(300, 100), Rotation.TOP_LR, (byte) 2, (byte) 0x0F, GameA1);
            g.txt(new Point(300, 60), Rotation.TOP_LR, (byte) 2, (byte) 0x0F, GameB1);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void bindActions() {
        //        this.toast("Binding actions");
        // If BT is not on, request that it be enabled.
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled()) {
            Toast.makeText(getApplicationContext(),
                    "Your BLUETOOTH is not open !!!/n>>>relaunch the application", Toast.LENGTH_LONG).show();
        }
        this.findViewById(R.id.scan).setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, ScanningActivity.class);
            MainActivity.this.startActivityForResult(intent, Activity.RESULT_FIRST_USER);
        });
        Aname  = findViewById(R.id.PlayerA);
        Aname.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            public void afterTextChanged(Editable s) {if (s!=null && s.length()!=0) {savePreferences();MainActivity.this.Player(1);}}
        });
        Bname  = findViewById(R.id.PlayerB);
        Bname.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            public void afterTextChanged(Editable s) {if (s!=null && s.length()!=0) {savePreferences();MainActivity.this.Player(2);}}
        });
        this.findViewById(R.id.PlusPointA).setOnClickListener(view -> MainActivity.this.Point(1,1));
        this.findViewById(R.id.MinusPointA).setOnClickListener(view -> MainActivity.this.Point(1,2));
        this.findViewById(R.id.PlusPointB).setOnClickListener(view -> MainActivity.this.Point(2,1));
        this.findViewById(R.id.MinusPointB).setOnClickListener(view -> MainActivity.this.Point(2,2));

        this.findViewById(R.id.PlusGameA1).setOnClickListener(view -> MainActivity.this.Game(1,1,1));
        this.findViewById(R.id.MinusGameA1).setOnClickListener(view -> MainActivity.this.Game(1,1,2));
        this.findViewById(R.id.PlusGameA2).setOnClickListener(view -> MainActivity.this.Game(1,2,1));
        this.findViewById(R.id.MinusGameA2).setOnClickListener(view -> MainActivity.this.Game(1,2,2));
        this.findViewById(R.id.PlusGameA3).setOnClickListener(view -> MainActivity.this.Game(1,3,1));
        this.findViewById(R.id.MinusGameA3).setOnClickListener(view -> MainActivity.this.Game(1,3,2));
        this.findViewById(R.id.PlusGameA4).setOnClickListener(view -> MainActivity.this.Game(1,4,1));
        this.findViewById(R.id.MinusGameA4).setOnClickListener(view -> MainActivity.this.Game(1,4,2));
        this.findViewById(R.id.PlusGameA5).setOnClickListener(view -> MainActivity.this.Game(1,5,1));
        this.findViewById(R.id.MinusGameA5).setOnClickListener(view -> MainActivity.this.Game(1,5,2));
        this.findViewById(R.id.PlusGameB1).setOnClickListener(view -> MainActivity.this.Game(2,1,1));
        this.findViewById(R.id.MinusGameB1).setOnClickListener(view -> MainActivity.this.Game(2,1,2));
        this.findViewById(R.id.PlusGameB2).setOnClickListener(view -> MainActivity.this.Game(2,2,1));
        this.findViewById(R.id.MinusGameB2).setOnClickListener(view -> MainActivity.this.Game(2,2,2));
        this.findViewById(R.id.PlusGameB3).setOnClickListener(view -> MainActivity.this.Game(2,3,1));
        this.findViewById(R.id.MinusGameB3).setOnClickListener(view -> MainActivity.this.Game(2,3,2));
        this.findViewById(R.id.PlusGameB4).setOnClickListener(view -> MainActivity.this.Game(2,4,1));
        this.findViewById(R.id.MinusGameB4).setOnClickListener(view -> MainActivity.this.Game(2,4,2));
        this.findViewById(R.id.PlusGameB5).setOnClickListener(view -> MainActivity.this.Game(2,5,1));
        this.findViewById(R.id.MinusGameB5).setOnClickListener(view -> MainActivity.this.Game(2,5,2));
        this.findViewById(R.id.Set35).setOnClickListener(view -> {
            if (matchSet==2) {matchSet = 3; Set35.setText("5\n\nset");}
            else {matchSet = 2; Set35.setText("3\n\nset");} });
        this.findViewById(R.id.button_disconnect).setOnClickListener(view -> {
            MainActivity.this.sensorSwitch(true);
            connectedGlasses.sensor(true);
            MainActivity.this.connectedGlasses.disconnect();
            MainActivity.this.connectedGlasses = null;
            MainActivity.this.updateVisibility();
            this.snack("Disconnected");
        });
        sensorSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> MainActivity.this.sensorSwitch(isChecked));
        luminanceSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressChangedValue = 0;
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressChangedValue = progress;
                MainActivity.this.lumaButton(progressChangedValue);
            }
            public void onStartTrackingTouch(SeekBar seekBar) { }
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });
        clockHandler.removeCallbacks(clockRunnable);
        clockHandler.postDelayed(clockRunnable, 60000);// on redemande toutes les 1000ms
    }

    //////////     PLAYERS  buttons
    private void Player(int player){
        final Glasses g = this.connectedGlasses;
        String pname="";
        if (player==1) {pname=nameA.replaceFirst("^\\s+", ""); shortNameA=ShortName(nameA);}
        if (player==2) {pname=nameB.replaceFirst("^\\s+", ""); shortNameB=ShortName(nameA);}
        if (connectedGlasses != null) {
            g.txt(new Point(290-(2-player)*150, 200), Rotation.TOP_LR, (byte) 1, (byte) 0x0F, pname.substring(0,min(12,pname.length())));
    } }

    private String ShortName(String name) {
        String sname=name.replaceFirst("^\\s+", "");
        if (name.equals("")) {sname = "";}
        else { if (sname.indexOf(" ") != 0) {
                sname = sname.substring(0, 0).toUpperCase().concat(sname.substring(sname.indexOf(" ", 1) + 1, sname.indexOf(" ", 1) + 1)).toUpperCase();}
            else { sname = sname.substring(0, 0).toUpperCase();}
        }
        return sname;
    }

    //////////     Point +/-  buttons
    @SuppressLint("DefaultLocale")
    private void Point(int player, int PlusMinus){
        final Glasses g = this.connectedGlasses;
        String ppoint="",npoint="";
        boolean gwin=false;

        if (!tieBreak) {
        if (player==1) {ppoint=PointA;}
        if (player==2) {ppoint=PointB;}
        if (PlusMinus==1){
            if (ppoint.equals("0")) {npoint="15";}
            if (ppoint.equals("15")) {npoint="30";}
            if (ppoint.equals("30")) {npoint="40";}
            if (ppoint.equals("Eq")) {
                if (player==1) {PointA="Av"; PointB="-"; npoint="Av"; Apoint.setText(PointA); Bpoint.setText(PointB);
                            g.txt(new Point(130, 175), Rotation.TOP_LR, (byte) 3, (byte) 0x0F,"-  ");}
                else {PointA="-"; PointB="Av"; npoint="Av"; Apoint.setText(PointA); Bpoint.setText(PointB);
                            g.txt(new Point(230, 175), Rotation.TOP_LR, (byte) 3, (byte) 0x0F,"-  ");}}
            if (ppoint.equals("-")) { PointA="Eq"; PointB="Eq"; npoint="Eq"; Apoint.setText(PointA); Bpoint.setText(PointB);}
            if (ppoint.equals("40")) {
                if (player==1) {if (PointB.equals("0") || PointB.equals("15") || PointB.equals("30"))
                             {Game(1,set,1); gwin=true;}
                        else {PointA="Av"; PointB="-"; npoint="Av"; Apoint.setText(PointA); Bpoint.setText(PointB);
                            g.txt(new Point(130, 175), Rotation.TOP_LR, (byte) 3, (byte) 0x0F,"-  ");}}
                if (player==2) {if (PointA.equals("0") || PointA.equals("15") || PointA.equals("30"))
                              {Game(2,set,1); gwin=true;}
                        else {PointA="-"; PointB="Av"; npoint="Av"; Apoint.setText(PointA); Bpoint.setText(PointB);
                            g.txt(new Point(230, 175), Rotation.TOP_LR, (byte) 3, (byte) 0x0F,"-  ");}}
                 }
            if (ppoint.equals("Av")) {
                if (player==1) {Game(1,set,1); npoint="0";gwin=true;}
                if (player==2) {Game(2,set,1); npoint="0";gwin=true;} }
        }
        if (PlusMinus==2){
            if (ppoint.equals("0")) {npoint="0";}
            if (ppoint.equals("15")) {npoint="0";}
            if (ppoint.equals("30")) {npoint="15";}
            if (ppoint.equals("40")) {npoint="30";}
            if (ppoint.equals("Av")) {npoint="Eq";}
        }
        if (player==1) {PointA=npoint; Apoint.setText(npoint);}
        if (player==2) {PointB=npoint; Bpoint.setText(npoint);}
        if (g != null) {
            if (gwin) { gwin=false; PointA="0";PointB="0";Apoint.setText("0");Bpoint.setText("0");
                g.txt(new Point(230, 175), Rotation.TOP_LR, (byte) 3, (byte) 0x0F, "0  ");
                g.txt(new Point(130, 175), Rotation.TOP_LR, (byte) 3, (byte) 0x0F, "0  " +
                        ""); }
            else {g.txt(new Point(330 - player * 100, 175), Rotation.TOP_LR, (byte) 3, (byte) 0x0F, npoint + "  ");
            if (npoint.equals("Eq")) {g.txt(new Point(130, 175), Rotation.TOP_LR, (byte) 3, (byte) 0x0F,"Eq ");
                                      g.txt(new Point(230, 175), Rotation.TOP_LR, (byte) 3, (byte) 0x0F,"Eq ");}}
        }   }
        else {
            if (player==1) {TBA++;}
            if (player==2) {TBB++;}
            if (TBA<7 && TBB<7) { Apoint.setText(String.format("%d", TBA)); Bpoint.setText(String.format("%d", TBB));
                if (g != null) {
                    g.txt(new Point(230, 175), Rotation.TOP_LR, (byte) 3, (byte) 0x0F, String.format("%d", TBA));
                    g.txt(new Point(130, 175), Rotation.TOP_LR, (byte) 3, (byte) 0x0F, String.format("%d", TBB)); }
                }
            if (TBA>6 || TBB>6) {
                if (TBA>TBB+1) {Game(1,set,1); tieBreak=false; setA++; }
                if (TBB>TBA+1) {Game(2,set,1); tieBreak=false; setB++; }
                if (TBA<TBB+2 && TBB<TBA+2) {Apoint.setText(String.format("%d", TBA)); Bpoint.setText(String.format("%d", TBB));
                    if (g != null) {
                        g.txt(new Point(230, 175), Rotation.TOP_LR, (byte) 3, (byte) 0x0F, String.format("%d", TBA));
                        g.txt(new Point(130, 175), Rotation.TOP_LR, (byte) 3, (byte) 0x0F, String.format("%d", TBB)); }
                }
        }   }
    }

    //////////     Game +/-  buttons
    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    private void Game(int player, int sset, int PlusMinus){
        final Glasses g = this.connectedGlasses;
        String ggame="", ngame="", ggA="", ggB="";
        boolean setwin=false;
        if(!tieBreak) {
        if (player==1) {
            if (sset==1) {ggame=GameA1;ggA=GameA1;ggB=GameB1;}
            if (sset==2) {ggame=GameA2;ggA=GameA2;ggB=GameB2;}
            if (sset==3) {ggame=GameA3;ggA=GameA3;ggB=GameB3;}
            if (sset==4) {ggame=GameA4;ggA=GameA4;ggB=GameB4;}
            if (sset==5) {ggame=GameA5;ggA=GameA5;ggB=GameB5;}
        }
        if (player==2) {
            if (sset==1) {ggame=GameB1;ggA=GameA1;ggB=GameB1;}
            if (sset==2) {ggame=GameB2;ggA=GameA2;ggB=GameB2;}
            if (sset==3) {ggame=GameB3;ggA=GameA3;ggB=GameB3;}
            if (sset==4) {ggame=GameB4;ggA=GameA4;ggB=GameB4;}
            if (sset==5) {ggame=GameB5;ggA=GameA5;ggB=GameB5;}
        }
        if (PlusMinus==1){
            if (ggame.equals("0")){ngame="1";}
            if (ggame.equals("1")){ngame="2";}
            if (ggame.equals("2")){ngame="3";}
            if (ggame.equals("3")){ngame="4";}
            if (ggame.equals("4")){ngame="5";}
            if (ggame.equals("6") && sset==set){ngame="7";setwin=true;
                if (player==1) {setA++;} else {setB++;}}
            if (ggame.equals("5")){ngame="6";
                if (player==1 && (ggB.equals("0") || ggB.equals("1") || ggB.equals("2") || ggB.equals("3") || ggB.equals("4"))){setwin=true; setA++;}
                if (player==2 && (ggA.equals("0") || ggA.equals("1") || ggA.equals("2") || ggA.equals("3") || ggA.equals("4"))){setwin=true; setB++;}
                if (player==1 && ggB.equals("6")) {tieBreak=true; }
                if (player==2 && ggA.equals("6")) {tieBreak=true; }
                if (tieBreak) {
                    if (g != null) { TieBreakTV.setText("Tie Break");
                        g.txt(new Point(200, 128), Rotation.TOP_LR, (byte) 1, (byte) 0x0F, "Tie Break");
                    }
                }
            } }
        if (PlusMinus==2){
            if (ggame.equals("0")){ngame="0";}
            if (ggame.equals("1")){ngame="0";}
            if (ggame.equals("2")){ngame="1";}
            if (ggame.equals("3")){ngame="2";}
            if (ggame.equals("4")){ngame="3";}
            if (ggame.equals("5")){ngame="4";}
            if (ggame.equals("6")){ngame="5";}
        }
        if (player==1 && sset==set) {
            if (sset==1) {GameA1=ngame; A1game.setText(ngame);}
            if (sset==2) {GameA2=ngame; A2game.setText(ngame);}
            if (sset==3) {GameA3=ngame; A3game.setText(ngame);}
            if (sset==4) {GameA4=ngame; A4game.setText(ngame);}
            if (sset==5) {GameA5=ngame; A5game.setText(ngame);}
        }
        if (player==2 && sset==set) {
            if (sset==1) {GameB1=ngame; B1game.setText(ngame);}
            if (sset==2) {GameB2=ngame; B2game.setText(ngame);}
            if (sset==3) {GameB3=ngame; B3game.setText(ngame);}
            if (sset==4) {GameB4=ngame; B4game.setText(ngame);}
            if (sset==5) {GameB5=ngame; B5game.setText(ngame);}
        }
        if (g != null && sset==set) {
                g.txt(new Point(360-set*60, 140-player*40), Rotation.TOP_LR, (byte) 2, (byte) 0x0F, ngame);
        }   }
        else { // tieBreak won
            setwin=true; tieBreak=false; TieBreakTV.setText(" ");
            if (g != null) {
                g.txt(new Point(200, 128), Rotation.TOP_LR, (byte) 1, (byte) 0x0F, "             ");
                g.txt(new Point(230, 175), Rotation.TOP_LR, (byte) 3, (byte) 0x0F, "0   ");
                g.txt(new Point(130, 175), Rotation.TOP_LR, (byte) 3, (byte) 0x0F, "0   ");
                g.txt(new Point(360-set*60, 100), Rotation.TOP_LR, (byte) 2, (byte) 0x0F, "6("+String.format("%d", TBA)+")");
                g.txt(new Point(360-set*60, 60), Rotation.TOP_LR, (byte) 2, (byte) 0x0F, "6("+String.format("%d", TBB)+")");
            }
            if (sset==1) {A1game.setText("6("+String.format("%d", TBA)+")"); B1game.setText("6("+String.format("%d", TBB)+")");}
            if (sset==2) {A2game.setText("6("+String.format("%d", TBA)+")"); B2game.setText("6("+String.format("%d", TBB)+")");}
            if (sset==3) {A3game.setText("6("+String.format("%d", TBA)+")"); B3game.setText("6("+String.format("%d", TBB)+")");}
            if (sset==4) {A4game.setText("6("+String.format("%d", TBA)+")"); B4game.setText("6("+String.format("%d", TBB)+")");}
            if (sset==5) {A5game.setText("6("+String.format("%d", TBA)+")"); B5game.setText("6("+String.format("%d", TBB)+")");}
            TBA=0; TBB=0; PointA="0"; PointB="0";Apoint.setText("0");Bpoint.setText("0");
        }
        if (setwin) {
            if (setA.equals(matchSet)) {matchWin=true; TieBreakTV.setText("MATCH WIN !");
                this.findViewById(R.id.PlayerA).setBackgroundColor(getResources().getColor(R.color.success));
                this.findViewById(R.id.PlayerB).setBackgroundColor(getResources().getColor(R.color.grey));
                g.txt(new Point(300, 128), Rotation.TOP_LR, (byte) 1, (byte) 0x0F, "MATCH WIN !");
            }
            if (setB.equals(matchSet)) {matchWin=true; TieBreakTV.setText("MATCH WIN !");
                this.findViewById(R.id.PlayerB).setBackgroundColor(getResources().getColor(R.color.success));
                this.findViewById(R.id.PlayerA).setBackgroundColor(getResources().getColor(R.color.grey));
                g.txt(new Point(150, 128), Rotation.TOP_LR, (byte) 1, (byte) 0x0F, "MATCH WIN !");
            }
            set++;
            if (g != null && !matchWin) {
                g.txt(new Point(360-set*60, 100), Rotation.TOP_LR, (byte) 2, (byte) 0x0F, "0");
                g.txt(new Point(360-set*60, 60), Rotation.TOP_LR, (byte) 2, (byte) 0x0F, "0");}
                if (set==2){
                    this.findViewById(R.id.GameA2).setVisibility(View.VISIBLE);
                    this.findViewById(R.id.PlusGameA2).setVisibility(View.VISIBLE);
                    this.findViewById(R.id.MinusGameA2).setVisibility(View.VISIBLE);
                    this.findViewById(R.id.GameB2).setVisibility(View.VISIBLE);
                    this.findViewById(R.id.PlusGameB2).setVisibility(View.VISIBLE);
                    this.findViewById(R.id.MinusGameB2).setVisibility(View.VISIBLE);
                }
                if (set==3 && !matchWin){
                    this.findViewById(R.id.GameA3).setVisibility(View.VISIBLE);
                    this.findViewById(R.id.PlusGameA3).setVisibility(View.VISIBLE);
                    this.findViewById(R.id.MinusGameA3).setVisibility(View.VISIBLE);
                    this.findViewById(R.id.GameB3).setVisibility(View.VISIBLE);
                    this.findViewById(R.id.PlusGameB3).setVisibility(View.VISIBLE);
                    this.findViewById(R.id.MinusGameB3).setVisibility(View.VISIBLE);
                }
                if (set==4 && !matchWin){
                    this.findViewById(R.id.GameA4).setVisibility(View.VISIBLE);
                    this.findViewById(R.id.PlusGameA4).setVisibility(View.VISIBLE);
                    this.findViewById(R.id.MinusGameA4).setVisibility(View.VISIBLE);
                    this.findViewById(R.id.GameB4).setVisibility(View.VISIBLE);
                    this.findViewById(R.id.PlusGameB4).setVisibility(View.VISIBLE);
                    this.findViewById(R.id.MinusGameB4).setVisibility(View.VISIBLE);
                }
                if (set==5 && !matchWin){
                    this.findViewById(R.id.GameA5).setVisibility(View.VISIBLE);
                    this.findViewById(R.id.PlusGameA5).setVisibility(View.VISIBLE);
                    this.findViewById(R.id.MinusGameA5).setVisibility(View.VISIBLE);
                    this.findViewById(R.id.GameB5).setVisibility(View.VISIBLE);
                    this.findViewById(R.id.PlusGameB5).setVisibility(View.VISIBLE);
                    this.findViewById(R.id.MinusGameB5).setVisibility(View.VISIBLE);
                }
            }
      }

    /////////  LUMINANCE  bar and switch
    private void lumaButton(int luma){
        final Glasses glasses = this.connectedGlasses;
        glasses.luma((byte) luma);
    }

    private void sensorSwitch(boolean on){
        final Glasses g = this.connectedGlasses;
        g.sensor(on);
    }

    private void savePreferences() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        @SuppressLint("CommitPrefEdits") SharedPreferences.Editor editor = sharedPreferences.edit();
        //editor.putString(key, value);
        if (Aname.getText().toString().equals("")) {editor.putString("Aname","Player A");}
        else {editor.putString("Aname",Aname.getText().toString());}
        if (Bname.getText().toString().equals("")) {editor.putString("Bname","Player B");}
        else {editor.putString("Bname",Bname.getText().toString());}
        }

    @SuppressLint("SetTextI18n")
    private void setUIGlassesInformations(){
        final Glasses glasses = this.connectedGlasses;
        glasses.settings(r -> sensorSwitch.setChecked(r.isGestureEnable()));
        glasses.settings(r -> luminanceSeekBar.setProgress(r.getLuma()));

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == requestCode && requestCode == Activity.RESULT_FIRST_USER) {
            if (data != null && data.hasExtra("connectedGlasses")) {

                this.connectedGlasses= data.getExtras().getParcelable("connectedGlasses");
                this.connectedGlasses.setOnDisconnected(glasses -> MainActivity.this.disconnect());
                runOnUiThread(MainActivity.this::setUIGlassesInformations);
            }
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        if (this.connectedGlasses != null) {
            savedInstanceState.putParcelable("connectedGlasses", this.connectedGlasses);
        }
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // If BT is not on, request that it be enabled.
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled()) {
            Toast.makeText(getApplicationContext(), "Your BlueTooth is not open !!!", Toast.LENGTH_LONG).show();
        }
        if (!((DemoApp) this.getApplication()).isConnected()) {
            this.connectedGlasses = null;
        }
        clockHandler.removeCallbacks(clockRunnable);
        clockHandler.postDelayed(clockRunnable,60000); // on redemande toutes les 1000ms
        this.updateVisibility();
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onStart() {
        super.onStart();
        // If BT is not on, request that it be enabled.
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled()) {
            Toast.makeText(getApplicationContext(), "Your BlueTooth is not open !!!", Toast.LENGTH_LONG).show();
            largeText.setText("Your BlueTooth is not open !!\n\n" +
                    "Please open BlueTooth and\n\n relaunch the application.");
            largeText.setTextColor(Color.parseColor("#FF0000"));
            largeText.setTypeface(largeText.getTypeface(), Typeface.BOLD);
        }
        if (!((DemoApp) this.getApplication()).isConnected()) {
            this.connectedGlasses = null;
        }
        clockHandler.removeCallbacks(clockRunnable);
        clockHandler.postDelayed(clockRunnable,60000); // on redemande toutes les 1000ms

        if (connectedGlasses != null) {
            connectedGlasses.txt(new Point(290, 200), Rotation.TOP_LR, (byte) 1, (byte) 0x0F, nameA.substring(0, min(12, nameA.length())));
            connectedGlasses.txt(new Point(140, 200), Rotation.TOP_LR, (byte) 1, (byte) 0x0F, nameB.substring(0, min(12, nameB.length())));
            connectedGlasses.txt(new Point(230, 175), Rotation.TOP_LR, (byte) 3, (byte) 0x0F, PointA+"  ");
            connectedGlasses.txt(new Point(130, 175), Rotation.TOP_LR, (byte) 3, (byte) 0x0F, PointB+"  ");
            connectedGlasses.txt(new Point(300, 100), Rotation.TOP_LR, (byte) 2, (byte) 0x0F, GameA1);
            connectedGlasses.txt(new Point(300, 60), Rotation.TOP_LR, (byte) 2, (byte) 0x0F, GameB1);
        }

        this.updateVisibility();
    }

    protected void onPause() {
        super.onPause();
    }

    protected void onStop() {
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_scrolling, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        final Glasses g = this.connectedGlasses;

        //noinspection SimplifiableIfStatement
        if (id == R.id.about_app) {Toast.makeText(this.getApplicationContext(),
                getString(R.string.app_name) + "\nVersion " + getString(R.string.app_version),
                Toast.LENGTH_LONG).show();
            return true;}
        if (id == R.id.about_glasses) {
            if( g!=null) {Toast.makeText(this.getApplicationContext(),
                    "Glasses Name : " + g.getName() + "\n"
                            + "Firmware : " + g.getDeviceInformation().getFirmwareVersion(),
                    Toast.LENGTH_LONG).show();}
            else {Toast.makeText(this.getApplicationContext(),
                    "No connected glasses found!",
                    Toast.LENGTH_LONG).show();}
            return true;}
        return super.onOptionsItemSelected(item);
    }

    private void disconnect() {
        runOnUiThread(() -> {
            ((DemoApp) this.getApplication()).onDisconnected();
            MainActivity.this.connectedGlasses = null;
            MainActivity.this.updateVisibility();
            MainActivity.this.snack("Disconnected");
        });
    }

    private Toast toast(Object data) {
        Log.d("MainActivity", data.toString());
        Toast toast = Toast.makeText(this, data.toString(), Toast.LENGTH_LONG);
        toast.show();
        return toast;
    }

    private Snackbar snack() { return this.snack(null, null); }
    private Snackbar snack(Object data) { return this.snack(null, data); }
    private Snackbar snack(View snackView, Object data) {
        if (snackView == null) { snackView = this.findViewById(R.id.toolbar); }
        final String msg = data == null ? "" : data.toString();
        Snackbar snack = Snackbar.make(snackView, msg, Snackbar.LENGTH_LONG);
        snack.show();
        if (data != null) { Log.d("MainActivity", data.toString()); }
        else { snack.dismiss(); }
        return snack;
    }

}
