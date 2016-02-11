package com.example.ashish.mymapproject;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class MapsActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener,LocationListener {

   // private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    FusedLocationProviderApi locationProviderApi= LocationServices.FusedLocationApi;
    private GoogleApiClient googleApiClient;
    int ZOOM_VALUE=12;
    EditText location;
    LocationRequest locationRequest;
    Marker marker;
    GoogleMap googleMap;
    Toolbar toolbar;
    Location mLocation;
    List<LocationInfo> data=new ArrayList<LocationInfo>();
    Intent intent;
    LocationInfo info;
    int n=1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(serviceOK()){

            setContentView(R.layout.activity_maps);

            if(initMap()){
                Intent intent=getIntent();
                info=intent.getParcelableExtra("mapData");
                Toast.makeText(this,"distance :"+info.getDistFromCurrent()+" Km",Toast.LENGTH_LONG).show();
                googleApiClient=new GoogleApiClient.Builder(this)
                        .addApi(LocationServices.API)
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .build();
                googleApiClient.connect();
                googleMap.setMyLocationEnabled(true);
                LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                Criteria criteria = new Criteria();
                String bestProvider = locationManager.getBestProvider(criteria, true);
                mLocation = locationManager.getLastKnownLocation(bestProvider);
                ParserTask parserTask=new ParserTask();
                parserTask.execute(info.getPoly());
                findDistance();
                if (mLocation != null) {
                    findDistance();
                }
                locationManager.requestLocationUpdates(bestProvider, 20000, 0, new android.location.LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        mLocation = location;

                        //Toast.makeText(getApplicationContext(), "changed" , Toast.LENGTH_LONG).show();

                        if (n != 0) {
                            findDistance();
                            n--;
                        }
                    }

                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {

                    }

                    @Override
                    public void onProviderEnabled(String provider) {

                    }

                    @Override
                    public void onProviderDisabled(String provider) {

                    }
                });


                locationRequest=LocationRequest.create();
                locationRequest.setInterval(5000);
                locationRequest.setFastestInterval(1000);
                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            }
            else{
                Toast.makeText(this,"map is not initialized!!",Toast.LENGTH_LONG).show();
            }
        }
        else {
            setContentView(R.layout.activity_main);
        }

        //setUpMapIfNeeded();
    }

    public void findDistance(){

        gotoCurrentLocation();
        gotoLocation(info.lat,info.lon,ZOOM_VALUE);
        //Polyline polylineToAdd = googleMap.addPolyline(new PolylineOptions().addAll(info.getPoly()).width(3).color(Color.RED));
        setMarker(info.getName(),info.getLat(),info.getLon());

    }
    private class ParserTask extends AsyncTask<List<List<HashMap<String,String>>>, Integer, List<List<HashMap<String,String>>> > {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(List<List<HashMap<String,String>>>... routss) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try{


                // Starts parsing data
                routes = routss[0];
            }catch(Exception e){
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {

            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;

            // Traversing through all the routes
            for(int i=0;i<result.size();i++){
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for(int j=0;j<path.size();j++){
                    HashMap<String,String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(7);
                lineOptions.color(Color.BLUE);
            }

            // Drawing polyline in the Google Map for the i-th route
            googleMap.addPolyline(lineOptions);
        }
    }




    private void gotoCurrentLocation() {
       // mLocation= LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        if(mLocation==null){
            //Toast.makeText(this, "cant find current location", Toast.LENGTH_LONG).show();
        }
        else {
            LatLng ll=new LatLng(mLocation.getLatitude(),mLocation.getLongitude());
            CameraUpdate update= CameraUpdateFactory.newLatLngZoom(ll, ZOOM_VALUE);
            googleMap.animateCamera(update);
            setMarker("your location",mLocation.getLatitude(),mLocation.getLongitude());
        }
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
    public boolean initMap(){
        if(googleMap==null){

            SupportMapFragment mapFragment=(SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            googleMap=mapFragment.getMap();
        }
        return (googleMap != null);
    }
    public void gotoLocation(double lat,double lon,int zoom){
        LatLng ll=new LatLng(lat,lon);
        CameraUpdate update= CameraUpdateFactory.newLatLngZoom(ll,zoom);
        googleMap.moveCamera(update);

    }

    private void hideSoftKeyboad(View view){
        InputMethodManager imm= (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    public void onConnected(Bundle bundle) {
        requestLocationUpdate();

    }

    private void requestLocationUpdate() {
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
       // Toast.makeText(this,"location changed :"+location.getLatitude() +" "+location.getLongitude(),Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        googleApiClient.disconnect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(googleApiClient.isConnected()){
            requestLocationUpdate();
           // setUpMapIfNeeded();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
    }
    public void setMarker(String locality,double lat,double lng){
      //  if(marker!=null){
         //   marker.remove();
       // }
        MarkerOptions options=new MarkerOptions()
                .title(locality)
                .position(new LatLng(lat,lng));
        marker=googleMap.addMarker(options);

    }
    public void setCurrentLocation(Location location){
        mLocation=location;
    }
    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #} is not null.
     */
   /* private void setUpMap() {
        mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
    }*/

}
