package ca.concordia.teamc.soundlevelapp;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.content.Intent;
import android.widget.TextView;

import java.util.Date;

public class metersinfopts extends AppCompatActivity {

    private MeterController meterController;
    private Meter meter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_metersinfopts);

        TextView sensorNameTV = (TextView) findViewById(R.id.tvSensorName);
        TextView projectNameTV = (TextView) findViewById(R.id.tvProject);
        TextView locationTV = (TextView) findViewById(R.id.tvLocation);
        TextView MACAddressTV = (TextView) findViewById(R.id.tvMACAddress);
        TextView lastConnectedTV = (TextView) findViewById(R.id.tvLastConnected);
        TextView startRecordTV = (TextView) findViewById(R.id.tvRecordingChange);
        TextView recordStatusTV = (TextView) findViewById(R.id.tvRecordingStatus);

        int ID = getIntent().getIntExtra("SensorID", 0);
        meterController = MeterController.getInstance(this);
        meter = meterController.getSelectedMeterRecord(ID);

        String sName = meter.getSensorName();
        String pName = meter.getLastKnownProject();
        String location = meter.getLocation();
        String mac = meter.getMacAddress();
        Date lastconnectedDate = new Date(meter.getLastConnectionDate());
        Date startRecord = new Date(meter.getStartRecordingDate());
        boolean recordStatus = meter.getRecordingStatus();

        sensorNameTV.setText(sName);
        projectNameTV.setText(pName);
        locationTV.setText(location);
        MACAddressTV.setText(mac);
        lastConnectedTV.setText(lastconnectedDate.toString());
        startRecordTV.setText(startRecord.toString());

        if(recordStatus){
            recordStatusTV.setText("ON");
            recordStatusTV.setTextColor(Color.GREEN);
        }else {
            recordStatusTV.setText("OFF");
            recordStatusTV.setTextColor(Color.RED);
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


    }
}