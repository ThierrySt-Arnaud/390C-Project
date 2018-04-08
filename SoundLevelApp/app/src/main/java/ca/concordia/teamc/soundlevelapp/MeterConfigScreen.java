package ca.concordia.teamc.soundlevelapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import android.content.DialogInterface;

import com.google.common.primitives.Bytes;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.io.UnsupportedEncodingException;
import java.sql.Time;

import static android.support.v4.os.LocaleListCompat.create;


public class MeterConfigScreen extends AppCompatActivity{

    private final static String TAG = "MeterConfig";
    protected EditText ProjectText =null;
    protected EditText LocationText =null;
    protected EditText LastDateText =null;
    protected TextView DataText = null;
    protected ProgressBar Storage = null;
    protected Button saveButton = null;
    protected Button downloadButton = null;
    protected Button recordButton = null;
    protected Button btn;

    Profile profile = new Profile();
    MeterController meterController = null;
    DataSetController dsc = null;
    DataFile newDF = null;

    BroadcastReceiver mReceiver;
    BluetoothService BTService;
    boolean bound=false;

    boolean isDownloadRequestSend = false;
    boolean isDownloadRequestOK = false;
    boolean isUploadRequestSend = false;
    boolean isUploadRequestOk = false;
    boolean isRecordRequestSend = false;

    static final byte[] downloadSequence = {123,123,123};
    static final byte[] uploadSequence = {125,125,125};
    static final byte[] dataStartSequence = {60,60,60};
    static final byte[] dataEndSequence = {62,62,62,26};
    static final byte[] okSequence = {111,107};
    static final byte[] rcvSequence = {82, 67, 86};
    static final byte[] recordSequence = {35,35,35};

    Meter currentMeter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.meter_config_screen);

        ProjectText = (EditText) findViewById(R.id.ProjectEditText);
        LocationText = (EditText) findViewById(R.id.LocationEditText);
        LastDateText = (EditText) findViewById(R.id.LastDateEditText);
        DataText = (TextView) findViewById(R.id.textView2);
        Storage = (ProgressBar) findViewById(R.id.progressBar);
        saveButton = (Button) findViewById(R.id.saveButton);
        downloadButton = (Button) findViewById(R.id.downloadButton);
        recordButton = (Button) findViewById(R.id.button5);

        final SharedPreferenceHelper sharedPreferenceHelper = new SharedPreferenceHelper(MeterConfigScreen.this);

        if (sharedPreferenceHelper.getProfileProject() != null) {
            LocationText.setText(sharedPreferenceHelper.getProfileLocation());
        }

        saveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d("SEND", "upload");
            if(isUploadRequestSend && isUploadRequestOk){
                // expect RCV
                Log.d(TAG, "Still waiting for RCV");
            }else if (!isUploadRequestOk){
                // expect ok, wait for ok
                Log.d(TAG, "Still waiting for upload OK");
            }else{
                // send download message
                BTService.write(uploadSequence);
                isUploadRequestSend = true;
            }
            }
        });

        downloadButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            Log.d("SEND", "download");
            if(isDownloadRequestSend && isDownloadRequestOK){
                // expect config file, do nothing
                Log.d(TAG, "Still waiting for config and data file");
            }else if (!isDownloadRequestOK){
                // expect ok, wait for ok
                Log.d(TAG, "Still waiting for download OK");
            }else{
             // send download message
                BTService.write(downloadSequence);
                isDownloadRequestSend = true;
            }
            }
        });

        recordButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            Log.d("SEND", "record");
            if(isRecordRequestSend){
                // expect config file, do nothing
                Log.d("MeterConfig", "Still waiting for config and data file");
            } else{
                // send download message
                BTService.write(recordSequence);
                isRecordRequestSend = true;
            }
        }
    });
    }

    @Override
    public void onStart(){
        super.onStart();
        Intent intent = getIntent();
        String project = intent.getStringExtra("projectName");
        String location = intent.getStringExtra("meterLocation");
        String lastdate = intent.getStringExtra("profilelastdate");

        meterController = MeterController.getInstance(this);
        dsc = DataSetController.getInstance(this);

        ProjectText.setText(project);
        LocationText.setText(location);
        LastDateText.setText(lastdate);

        Intent BTSIntent = new Intent(this, BluetoothService.class);
        bindService(BTSIntent, connection, Context.BIND_AUTO_CREATE);

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothService.BT_MESSAGE);

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                byte[] orgMsg = intent.getByteArrayExtra("message");
                int msgLength = intent.getIntExtra("length", 0);
                byte[] msg = Arrays.copyOfRange(orgMsg, 0, msgLength);
                Log.d("Receiver", "got length: "+ msgLength);
                Log.d("Receiver", Arrays.toString(msg));

                if(isDownloadRequestSend && isDownloadRequestOK){
                    if (Bytes.indexOf(msg, dataEndSequence) > -1) {

                        long currentTime = System.currentTimeMillis();
                        long startTime = currentTime - newDF.getSize()*125;
                        DataSet dataSet = new DataSet(currentMeter.getLastKnownProject(),currentMeter.getLocation(), currentTime, startTime, "meterRef",newDF.getFileName() );
                        dsc.addDataSet(dataSet);
                        newDF = null;
                        BTService.write(rcvSequence);
                        isDownloadRequestOK = false;
                        isDownloadRequestSend = false;
                    } else {
                        newDF.writeToFile(msg);
                    }
                } else if(!isDownloadRequestOK){
                    // expect ok
                    if (Bytes.indexOf(msg,okSequence) > -1){
                        isDownloadRequestOK = true;
                        newDF = new DataFile(context);
                        Log.d("MeterConfig", "Received download OK");
                        Log.d("MeterConfig", "Received OK");
                    }
                } else if(isUploadRequestSend && isUploadRequestOk){
                    // expect config file
                    if (Bytes.indexOf(msg,rcvSequence) > -1){
                        isUploadRequestOk = false;
                        isUploadRequestSend = false;

                    }
                }else if(isUploadRequestOk){
                    // expect ok to send data
                    if (Bytes.indexOf(msg,okSequence) > -1) {
                        Log.d("MeterConfig", "Received upload OK");
                        String projectName = ProjectText.getText().toString();
                        String projectLocation = LocationText.getText().toString();
                        currentMeter.setLastKnownProject(projectName);
                        currentMeter.setLocation(projectLocation);
                        currentMeter.setLastConnectionDate(System.currentTimeMillis());
                        meterController.updateMeterRecord(currentMeter);
                    }
                } else if(isRecordRequestSend){
                    if (Bytes.indexOf(msg,okSequence) > -1){
                        long currentTime = System.currentTimeMillis();
                        currentMeter.setRecordingStatus(!currentMeter.getRecordingStatus());
                        currentMeter.setStartRecordingDate(currentTime);
                        currentMeter.setLastConnectionDate(currentTime);
                        Log.d(TAG,"Meter Confirmed new recording status");
                    }
                } else if(msgLength == 1){
                    int value = msg[0];
                    double dB = (((value+128)*66.22235685/256)+42.26779888);
                    Log.d("DEBUG", ": 8bit int: "+ Integer.toString(value));
                    DataText.setText(String.format("%.1f", dB));
                }else{
                    Log.w("DEBUG", "msgLength > 1, not a reading");
                }
            }
        };
        registerReceiver(mReceiver, filter);
        currentMeter = meterController.getSelectedMeterRecord(BTService.getMACAddress());
        if (currentMeter == null){
            currentMeter = new Meter();
            currentMeter.setSensorName(BTService.getDeviceName());
            currentMeter.setMacAddress(BTService.getMACAddress());
            currentMeter.setLastConnectionDate(System.currentTimeMillis());
            currentMeter.setRecordingStatus(false);
            currentMeter.setStartRecordingDate(System.currentTimeMillis());
            String lp = LocationText.getText().toString();
            if(lp.isEmpty()){
                lp = "Empty location";
            }
            currentMeter.setLocation(lp);
            lp = ProjectText.getText().toString();
            if(lp.isEmpty()){
                lp = "Empty project";
            }
            currentMeter.setLastKnownProject(lp);
            meterController.addMeterData(currentMeter);
        }
    }
    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            BluetoothService.LocalBinder binder = (BluetoothService.LocalBinder) service;
            BTService = binder.getService();
            bound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bound = false;
        }
    };

    public void dialogevent(View view){

    btn = (Button) findViewById(R.id.saveButton);
    btn.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if ((LocationText.getText().toString().matches(""))
                    || (ProjectText.getText().toString().matches(""))|| (LastDateText.getText().toString().matches("")) ) {
                Toast msg = Toast.makeText(getApplicationContext(), "Please fill any empty fields.", Toast.LENGTH_LONG);
                msg.show();
            } else {
                AlertDialog.Builder altdial = new AlertDialog.Builder(MeterConfigScreen.this);
                altdial.setMessage("Are you sure you want to upload the following changes for this device?").setCancelable(false)
                        .setPositiveButton("Upload", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                finish();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        });
                AlertDialog alert = altdial.create();
                alert.setTitle("Confirmation");
                alert.show();

            }
        }
    });

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

    /*public int indexOf(byte[] outerArray, byte[] smallerArray) {
        for(int i = 0; i < outerArray.length - smallerArray.length+1; ++i) {
            boolean found = true;
            for(int j = 0; j < smallerArray.length; ++j) {
                if (outerArray[i+j] != smallerArray[j]) {
                    found = false;
                    break;
                }
            }
            if (found) return i;
        }
        return -1;
    }*/

    @Override
    public void onStop(){
        super.onStop();
        try{
            unregisterReceiver(mReceiver);
        }catch (IllegalArgumentException e){
            // no receiver registred
        }

        unbindService(connection);
        bound = false;
    }
}
