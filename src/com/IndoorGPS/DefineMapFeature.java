package com.IndoorGPS;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

public class DefineMapFeature extends Activity{

	private RadioGroup mapfeatureGroup = null;
	private RadioButton LMButton = null;
	private RadioButton DESTButton = null;
	private RadioButton TURNButton = null;
	private RadioButton DOORButton = null;
	private RadioButton WALLButton = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.definemapfeature);
		
		mapfeatureGroup = (RadioGroup)findViewById(R.id.mapfeatureGroup);
		LMButton = (RadioButton)findViewById(R.id.LMButton);
		DESTButton = (RadioButton)findViewById(R.id.DESTButton);
		TURNButton = (RadioButton)findViewById(R.id.TURNButton);
		DOORButton = (RadioButton)findViewById(R.id.DOORButton);
		WALLButton = (RadioButton)findViewById(R.id.WALLButton);
		
		mapfeatureGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
		private int mapfeatureType = 0;
		
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				// TODO Auto-generated method stub
				if(LMButton.getId() == checkedId){
					mapfeatureType = 0; //"_NODE_";
					Toast.makeText(DefineMapFeature.this, "Map Feature Type: _Node_", Toast.LENGTH_SHORT).show();
					
					Intent intent = getIntent();
					intent.putExtra("mapfeatureType", mapfeatureType);
					setResult(RESULT_OK, intent);
					
					finish();
				}
				else if(DESTButton.getId() == checkedId){
					mapfeatureType = 1; //"DESCRIP";
					Toast.makeText(DefineMapFeature.this, "Map Feature Type: _DESTINATION_", Toast.LENGTH_SHORT).show();
					
					Intent intent = getIntent();
					intent.putExtra("mapfeatureType", mapfeatureType);
					setResult(RESULT_OK, intent);
					
					finish();
				}
				
				else if(TURNButton.getId() == checkedId){
					mapfeatureType = 2; //"TURN";
					Toast.makeText(DefineMapFeature.this, "Map Feature Type: _TURN_", Toast.LENGTH_SHORT).show();
					
					Intent intent = getIntent();
					intent.putExtra("mapfeatureType", mapfeatureType);
					setResult(RESULT_OK, intent);
					
					finish();
				}
				
				else if(DOORButton.getId() == checkedId){
					mapfeatureType = 3; //"DOOR";
					Toast.makeText(DefineMapFeature.this, "Map Feature Type: _DOOR_", Toast.LENGTH_SHORT).show();
					
					Intent intent = getIntent();
					intent.putExtra("mapfeatureType", mapfeatureType);
					setResult(RESULT_OK, intent);
					
					finish();
				}
				else if(WALLButton.getId() == checkedId){
					mapfeatureType = 4; //"WALL";
					Toast.makeText(DefineMapFeature.this, "Map Feature Type: _WALL_", Toast.LENGTH_SHORT).show();
					
					Intent intent = getIntent();
					intent.putExtra("mapfeatureType", mapfeatureType);
					setResult(RESULT_OK, intent);
					
					finish();
				}
				
			}
		});
		
	}
}
