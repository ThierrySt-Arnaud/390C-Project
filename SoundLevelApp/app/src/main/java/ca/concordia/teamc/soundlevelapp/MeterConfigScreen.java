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

public class MeterConfigScreen extends AppCompatActivity{
    protected EditText nameText = null;
    protected EditText ProjectText =null;
    protected EditText LocationText =null;
    protected Button saveButton = null;
    Profile profile = new Profile();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.meter_config_screen);

        nameText = (EditText) findViewById(R.id.nameEditText); //connecting edittext for each
        ProjectText = (EditText) findViewById(R.id.ProjectEditText);
        LocationText = (EditText) findViewById(R.id.LocationEditText);

        saveButton = (Button) findViewById(R.id.saveButton);

        editText(false);


        final SharedPreferenceHelper sharedPreferenceHelper = new SharedPreferenceHelper(MeterConfigScreen.this);

        if (sharedPreferenceHelper.getProfileName() != null) {
            nameText.setText(sharedPreferenceHelper.getProfileName());
            ProjectText.setText(sharedPreferenceHelper.getProfileProject());
            LocationText.setText(sharedPreferenceHelper.getProfileLocation());
        }

        saveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if ((LocationText.getText().toString().matches(""))
                        || (nameText.getText().toString().matches(""))
                        || (ProjectText.getText().toString().matches(""))) {
                    Toast msg = Toast.makeText(getApplicationContext(), "Invalid Input!", Toast.LENGTH_LONG);
                    msg.show();
                } else {
                    profile.setName(nameText.getText().toString());
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
        return super.onOptionsItemSelected(item);
    }


    protected void editText(boolean bool){
        nameText.setFocusableInTouchMode(bool);
        nameText.setFocusable(bool);

        ProjectText.setFocusableInTouchMode(bool);
        ProjectText.setFocusable(bool);

        LocationText.setFocusableInTouchMode(bool);
        LocationText.setFocusable(bool);
    }
}

