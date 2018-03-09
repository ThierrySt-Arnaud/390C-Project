package ca.concordia.teamc.soundlevelapp;

import android.bluetooth.*;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Set;

public class ListDevicesActivity extends AppCompatActivity {

    private BluetoothAdapter bluetooth;
    private Set<BluetoothDevice> pairedDevices;
    private ArrayAdapter<String> BTArrayAdapter;

    Button searchBluetooth;
    ListView bluetoothDevicesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_devices);

        searchBluetooth = (Button) findViewById(R.id.searchBluetoothButton);

        bluetooth = BluetoothAdapter.getDefaultAdapter();

        if (!bluetooth.isEnabled()) {
            Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOn, 0);
            Toast.makeText(getApplicationContext(), "Turned on", Toast.LENGTH_LONG).show();

        }

        bluetoothDevicesList = (ListView) findViewById(R.id.listView);

    }

    public void list(View V) {
        pairedDevices = bluetooth.getBondedDevices();

        ArrayList list = new ArrayList();

        for (BluetoothDevice bt : pairedDevices) list.add(bt.getName());
        Toast.makeText(getApplicationContext(), "Showing Paired Devices", Toast.LENGTH_SHORT).show();

        final ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, list);

        bluetoothDevicesList.setAdapter(adapter);
    }

    final BroadcastReceiver bReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // add the name and the MAC address of the object to the arrayAdapter
                BTArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                BTArrayAdapter.notifyDataSetChanged();
            }
        }
    };

    public void find(View view) {
            BTArrayAdapter.clear();
            bluetooth.startDiscovery();

            registerReceiver(bReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));

    }
}



