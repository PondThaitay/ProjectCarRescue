package com.cm_smarthome.carrescue;


import android.util.Log;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class Wifi {

    private Socket socket = null;
    private final String DEBUG_TAG = "Car Reuse";

    public void WifiControl(String ip, String port, String msg) {
        String message = msg;
        String IP_Address = ip;
        String Port = port;
        try {
            socket = new Socket(IP_Address, Integer.valueOf(Port));
            PrintWriter out = new PrintWriter(new BufferedWriter(
                    new OutputStreamWriter(socket.getOutputStream())), true);
            out.println(message);
            out.close();
            socket.close();
        } catch (Exception e) {
            Log.e(DEBUG_TAG, e.toString());
        }
    }
}
