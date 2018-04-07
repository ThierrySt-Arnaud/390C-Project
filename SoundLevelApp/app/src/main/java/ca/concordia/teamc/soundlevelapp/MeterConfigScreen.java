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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.io.UnsupportedEncodingException;
import java.sql.Time;

import static android.support.v4.os.LocaleListCompat.create;


public class MeterConfigScreen extends AppCompatActivity{
    protected EditText ProjectText =null;
    protected EditText LocationText =null;
    protected EditText LastDateText =null;
    protected TextView DataText = null;
    protected ProgressBar Storage = null;
    protected Button saveButton = null;
    protected Button downloadButton = null;
    protected static Button btn;

    Profile profile = new Profile();
    MeterController meterController = null;
    DataFileController dfc = null;
    DataSetController dsc = null;

    BroadcastReceiver mReceiver;
    BluetoothService BTService;
    boolean bound=false;

    boolean isDownloadRequestSend = false;
    boolean isDownloadRequestOK = false;
    boolean isUploadRequestSend = false;
    boolean isUploadRequestOk = false;

    byte[] downloadSequence = {123,123,123};
    byte[] uploadSequence = {125,125,125};
    byte[] configStartSequence = {60,60,60};
    byte[] configEndSequence = {62,62,62};
    byte[] okSequence = {111,107};
    byte[] rcvSequence = {82, 67, 86};

    boolean editable = false;

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

        final SharedPreferenceHelper sharedPreferenceHelper = new SharedPreferenceHelper(MeterConfigScreen.this);

        if (sharedPreferenceHelper.getProfileProject() != null) {
            LocationText.setText(sharedPreferenceHelper.getProfileLocation());
        }

        saveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d("SEND", "upload");
                if(isUploadRequestSend && isUploadRequestOk){
                    // expect RCV
                    Log.d("MeterConfig", "Still waiting for RCV");
                }else if (isUploadRequestSend && !isUploadRequestOk){
                    // expect ok, wait for ok
                    Log.d("MeterConfig", "Still waiting for upload OK");
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
                    Log.d("MeterConfig", "Still waiting for config and data file");
                }else if (isDownloadRequestSend && !isDownloadRequestOK){
                    // expect ok, wait for ok
                    Log.d("MeterConfig", "Still waiting for download OK");
                }else{
                 // send download message
                    BTService.write(downloadSequence);
                    isDownloadRequestSend = true;
                }
            }
        });

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
                    // expect config file
                    if (indexOf(msg,configStartSequence) != -1){
                        isDownloadRequestOK = false;
                        isDownloadRequestSend = false;
                        try{
                            //byte[] file = msg.split("<<<");
                            byte[] configByte = Arrays.copyOfRange(msg,0, indexOf(msg,configStartSequence)-1);
                            byte[] data = Arrays.copyOfRange(msg,indexOf(msg,configStartSequence)+3, indexOf(msg,configEndSequence)-1);

                            String configString = new String(configByte,0,configByte.length);
                            configString = configString.replace((char) 0x0A, '\n');
                            configString = configString.replace((char) 0x0D, '\n');
                            String[] config = configString.split("\\r?\\n");

                            for(String str : config){
                                Log.d("CONFIG", str);
                            }

                            Log.d("DATA", Arrays.toString(data));

                            dfc = new DataFileController(context, config[0], config[1], data);
                            dfc.addDataFile();

                            // DataRef is place holder
                            //dataSet = new DataSet(config[0],config[1], System.currentTimeMillis(), System.currentTimeMillis(), "meterRef", dfc.getFilePath());
                            dsc = new DataSetController(context, config[0],config[1], System.currentTimeMillis(), System.currentTimeMillis(), "meterRef", dfc.getFilePath());
                            dsc.addDataSet();

                            ProjectText.setText(config[0]);
                            LocationText.setText(config[1]);
                            Storage.setProgress(Integer.parseInt(config[2]));
                            DataText.setTextSize(20);
                            DataText.setText(Arrays.toString(data));
                        }catch (ArrayIndexOutOfBoundsException exception){
                            Log.w("MeterConfig", "Ill formatted file!");
                        }
                    }
                }else if(isDownloadRequestSend && !isDownloadRequestOK){
                    // expect ok
                    if (indexOf(msg,okSequence) != -1){
                        isDownloadRequestOK = true;
                        Log.d("MeterConfig", "Received download OK");
                        Log.d("MeterConfig", "Received OK");
                    }
                }
                if(isUploadRequestSend && isUploadRequestOk){
                    // expect config file
                    if (indexOf(msg,rcvSequence) != -1){
                        isUploadRequestOk = false;
                        isUploadRequestSend = false;

                    }
                }else if(isUploadRequestSend && !isUploadRequestOk){
                    // expect ok to send data
                    if (indexOf(msg,okSequence) != -1){
                        isUploadRequestOk = true;
                        Log.d("MeterConfig", "Received upload OK");
                        String projectName = ProjectText.getText().toString();
                        String projectLocation = LocationText.getText().toString();
                        long time = System.currentTimeMillis();
                        String configSend = projectName +">"+projectLocation;
                        BTService.write(configSend.getBytes());
                        // place holder verify later
                        //meter = new Meter(0,projectName, BTService.MACAddress, projectLocation,projectName,Long.toString(time),0,Long.toString(time));
                        MeterController mc = new MeterController(context,0,projectName, BTService.MACAddress, projectLocation,projectName,time,false,time);
                        mc.addMeterData();
                    }
                }

                if(msgLength == 1){
                    try{
                        //int value = Integer.parseInt(msg);
                        Byte byteValue = msg[0];
                        int value = byteValue.intValue();

                        if (value >= 0 && value <= 255){
                            int dB = value / 4 + 39;
                            Log.d("DEBUG", ": 8bit int: "+ Integer.toString(value));
                            DataText.setText(Integer.toString(dB));
                        }else{
                            Log.w("WARN", "int not 8bit");
                        }

                    }catch (NumberFormatException e){
                        Log.w("DEBUG", "Ill formated int");
                    }
                }else{
                    Log.w("DEBUG", "msgLength > 1, not a reading");
                }
            }
        };

        registerReceiver(mReceiver, filter);
    }

    public void onStart(){
        super.onStart();
        Intent intent = getIntent();
        String project = intent.getStringExtra("projectName");
        String location = intent.getStringExtra("meterLocation");
        String lastdate = intent.getStringExtra("profilelastdate");

        ProjectText.setText(project);
        LocationText.setText(location);
        LastDateText.setText(lastdate);

        Intent BTSIntent = new Intent(this, BluetoothService.class);
        bindService(BTSIntent, connection, Context.BIND_AUTO_CREATE);
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


    protected void editText(boolean bool){

        ProjectText.setFocusableInTouchMode(bool);
        ProjectText.setFocusable(bool);


        LocationText.setFocusableInTouchMode(bool);
        LocationText.setFocusable(bool);

    }

    public void switchEdit(View v){
        editable = !editable;
        editText(editable);
    }

    public int indexOf(byte[] outerArray, byte[] smallerArray) {
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
    }

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
