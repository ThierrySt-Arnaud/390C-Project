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
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;


public class MeterListAdapter extends BaseAdapter

{
    private Context context; //context
    private List<Meter> meters; //data source of the list adapter

    //public constructor
    public MeterListAdapter(Context context, List<Meter> meters) {
        this.context = context;
        this.meters = meters;
    }

    @Override
    public int getCount() {
        return meters.size(); //returns total of items in the list
    }

    @Override
    public Object getItem(int position) {
        return meters.get(position); //returns list item at the specified position
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
                    inflate(R.layout.meterlist_item, parent, false);
        }

        // get current item to be displayed
        Meter currentItem = (Meter) getItem(position);

        // get the TextView for item name and item description
        TextView textViewItemName = (TextView)
                convertView.findViewById(R.id.text_view_item_name);
        TextView textViewItemDescription = (TextView)
                convertView.findViewById(R.id.text_view_item_description);
        TextView textViewItemMeterLocation = (TextView)
                convertView.findViewById(R.id.text_view_item_meterlocation);
        ImageView microphoneset = convertView.findViewById(R.id.microphone_On);
        ImageView microphoneNot = convertView.findViewById(R.id.microphone_Off);

        if (currentItem.getRecordingStatus()) {
            microphoneset.setVisibility(View.VISIBLE);
            microphoneNot.setVisibility(View.GONE);
        } else {
            microphoneset.setVisibility(View.GONE);
            microphoneNot.setVisibility(View.VISIBLE);
        }

        //sets the text for item name and item description from the current item object
        textViewItemName.setText(currentItem.getSensorName());
        textViewItemDescription.setText(currentItem.getLastKnownProject());
        textViewItemMeterLocation.setText(currentItem.getLocation());


        // returns the view for the current row
        return convertView;
    }


}

