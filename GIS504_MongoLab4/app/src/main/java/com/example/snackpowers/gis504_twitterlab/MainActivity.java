package com.example.snackpowers.gis504_twitterlab;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ShareActionProvider;
import android.widget.TextView;
import android.widget.Toast;
import android.net.Uri;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MainActivity extends Activity implements
GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{
private ShareActionProvider mShareActionProvider;
    //define variables and tell it what scope they are.

    protected GoogleApiClient mGoogleApiClient;
    protected TextView mLatitudeText;
    protected TextView mLongitudeText;
    protected Location mLastLocation;
    protected String mShare;

    protected static final String TAG = "FINDING LOCATION, WAIT!";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLatitudeText = (TextView) findViewById((R.id.latitude_text));
        mLongitudeText = (TextView) findViewById((R.id.longitude_text));

        buildGoogleApiClient();}


    public void buttonOnClick(View v){
        //Do something when the button is clicked
        Button button=(Button)  v;
        ((Button) v).setText("clicked");
    }
    /*public void buttonOnClickDist(View v){
        Button Dist_button = (Button) v;
        ((Button) v).
    }*/
    protected synchronized void buildGoogleApiClient(){
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()){
            mGoogleApiClient.disconnect();
        }
    }

    public void onConnected(Bundle connectionHint){
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            mLatitudeText.setText(String.valueOf(mLastLocation.getLatitude()));
            mLongitudeText.setText(String.valueOf(mLastLocation.getLongitude()));
            mShare = "I'm at " + String.valueOf(mLastLocation.getLatitude()) + " degrees Latitude and " + String.valueOf(mLastLocation.getLongitude()) + " degrees Longtitude";

        }
        else{
            Toast.makeText(this, "No Location detected!", Toast.LENGTH_LONG).show();
           
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result){
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode()= " + result.getErrorCode());
    }
    @Override
    public void onConnectionSuspended(int cause){
        Log.i(TAG, "Connection Suspended");
        mGoogleApiClient.connect();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        //locate MenuItem with ShareActionProvider
        MenuItem item = menu.findItem(R.id.menu_item_share);

        //fetch and store ShareActionProvider
        mShareActionProvider = (ShareActionProvider) item.getActionProvider();
        mShareActionProvider.setShareIntent(createShareIntent());

        //Return true to display menu
        return true;
    }
    //Call to update the share intent
    private void setShareIntent(Intent shareIntent){
        if (mShareActionProvider !=null){
            mShareActionProvider.setShareIntent(shareIntent);

        }
        else {
            Toast.makeText(this, "cannot access Twitter!! Boo! Try again", Toast.LENGTH_LONG).show();
        }
    }
    //method for button that posts to Mongo. .execute() is a method that will run through doInBackground
    public void onPostClick(View view){
        PostLocation postlocation = new PostLocation();
        postlocation.execute();
        Toast.makeText(this, "Coordinates Submitted", Toast.LENGTH_SHORT).show();
    }


    //we are making PostLocation subclass of ASyncTask
    private class PostLocation extends AsyncTask<Void, Void, String>{
        protected String doInBackground(Void... voids){
            try{
                //connect to mongo! GO to mongodb online and find your unique address and port
                //not best practice because someone can decompile your code and steal your password...but best way to interact with mongo.
                MongoClientURI uri = new MongoClientURI("mongodb://kpowers:Drummer2@ds043971.mongolab.com:43971/location");
                MongoClient client = new MongoClient(uri);
                DB db = client.getDB(uri.getDatabase());

                //database collection is called MyLatLng
                DBCollection MyLatLng = db.getCollection("MyLatLng");

                SimpleDateFormat time = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
                String now = time.format(new Date());

                if (mLastLocation != null) {
                    BasicDBObject LastLocation = new BasicDBObject();
                    LastLocation.put("Latitude", String.valueOf(mLastLocation.getLatitude()));
                    LastLocation.put("Longitude", String.valueOf(mLastLocation.getLongitude()));
                    LastLocation.put("Time", String.valueOf(now));

                    MyLatLng.insert(LastLocation);
                    client.close();

                    return "Coordinates Submitted!";

                }else{
                    client.close();
                    return "No Location Detected, no location submitted. boo.";
                }}
                catch (UnknownHostException e){
                    return "Unknown Host Exception. boo. try again.";
                }
            }
        }

    private Intent createShareIntent(){
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(android.content.Intent.EXTRA_TEXT,mShare);
        return intent;
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
    public void onQueryClick(View view) {
        Intent intent = new Intent(this, QueryActivity.class);
        startActivity(intent);
    }
}
