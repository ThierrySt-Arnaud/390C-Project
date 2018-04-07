package ca.concordia.teamc.soundlevelapp;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.Arrays;
import java.util.Random;

public class myGraph extends AppCompatActivity {

    LineGraphSeries<DataPoint> series;
    int DSID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_graph);

        //Beginning of x Axis
        int x =0;

        Random rand = new Random();

        double  n = 0;
        //Dummy data of the sound recorded
        //int mySoundData[]={33,3,4,5,56,76,87,35,7,67,86,87};
        String filePath = getIntent().getStringExtra("FilePath");
        int DSID = getIntent().getIntExtra("DSID",0);
        Log.d("Graph", "file: " + filePath);
        int[] fileData = DataFileController.getdata(filePath);
        Log.d("Graph", "Data from file: " + Arrays.toString(fileData));

        int mySoundData[]=fileData;
        //Lenght of the array of sound recorded
        //int lengthOfmySoundData = mySoundData.length;

        //Recognition of the ID of the graph from the layout
        GraphView graph = (GraphView) findViewById(R.id.graph);

        //creating graph
        series = new LineGraphSeries <DataPoint>();
        for (int i=0; i<fileData.length; i++){
            x=1+x;
            series.appendData(new DataPoint(x , mySoundData[i] ), true ,102);
        }

        graph.addSeries(series);

        //The names of the Axis of the graph as well as the color is defined.
        graph.getGridLabelRenderer().setVerticalAxisTitle("Level of dB");
        graph.getGridLabelRenderer().setHorizontalAxisTitle("Time in s");
        series.setColor(Color.RED);
    }

}
