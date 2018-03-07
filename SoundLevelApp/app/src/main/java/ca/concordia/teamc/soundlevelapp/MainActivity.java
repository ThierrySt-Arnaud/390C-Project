package ca.concordia.teamc.soundlevelapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    protected Button goToRangePage = null;
    protected Button goToHistoryPage = null;
    protected Button goToConnectPage = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
            Intent intent = new Intent(this, rangepage.class);
            startActivity(intent);
        } else if (v.getId() == R.id.buthistory) {
            Intent intent = new Intent(this, historypage.class);
            startActivity(intent);
        } else if (v.getId() == R.id.butlastconnect) {
            Intent intent = new Intent(this, connectpage.class);
            startActivity(intent);
        }
    }

}
