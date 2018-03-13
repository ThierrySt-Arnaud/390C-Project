package ca.concordia.teamc.soundlevelapp;

import android.bluetooth.*;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.UUID;

public class ListDevicesActivity extends AppCompatActivity {
    private static final String TAG = "ListDevices activity";
    private BluetoothAdapter bluetooth;
    private Set<BluetoothDevice> pairedDevices;
    private ArrayAdapter<String> BTArrayAdapter;
    Thread listen;

    private BluetoothSocket BTSocket = null; // bi-directional client-to-client data path

    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // "random" unique identifier

    ListView bluetoothDevicesList;

    //Demo test views
    TextView deviceName;
    TextView deviceAddress;
    TextView receivedChars;
    TextView receivedLegend;


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

        deviceAddress = findViewById(R.id.device_address);
        deviceName = findViewById(R.id.device_name);
        receivedLegend = findViewById(R.id.device_legend);
        receivedChars = findViewById(R.id.received_chars);

    }

    @Override
    protected void onStart(){
        super.onStart();

        bluetoothDevicesList.setVisibility(View.VISIBLE);
        deviceName.setVisibility(View.GONE);
        deviceAddress.setVisibility(View.GONE);
        receivedLegend.setVisibility(View.GONE);
        receivedChars.setVisibility(View.GONE);

    }

    public void listPairedDevices(View V) {
        // clear list
        BTArrayAdapter.clear();
        //get paired devices
        pairedDevices = bluetooth.getBondedDevices();
        BTArrayAdapter.add("ExampleDevice\n00:00:00:00:00:00");
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
        Toast.makeText(getApplicationContext(), "Searching for Devices", Toast.LENGTH_SHORT).show();
    }

    private void updateReceivedChars(String received){
        receivedChars.setText(received);
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

            boolean fail = false;
            try {
                Log.d(TAG,"Cancelling discovery if open");
                bluetooth.cancelDiscovery();
            } catch(Exception e){
                Log.e(TAG,"Couldn't cancel discovery",e);
            }

            if (address.equals("00:00:00:00:00:00")){

                Intent intent = new Intent(v.getContext(), MeterConfigScreen.class);
                startActivity(intent);

            } else {
                BluetoothDevice device = bluetooth.getRemoteDevice(address);

                try {
                    Log.d(TAG, "Creating socket");
                    BTSocket = createBluetoothSocket(device);
                } catch (IOException e) {
                    fail = true;
                    Toast.makeText(getBaseContext(),
                            "Socket creation failed", Toast.LENGTH_SHORT).show();
                }
                // Establish the Bluetooth socket connection.
                try {
                    Log.d(TAG, "Connecting");
                    BTSocket.connect();
                } catch (IOException e) {
                    try {
                        fail = true;
                        BTSocket.close();
                        return;
                    } catch (IOException e2) {
                        //insert code to deal with this
                        Toast.makeText(getBaseContext(),
                                "Socket creation failed", Toast.LENGTH_SHORT).show();
                    }
                }
                if (!fail) {
                    Log.d(TAG, "Starting listen mode");
                    deviceName.setText(device.getName());
                    deviceAddress.setText(device.getAddress());
                    receivedChars.setText("");
                    bluetoothDevicesList.setVisibility(View.GONE);
                    deviceName.setVisibility(View.VISIBLE);
                    deviceAddress.setVisibility(View.VISIBLE);
                    receivedLegend.setVisibility(View.VISIBLE);
                    receivedChars.setVisibility(View.VISIBLE);

                    listen = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "Started listen thread");

                            final InputStream mmInStream;
                            InputStream tmpIn = null;

                            // Get the input and output streams, using temp objects because
                            // member streams are final
                            try

                            {
                                tmpIn = BTSocket.getInputStream();
                            } catch (
                                    IOException e)

                            {
                                Log.e(TAG, "Couldn't get input stream", e);
                            }

                            mmInStream = tmpIn;
                            byte[] buffer = new byte[1024];  // buffer store for the stream
                            int bytes; // bytes returned from read()
                            // Keep listening to the InputStream until an exception occurs
                            while (true) {
                                Log.d(TAG, "Starting listen loop");
                                try {
                                    // Read from the InputStream
                                    assert mmInStream != null;
                                    bytes = mmInStream.read(buffer);
                                    if (bytes > 0) {
                                        Log.d(TAG, "Received data:");
                                        char[] string = new char[bytes];
                                        for (int i = 0; i < bytes; i++) {
                                            string[i] = (char) buffer[i];
                                        }
                                        final String received = String.valueOf(string);
                                        Log.d(TAG, received);
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                updateReceivedChars(received);
                                            }
                                        });
                                    }
                                } catch (IOException e) {
                                    Log.d(TAG, "Input stream disconnected.", e);
                                    break;
                                }
                            }
                        }
                    });
                    listen.start();
                }
            }
        }
    };

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        return  device.createInsecureRfcommSocketToServiceRecord(BTMODULEUUID);
        //creates insecure outgoing connection with BT device using UUID
    }
    @Override
    protected void onPause() {
        super.onPause();
        try{
            BTSocket.close();
        } catch (Exception e){
            Log.e(TAG,"Couldn't close socket.", e);
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        try{
            unregisterReceiver(btReceiver);
        }catch (IllegalArgumentException e){
            // no receiver registred
        }
    }
}