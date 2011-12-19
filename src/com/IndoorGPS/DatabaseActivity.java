/**
 * 
 */
package com.IndoorGPS;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class DatabaseActivity extends ListActivity {
    private static final String MSG_TAG = "DatabaseActivity";
	private static final int ACTIVITY_CREATE=0;
    private static final int ACTIVITY_EDIT=1;
    
    private static final int INSERT_ID = Menu.FIRST;
    private static final int DELETE_ID = Menu.FIRST + 1;
    private static final int DELETEALL_ID = Menu.FIRST + 2;
    private static final int DUMP_ID = Menu.FIRST + 3;
    
    /** table name showing up in this activity. */
    private static final String DATABASE_TABLE = "rssreading";
    
    private DBManager mDbHelper;
    private Cursor mNotesCursor;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rows_list);
        
        Log.v(MSG_TAG, "OnCreate start\n");
        mDbHelper = new DBManager(this);
        mDbHelper.open();
        
        fillData();
        registerForContextMenu(getListView());
        Log.v(MSG_TAG, "OnCreate exit\n");
    }

	public void onPause()
    {
		//mDbHelper.close();
    	super.onPause();
    }
	public void onResume()
    {
    	//mDbHelper.open();
    	super.onResume();
    }
	public void onStop()
    {
    	//mDbHelper.close();
    	super.onStop();
    }
	public void onDestroy()
    {
		//mDbHelper.close();
		//mNotesCursor.close();
    	super.onDestroy();
    }

    private void fillData()
    {
        // Get all of the rows from the database and create the item list
        mNotesCursor = mDbHelper.fetchAllRows(DATABASE_TABLE);
        
        startManagingCursor(mNotesCursor);
        
        // Create an array to specify the fields we want to display in the list
        String[] from = new String[]{DBManager.KEY_MAC_ADDR};
        
        // and an array of the fields we want to bind those fields to (in this case just text1)
        int[] to = new int[]{R.id.text1};
        
        // Now create a simple cursor adapter and set it to display
        SimpleCursorAdapter notes = 
        	    new SimpleCursorAdapter(this, R.layout.notes_row, mNotesCursor, from, to);
        setListAdapter(notes);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, INSERT_ID,0, R.string.menu_insert);
        menu.add(0, DELETEALL_ID,1, R.string.menu_delete);
        menu.add(0, DUMP_ID,1, R.string.menu_dump);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch(item.getItemId()) {
        case INSERT_ID:
            createNote();
            break;
        case DELETEALL_ID:
        	mDbHelper.deleteAll(DATABASE_TABLE);
        	fillData();
        	break;
        case DUMP_ID:
        	mDbHelper.dumpAll();
        	return true;
        }
              
        return super.onMenuItemSelected(featureId, item);
    }

    @Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add(0,DELETE_ID, 0, R.string.menu_delete);
        // TODO: fill in rest of method
	}

    @Override
	public boolean onContextItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case DELETE_ID:
			AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
			mDbHelper.deleteRow(info.id);
			fillData();
			return true;
		}    	
    	return super.onContextItemSelected(item);
		
        // TODO: fill in rest of method
	}

    private void createNote() {
        // TODO: fill in implementation
    	Intent i = new Intent(this, RowEdit.class);
    	startActivityForResult(i, ACTIVITY_CREATE);
    }
   
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        // TODO: fill in rest of method
        Cursor c = mNotesCursor;
        c.moveToPosition(position);
        Intent i = new Intent(this, RowEdit.class);
        i.putExtra(DBManager.KEY_ROWID, id);
       
        i.putExtra(DBManager.KEY_X_COORD, c.getString(c.getColumnIndexOrThrow(DBManager.KEY_X_COORD)));
        i.putExtra(DBManager.KEY_Y_COORD, c.getString(c.getColumnIndexOrThrow(DBManager.KEY_Y_COORD)));
        i.putExtra(DBManager.KEY_MAC_ADDR, c.getString(c.getColumnIndexOrThrow(DBManager.KEY_MAC_ADDR))); 
        i.putExtra(DBManager.KEY_AVG_RSS, c.getString(c.getColumnIndexOrThrow(DBManager.KEY_AVG_RSS)));
        i.putExtra(DBManager.KEY_VARIANCE, c.getString(c.getColumnIndexOrThrow(DBManager.KEY_VARIANCE)));
        i.putExtra(DBManager.KEY_SAMPLE_NUM, c.getString(c.getColumnIndexOrThrow(DBManager.KEY_SAMPLE_NUM)));
        
        startActivityForResult(i, ACTIVITY_EDIT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        
        // TODO: fill in rest of method
        fillData();
        /*
        Bundle extras = intent.getExtras();
        if(extras == null){
        	fillData();
        }else{
        	switch(requestCode){
        	case ACTIVITY_CREATE:
        	
        		String column1 = extras.getString(DBManager.KEY_X_COORD);
        		String column2 = extras.getString(DBManager.KEY_Y_COORD);
        		String column3 = extras.getString(DBManager.KEY_MAC_ADDR);
        		String column4 = extras.getString(DBManager.KEY_AVG_RSS);
        		String column5 = extras.getString(DBManager.KEY_VARIANCE);
        		String column6 = extras.getString(DBManager.KEY_SAMPLE_NUM);
        	        	
        		//mDbHelper.createNote(column1, column2, column3);
        		mDbHelper.insertRow(DATABASE_TABLE, column1, column2, column3, column4, column5, column6);
        		fillData();
        		break;
        	case ACTIVITY_EDIT:
        		Long mRowId = extras.getLong(DBManager.KEY_ROWID);
        		if(mRowId != null){
        	
        			String editColumn1 = extras.getString(DBManager.KEY_X_COORD);
        			String editColumn2 = extras.getString(DBManager.KEY_Y_COORD);
        			String editColumn3 = extras.getString(DBManager.KEY_MAC_ADDR);
        			String editColumn4 = extras.getString(DBManager.KEY_AVG_RSS);
        			String editColumn5 = extras.getString(DBManager.KEY_VARIANCE);
        			String editColumn6 = extras.getString(DBManager.KEY_SAMPLE_NUM);
        		         		
        			mDbHelper.updateRow(DATABASE_TABLE, mRowId, editColumn1, editColumn2, editColumn3, editColumn4, editColumn5, editColumn6);
        		}
        		fillData();
        		break;
        	}
        }*/
    }
}
