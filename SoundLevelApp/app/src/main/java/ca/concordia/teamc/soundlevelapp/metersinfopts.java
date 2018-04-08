package ca.concordia.teamc.soundlevelapp;

import android.os.TestLooperManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.content.Intent;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.Date;

import javax.crypto.Mac;

public class metersinfopts extends AppCompatActivity {

    private MeterController meterController;
    private Meter meter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_metersinfopts);

        TextView projectNameTV = (TextView) findViewById(R.id.project_name);
        TextView locationTV = (TextView) findViewById(R.id.location);
        TextView MACAddressTV = (TextView) findViewById(R.id.sensor_macaddress);
        TextView lastConnectedTV = (TextView) findViewById(R.id.last_connected);
        TextView startRecordTV = (TextView) findViewById(R.id.last_recorded);
        TextView recordStatusTV = (TextView) findViewById(R.id.recording_status);

        int ID = getIntent().getIntExtra("SensorID", 0);
        meterController = MeterController.getInstance(this);
        meter = meterController.getSelectedMeterRecord(ID);

        String pName = meter.getSensorName();
        String location = meter.getLocation();
        String mac = meter.getMacAddress();
        Date lastconnectedDate = new Date(meter.getLastConnectionDate());
        Date startRecord = new Date(meter.getStartRecordingDate());
        boolean recordStatus = meter.getRecordingStatus();

        projectNameTV.setText("Sensor Name: "+ pName);
        locationTV.setText("Location: "+ location);
        MACAddressTV.setText("MAC Address: "+ mac);
        lastConnectedTV.setText("Last Connected:\n"+ lastconnectedDate.toString());
        startRecordTV.setText("Start Recorded:\n"+ startRecord.toString());

        if(recordStatus){
            recordStatusTV.setText("Recording Status: ON");
        }else {
            recordStatusTV.setText("Recording Status: OFF");
        }

    }

    @Override
    public boolean onCreateOptionsMenu (Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.actionbutton_metersinfo, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected (MenuItem item) {


        if (item.getItemId() == R.id.action_metersinrange) {


            Intent intent = new Intent(this, ListDevicesActivity.class);
            startActivity(intent);
            return true;

        }

        if (item.getItemId() == R.id.action_datasets) {


            Intent intent = new Intent(this, myDataSets.class);
            startActivity(intent);
            return true;

        }
        return super.onOptionsItemSelected(item);


    }}