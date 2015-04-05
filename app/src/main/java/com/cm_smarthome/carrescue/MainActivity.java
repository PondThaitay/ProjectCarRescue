package com.cm_smarthome.carrescue;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.app.Activity;
import android.os.StrictMode;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;
import app.akexorcist.bluetotohspp.library.DeviceList;


public class MainActivity extends Activity {

    Wifi wifi = new Wifi();

    Context context = this;

    Boolean isInternetPresent = false;
    ConnectionDetector cd;

    private WebView webView;

    private Button btnBluetooth;
    private Button btnWifi;

    private TextView tvStatusVDO;
    private TextView tvSelectBluttoth;
    private TextView tvWifi;

    RelativeLayout layout_joystick;
    TextView textView5;
    JoyStickClass js;

    BluetoothSPP bt;

    private static String IP_Address = "192.168.2.115";
    private static String Port = "8080";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        cd = new ConnectionDetector(getApplicationContext());

        bt = new BluetoothSPP(context);

        isInternetPresent = cd.isConnectingToInternet();

        String url = "http://192.168.2.114:8080/jsfs.html";
        webView = (WebView) findViewById(R.id.myWebView);

        btnBluetooth = (Button) findViewById(R.id.btnBluetooth);
        btnWifi = (Button) findViewById(R.id.btnWifi);

        textView5 = (TextView) findViewById(R.id.textView5);
        tvStatusVDO = (TextView) findViewById(R.id.tvStatusVDO);

        tvSelectBluttoth = (TextView) findViewById(R.id.tvSelect);
        tvSelectBluttoth.setPaintFlags(tvSelectBluttoth.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        tvWifi = (TextView) findViewById(R.id.tvWifi);
        tvWifi.setPaintFlags(tvWifi.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        layout_joystick = (RelativeLayout) findViewById(R.id.layout_joystick);

        if (isInternetPresent) {
            webView.getSettings().setJavaScriptEnabled(true);
            webView.loadUrl(url);
            tvStatusVDO.setText("มีการเชื่อมต่อกับสัญญาณภาพ");
        } else {
            showAlertDialog(context, "เกิดข้อผิดพลาด", "กรุณาเชื่อต่อกับเครือข่าย เพื่อเชื่อมต่อสัญญาณภาพ", false);
        }

        btnWifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isInternetPresent) {
                    btnWifi.setBackgroundResource(R.drawable.ic_action_wifi_on);
                    tvWifi.setVisibility(View.VISIBLE);
                    AvailableWifi(true);
                } else {
                    showAlertDialog(context, "เกิดข้อผิดพลาด", "กรุณาเชื่อต่อกับเครือข่าย", false);
                }
            }
        });

        tvWifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnWifi.setBackgroundResource(R.drawable.ic_action_wifi);
                AvailableWifi(false);
                tvWifi.setVisibility(View.GONE);
            }
        });

        btnBluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CheckBluetooth();
                tvSelectBluttoth.setVisibility(View.VISIBLE);
                tvSelectBluttoth.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (bt.getServiceState() == BluetoothState.STATE_CONNECTED) {
                            bt.disconnect();
                        } else {
                            Intent intent = new Intent(getApplicationContext(), DeviceList.class);
                            startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
                        }
                    }
                });

                AvailableBluetooth();

                if (!bt.isBluetoothAvailable()) {
                    Toast.makeText(getApplicationContext()
                            , "Bluetooth is not available"
                            , Toast.LENGTH_SHORT).show();
                }

                bt.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() {
                    public void onDataReceived(byte[] data, String message) {
                        Log.i("INPUT :", message);
                    }
                });

                bt.setBluetoothConnectionListener(new BluetoothSPP.BluetoothConnectionListener() {
                    public void onDeviceConnected(String name, String address) {
                        Toast.makeText(context
                                , "Connected to " + name + "\n" + address
                                , Toast.LENGTH_SHORT).show();
                        tvSelectBluttoth.setText("ตัดการเชื่อมต่อ");
                        btnBluetooth.setBackgroundResource(R.drawable.ic_action_bluetooth_on);
                    }

                    public void onDeviceDisconnected() {
                        Toast.makeText(context
                                , "DisConnect", Toast.LENGTH_SHORT).show();
                        tvSelectBluttoth.setText("เลือกอุปกรณ์ Bluetooth");
                        btnBluetooth.setBackgroundResource(R.drawable.ic_action_bluetooth);
                    }

                    public void onDeviceConnectionFailed() {
                        Toast.makeText(context
                                , "Unable To Connect", Toast.LENGTH_SHORT).show();
                        btnBluetooth.setBackgroundResource(R.drawable.ic_action_bluetooth);
                    }
                });
            }
        });

        js = new JoyStickClass(getApplicationContext()
                , layout_joystick, R.drawable.joystick);
        js.setStickSize(80, 80);
        js.setLayoutSize(300, 300);
        js.setOffset(50);
        js.setMinimumDistance(50);
    }

    public void AvailableBluetooth() {
        layout_joystick.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View arg0, MotionEvent arg1) {
                js.drawStick(arg1);
                if (arg1.getAction() == MotionEvent.ACTION_DOWN
                        || arg1.getAction() == MotionEvent.ACTION_MOVE) {

                    int direction = js.get8Direction();
                    if (direction == JoyStickClass.STICK_UP) {
                        bt.send("U", true);
                        textView5.setText("ทิศทาง : เดินหน้า");
                    } else if (direction == JoyStickClass.STICK_UPRIGHT) {
                        bt.send("UR", true);
                        textView5.setText("ทิศทาง  : เดินหน้าเลี้ยวขวา");
                    } else if (direction == JoyStickClass.STICK_RIGHT) {
                        bt.send("R", true);
                        textView5.setText("ทิศทาง  : เลี้ยวขวา");
                    } else if (direction == JoyStickClass.STICK_DOWNRIGHT) {
                        bt.send("DR", true);
                        textView5.setText("ทิศทาง  : ถอยหลังเลี้ยวขวา");
                    } else if (direction == JoyStickClass.STICK_DOWN) {
                        bt.send("D", true);
                        textView5.setText("ทิศทาง  : ถอยหลัง");
                    } else if (direction == JoyStickClass.STICK_DOWNLEFT) {
                        bt.send("DL", true);
                        textView5.setText("ทิศทาง  : ถอยหลังเลี้ยวซ้าย");
                    } else if (direction == JoyStickClass.STICK_LEFT) {
                        bt.send("L", true);
                        textView5.setText("ทิศทาง  : เลี้ยวซ้าย");
                    } else if (direction == JoyStickClass.STICK_UPLEFT) {
                        bt.send("UL", true);
                        textView5.setText("ทิศทาง  : เดินหน้าเลี้ยงซ้าย");
                    } else if (direction == JoyStickClass.STICK_NONE) {
                        textView5.setText("ทิศทาง  : หยุด");
                    }
                } else if (arg1.getAction() == MotionEvent.ACTION_UP) {
                    textView5.setText("ทิศทาง  : หยุด");
                    bt.send("S", true);
                }
                return true;
            }
        });
    }

    public void AvailableWifi(boolean flag) {
        if (flag == true) {
            layout_joystick.setOnTouchListener(new OnTouchListener() {
                public boolean onTouch(View arg0, MotionEvent arg1) {
                    js.drawStick(arg1);
                    if (arg1.getAction() == MotionEvent.ACTION_DOWN
                            || arg1.getAction() == MotionEvent.ACTION_MOVE) {

                        int direction = js.get8Direction();
                        if (direction == JoyStickClass.STICK_UP) {
                            //wifi.WifiControl(IP_Address, Port, "UW");
                            textView5.setText("ทิศทาง : เดินหน้า");
                            Log.i("Control by Wifi", "OK");
                        } else if (direction == JoyStickClass.STICK_UPRIGHT) {
                            wifi.WifiControl(IP_Address, Port, "URW");
                            textView5.setText("ทิศทาง  : เดินหน้าเลี้ยวขวา");
                        } else if (direction == JoyStickClass.STICK_RIGHT) {
                            wifi.WifiControl(IP_Address, Port, "RW");
                            textView5.setText("ทิศทาง  : เลี้ยวขวา");
                        } else if (direction == JoyStickClass.STICK_DOWNRIGHT) {
                            wifi.WifiControl(IP_Address, Port, "DRW");
                            textView5.setText("ทิศทาง  : ถอยหลังเลี้ยวขวา");
                        } else if (direction == JoyStickClass.STICK_DOWN) {
                            wifi.WifiControl(IP_Address, Port, "DW");
                            textView5.setText("ทิศทาง  : ถอยหลัง");
                        } else if (direction == JoyStickClass.STICK_DOWNLEFT) {
                            wifi.WifiControl(IP_Address, Port, "DLW");
                            textView5.setText("ทิศทาง  : ถอยหลังเลี้ยวซ้าย");
                        } else if (direction == JoyStickClass.STICK_LEFT) {
                            wifi.WifiControl(IP_Address, Port, "LW");
                            textView5.setText("ทิศทาง  : เลี้ยวซ้าย");
                        } else if (direction == JoyStickClass.STICK_UPLEFT) {
                            wifi.WifiControl(IP_Address, Port, "ULW");
                            textView5.setText("ทิศทาง  : เดินหน้าเลี้ยงซ้าย");
                        } else if (direction == JoyStickClass.STICK_NONE) {
                            textView5.setText("ทิศทาง  : หยุด");
                        }
                    } else if (arg1.getAction() == MotionEvent.ACTION_UP) {
                        wifi.WifiControl(IP_Address, Port, "US");
                        textView5.setText("ทิศทาง  : หยุด");
                    }
                    return true;
                }
            });
        } else {
            layout_joystick.setOnTouchListener(new OnTouchListener() {
                public boolean onTouch(View arg0, MotionEvent arg1) {
                    js.drawStick(arg1);
                    if (arg1.getAction() == MotionEvent.ACTION_DOWN
                            || arg1.getAction() == MotionEvent.ACTION_MOVE) {

                        int direction = js.get8Direction();
                        if (direction == JoyStickClass.STICK_UP) {
                            textView5.setText("ทิศทาง : เดินหน้า");
                            Log.i("Control by Wifi", "NO");
                        } else if (direction == JoyStickClass.STICK_UPRIGHT) {
                            textView5.setText("ทิศทาง  : เดินหน้าเลี้ยวขวา");
                        } else if (direction == JoyStickClass.STICK_RIGHT) {
                            textView5.setText("ทิศทาง  : เลี้ยวขวา");
                        } else if (direction == JoyStickClass.STICK_DOWNRIGHT) {
                            textView5.setText("ทิศทาง  : ถอยหลังเลี้ยวขวา");
                        } else if (direction == JoyStickClass.STICK_DOWN) {
                            textView5.setText("ทิศทาง  : ถอยหลัง");
                        } else if (direction == JoyStickClass.STICK_DOWNLEFT) {
                            textView5.setText("ทิศทาง  : ถอยหลังเลี้ยวซ้าย");
                        } else if (direction == JoyStickClass.STICK_LEFT) {
                            textView5.setText("ทิศทาง  : เลี้ยวซ้าย");
                        } else if (direction == JoyStickClass.STICK_UPLEFT) {
                            textView5.setText("ทิศทาง  : เดินหน้าเลี้ยงซ้าย");
                        } else if (direction == JoyStickClass.STICK_NONE) {
                            textView5.setText("ทิศทาง  : หยุด");
                        }
                    } else if (arg1.getAction() == MotionEvent.ACTION_UP) {
                        textView5.setText("ทิศทาง  : หยุด");
                    }
                    return true;
                }
            });
        }
    }

    public void showAlertDialog(Context context, String title, String message, Boolean status) {
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        alertDialog.show();
    }

    public void onDestroy() {
        super.onDestroy();
        bt.stopService();
    }

    public void CheckBluetooth() {
        super.onStart();
        if (!bt.isBluetoothEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, BluetoothState.REQUEST_ENABLE_BT);
        } else {
            if (!bt.isServiceAvailable()) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_OTHER);
            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
            if (resultCode == Activity.RESULT_OK)
                bt.connect(data);
        } else if (requestCode == BluetoothState.REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_OTHER);
            } else {
                Toast.makeText(context
                        , "Bluetooth was not enabled."
                        , Toast.LENGTH_SHORT).show();
            }
        }
    }
}

