package ca.concordia.teamc.soundlevelapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import java.lang.String;
import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

public class metersinfo extends AppCompatActivity {

    private MeterListAdapter meterListAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_metersinfo);
        final EditText meterSearch = (EditText) findViewById(R.id.MeterSearch);


        List<Meter> Mylist = new ArrayList<>();
        Meter meterA = new Meter("test 1", "test mac address 1", "location 1", "project A", "00-00-00", false, "00-00-00");
        Meter meterB = new Meter("test 2", "test mac address 2", "location 2", "project B", "00-00-00", false, "00-00-00");
        Meter meterC = new Meter("test 3", "test mac address 3", "location 3", "project C", "00-00-00", true, "00-00-00");

        Mylist.add(meterA);
        Mylist.add(meterB);
        Mylist.add(meterC);

        meterListAdapter = new MeterListAdapter(this, Mylist);
        ListView listView = (ListView) findViewById(R.id.listview);

        listView.setAdapter(meterListAdapter);
        listView.setOnItemClickListener(meterListListener);

        meterSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                (metersinfo.this).meterListAdapter.getFilter().filter(meterSearch.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
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
