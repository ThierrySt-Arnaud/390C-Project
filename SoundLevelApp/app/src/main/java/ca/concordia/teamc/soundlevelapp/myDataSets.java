package ca.concordia.teamc.soundlevelapp;

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
import android.content.Intent;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

public class myDataSets extends AppCompatActivity {

    private DataListAdapter dataListAdapter;
    private DataSetController dsc = DataSetController.getInstance(this);
    private List<DataSet> Mylist=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mydatasets);
    }

    @Override
    public void onStart(){
        super.onStart();
        final EditText dataSetSearch = (EditText) findViewById(R.id.DataSetSearch);
        /* List<String> Mylist = new ArrayList<>();
        Mylist.add("For Sprint 2");

        ListView listView = (ListView) findViewById(R.id.listview);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,Mylist);
        listView.setAdapter(adapter);*/

        Mylist = dsc.getAllDataSet();
        dataListAdapter = new DataListAdapter(this, Mylist);
        ListView listView = (ListView) findViewById(R.id.listview);

        listView.setAdapter(dataListAdapter);
        listView.setOnItemClickListener(dataListListener);

        dataSetSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                (myDataSets.this).dataListAdapter.getFilter().filter(dataSetSearch.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

    }

    AdapterView.OnItemClickListener dataListListener = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            Log.d(TAG, "Item " + i + "  got clicked");
            Intent myIntent = new Intent(adapterView.getContext(), myDataSets_Detailed.class);
            int id = Mylist.get(i).getDataSetID();
            myIntent.putExtra("ID", id);
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
