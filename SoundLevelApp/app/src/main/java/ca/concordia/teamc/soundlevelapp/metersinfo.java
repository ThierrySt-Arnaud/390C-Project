package ca.concordia.teamc.soundlevelapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class metersinfo extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_metersinfo);
        List<String> Mylist = new ArrayList<>();
        Mylist.add("Meter A");
        Mylist.add("Meter B");
        Mylist.add("Meter C");
        ListView listView = (ListView) findViewById(R.id.listview);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,Mylist);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                if(position == 0)
                {
                    Intent myIntent = new Intent(metersinfo.this, metersinfopts.class);
                    startActivityForResult(myIntent, 0);
                }

           /* if(position == 2)
            {

                Intent myIntent =  new Intent(YourActivity.this, ThirdActivity.class);
                startActivityForResult(myIntent, 0);
            }*/
            }
        });
    }
}