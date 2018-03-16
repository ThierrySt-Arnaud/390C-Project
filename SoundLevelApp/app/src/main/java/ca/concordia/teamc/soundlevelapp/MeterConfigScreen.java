package ca.concordia.teamc.soundlevelapp;

import android.hardware.camera2.params.MeteringRectangle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.content.Intent;
import android.content.DialogInterface;

import static android.support.v4.os.LocaleListCompat.create;


public class MeterConfigScreen extends AppCompatActivity{
    protected EditText ProjectText =null;
    protected EditText LocationText =null;
    //protected Button saveButton = null;
    Profile profile = new Profile();

 public static Button btn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.meter_config_screen);

        ProjectText = (EditText) findViewById(R.id.ProjectEditText);
        LocationText = (EditText) findViewById(R.id.LocationEditText);

        //saveButton = (Button) findViewById(R.id.saveButton);
        //saveButton.setOnClickListener(saveListener);

       /*final SharedPreferenceHelper sharedPreferenceHelper = new SharedPreferenceHelper(MeterConfigScreen.this);

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
                    //editText(false);
                    Toast toast = Toast.makeText(getApplicationContext(), "Saved!", Toast.LENGTH_LONG);
                    toast.show();
                }
            }
        });*/
    }

    public void dialogevent(View view){

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

}