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
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;


public class MeterListAdapter extends BaseAdapter implements Filterable

{
    private Context context; //context
    private List<Meter> meters; //data source of the list adapter
    protected List<Meter> meters_filtered;
    private MeterListAdapter.ItemFilter mFilter = new MeterListAdapter.ItemFilter();

    //public constructor
    public MeterListAdapter(Context context, List<Meter> meters) {
        this.context = context;
        this.meters = meters;
        this.meters_filtered = meters;
    }

    @Override
    public int getCount() {
        return meters_filtered.size(); //returns total of items in the list
    }

    @Override
    public Object getItem(int position) {
        return meters_filtered.get(position); //returns list item at the specified position
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

        if (currentItem.getRecordingStatus() != 0) {
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

    public Filter getFilter() {
        return mFilter;
    }

    private class ItemFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            Log.d("MeterListAdapter", "Filtering ...");
            String filterString = constraint.toString().toLowerCase();

            FilterResults results = new FilterResults();

            final List<Meter> list = meters;

            int count = list.size();
            final ArrayList<Meter> nlist = new ArrayList<Meter>(count);

            String filterableStringSensorName;
            String filterableStringLastProjectName ;

            Log.d("MeterListAdapter", "Searching for: " + filterString);

            for (int i = 0; i < count; i++) {
                filterableStringSensorName = list.get(i).getSensorName();
                filterableStringLastProjectName = list.get(i).getLastKnownProject();

                Log.d("MeterListAdapter", "matching with"+ filterableStringSensorName);
                Log.d("MeterListAdapter", "matching with"+ filterableStringLastProjectName);

                if (filterableStringSensorName.toLowerCase().contains(filterString) || filterableStringLastProjectName.toLowerCase().contains(filterString)) {
                    Log.d("MeterListAdapter", "Found item, adding to nList");
                    nlist.add(list.get(i));
                }
            }

            results.values = nlist;
            results.count = nlist.size();

            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            meters_filtered = (ArrayList<Meter>) results.values;
            Log.d("MeterListAdapter", "Publishing results");
            notifyDataSetChanged();
            notifyDataSetInvalidated();
        }
    }


}

