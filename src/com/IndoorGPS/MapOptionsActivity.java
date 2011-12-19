package com.IndoorGPS;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

public class MapOptionsActivity extends Activity{
	private RadioGroup mapGroup = null;
	private RadioButton Button1LoadMap = null;
	private RadioButton Button2CameraMap = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mapoptions);
		
		mapGroup = (RadioGroup)findViewById(R.id.mapGroup);
		Button1LoadMap = (RadioButton)findViewById(R.id.Button1LoadMap);
		Button2CameraMap = (RadioButton)findViewById(R.id.Button2CameraMap);
		
		mapGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			private String mapOptionType = null;
			
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				// TODO Auto-generated method stub
				if(Button1LoadMap.getId() == checkedId){
					Toast.makeText(MapOptionsActivity.this, "Load from Memory", Toast.LENGTH_LONG).show();
					mapOptionType = "loadmap";
					
					Intent intent = getIntent();
					intent.putExtra("mapOptionType", mapOptionType);
					setResult(RESULT_OK, intent);

					finish();
				}
				else if(Button2CameraMap.getId() == checkedId){
					Toast.makeText(MapOptionsActivity.this, "Load from Memory", Toast.LENGTH_LONG).show();
					mapOptionType = "cameramap";
					
					Intent intent = getIntent();
					intent.putExtra("mapOptionType", mapOptionType);
					setResult(RESULT_OK, intent);

					finish();
				}
			}
			
		});
	}
}
