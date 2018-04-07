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
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static android.content.ContentValues.TAG;


public class DataListAdapter extends BaseAdapter implements Filterable

{
    private Context context; //context
    protected List<DataSet> datasets; //data source of the list adapter
    protected List<DataSet> datasets_filtered;
    private ItemFilter mFilter = new ItemFilter();

    //public constructor
    public DataListAdapter(Context context, List<DataSet> datasets) {
        this.context = context;
        this.datasets = datasets;
        this.datasets_filtered = datasets;
    }

    @Override
    public int getCount() {
        return datasets_filtered.size(); //returns total of items in the list
    }

    @Override
    public Object getItem(int position) {
        return datasets_filtered.get(position); //returns list item at the specified position
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
        DataSet currentItem = (DataSet) getItem(position);

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
        textViewItemName.setText(currentItem.getProjectName());
        textViewItemLocation.setText(currentItem.getLocation());
        textViewItemDateStarted.setText(Long.toString(currentItem.getDateStartRecord()));
        textViewItemDateDownloaded.setText(Long.toString(currentItem.getDateOfDownload()));

        // returns the view for the current row
        return convertView;
    }

    public Filter getFilter() {
        return mFilter;
    }

    private class ItemFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            Log.d("DataListAdapter", "Filtering ...");
            String filterString = constraint.toString().toLowerCase();

            FilterResults results = new FilterResults();

            final List<DataSet> list = datasets;

            int count = list.size();
            final ArrayList<DataSet> nlist = new ArrayList<DataSet>(count);

            String filterableStringProjectName;
            String filterableStringMeterRefereance ;

            Log.d("DataListAdapter", "Searching for: " + filterString);

            for (int i = 0; i < count; i++) {
                filterableStringProjectName = list.get(i).getProjectName();
                filterableStringMeterRefereance = list.get(i).getMeterReferenceRecord();

                Log.d("DataListAdapter", "matching with"+ filterableStringProjectName);
                Log.d("DataListAdapter", "matching with"+ filterableStringMeterRefereance);

                if (filterableStringProjectName.toLowerCase().contains(filterString) || filterableStringMeterRefereance.toLowerCase().contains(filterString)) {
                    Log.d("DataListAdapter", "Found item, adding to nList");
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
            datasets_filtered = (ArrayList<DataSet>) results.values;
            Log.d("DataListAdapter", "Publishing results");
            notifyDataSetChanged();
            notifyDataSetInvalidated();
        }
    }


//    public void filter(String charText) {
//        charText = charText.toLowerCase(Locale.getDefault());
//        datasets.clear();
//        if (charText.length() == 0) {
//            datasets.addAll(datasets_filtered);
//        }
//        else
//        {
//            for (DataSet ds : datasets_filtered) {
//                if (ds.getProjectName().toLowerCase(Locale.getDefault()).contains(charText) || ds.getMeterReferenceRecord().toLowerCase(Locale.getDefault()).contains(charText)) {
//                    datasets.add(ds);
//                }
//            }
//        }
//        notifyDataSetChanged();
//    }

}
