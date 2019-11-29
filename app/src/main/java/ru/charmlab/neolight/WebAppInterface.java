package ru.charmlab.neolight;

import android.content.Context;
import android.os.ParcelUuid;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.widget.Toast;
import android.bluetooth.*;

import org.json.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


public class WebAppInterface {
    private Map<String, BluetoothDevice> devices = new HashMap<String, BluetoothDevice>();
    private Map<String, BluetoothSocket> sockets = new HashMap<String, BluetoothSocket>();
    private Context mContext;

    /**
     * Instantiate the interface and set the context
     */
    WebAppInterface(Context c) {
        mContext = c;
    }

    /**
     * Show a toast from the web page
     */
    @JavascriptInterface
    public void showToast(String toast) {
        Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
    }

    @JavascriptInterface
    public String searchDecice() throws org.json.JSONException, IOException {
        BluetoothAdapter bluetooth = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = bluetooth.getBondedDevices();
        String DeviceList = "";
        this.devices.clear();
        this.sockets.clear();

        if (pairedDevices.size() > 0) {
            DeviceList = "[";
            for (BluetoothDevice device : pairedDevices) {
                if (device.getName().length() > 8 && device.getName().substring(0,8).equals("NeoLight")) {
                    devices.put(device.getAddress(), device);
                    DeviceList += "{\"address\":\"" + device.getAddress() + "\", \"name\":\"" + device.getName() + "\"},";
                    BluetoothSocket socket = device.createRfcommSocketToServiceRecord(device.getUuids()[0].getUuid());
                    socket.connect();
                    this.sockets.put(device.getAddress(), socket);
                }
            }
            DeviceList = DeviceList.substring(0, DeviceList.length() - 1);
            DeviceList += "]";
        }
        return DeviceList;
    }

    @JavascriptInterface
    public void SendColor(String color) throws org.json.JSONException, IOException {
        Log.i("SendColor", color);
        JSONObject jsonObject = new JSONObject(color.trim());
        BluetoothSocket socket;
        Iterator<String> keys = jsonObject.keys();
        InputStream tmpIn = null;
        OutputStream tmpOut = null;
        while (keys.hasNext()) {
            String key = keys.next();
            String value = jsonObject.get(key).toString();
            Log.i("SendColor", key);
            BluetoothDevice device = this.devices.get(key);
            ParcelUuid[] uuids = device.getUuids();
            socket = this.sockets.get(device.getAddress());
            tmpOut = socket.getOutputStream();
            tmpOut.write(value.getBytes());
        }
    }
}
