package com.robot.nabil.robottracker;

import android.provider.Settings.Secure;

import com.loopj.android.http.*;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    /* GEAR PARAMETERS */
    private TextView txtview = null;
    private BluetoothAdapter BTAdapter;
    private AsyncHttpClient httpClient;
    private String android_id;
    static String api_uri = "http://192.168.1.162:3000/data";

    private final BroadcastReceiver receiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)) {
                String name = intent.getStringExtra(BluetoothDevice.EXTRA_NAME);
                if(name == null) return;
                if(!name.equals("HC-05")) {
                    return;
                }
                short rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI,Short.MIN_VALUE);


                Toast.makeText(getApplicationContext(), "Found " + name + " at " + rssi + "dBm ", Toast.LENGTH_SHORT).show();

                txtview.append("Found " + name + " at " + rssi + "dBm.\n");

                sendData(rssi);
            }
            else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                Toast.makeText(getApplicationContext(), "Starting bluetooth scan...", Toast.LENGTH_SHORT).show();
            }
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Toast.makeText(getApplicationContext(), "Bluetooth scan finished.", Toast.LENGTH_SHORT).show();

                BTAdapter.startDiscovery();
            }


        }
    };

    private void sendData(int rssi) {
        // Add your data
        RequestParams params = new RequestParams();
        params.put("device_id", android_id);
        params.put("rssi", rssi + "");

        // Execute HTTP Post Request
        httpClient.post(getApplicationContext(), api_uri, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, org.apache.http.Header[] headers, byte[] responseBody) {
                Log.i("API", "Data has been sent");
            }

            @Override
            public void onFailure(int statusCode, org.apache.http.Header[] headers, byte[] responseBody, java.lang.Throwable error) {
                Toast.makeText(getApplicationContext(), "Failed to send data to server: " + statusCode, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        android_id = Secure.getString(getBaseContext().getContentResolver(), Secure.ANDROID_ID);
        Button start_button = (Button) findViewById(R.id.start_button);
        txtview = (TextView) findViewById(R.id.textView);
        start_button.setOnClickListener(new OnClickListener(){
            public void onClick(View v) {
                if (BTAdapter != null) {
                    BTAdapter.startDiscovery();
                }
                else Toast.makeText(getApplicationContext(), "Bluetooth not working.", Toast.LENGTH_SHORT).show();
            }
        });

        Button clear_button = (Button) findViewById(R.id.clear_button);
        clear_button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                txtview.setText("");
            }
        });

        Button stop_button = (Button) findViewById(R.id.stop_button);
        stop_button.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Bluetooth scan cancelled.", Toast.LENGTH_SHORT).show();
                BTAdapter.cancelDiscovery();
            }
        });

        final EditText etxt = (EditText) findViewById(R.id.url);
        Button url_btn = (Button) findViewById(R.id.btn_url);

        url_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                api_uri = "http://" + etxt.getText().toString() + "/data";
            }
        });

        // Create a new HttpClient
        httpClient = new AsyncHttpClient();

        BTAdapter = BluetoothAdapter.getDefaultAdapter();
        if (BTAdapter == null) Toast.makeText(getApplicationContext(), "Bluetooth not found", Toast.LENGTH_SHORT).show();
        else if (BTAdapter.isEnabled()) Toast.makeText(getApplicationContext(), "Bluetooth is working. Proceed.", Toast.LENGTH_SHORT).show();
        else Toast.makeText(getApplicationContext(), "Bluetooth not enabled.", Toast.LENGTH_SHORT).show();

        registerReceiver(receiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        registerReceiver(receiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED));
        registerReceiver(receiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
    }
}