package com.example.thilak.testdatabase;

import android.database.Cursor;
import android.nfc.Tag;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {


    MeterController meterDB = new MeterController(this);

    Button btnAdd;
    Button btnView;
    Button btnDelete;
    Button btnUpdate;

    EditText etSensorName, etMacAddress, etLocation, etLastKnownProject, etLastConnectionDate, etRecordingStatus, etStartRecordingData;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //MeterController meterDB = new MeterController(this);

        etSensorName = (EditText) findViewById(R.id.etSensorName);
        etMacAddress = (EditText) findViewById(R.id.etMacAddress);
        etLocation = (EditText) findViewById(R.id.etLocation);
        etLastKnownProject = (EditText) findViewById(R.id.etLastKnownProject);
        etLastConnectionDate = (EditText) findViewById(R.id.etLastConnectionDate);
        etRecordingStatus = (EditText) findViewById(R.id.etRecordingStatus);
        etStartRecordingData = (EditText) findViewById(R.id.etStartRecordingDate);
        btnAdd = (Button) findViewById(R.id.buttonAdd);
        btnView = (Button) findViewById(R.id.buttonView);
        btnDelete = (Button) findViewById(R.id.buttonDelete);
        btnUpdate = (Button) findViewById(R.id.buttonUpdate);

        AddData();
        ViewData();
        DeleteData();
        UpdateData();

    }

    private void AddData(){
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Meter meter = new Meter();

                meter.setSensorName(etSensorName.getText().toString());
                meter.setMacAddress(etMacAddress.getText().toString());
                meter.setLocation(etLocation.getText().toString());
                meter.setLastKnownProject(etLastKnownProject.getText().toString());
                meter.setLastConnectionDate(etLastConnectionDate.getText().toString());
                meter.setRecordingStatus(Boolean.parseBoolean(etRecordingStatus.getText().toString()));
                meter.setStartRecordingDate(etStartRecordingData.getText().toString());

                boolean insertData = meterDB.addMeterData(meter);

                if(insertData == true){
                    Toast.makeText(MainActivity.this, "Data success!",Toast.LENGTH_LONG).show();
                }

               // boolean insertData = meterDB.addMeterData();


            }
        });
    }
    public void UpdateData(){
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Meter meter = new Meter();

                meter.setSensorName(etSensorName.getText().toString());
                meter.setMacAddress(etMacAddress.getText().toString());
                meter.setLocation(etLocation.getText().toString());
                meter.setLastKnownProject(etLastKnownProject.getText().toString());
                meter.setLastConnectionDate(etLastConnectionDate.getText().toString());
                meter.setRecordingStatus(Boolean.parseBoolean(etRecordingStatus.getText().toString()));
                meter.setStartRecordingDate(etStartRecordingData.getText().toString());

                int temp = etSensorName.getText().toString().length();
                if(temp > 0){
                    int update = meterDB.updateMeterRecord(meter);
                }else{
                    Toast.makeText(MainActivity.this, "forgot to enter sensorname !",Toast.LENGTH_LONG).show();

                }
            }
        });
    }
    public void DeleteData(){
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int temp = etSensorName.getText().toString().length();
                if(temp > 0){
                    Integer deleteRow = meterDB.deleteMeterData(etSensorName.getText().toString());
                    if (deleteRow > 0) {
                        Toast.makeText(MainActivity.this, "Delete success!", Toast.LENGTH_LONG).show();
                    }else{
                        Toast.makeText(MainActivity.this, "something worng!",Toast.LENGTH_LONG).show();
                    }
                }else{
                    Toast.makeText(MainActivity.this, "enter the sensorname!",Toast.LENGTH_LONG).show();

                }
            }
        });
    }

    public void ViewData(){
        btnView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<Meter> meterList = meterDB.getAllMeterRecord();

                for(Meter meter: meterList){
                    Log.d("DB test",meter.getSensorName()+"\n");
                }

                Cursor data = meterDB.showData();

                if(data.getCount() == 0){
                    display("ERROR", "No Data");
                    return;

                }
                StringBuffer buffer = new StringBuffer();
                while(data.moveToNext()) {
                    buffer.append("ID: " + data.getString(0) + "\n");
                    buffer.append("SensorName: " + data.getString(1) + "\n");
                    buffer.append("MacAdress: " + data.getString(2) + "\n");
                    buffer.append("Location: " + data.getString(3) + "\n");
                    buffer.append("LastKnownProject: " + data.getString(4) + "\n");
                    buffer.append("LastConnectionDate: " + data.getString(5) + "\n");
                    buffer.append("RecordingStatus: " + data.getString(6) + "\n");
                    buffer.append("StartRecordingData: " + data.getString(7) + "\n");

                    display("All Stored: ", buffer.toString());
                }
            }
        });
    }
    public void display(String title, String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.show();
    }



}
