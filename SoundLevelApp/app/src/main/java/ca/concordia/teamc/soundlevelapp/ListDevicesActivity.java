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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.os.Parcelable;
import android.widget.ToggleButton;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.UUID;

public class ListDevicesActivity extends AppCompatActivity {
    private static final String TAG = "ListDevices activity";
    private BluetoothAdapter bluetooth;
    private ArrayAdapter<String> BTArrayAdapter;
    private ArrayAdapter<String> discoveredBTArrayAdapter;
    BroadcastReceiver btReceiver;

    ToggleButton Tbutton;

    private BluetoothSocket BTSocket = null; // bi-directional client-to-client data path

    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // "random" unique identifier

    ListView bluetoothDevicesList;
    TextView newDevicesTextView;
    ListView discoveredDevicesList;

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

        discoveredDevicesList = (ListView) findViewById(R.id.discovered_list);
        newDevicesTextView = findViewById(R.id.NewDevices);

        discoveredBTArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);

        discoveredBTArrayAdapter.add("Test device" + "\n" + "00:00:00:00:00:00");
        discoveredDevicesList.setAdapter(discoveredBTArrayAdapter);
        btReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                // new device found
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    // Get the BluetoothDevice
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    // show name & address
                    discoveredBTArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                    //update array adapter with new data
                    discoveredBTArrayAdapter.notifyDataSetChanged();
                }else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)){
                    // device connected, go to meter config screen
                    Log.d("BT SERVICE", "Device connected, show config");
                    Intent meterConfigScreenIntent = new Intent(ListDevicesActivity.this, MeterConfigScreen.class);
                    startActivity(meterConfigScreenIntent);
                }
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        registerReceiver(btReceiver, filter);
        Tbutton = (ToggleButton)findViewById(R.id.toggleButton2);
        Tbutton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
            if(Tbutton.isChecked()){
                if(!discoveredDevicesList.isShown()){
                    bluetoothDevicesList.setVisibility(View.VISIBLE);
                    newDevicesTextView.setVisibility(View.VISIBLE);
                }
                // start searching for all devices in range
                if(!bluetooth.isDiscovering()){
                    bluetooth.startDiscovery();

                }
                Toast.makeText(getApplicationContext(), "Searching for New Devices", Toast.LENGTH_SHORT).show();
            } else{
                if(bluetooth.isDiscovering()){
                    bluetooth.cancelDiscovery();

                }
                Toast.makeText(getApplicationContext(), "Stopping search", Toast.LENGTH_SHORT).show();
            }

            }
        });
    }

    @Override
    protected void onStart(){
        super.onStart();

        Tbutton.setChecked(false);
        if (BTArrayAdapter.getCount() < 1) {
            Set<BluetoothDevice> pairedDevices = bluetooth.getBondedDevices();
            for (BluetoothDevice device : pairedDevices) {
                // show name & address
                BTArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        }

        discoveredDevicesList.setOnItemClickListener(discoveredDevicesClickListener);
        bluetoothDevicesList.setOnItemClickListener(pairedDevicesClickListener);
    }

    public void listNewDevices(View view) {
        //call receiver
    }


    private AdapterView.OnItemClickListener pairedDevicesClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {

            // Get the device MAC address, which is the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            final String address = info.substring(info.length() - 17);

            Log.d("DeviceList", "Current Address: "+ BluetoothService.MACAddress);
            Log.d("DeviceList", "Clicked Address" + address);


            if(BluetoothService.MACAddress.equalsIgnoreCase(address)){
                Intent meterConfigScreenIntent = new Intent(ListDevicesActivity.this, MeterConfigScreen.class);
                startActivity(meterConfigScreenIntent);
            }else{
                Log.d(TAG, "Sending address to service:" + address);
                Intent intent = new Intent(ListDevicesActivity.this.getBaseContext(), BluetoothService.class);
                intent.putExtra("address", address);
                startService(intent);
            }
        }
    };

    private AdapterView.OnItemClickListener discoveredDevicesClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {

            // Get the device MAC address, which is the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            final String address = info.substring(info.length() - 17);

            Log.d("DeviceList", "Current Address: " + BluetoothService.MACAddress);
            Log.d("DeviceList", "Clicked Address" + address);


            if(address.contentEquals("00:00:00:00:00:00")){
                Intent dummyConfigIntent = new Intent(ListDevicesActivity.this, DummyMeterConfigScreen.class);
                startActivity(dummyConfigIntent);
            } else if(BluetoothService.MACAddress.equalsIgnoreCase(address)){
                Intent meterConfigScreenIntent = new Intent(ListDevicesActivity.this, MeterConfigScreen.class);
                startActivity(meterConfigScreenIntent);
            }else{
                Intent intent = new Intent(ListDevicesActivity.this.getBaseContext(), BluetoothService.class);
                intent.putExtra("address", address);
                startService(intent);
            }
        }
    };

    @Override
    protected void onPause(){
        if(bluetooth.isDiscovering()) {
            bluetooth.cancelDiscovery();
        }
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_button, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.action_datasets) {
            Intent intent= new Intent(this, myDataSets.class);
            startActivity(intent);
            return true;
        }

        if (item.getItemId() == R.id.action_knownmeters) {
            Intent intent= new Intent(this, metersinfo.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}