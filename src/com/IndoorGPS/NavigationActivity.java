package com.IndoorGPS;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

public class NavigationActivity extends Activity{

	private RadioGroup destGroup = null;
	private RadioButton dest1Button = null;
	private RadioButton dest2Button = null;
	private RadioButton dest3Button = null;
	private RadioButton dest4Button = null;
	private RadioButton dest5Button = null;
	private RadioButton dest6Button = null;
	
	private String userDestination = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.navi);
		
		destGroup = (RadioGroup)findViewById(R.id.destGroup);
		dest1Button = (RadioButton)findViewById(R.id.dest1Button);
		dest2Button = (RadioButton)findViewById(R.id.dest2Button);
		dest3Button = (RadioButton)findViewById(R.id.dest3Button);
		dest4Button = (RadioButton)findViewById(R.id.dest4Button);
		dest5Button = (RadioButton)findViewById(R.id.dest5Button);
		dest6Button = (RadioButton)findViewById(R.id.dest6Button);
		
		destGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
		private String userDestination = null;
		
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				// TODO Auto-generated method stub
				if(dest1Button.getId() == checkedId){
					userDestination = "218C";
					Toast.makeText(NavigationActivity.this, "Destination: 218C", Toast.LENGTH_LONG).show();
					
					Intent intent = getIntent();
					intent.putExtra("Destination", userDestination);
					setResult(RESULT_OK, intent);
					
					finish();
				}
				else if(dest2Button.getId() == checkedId)
				{
					userDestination = "StorageRoom";
					Toast.makeText(NavigationActivity.this, "Destination: Storage Room", Toast.LENGTH_LONG).show();
					Intent intent = getIntent();
					intent.putExtra("Destination", userDestination);
					setResult(RESULT_OK, intent);
					
					finish();
				}
				else if(dest3Button.getId() == checkedId)
				{
					userDestination = "Fountain";
					Toast.makeText(NavigationActivity.this, "Destination: Fountain", Toast.LENGTH_LONG).show();
					Intent intent = getIntent();
					intent.putExtra("Destination", userDestination);
					setResult(RESULT_OK, intent);
					
					finish();
				}
				else if(dest4Button.getId() == checkedId)
				{
					userDestination = "Elevator";
					Toast.makeText(NavigationActivity.this, "Destination: Elevator", Toast.LENGTH_LONG).show();
					Intent intent = getIntent();
					intent.putExtra("Destination", userDestination);
					setResult(RESULT_OK, intent);
					
					finish();
				}
				else if(dest5Button.getId() == checkedId)
				{
					userDestination = "Men's Washroom";
					Toast.makeText(NavigationActivity.this, "Destination: Men's Washroom", Toast.LENGTH_LONG).show();
					Intent intent = getIntent();
					intent.putExtra("Destination", userDestination);
					setResult(RESULT_OK, intent);
					
					finish();
				}
				else if(dest6Button.getId() == checkedId)
				{
					userDestination = "Women's Washroom";
					Toast.makeText(NavigationActivity.this, "Destination: Women's Washroom", Toast.LENGTH_LONG).show();
					Intent intent = getIntent();
					intent.putExtra("Destination", userDestination);
					setResult(RESULT_OK, intent);
					
					finish();
				}
				
			}
		});
		
		
		

	}
	

}
