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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_graph);

        //Beginning of x Axis
        double x = 0;
        double y = 0;
        Random rand = new Random();

        //Dummy data of the sound recorded
        //int mySoundData[]={33,3,4,5,56,76,87,35,7,67,86,87};

        String filePath = getIntent().getStringExtra("FilePath");
        byte[] fileData;
        if (filePath.isEmpty()){
            fileData = new byte[128*1024];
            rand.nextBytes(fileData);
        } else{
            Log.d("Graph", "file: " + filePath);
            DataFile df = new DataFile(this, filePath);
            fileData = df.getData();
            Log.d("Graph", "Data from file: " + Arrays.toString(fileData));
        }

        //double[] mySoundData= new double[fileData.length];
        //Lenght of the array of sound recorded
        //int lengthOfmySoundData = mySoundData.length;

        //Recognition of the ID of the graph from the layout
        GraphView graph = (GraphView) findViewById(R.id.graph);

        //creating graph
        series = new LineGraphSeries <DataPoint>();
        for (byte value: fileData){
            x += 0.125;
            y = (((value+128)*66.22235685/256)-12.26779888);
            series.appendData(new DataPoint(x , y), true ,2*1024*1024);
        }

        graph.addSeries(series);

        //The names of the Axis of the graph as well as the color is defined.
        graph.getGridLabelRenderer().setVerticalAxisTitle("Level of dB");
        graph.getGridLabelRenderer().setHorizontalAxisTitle("Time in s");
        series.setColor(Color.RED);

        graph.getViewport().setScalable(true);

        // set manual Y bounds
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinY(40);
        graph.getViewport().setMaxY(120);

        //set manual X bounds
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(40);
        graph.getViewport().setMaxX(120);
    }

}
