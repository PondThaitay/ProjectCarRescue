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
import android.provider.Settings;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.webkit.WebView;
import android.widget.Button;
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
    private Button btnUp;
    private Button btnDown;
    private Button btnLeft;
    private Button btnRight;

    private TextView tvStatusVDO;
    private TextView tvSelectBluttoth;
    private TextView tvWifi;

    TextView textView5;

    BluetoothSPP bt;

    private static String IP_Address = "192.168.4.1";
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

        String url = "http://192.168.4.101:8080/jsfs.html";
        webView = (WebView) findViewById(R.id.myWebView);

        btnBluetooth = (Button) findViewById(R.id.btnBluetooth);
        btnWifi = (Button) findViewById(R.id.btnWifi);

        textView5 = (TextView) findViewById(R.id.textView5);
        tvStatusVDO = (TextView) findViewById(R.id.tvStatusVDO);

        tvSelectBluttoth = (TextView) findViewById(R.id.tvSelect);
        tvSelectBluttoth.setPaintFlags(tvSelectBluttoth.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        tvWifi = (TextView) findViewById(R.id.tvWifi);
        tvWifi.setPaintFlags(tvWifi.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        btnUp = (Button) findViewById(R.id.btnUp);
        btnDown = (Button) findViewById(R.id.btnDown);
        btnLeft = (Button) findViewById(R.id.btnLeft);
        btnRight = (Button) findViewById(R.id.btnRight);

        if (isInternetPresent) {
            webView.getSettings().setJavaScriptEnabled(true);
            webView.loadUrl(url);
            tvStatusVDO.setText("มีการเชื่อมต่อกับสัญญาณภาพ");
            webView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return true;
                }
            });
        } else {
            showAlertDialog(context, "เกิดข้อผิดพลาด", "กรุณาเชื่อต่อกับเครือข่าย เพื่อเชื่อมต่อสัญญาณภาพ", false);
        }

        btnWifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                btnWifi.setBackgroundResource(R.drawable.ic_action_wifi_on);
                Toast.makeText(context, "กรุณาเชื่อมต่อกับเครือข่าย", Toast.LENGTH_SHORT).show();
                tvWifi.setVisibility(View.VISIBLE);
                AvailableWifi(true);

            }
        });

        tvWifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnWifi.setBackgroundResource(R.drawable.ic_action_wifi);
                Toast.makeText(context, "ตัดการเชื่อมต่อสำเร็จ", Toast.LENGTH_SHORT).show();
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
                                , "เชื่อมต่อกับอุปกรณ์ : " + name + "\n" + address
                                , Toast.LENGTH_SHORT).show();
                        tvSelectBluttoth.setText("Bluetooth : ตัดการเชื่อมต่อ");
                        btnBluetooth.setBackgroundResource(R.drawable.ic_action_bluetooth_on);
                        AvailableBluetooth();
                    }

                    public void onDeviceDisconnected() {
                        Toast.makeText(context
                                , "ตัดการเชื่อมต่อสำเร็จ", Toast.LENGTH_SHORT).show();
                        tvSelectBluttoth.setText("เลือกอุปกรณ์ Bluetooth");
                        tvSelectBluttoth.setVisibility(View.GONE);
                        btnBluetooth.setBackgroundResource(R.drawable.ic_action_bluetooth);
                    }

                    public void onDeviceConnectionFailed() {
                        Toast.makeText(context
                                , "ไม่สามารถเชื่อมต่อกับอุปกรณ์ได้", Toast.LENGTH_SHORT).show();
                        btnBluetooth.setBackgroundResource(R.drawable.ic_action_bluetooth);
                    }
                });
            }
        });
    }

    public void AvailableBluetooth() {

        btnUp.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    bt.send("U", true);
                    textView5.setText("ทิศทาง : เดินหน้า");
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    bt.send("S", true);
                    textView5.setText("ทิศทาง  : หยุด");
                }
                return false;
            }
        });

        btnDown.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    bt.send("D", true);
                    textView5.setText("ทิศทาง  : ถอยหลัง");
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    bt.send("S", true);
                    textView5.setText("ทิศทาง  : หยุด");
                }
                return false;
            }
        });

        btnRight.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    bt.send("R", true);
                    textView5.setText("ทิศทาง  : เลี้ยวขวา");
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    bt.send("S", true);
                    textView5.setText("ทิศทาง  : หยุด");
                }
                return false;
            }
        });

        btnLeft.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    bt.send("L", true);
                    textView5.setText("ทิศทาง  : เลี้ยวซ้าย");
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    bt.send("S", true);
                    textView5.setText("ทิศทาง  : หยุด");
                }
                return false;
            }
        });
    }

    public void AvailableWifi(boolean flag) {
        if (flag == true) {
            btnUp.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        wifi.WifiControl(IP_Address, Port, "LED ON");
                        textView5.setText("ทิศทาง : เดินหน้า");
                        Log.i("Control by Wifi", "OK");
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        wifi.WifiControl(IP_Address, Port, "LED OFF");
                        textView5.setText("ทิศทาง  : หยุด");
                    }
                    return false;
                }
            });

            btnDown.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        wifi.WifiControl(IP_Address, Port, "LED ON");
                        textView5.setText("ทิศทาง  : ถอยหลัง");
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        wifi.WifiControl(IP_Address, Port, "LED OFF");
                        textView5.setText("ทิศทาง  : หยุด");
                    }
                    return false;
                }
            });

            btnRight.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        wifi.WifiControl(IP_Address, Port, "LED ON");
                        textView5.setText("ทิศทาง  : เลี้ยวขวา");
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        wifi.WifiControl(IP_Address, Port, "LED OFF");
                        textView5.setText("ทิศทาง  : หยุด");
                    }
                    return false;
                }
            });

            btnLeft.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        wifi.WifiControl(IP_Address, Port, "LED ON");
                        textView5.setText("ทิศทาง  : เลี้ยวซ้าย");
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        wifi.WifiControl(IP_Address, Port, "LED OFF");
                        textView5.setText("ทิศทาง  : หยุด");
                    }
                    return false;
                }
            });

        } else {
            btnUp.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        textView5.setText("ทิศทาง : เดินหน้า");
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        textView5.setText("ทิศทาง  : หยุด");
                    }
                    return false;
                }
            });

            btnDown.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        textView5.setText("ทิศทาง  : ถอยหลัง");
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        textView5.setText("ทิศทาง  : หยุด");
                    }
                    return false;
                }
            });

            btnRight.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        textView5.setText("ทิศทาง  : เลี้ยวขวา");
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        textView5.setText("ทิศทาง  : หยุด");
                    }
                    return false;
                }
            });

            btnLeft.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        textView5.setText("ทิศทาง  : เลี้ยวขวา");
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        textView5.setText("ทิศทาง  : หยุด");
                    }
                    return false;
                }
            });
        }
    }

    public void showAlertDialog(Context context, String title, String message, Boolean status) {
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setButton("ตกลง", new DialogInterface.OnClickListener() {
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