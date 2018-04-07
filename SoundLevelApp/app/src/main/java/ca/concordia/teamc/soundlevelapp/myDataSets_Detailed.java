package ca.concordia.teamc.soundlevelapp;


import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Date;

public class myDataSets_Detailed extends AppCompatActivity{

    private DataSetController dsc = new DataSetController(this);
    private DataSet dataSet = new DataSet();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mydatasets_detailed);

        Button yourButton = (Button) findViewById(R.id.button_viewgraph);
        TextView pNameTV = (TextView) findViewById(R.id.dsd_project_name);
        TextView locationTV = (TextView) findViewById(R.id.dsd_location);
        TextView sNameTV = (TextView) findViewById(R.id.dsd_sensor_name);
        TextView startedTV = (TextView) findViewById(R.id.dsd_data_started);
        TextView downloadedTV = (TextView) findViewById(R.id.dsd_date_downloaded);

        int ID = getIntent().getIntExtra("ID", 0);
        dataSet = dsc.getSelectedDataSetRecord(ID);

        String pName = dataSet.getProjectName();
        String location = dataSet.getLocation();
        String sName = dataSet.getMeterReferenceRecord();
        Date startedDate = new Date(dataSet.getDateStartRecord());
        Date downloadDate = new Date(dataSet.getDateOfDownload());
        Log.d("DSD", "FilePath" + dataSet.getDatafile());

        pNameTV.setText("Project Name: " + pName);
        locationTV.setText("Location: " + location);
        sNameTV.setText("Sensor Name: "+ sName);
        startedTV.setText("Date Started:\n" + startedDate.toString());
        downloadedTV.setText("Date Downloaded:\n"+ downloadDate.toString());

        yourButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Intent myIntent = new Intent(myDataSets_Detailed.this, myGraph.class);
                myIntent.putExtra("FilePath", dataSet.getDatafile());
                myIntent.putExtra("DSID", dataSet.getDatSetId());
                Log.d("DSD", "FilePath" + dataSet.getDatafile());
                startActivity(myIntent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu (Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.actionbutton_datasets, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected (MenuItem item) {


        if (item.getItemId() == R.id.action_metersinrange) {


            Intent intent = new Intent(this, ListDevicesActivity.class);
            startActivity(intent);
            return true;

        }

        if (item.getItemId() == R.id.action_knownmeters) {


            Intent intent= new Intent(this, metersinfo.class);
            startActivity(intent);
            return true;


        }
        return super.onOptionsItemSelected(item);


    }}

