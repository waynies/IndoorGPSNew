package com.IndoorGPS;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

public class ChooseClusterInfo extends Activity{

	private RadioGroup orientGroup = null;
	private RadioButton orient1Button = null;
	private RadioButton orient2Button = null;
	private RadioButton orient3Button = null;
	private RadioButton orient4Button = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.orientation);
		
		orientGroup = (RadioGroup)findViewById(R.id.orientGroup);
		orient1Button = (RadioButton)findViewById(R.id.orient1Button);
		orient2Button = (RadioButton)findViewById(R.id.orient2Button);
		orient3Button = (RadioButton)findViewById(R.id.orient3Button);
		orient4Button = (RadioButton)findViewById(R.id.orient4Button);
		
		orientGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
		private String orientation = null;
		
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				// TODO Auto-generated method stub
				if(orient1Button.getId() == checkedId){
					orientation = "North";
					Toast.makeText(ChooseClusterInfo.this, "Orientation: North", Toast.LENGTH_SHORT).show();
					
					Intent intent = getIntent();
					intent.putExtra("orientation", orientation);
					setResult(RESULT_OK, intent);
					
					finish();
				}
				else if(orient2Button.getId() == checkedId){
					orientation = "South";
					Toast.makeText(ChooseClusterInfo.this, "Orientation: South", Toast.LENGTH_SHORT).show();
					
					Intent intent = getIntent();
					intent.putExtra("orientation", orientation);
					setResult(RESULT_OK, intent);
					
					finish();
				}
				else if(orient3Button.getId() == checkedId){
					orientation = "West";
					Toast.makeText(ChooseClusterInfo.this, "Orientation: West", Toast.LENGTH_SHORT).show();
					
					Intent intent = getIntent();
					intent.putExtra("orientation", orientation);
					setResult(RESULT_OK, intent);
					
					finish();
				}
				else if(orient4Button.getId() == checkedId){
					orientation = "East";
					Toast.makeText(ChooseClusterInfo.this, "Orientation: East", Toast.LENGTH_SHORT).show();
					
					Intent intent = getIntent();
					intent.putExtra("orientation", orientation);
					setResult(RESULT_OK, intent);
					
					finish();
				}
				
			}
		});
		
	}

}