package ca.concordia.teamc.soundlevelapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    protected SharedPreferenceHelper sharedPreferenceHelper;
    protected Button goToRangePage = null;
    protected Button goToHistoryPage = null;
    protected Button goToConnectPage = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sharedPreferenceHelper = new SharedPreferenceHelper(MainActivity.this);

        DataSetController dsc = DataSetController.getInstance(this);
        MeterController mc = MeterController.getInstance(this);

        if (mc.getAllMeterRecord().isEmpty()){
            Meter meterA = new Meter("test 1", "test mac address 1", "location 1", "project A", System.currentTimeMillis(), false, System.currentTimeMillis());
            Meter meterB = new Meter("test 2", "test mac address 2", "location 2", "project B", System.currentTimeMillis(), false, System.currentTimeMillis());
            Meter meterC = new Meter("test 3", "test mac address 3", "location 3", "project C", System.currentTimeMillis(), true, System.currentTimeMillis());

            mc.addMeterData(meterA);
            mc.addMeterData(meterB);
            mc.addMeterData(meterC);
        }
        if (dsc.getAllDataSet().isEmpty()){
            DataSet dataA = new DataSet("Project x","Location A",System.currentTimeMillis(),System.currentTimeMillis(),"","");
            DataSet dataB = new DataSet("Project y","Location B",System.currentTimeMillis(),System.currentTimeMillis(),"","");
            DataSet dataC = new DataSet("Project z","Location C",System.currentTimeMillis(),System.currentTimeMillis(),"","");
            DataSet dataD = new DataSet("Project 1","Location D",System.currentTimeMillis(),System.currentTimeMillis(),"","");
            DataSet dataE = new DataSet("Project 2","Location E",System.currentTimeMillis(),System.currentTimeMillis(),"","");
            DataSet dataF = new DataSet("Project 3","Location F",System.currentTimeMillis(),System.currentTimeMillis(),"","");

            dsc.addDataSet(dataA);
            dsc.addDataSet(dataB);
            dsc.addDataSet(dataC);
            dsc.addDataSet(dataD);
            dsc.addDataSet(dataE);
            dsc.addDataSet(dataF);
        }

        setupUI();
    }

    protected void setupUI(){
        goToRangePage = (Button) findViewById(R.id.butrange);
        goToHistoryPage = (Button) findViewById(R.id.buthistory);
        goToConnectPage = (Button) findViewById(R.id.butlastconnect);
        goToRangePage.setOnClickListener(this);
        goToHistoryPage.setOnClickListener(this);
        goToConnectPage.setOnClickListener(this);
    }

    public void onClick(View v) {

        if (v.getId() == R.id.butrange) {
            Intent intent = new Intent(this, ListDevicesActivity.class);
            startActivity(intent);
        } else if (v.getId() == R.id.buthistory) {
            Intent intent = new Intent(this, metersinfo.class);
            startActivity(intent);
        } else if (v.getId() == R.id.butlastconnect) {
            Intent intent = new Intent(this, myDataSets.class);
            startActivity(intent);
        }
    }
}
