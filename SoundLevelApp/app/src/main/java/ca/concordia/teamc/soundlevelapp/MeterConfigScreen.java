package ca.concordia.teamc.soundlevelapp;

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

public class MeterConfigScreen extends AppCompatActivity{

    private final static String TAG = "MeterConfig";
    protected EditText ProjectText = null;
    protected EditText LocationText = null;
    protected EditText LastDateText = null;
    protected TextView DataText = null;
    protected ProgressBar Storage = null;
    protected Button saveButton = null;
    protected Button downloadButton = null;
    protected Button recordButton = null;

    Profile profile = new Profile();
    MeterController meterController = null;
    DataSetController dsc = null;
    DataFile newDF = null;

    BroadcastReceiver mReceiver;
    static BluetoothService BTService;
    boolean bound = false;

    int currentSequence = 0;
    boolean isDownloadRequestOK = false;
    boolean gotMeter;

    static final byte[] downloadSequence = {'{','{','{'};
    static final byte[] uploadSequence = {'}','}','}'};
    static final byte[] dataStartSequence = {60,60,60};
    static final byte[] dataEndSequence = {62,62,62};
    static final byte[] okSequence = {'O','K'};
    static final byte[] rcvSequence = {'r','c','v'};
    static final byte[] recordSequence = {'#','#','#'};

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

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothService.BT_MESSAGE);


        if (sharedPreferenceHelper.getProfileProject() != null) {
            LocationText.setText(sharedPreferenceHelper.getProfileLocation());
        }

        saveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(currentSequence == 0){
                    if ((LocationText.getText().toString().matches(""))
                            || (ProjectText.getText().toString().matches(""))/* || (LastDateText.getText().toString().matches(""))*/ ) {
                        Toast msg = Toast.makeText(getApplicationContext(), "Please fill any empty fields.", Toast.LENGTH_LONG);
                        msg.show();
                    } else {
                        AlertDialog.Builder altdial = new AlertDialog.Builder(MeterConfigScreen.this);
                        altdial.setMessage("Are you sure you want to upload the following changes for this device?").setCancelable(false)
                                .setPositiveButton("Upload", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        // send download message
                                        Log.d("SEND", "upload");
                                        BTService.write(uploadSequence);
                                        currentSequence = 3;
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
                }else{
                    Log.d(TAG,"Waiting for sequence " + currentSequence + " to finish.");
                }
            }
        });

        downloadButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(currentSequence == 0){
                 // send download message
                    Log.d("SEND", "download");
                    BTService.write(downloadSequence);
                    currentSequence = 1;
                }else{
                    Log.d(TAG,"Waiting for sequence " + currentSequence + " to finish.");
                }
            }
        });

        recordButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(currentSequence == 0){
                    // send download message
                    Log.d("SEND", "record");
                    BTService.write(recordSequence);
                    currentSequence = 2;
                }else{
                    Log.d(TAG,"Waiting for sequence " + currentSequence + " to finish.");
                }
            }
        });

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (!gotMeter) {
                    currentMeter = meterController.getSelectedMeterRecord(BTService.getMACAddress());
                    if (currentMeter == null) {
                        currentMeter = new Meter();
                        currentMeter.setSensorName(BTService.getDeviceName());
                        currentMeter.setMacAddress(BTService.getMACAddress());
                        currentMeter.setRecordingStatus(false);
                        currentMeter.setStartRecordingDate(System.currentTimeMillis());
                        String lp = LocationText.getText().toString();
                        if (lp.isEmpty()) {
                            lp = "Empty";
                        }
                        currentMeter.setLocation(lp);
                        lp = ProjectText.getText().toString();
                        if (lp.isEmpty()) {
                            lp = "Empty";
                        }
                        currentMeter.setLastKnownProject(lp);
                        meterController.addMeterData(currentMeter);
                    }
                    currentMeter.setLastConnectionDate(System.currentTimeMillis());

                    ProjectText.setText(currentMeter.getLastKnownProject());
                    LocationText.setText(currentMeter.getLocation());
                    gotMeter = true;
                }

                byte[] orgMsg = intent.getByteArrayExtra("message");
                int msgLength = intent.getIntExtra("length", 0);
                byte[] msg = Arrays.copyOfRange(orgMsg, 0, msgLength);
                Log.d(TAG, "got length: "+ msgLength);
                Log.d(TAG, Arrays.toString(msg));

                switch(currentSequence){
                    case 1:
                        if (isDownloadRequestOK){
                            if (Bytes.indexOf(msg, dataEndSequence) > -1) {
                                long currentTime = System.currentTimeMillis();
                                long startTime = currentTime - newDF.getSize()*125;
                                DataSet dataSet = new DataSet(currentMeter.getLastKnownProject(),currentMeter.getLocation(), currentTime, startTime, currentMeter.getSensorName(),newDF.getFileName() );
                                dsc.addDataSet(dataSet);
                                newDF = null;
                                BTService.write(rcvSequence);
                                currentSequence = 0;
                                isDownloadRequestOK = false;
                                Toast.makeText(getApplicationContext(), "New dataset created", Toast.LENGTH_SHORT).show();
                            } else {
                                if (newDF == null){
                                    newDF = new DataFile(MeterConfigScreen.this.getApplicationContext());
                                }
                                newDF.writeToFile(msg);
                            }
                        } else {
                            if (Bytes.indexOf(msg, okSequence) > -1){
                                isDownloadRequestOK = true;
                            }
                        }
                        break;
                    case 2:
                        if (Bytes.indexOf(msg,okSequence) > -1){
                            long currentTime = System.currentTimeMillis();
                            boolean newRecordingStatus = !currentMeter.getRecordingStatus();
                            currentMeter.setRecordingStatus(newRecordingStatus);
                            currentMeter.setStartRecordingDate(currentTime);
                            currentMeter.setLastConnectionDate(currentTime);
                            meterController.updateMeterRecord(currentMeter);
                            if (newRecordingStatus) {
                                Toast.makeText(getApplicationContext(), "Meter now recording", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getApplicationContext(), "Meter stopped recording", Toast.LENGTH_SHORT).show();
                            }
                            currentSequence = 0;
                            Log.d(TAG,"Meter Confirmed new recording status");
                        }
                        break;
                    case 3:
                        // expect ok to send data
                        if (Bytes.indexOf(msg,okSequence) > -1) {
                            Log.d(TAG, "Received upload OK");
                            String projectName = ProjectText.getText().toString();
                            String projectLocation = LocationText.getText().toString();
                            currentMeter.setLastKnownProject(projectName);
                            currentMeter.setLocation(projectLocation);
                            currentMeter.setLastConnectionDate(System.currentTimeMillis());
                            meterController.updateMeterRecord(currentMeter);
                            currentSequence = 0;

                            Toast.makeText(getApplicationContext(), "Configuration uploaded", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    default:
                        int value = msg[0];
                        double dB = (((value+128)*66.22235685/256)+28.26779888);
                        Log.d(TAG, ": 8bit int: "+ Integer.toString(value));
                        DataText.setText(String.format("%.1f", dB));
                }
            }
        };
        registerReceiver(mReceiver, filter);
    }



    @Override
    public void onStart(){
        super.onStart();

        meterController = MeterController.getInstance(this);
        dsc = DataSetController.getInstance(this);

        Intent BTSIntent = new Intent(this, BluetoothService.class);
        bindService(BTSIntent, connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onResume(){
        super.onResume();
        gotMeter = false;
    }

    @Override
    public void onPause(){
        super.onPause();
        try{
            unregisterReceiver(mReceiver);
        }catch (IllegalArgumentException e){
            // no receiver registred
        }
        BTService.disconnect();
        unbindService(connection);
        bound = false;

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
            finish();
        }
    };
    /*@Override
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
    }*/

    @Override
    public void onStop(){
        super.onStop();
    }
}
