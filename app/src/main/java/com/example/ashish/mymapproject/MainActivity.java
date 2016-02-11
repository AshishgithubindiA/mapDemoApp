package com.example.ashish.mymapproject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;

import android.location.Location;
import android.location.LocationManager;

import android.provider.Settings;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.widget.Button;

import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.appdatasearch.GetRecentContextCall;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApiClient;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import android.os.Handler;


public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener {


    GoogleMap googleMap;
    Location mLocation;
    private boolean gps_enabled = false;
    private boolean network_enabled = false;
    LocationManager locationManager;

    Button find;
    int n=1;

    ProgressDialog progressDialog;
    String KEY="AIzaSyCz82lZJuALNidYK4G_FhsUUgV4-eBC4uM";
    String jsonString;
    List<LocationInfo> data=new ArrayList<LocationInfo>();




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(serviceOK()) {
            setContentView(R.layout.activity_main);
            find= (Button) findViewById(R.id.button2);

            jsonString = loadJSONFromAsset();

            try {
                prseJson();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setTitle("Processing...");
            progressDialog.setMessage("Please wait data is loading...");
            progressDialog.setCancelable(true);
            progressDialog.setCanceledOnTouchOutside(false);
            try {
                gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            } catch(Exception ex) {}

            try {
                network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            } catch(Exception ex) {}

            if(!gps_enabled && !network_enabled) {
                checkForLocationService(locationManager);
            }
            else {
                loadingDataIfGpsOn();
            }



        }
        else{
            Toast.makeText(getApplicationContext(), "service not available", Toast.LENGTH_LONG).show();
        }
        find.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {



                    Collections.sort(data,new ObjectComparator());
                    Intent intent=new Intent(MainActivity.this,ListActivity.class);
                    intent.putExtra("listData",(ArrayList<LocationInfo>)data);
                    startActivity(intent);


            }
        });
       // setDistanceData();

    }

    public void loadingDataIfGpsOn(){




        List<String> providers = locationManager.getProviders(true);

        Location l = null;

        for (int i = 0; i < providers.size(); i++) {
            l = locationManager.getLastKnownLocation(providers.get(i));
            if (l != null){
                mLocation=l;
                progressDialog.show();
                currentLocation();
                break;
            }

        }


    }

    public void checkForLocationService(LocationManager lm){



        if(!gps_enabled && !network_enabled) {
            // notify user
            final AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
            dialog.setMessage("gps network is not enabled");
            dialog.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    // TODO Auto-generated method stub
                     Intent myIntent = new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    MainActivity.this.startActivity(myIntent);
                    Toast.makeText(getApplicationContext(),"activet locationn and wait 2 sec tnen press back",Toast.LENGTH_LONG).show();
                    //get gps



                }
            });
            dialog.setNegativeButton("cancel", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    // TODO Auto-generated method stub

                }
            });
            dialog.show();
        }

    }

    private void prseJson() throws JSONException {
        JSONObject obj = new JSONObject(jsonString);
        JSONArray locations=obj.getJSONArray("locations");
        for(int i=0;i<locations.length();i++){
            JSONObject eachLocation=locations.getJSONObject(i);
            LocationInfo eachInfo=new LocationInfo();
            eachInfo.setName(eachLocation.getString("name"));
            eachInfo.setLat(eachLocation.getDouble("latitude"));
            eachInfo.setLon(eachLocation.getDouble("longitude"));
            data.add(eachInfo);
        }

    }
    public void currentLocation(){
        if(mLocation!=null){
            Toast.makeText(getApplicationContext(),"yes",Toast.LENGTH_LONG).show();
            setDistanceData();


        }
        else {
            Toast.makeText(getApplicationContext(),"no",Toast.LENGTH_LONG).show();
        }
    }

    public void setDistanceData(){
        double currentLat=mLocation.getLatitude();
        double currentLon=mLocation.getLongitude();
        for(int i=0;i<data.size();i++){
            LocationInfo info=data.get(i);
            double destinationLat=info.getLat();
            double destinationLon=info.getLon();
            directionApiRequest(currentLat, currentLon, destinationLat, destinationLon, i);

        }

    }


    private void directionApiRequest(double currentLat, double currentLon, double destinationLat, double destinationLon,final int i) {
        RequestQueue requestQueue=Volley.newRequestQueue(this);

        String directionRequestString=
                "https://maps.googleapis.com/maps/api/directions/json?" +
                        "origin="+currentLat+","+currentLon+"&destination="+destinationLat+","+destinationLon+"&key="+KEY;
        JsonObjectRequest request=new JsonObjectRequest(Request.Method.POST, directionRequestString, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                //Toast.makeText(getApplicationContext(),""+jsonObject,Toast.LENGTH_LONG).show();
                try {
                    JSONArray array = jsonObject.getJSONArray("routes");

                    JSONObject routes = array.getJSONObject(0);

                    JSONArray legs = routes.getJSONArray("legs");

                    JSONObject steps = legs.getJSONObject(0);

                    JSONObject distance = steps.getJSONObject("distance");

                    JSONArray stepsarray = steps.getJSONArray("steps");


                    Double dist=null;
                     dist= Double.parseDouble(distance.getString("text").replaceAll("[^\\.0123456789]", ""));
                    LocationInfo info=data.get(i);
                    if(info==null){
                        Toast.makeText(getApplicationContext(),"object not found"+dist,Toast.LENGTH_LONG).show();
                    }
                    info.setDistFromCurrent(dist);
                    List<List<HashMap<String,String>>> routess=parseForPoly(jsonObject);

                    info.setPoly(routess);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

            }
        });
        requestQueue.add(request);

    }
    public List<List<HashMap<String,String>>> parseForPoly(JSONObject jObject){

        List<List<HashMap<String, String>>> routes = new ArrayList<List<HashMap<String,String>>>();
        JSONArray jRoutes = null;
        JSONArray jLegs = null;
        JSONArray jSteps = null;

        try {

            jRoutes = jObject.getJSONArray("routes");

            /** Traversing all routes */
            for(int i=0;i<jRoutes.length();i++){
                jLegs = ( (JSONObject)jRoutes.get(i)).getJSONArray("legs");
                List path = new ArrayList<HashMap<String, String>>();

                /** Traversing all legs */
                for(int j=0;j<jLegs.length();j++){
                    jSteps = ( (JSONObject)jLegs.get(j)).getJSONArray("steps");

                    /** Traversing all steps */
                    for(int k=0;k<jSteps.length();k++){
                        String polyline = "";
                        polyline = (String)((JSONObject)((JSONObject)jSteps.get(k)).get("polyline")).get("points");
                        List<LatLng> list = decodePolyline(polyline);

                        /** Traversing all points */
                        for(int l=0;l<list.size();l++){
                            HashMap<String, String> hm = new HashMap<String, String>();
                            hm.put("lat", Double.toString(((LatLng)list.get(l)).latitude) );
                            hm.put("lng", Double.toString(((LatLng)list.get(l)).longitude) );
                            path.add(hm);
                        }
                    }
                    routes.add(path);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }catch (Exception e){
        }
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 100ms
                progressDialog.dismiss();
            }
        }, 6000);
        return routes;
    }
    private List<LatLng> decodePolyline(String encoded) {

        List<LatLng> poly = new ArrayList<LatLng>();

        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((double) lat / 1E5, (double) lng / 1E5);
            poly.add(p);
        }

        return poly;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    public String loadJSONFromAsset() {
        String json = null;
        try {

            InputStream is = getAssets().open("locationjson.json");

            int size = is.available();

            byte[] buffer = new byte[size];

            is.read(buffer);

            is.close();

            json = new String(buffer, "UTF-8");


        } catch (IOException ex) {
            ex.printStackTrace();
            //return null;
        }
        return json;

    }


    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }



    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        loadingDataIfGpsOn();
    }

    @Override
    protected void onStart() {
        super.onStart();

    }
    public boolean serviceOK(){
        int isAvailable= GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if(isAvailable== ConnectionResult.SUCCESS){
            return true;
        }
        else if (GooglePlayServicesUtil.isUserRecoverableError(isAvailable)){
            Dialog dialog=GooglePlayServicesUtil.getErrorDialog(isAvailable, this, 2);
            dialog.show();
        }
        else {
            Toast.makeText(this,"cant connect to google play servise",Toast.LENGTH_LONG).show();
        }
        return false;
    }


    public class ObjectComparator implements Comparator<LocationInfo> {


        @Override
        public int compare(LocationInfo lhs, LocationInfo rhs) {

            //if(lhs!=null){Toast.makeText(getApplicationContext(),"yo...",Toast.LENGTH_LONG).show();}
            //if(rhs!=null){Toast.makeText(getApplicationContext(),"yo yo...",Toast.LENGTH_LONG).show();}

            double one = lhs.getDistFromCurrent();
            double two=rhs.getDistFromCurrent();

            if (one > two) {
                return 1;
            }
            if (one < two) {
                return -1;
            } else {
                return 0;
            }

        }
    }
}
