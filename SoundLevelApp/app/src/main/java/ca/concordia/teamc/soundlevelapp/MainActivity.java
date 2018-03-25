package ca.concordia.teamc.soundlevelapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    protected SharedPreferenceHelper sharedPreferenceHelper;
    protected Button goToRangePage = null;
    protected Button goToHistoryPage = null;
    protected Button goToConnectPage = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // DataFile test
//        DataSet ds = new DataSet();
//        ds.setProjectName("TestProject");
//        float[] f = new float[] { 1.2f, 9.7f, 3.4f };
//        DataFile df = new DataFile(getApplicationContext(),ds,f);
//
//        Log.d("MainActivity", df.readFile());
//        df.deleteFile();

        sharedPreferenceHelper = new SharedPreferenceHelper(MainActivity.this);

        setupUI();
    }

    protected void setupUI(){
        goToRangePage = (Button) findViewById(R.id.butrange);
        goToHistoryPage = (Button) findViewById(R.id.buthistory);
        goToConnectPage = (Button) findViewById(R.id.butlastconnect);
        goToRangePage.setOnClickListener(this);
        goToHistoryPage.setOnClickListener(this);
        goToConnectPage.setOnClickListener(this);
    }

    public void onClick(View v) {

        if (v.getId() == R.id.butrange) {
            Intent intent = new Intent(this, ListDevicesActivity.class);
            startActivity(intent);
        } else if (v.getId() == R.id.buthistory) {
            Intent intent = new Intent(this, metersinfo.class);
            startActivity(intent);
        } else if (v.getId() == R.id.butlastconnect) {
            Intent intent = new Intent(this, myDataSets.class);
            startActivity(intent);
        }
    }
}
