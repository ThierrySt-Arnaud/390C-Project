package ca.concordia.teamc.soundlevelapp;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
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

import com.google.common.primitives.Bytes;

import java.util.Arrays;


public class DummyMeterConfigScreen extends AppCompatActivity{
    protected EditText ProjectText =null;
    protected EditText LocationText =null;
    protected EditText LastDateText =null;
    protected TextView DataText = null;
    protected ProgressBar Storage = null;
    protected Button saveButton = null;
    protected Button downloadButton = null;
    protected Button recordButton = null;
    boolean bound=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dummy_meter_config_screen);

        ProjectText = (EditText) findViewById(R.id.ProjectEditText);
        LocationText = (EditText) findViewById(R.id.LocationEditText);
        LastDateText = (EditText) findViewById(R.id.LastDateEditText);
        DataText = (TextView) findViewById(R.id.textView2);
        Storage = (ProgressBar) findViewById(R.id.progressBar);
        saveButton = (Button) findViewById(R.id.saveButton);
        downloadButton = (Button) findViewById(R.id.downloadButton);
        recordButton = (Button) findViewById(R.id.button5);
    }

    @Override
    public void onStart(){
        super.onStart();
        Intent intent = getIntent();
        String project = intent.getStringExtra("projectName");
        String location = intent.getStringExtra("meterLocation");
        String lastdate = intent.getStringExtra("profilelastdate");

        ProjectText.setText(project);
        LocationText.setText(location);
        LastDateText.setText(lastdate);


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
}
