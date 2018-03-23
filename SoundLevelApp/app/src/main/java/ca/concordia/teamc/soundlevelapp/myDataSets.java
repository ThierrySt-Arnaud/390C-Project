package ca.concordia.teamc.soundlevelapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.content.Intent;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

public class myDataSets extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mydatasets);
       /* List<String> Mylist = new ArrayList<>();
        Mylist.add("For Sprint 2");

        ListView listView = (ListView) findViewById(R.id.listview);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,Mylist);
        listView.setAdapter(adapter);*/

        List<Data> Mylist = new ArrayList<>();
        Data dataA = new Data("Project A","Location","Date Started","Date Downloaded" );
        Data dataB = new Data("Project B","Location","Date Started","Date Downloaded");
        Data dataC = new Data("Project C","Location","Date Started","Date Downloaded");

        Mylist.add(dataA);
        Mylist.add(dataB);
        Mylist.add(dataC);

        DataListAdapter dataListAdapter = new DataListAdapter(this, Mylist);
        ListView listView = (ListView) findViewById(R.id.listview);

        listView.setAdapter(dataListAdapter);
        listView.setOnItemClickListener(dataListListener);
    }

    AdapterView.OnItemClickListener dataListListener = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            Log.d(TAG, "Item " + i + "  got clicked");
            Intent myIntent = new Intent(adapterView.getContext(), myDataSets_Detailed.class);
            startActivity(myIntent);
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.actionbutton_datasets, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


        if (item.getItemId() == R.id.action_metersinrange) {


            Intent intent= new Intent(this, ListDevicesActivity.class);
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
