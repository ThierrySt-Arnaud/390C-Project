package ca.concordia.teamc.soundlevelapp;

import android.bluetooth.*;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class ListDevicesActivity extends AppCompatActivity {

    private BluetoothAdapter bluetooth;
    private Set<BluetoothDevice> pairedDevices;
    private ArrayAdapter<String> BTArrayAdapter;

    private Handler handler; // Our main handler that will receive callback notifications
    private ConnectedThread connectedThread; // bluetooth background worker thread to send and receive data
    private BluetoothSocket BTSocket = null; // bi-directional client-to-client data path

    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // "random" unique identifier

    ListView bluetoothDevicesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_devices);

        // initiate bluetooth adapter object
        bluetooth = BluetoothAdapter.getDefaultAdapter();

        //if the bluetooth is off turn it on
        if (!bluetooth.isEnabled()) {
            Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOn, 0);
            Toast.makeText(getApplicationContext(), "Turned on", Toast.LENGTH_LONG).show();
        }

        // get list view for devices
        bluetoothDevicesList = (ListView) findViewById(R.id.listView);
        // initiate array adapter
        BTArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        //set adapter for list view
        bluetoothDevicesList.setAdapter(BTArrayAdapter);

        bluetoothDevicesList.setOnItemClickListener(devicesClickListener);

    }

    public void listPairedDevices(View V) {
        // clear list
        BTArrayAdapter.clear();
        //get paired devices
        pairedDevices = bluetooth.getBondedDevices();

        for (BluetoothDevice device : pairedDevices){
            // show name & address
            BTArrayAdapter.add(device.getName() + "\n" + device.getAddress() );
        }
        Toast.makeText(getApplicationContext(), "Showing Paired Devices", Toast.LENGTH_SHORT).show();
    }

    public void listNewDevices(View view) {
        //clear list
        BTArrayAdapter.clear();
        // start searching for all devices in range
        bluetooth.startDiscovery();
        //call receiver
        registerReceiver(btReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        Toast.makeText(getApplicationContext(), "Showing All Devices", Toast.LENGTH_SHORT).show();
    }

    final BroadcastReceiver btReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // new device found
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // show name & address
                BTArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                //update array adapter with new data
                BTArrayAdapter.notifyDataSetChanged();
            }
        }
    };

    private AdapterView.OnItemClickListener devicesClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {

            if(!bluetooth.isEnabled()) {
                Toast.makeText(getBaseContext(), "Bluetooth not on", Toast.LENGTH_SHORT).show();
                return;
            }

            // Get the device MAC address, which is the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            final String address = info.substring(info.length() - 17);
            final String name = info.substring(0,info.length() - 17);

            // Spawn a new thread to avoid blocking the GUI one
            new Thread()
            {
                public void run() {
                    boolean fail = false;

                    BluetoothDevice device = bluetooth.getRemoteDevice(address);

                    try {
                        BTSocket = createBluetoothSocket(device);
                    } catch (IOException e) {
                        fail = true;
                        Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_SHORT).show();
                    }
                    // Establish the Bluetooth socket connection.
                    try {
                        BTSocket.connect();
                    } catch (IOException e) {
                        try {
                            fail = true;
                            BTSocket.close();
                        } catch (IOException e2) {
                            //insert code to deal with this
                            Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                    if(fail == false) {
                        connectedThread = new ConnectedThread(BTSocket);
                        connectedThread.start();

                        Intent intent = new Intent(ListDevicesActivity.this, MeterConfigScreen.class);
                        startActivity(intent);
                    }
                }
            }.start();
        }
    };

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        return  device.createRfcommSocketToServiceRecord(BTMODULEUUID);
        //creates secure outgoing connection with BT device using UUID
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try{
            unregisterReceiver(btReceiver);
        }catch (IllegalArgumentException e){
            // no receiver registred
        }
    }
}