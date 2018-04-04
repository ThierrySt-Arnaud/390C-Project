package ca.concordia.teamc.firmwaretest;

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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import android.content.DialogInterface;


public class MeterConfigScreen extends AppCompatActivity {
    protected EditText ProjectText = null;
    protected EditText LocationText = null;
    protected TextView DataText = null;
    protected ProgressBar Storage = null;
    protected Button saveButton = null;
    protected Button downloadButton = null;
    BroadcastReceiver mReceiver;
    BluetoothService BTService;
    boolean bound = false;
    boolean startData = false; // we got a message with config started


    public static Button btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.meter_config_screen);

        ProjectText = (EditText) findViewById(R.id.ProjectEditText);
        LocationText = (EditText) findViewById(R.id.LocationEditText);
        DataText = (TextView) findViewById(R.id.textView2);
        Storage = (ProgressBar) findViewById(R.id.progressBar);

        saveButton = (Button) findViewById(R.id.saveButton);
        downloadButton = (Button) findViewById(R.id.downloadButton);


        saveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if ((LocationText.getText().toString().matches(""))
                        || (ProjectText.getText().toString().matches(""))) {
                    Toast msg = Toast.makeText(getApplicationContext(), "Invalid Input!", Toast.LENGTH_LONG);
                    msg.show();
                } else {
                    Log.d("SEND", ProjectText.getText().toString() + "\n" + LocationText.getText().toString());
                    BTService.write(ProjectText.getText().toString() + "\n");
                    BTService.write(LocationText.getText().toString() + "\n");

                    Toast toast = Toast.makeText(getApplicationContext(), "Saved!", Toast.LENGTH_LONG);
                    toast.show();
                }
            }
        });

        downloadButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d("SEND", "{{{");
                BTService.write("{{{");
            }
        });

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothService.BT_MESSAGE);

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String msg = intent.getStringExtra("message");
                Log.d("Receiver", "got message: " + msg);
                if (msg.lastIndexOf((char) 0x1A) != -1) {
                    Log.d("DETECTED", "detected EOF");
                }

                if (msg.lastIndexOf((char) 0x0A) != -1 || msg.lastIndexOf((char) 0x0D) != -1) {
                    Log.d("DETECTED", "detected NL");
                    msg = msg.replace((char) 0x0A, '\n');
                    msg = msg.replace((char) 0x0D, '\n');
                }

                if (msg.contains("<<<")) {
                    Log.d("DETECTED", "Start of Data");
                    startData = true;
                }

                if (msg.contains(">>>")) {
                    Log.d("DETECTED", "End of Data");
                    startData = false;
                }

                try {
                    String[] file = msg.split("<<<");
                    String[] config = file[0].split("\n");
                    String data = file[1].replace(">>>", "");

                    for (String str : config) {
                        Log.d("CONFIG", str);
                    }

                    Log.d("DATA", data);

                    ProjectText.setText(config[0]);
                    LocationText.setText(config[1]);
                    Storage.setProgress(Integer.parseInt(config[2]));
                    DataText.setTextSize(20);
                    DataText.setText(data);


                } catch (ArrayIndexOutOfBoundsException exception) {
                    Log.d("DEBUG", "Ill formated message");
                }
            }
        };

        registerReceiver(mReceiver, filter);
    }

    public void onStart() {
        super.onStart();
        Intent intent = getIntent();
        String project = intent.getStringExtra("projectName");
        String location = intent.getStringExtra("meterLocation");

        ProjectText.setText(project);
        LocationText.setText(location);

        Intent BTSIntent = new Intent(this, BluetoothService.class);
        bindService(BTSIntent, connection, Context.BIND_AUTO_CREATE);
    }

    /**
     * Defines callbacks for service binding, passed to bindService()
     */
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

    public void dialogevent(View view) {

        btn = (Button) findViewById(R.id.saveButton);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ((LocationText.getText().toString().matches(""))
                        || (ProjectText.getText().toString().matches(""))) {
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
}