package com.example.snackpowers.gis504_twitterlab;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

import java.net.UnknownHostException;


public class QueryActivity extends Activity {

    protected TextView latitude_query;
    protected TextView longitude_query;
    protected TextView time_query;
    protected TextView textView2;

    public String passlat;
    public String passlong;
    public String passtime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_query);

        QueryLocation querylocation = new QueryLocation();
        querylocation.execute();

        latitude_query = (TextView) findViewById((R.id.latitude_query));
        longitude_query = (TextView) findViewById((R.id.longitude_query));
        time_query = (TextView) findViewById((R.id.time_query));
        textView2 = (TextView) findViewById((R.id.textView2));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_query, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class QueryLocation extends AsyncTask<Void, Void, String> {
        @Override
                protected String doInBackground(Void... voids){

            try{
                MongoClientURI uri = new MongoClientURI("mongodb://kpowers:Drummer2@ds043971.mongolab.com:43971/location");
                MongoClient client = new MongoClient(uri);
                DB db = client.getDB(uri.getDatabase());

                DBCollection MyLatLng = db.getCollection("MyLatLng");

               //not a real cursor, but a DB object!
                DBObject cursor = MyLatLng.findOne();

                passlat = String.valueOf(cursor.get("Latitude"));
                passlong = String.valueOf(cursor.get("Longitude"));
                passtime = String.valueOf(cursor.get("Time"));
                client.close();

                return "Data Retrieved";
            } catch (UnknownHostException e){
                return "Unknown Host Exceptrion!";
            }
        }
        //onpostexecute will run every time asyncTask class is executed
        //but only when doInBackground has been completed.
        @Override
        protected void onPostExecute(String result){
            textView2.setText(result);
            latitude_query.setText(passlat);
            longitude_query.setText(passlong);
            time_query.setText(passtime);

        }
    }
}
