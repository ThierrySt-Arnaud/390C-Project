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

//    private AdapterView.OnItemClickListener BTDeviceClickListener = new AdapterView.OnItemClickListener() {
//        public void onItemClick(AdapterView parent, View v, int position, long id) {
//            Intent intent = new Intent(ListDevicesActivity.this, MeterConfigScreen.class);
//            startActivity(intent);
//        }
//    };

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

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()
            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.available();
                    if(bytes != 0) {
                        SystemClock.sleep(100); //pause and wait for rest of data. Adjust this depending on your sending speed.
                        bytes = mmInStream.available(); // how many bytes are ready to be read?
                        bytes = mmInStream.read(buffer, 0, bytes); // record how many bytes we actually read
                        //handler.obtainMessage(2, bytes, -1, buffer).sendToTarget(); // Send the obtained bytes to the UI activity
                    }
                } catch (IOException e) {
                    e.printStackTrace();

                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(String input) {
            byte[] bytes = input.getBytes();           //converts entered String into bytes
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) { }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
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