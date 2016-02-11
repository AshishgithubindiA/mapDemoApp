package com.example.ashish.mymapproject;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.android.gms.maps.model.LatLng;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Ashish on 1/15/2016.
 */
public class LocationInfo implements Parcelable {
    String name;
    double lat;
    double lon;
    Double distFromCurrent;
    List<List<HashMap<String,String>>> poly=new ArrayList<List<HashMap<String,String>>>();

    public LocationInfo(){

    }

    public LocationInfo(Parcel parcel){
        name=parcel.readString();
        lat=parcel.readDouble();
        lon=parcel.readDouble();
        distFromCurrent=parcel.readDouble();
        poly=parcel.readArrayList(LatLng.class.getClassLoader());
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public void setDistFromCurrent(Double distFromCurrent) {
        this.distFromCurrent = distFromCurrent;
    }
    public void setPoly(List<List<HashMap<String,String>>> poly){
        this.poly=poly;
    }

    public String getName(){
        return name;
    }
    public double getLat(){
        return lat;
    }
    public double getLon(){
        return lon;
    }

    public List<List<HashMap<String,String>>> getPoly() {
        return poly;
    }

    public Double getDistFromCurrent() {
        return distFromCurrent;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeDouble(lat);
        dest.writeDouble(lon);
        dest.writeDouble(distFromCurrent);
        dest.writeList(poly);
    }
    public static final Parcelable.Creator<LocationInfo> CREATOR
            = new Parcelable.Creator<LocationInfo>() {
        public LocationInfo createFromParcel(Parcel in) {
            return new LocationInfo(in);
        }

        public LocationInfo[] newArray(int size) {
            return new LocationInfo[size];
        }
    };
}
