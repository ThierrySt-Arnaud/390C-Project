package ca.concordia.teamc.soundlevelapp;


import android.content.Context;
import android.content.Intent;
import android.nfc.Tag;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;


public class DataListAdapter extends BaseAdapter

{
    private Context context; //context
    private List<Data> datasets; //data source of the list adapter

    //public constructor
    public DataListAdapter(Context context, List<Data> datasets) {
        this.context = context;
        this.datasets = datasets;
    }

    @Override
    public int getCount() {
        return datasets.size(); //returns total of items in the list
    }

    @Override
    public Object getItem(int position) {
        return datasets.get(position); //returns list item at the specified position
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // inflate the layout for each list row
        if (convertView == null) {
            convertView = LayoutInflater.from(context).
                    inflate(R.layout.datalist_item, parent, false);
        }

        // get current item to be displayed
        Data currentItem = (Data) getItem(position);

        // get the TextView for item name and item description
        TextView textViewItemName = (TextView)
                convertView.findViewById(R.id.text_view_project_name);
        TextView textViewItemLocation = (TextView)
                convertView.findViewById(R.id.text_view_item_location);
        TextView textViewItemDateStarted = (TextView)
                convertView.findViewById(R.id.text_view_item_datestarted);
        TextView textViewItemDateDownloaded = (TextView)
                convertView.findViewById(R.id.text_view_item_datedownloaded);

        //sets the text for item name and item description from the current item object
        textViewItemName.setText(currentItem.getItemName());
        textViewItemLocation.setText(currentItem.getItemLocation());
        textViewItemDateStarted.setText(currentItem.getItemDateStarted());
        textViewItemDateDownloaded.setText(currentItem.getItemDateDownloaded());

        // returns the view for the current row
        return convertView;
    }


}
