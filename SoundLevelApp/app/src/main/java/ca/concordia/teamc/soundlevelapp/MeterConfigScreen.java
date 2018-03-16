package ca.concordia.teamc.soundlevelapp;

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

public class MeterConfigScreen extends AppCompatActivity{
    protected EditText ProjectText =null;
    protected EditText LocationText =null;
    protected Button saveButton = null;
    Profile profile = new Profile();

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
                    editText(false);
                    Toast toast = Toast.makeText(getApplicationContext(), "Saved!", Toast.LENGTH_LONG);
                    toast.show();
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
}

