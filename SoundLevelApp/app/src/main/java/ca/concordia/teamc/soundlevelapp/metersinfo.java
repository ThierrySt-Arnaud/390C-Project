package ca.concordia.teamc.soundlevelapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import java.lang.String;
import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

public class metersinfo extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_metersinfo);


        List<Meter> Mylist = new ArrayList<>();
        Meter meterA = new Meter("Meter A","","Location","Project A","",false,"");
        Meter meterB = new Meter("Meter B", "", "Location","Project B","",false,"");
        Meter meterC = new Meter("Meter C","","Location","Project C","",true,"");

        Mylist.add(meterA);
        Mylist.add(meterB);
        Mylist.add(meterC);

        MeterListAdapter meterListAdapter = new MeterListAdapter(this, Mylist);
        ListView listView = (ListView) findViewById(R.id.listview);

        listView.setAdapter(meterListAdapter);
        listView.setOnItemClickListener(meterListListener);
    }

    AdapterView.OnItemClickListener meterListListener = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            Log.d(TAG, "Item " + i + "  got clicked");
            Intent myIntent = new Intent(adapterView.getContext(), metersinfopts.class);
            startActivity(myIntent);
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.actionbutton_metersinfo, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


        if (item.getItemId() == R.id.action_metersinrange) {


            Intent intent= new Intent(this, ListDevicesActivity.class);
            startActivity(intent);
            return true;

        }

        if (item.getItemId() == R.id.action_datasets) {


            Intent intent= new Intent(this, myDataSets.class);
            startActivity(intent);
            return true;

        }
        return super.onOptionsItemSelected(item);
    }
}
