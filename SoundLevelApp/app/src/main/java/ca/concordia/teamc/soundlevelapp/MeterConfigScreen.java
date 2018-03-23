package ca.concordia.teamc.soundlevelapp;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.content.Intent;

public class MeterConfigScreen extends AppCompatActivity{
    protected EditText ProjectText =null;
    protected EditText LocationText =null;
    protected Button saveButton = null;
    Profile profile = new Profile();
    BroadcastReceiver mReceiver;
    BluetoothService BTService;
    boolean bound=false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.meter_config_screen);

        ProjectText = (EditText) findViewById(R.id.ProjectEditText);
        LocationText = (EditText) findViewById(R.id.LocationEditText);

        saveButton = (Button) findViewById(R.id.saveButton);

        editText(false);


       final SharedPreferenceHelper sharedPreferenceHelper = new SharedPreferenceHelper(MeterConfigScreen.this);

        if (sharedPreferenceHelper.getProfileProject() != null) {
            LocationText.setText(sharedPreferenceHelper.getProfileLocation());
        }

        saveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if ((LocationText.getText().toString().matches(""))
                        || (ProjectText.getText().toString().matches(""))) {
                    Toast msg = Toast.makeText(getApplicationContext(), "Invalid Input!", Toast.LENGTH_LONG);
                    msg.show();
                } else {
                    profile.setProject(ProjectText.getText().toString());
                    profile.setLocation(LocationText.getText().toString());

                    sharedPreferenceHelper.saveProfileName(profile);

                    BTService.write("project: "+ ProjectText.getText().toString());
                    BTService.write("location: "+ LocationText.getText().toString());

                    editText(false);
                    Toast toast = Toast.makeText(getApplicationContext(), "Saved!", Toast.LENGTH_LONG);
                    toast.show();
                }
            }
        });

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothService.BT_MESSAGE);

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String msg = intent.getStringExtra("message");
                Log.d("Receiver", "got message: "+ msg);
                if (msg.lastIndexOf((char) 0x1A) != -1){
                    Log.d("DETECTED", "detected EOF");
                }
                if (msg.lastIndexOf((char) 0x0A) != -1 || msg.lastIndexOf((char) 0x0D) != -1){
                    Log.d("DETECTED", "detected NL");
                }

                String[] file = msg.split("<<<");
                String[] config = file[0].split("\\r?\\n"); // doesn't work
                String[] data = file[1].replace(">>>","").split("\\r?\\n"); // doesn't work

                for(String str : config){
                    Log.d("CONFIG", str);
                }

                for(String str : data){
                    Log.d("DATA", str);
                }

                ProjectText.setText(config[0]);
                LocationText.setText(config[1]);

            }
        };

        registerReceiver(mReceiver, filter);
    }

    public void onStart(){
        super.onStart();
        Intent intent = getIntent();
        String project = intent.getStringExtra("projectName");
        String location = intent.getStringExtra("meterLocation");

        ProjectText.setText(project);
        LocationText.setText(location);

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_button, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.Edit) {
            editText(true);
        }

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
        //editText(bool: true);

        LocationText.setFocusableInTouchMode(bool);
        LocationText.setFocusable(bool);
        //editText(bool: true);
    }

    @Override
    protected void onDestroy() {
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
            mReceiver = null;
        }

        super.onDestroy();
    }
}

