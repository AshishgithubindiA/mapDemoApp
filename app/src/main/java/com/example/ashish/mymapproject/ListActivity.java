package com.example.ashish.mymapproject;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


public class ListActivity extends AppCompatActivity implements AdapterView.OnItemClickListener{

    ListView listView;
    List<LocationInfo> data=new ArrayList<LocationInfo>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        listView= (ListView) findViewById(R.id.listView);
        Intent intent=getIntent();
        data=intent.getParcelableArrayListExtra("listData");
        if(data.get(0).getDistFromCurrent()!=null){
            Toast.makeText(getApplicationContext(), "distance : " + data.get(0).getDistFromCurrent(), Toast.LENGTH_LONG).show();
        }
        else{
            Toast.makeText(getApplicationContext(), "distance : NA", Toast.LENGTH_LONG).show();
        }

        MyAdapter myAdapter=new MyAdapter(this,data);
        listView.setAdapter(myAdapter);
        listView.setOnItemClickListener(this);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_list, menu);
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

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        LocationInfo info=data.get(position);
        Intent intent=new Intent(ListActivity.this,MapsActivity.class);
        intent.putExtra("mapData",info);
        startActivity(intent);
    }
}
class MyAdapter extends ArrayAdapter<LocationInfo>{

    List<LocationInfo> data=new ArrayList<LocationInfo>();
    MyAdapter(Context context,List<LocationInfo> data){
        super(context,R.layout.custom_row,data);
        this.data=data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater=LayoutInflater.from(this.getContext());
        View view=inflater.inflate(R.layout.custom_row, parent, false);
        TextView name= (TextView) view.findViewById(R.id.route_name);
        TextView distance= (TextView) view.findViewById(R.id.diastance);
        name.setText(data.get(position).getName());
        distance.setText(data.get(position).getDistFromCurrent()+"Km");
        return view;
    }
}