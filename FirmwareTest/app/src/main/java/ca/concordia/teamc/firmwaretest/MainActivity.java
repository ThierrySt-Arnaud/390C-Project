package ca.concordia.teamc.firmwaretest;

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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.os.Parcelable;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.UUID;

import ca.concordia.teamc.firmwaretest.R;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "ListDevices activity";
    private BluetoothAdapter bluetooth;
    private Set<BluetoothDevice> pairedDevices;
    private ArrayAdapter<String> BTArrayAdapter;
    Thread listen;
    BroadcastReceiver btReceiver;

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
        setContentView(R.layout.activity_main);

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

        btReceiver = new BroadcastReceiver() {
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
                }else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)){
                    // device connected, go to meter config screen
                    Log.d("BT SERVICE", "Device connected, show config");
                    Intent meterConfigScreenIntent = new Intent(MainActivity.this, MeterConfigScreen.class);
                    //meterConfigScreenIntent.putExtra("projectName", "MyProjectName");
                    //meterConfigScreenIntent.putExtra("meterLocation", "Room123");
                    startActivity(meterConfigScreenIntent);
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        registerReceiver(btReceiver, filter);
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


    private AdapterView.OnItemClickListener devicesClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {

            // Get the device MAC address, which is the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            final String address = info.substring(info.length() - 17);

            Intent intent = new Intent(MainActivity.this.getBaseContext(), BluetoothService.class);
            intent.putExtra("address", address);
            startService(intent);
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
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