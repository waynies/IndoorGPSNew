package com.IndoorGPS;

import java.io.File;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;


public class SDCardActivity extends ListActivity {
    private ArrayAdapter<String> dataAdapter;
    private String selected;
    final ArrayList<String> data = new ArrayList<String>();
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        int i=0;
        String SDPATH =  Environment.getExternalStorageDirectory().getPath();
        String MAPDIRECTORYPATH = SDPATH+"/MAPS";
        File file = new File(MAPDIRECTORYPATH);
 		String[] COUNTRIES=file.list();
        while(i<COUNTRIES.length){
        	  	data.add(COUNTRIES[i]);
        	  	i++;
        }
        	findViewById(R.layout.item);
        dataAdapter = new ArrayAdapter<String>(this, R.layout.item,
            R.id.itemName, data);
        
            Timer outer = new Timer();
        outer.schedule(new TimerTask()
        {

            @Override
            public void run()
            {
                Log.i("DATA:", "Start");
                for (String item : data)
                {
                    Log.i("DATA: ", item.toString());
                }
                Log.i("DATA: ", "finish");
            }
        }, 5000, 5000);

        setListAdapter(dataAdapter);
        this.setTitle("Map List");
             

    }
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
     this.selected = Environment.getExternalStorageDirectory().getPath()+"/Maps/"+ this.data.get(position);
     finish();
      super.onListItemClick(l, v, position, id);
    }
  @Override
  public void finish(){
	  Intent data = new Intent();
	  if(selected !=null){
	  data.putExtra("returnKey1", selected);
	  setResult(RESULT_OK,data);
	  }
	  super.finish();
  }

}
