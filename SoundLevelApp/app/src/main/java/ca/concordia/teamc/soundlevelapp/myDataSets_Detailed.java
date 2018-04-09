package ca.concordia.teamc.soundlevelapp;


import android.content.Intent;
import android.nfc.Tag;
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
import android.widget.Toast;

import java.util.Date;

public class myDataSets_Detailed extends AppCompatActivity{
    static final String TAG = "DataSet View";
    private DataSetController dsc;
    private DataSet dataSet;
    Button yourButton;
    TextView pNameTV;
    TextView locationTV;
    TextView sNameTV;
    TextView startedTV;
    TextView downloadedTV;
    int ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG,"Called onCreate");
        setContentView(R.layout.activity_mydatasets_detailed);

        yourButton = (Button) findViewById(R.id.button_viewgraph);
        pNameTV = (TextView) findViewById(R.id.tvDSProject);
        locationTV = (TextView) findViewById(R.id.tvDSLocation);
        sNameTV = (TextView) findViewById(R.id.tvDSSensor);
        startedTV = (TextView) findViewById(R.id.tvDSDateStarted);
        downloadedTV = (TextView) findViewById(R.id.tvDSDateDownloaded);

        ID = getIntent().getIntExtra("ID",0);

    }

    @Override
    public void onStart(){
        super.onStart();
        Log.d(TAG,"Called onStart");
        dsc = DataSetController.getInstance(this);
        Log.d(TAG,"ID is "+ ID);
    }

    @Override
    public void onSaveInstanceState(Bundle saveInstanceState){
        Log.d(TAG,"Called onSaveInstanceState");

        saveInstanceState.putInt("ID", ID);
        Log.d(TAG,"Saved ID as " + ID);
        super.onSaveInstanceState(saveInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle saveInstanceState){
        super.onRestoreInstanceState(saveInstanceState);
        Log.d(TAG,"Called onSaveInstanceState");
        if (saveInstanceState != null){
            ID = saveInstanceState.getInt("ID", 0);
            Log.d(TAG,"Instance ID is : " + ID);
        }
    }

    @Override
    public  void onResume(){
        super.onResume();
        Log.d(TAG,"Called onResume");
        if (ID == 0){
            ID = getIntent().getIntExtra("ID",0);
            Log.d(TAG,"Intent ID is: " + ID);
        }
        dataSet = dsc.getSelectedDataSet(ID);
        yourButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Intent myIntent = new Intent(myDataSets_Detailed.this, myGraph.class);
                myIntent.putExtra("FilePath", dataSet.getDatafile());
                myIntent.putExtra("DSID", dataSet.getDataSetID());
                Log.d("DSD", "FilePath" + dataSet.getDatafile());
                startActivity(myIntent);
            }
        });
        String pName = dataSet.getProjectName();
        String location = dataSet.getLocation();
        String sName = dataSet.getMeterReferenceRecord();
        Date startedDate = new Date(dataSet.getDateStartRecord());
        Date downloadDate = new Date(dataSet.getDateOfDownload());
        Log.d("DSD", "FilePath" + dataSet.getDatafile());

        pNameTV.setText(pName);
        locationTV.setText(location);
        sNameTV.setText(sName);
        startedTV.setText(startedDate.toString());
        downloadedTV.setText(downloadDate.toString());
    }

    @Override
    public void onStop(){
        Log.d(TAG,"Called onStop");

        getIntent().putExtra("ID",ID);
        Log.d(TAG,"Saved ID as " + ID);
        super.onStop();
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

