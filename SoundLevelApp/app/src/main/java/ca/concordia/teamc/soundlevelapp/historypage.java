package ca.concordia.teamc.soundlevelapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class historypage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historypage);
        List<String> Mylist = new ArrayList<>();
        Mylist.add("Meter A");
        Mylist.add("Meter B");
        Mylist.add("Meter C");
        ListView listView = (ListView) findViewById(R.id.listview);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,Mylist);
        listView.setAdapter(adapter);
    }
}
