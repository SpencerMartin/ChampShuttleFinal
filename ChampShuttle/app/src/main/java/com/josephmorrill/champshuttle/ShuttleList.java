/* Joseph Morrill, Spencer Martin */
package com.josephmorrill.champshuttle;

import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class ShuttleList extends ListActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shuttle_list);

        refresh();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_shuttle_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.menu_list_refresh) {
            refresh();
            return true;
        }else if( id == R.id.menu_list_back ){
            onBackPressed();
            return true;
        }else if( id == android.R.id.home ){
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class ShuttleListAdapter extends ArrayAdapter<Shuttle>
    {
        private ArrayList<Shuttle> items;

        public ShuttleListAdapter(Context context, int textViewResourceId, ArrayList<Shuttle> items) {
            super(context, textViewResourceId, items);
            this.items = items;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.listviewitem_shuttle, null);
            }
            Shuttle r = items.get(position);
            if (r != null) {
                if (position % 2 == 0){
                    v.setBackgroundResource(R.drawable.alterselector_1);
                } else {
                    v.setBackgroundResource(R.drawable.alterselector_2);
                }
                ((TextView)v.findViewById(R.id.listitem_name_lbl)).setText(r.name + " (" + r.id + ")");
                ((TextView)v.findViewById(R.id.listitem_location_lbl)).setText(r.latitude + ", " + r.longitude);
                ((TextView) v.findViewById(R.id.listitem_movement_lbl)).setText(r.direction + "deg   " + r.mph + "mph");
                ((TextView) v.findViewById(R.id.listitem_updated_lbl)).setText(r.last_update.toString());
            }
            return v;
        }
    }

    public void refresh(){
        ShuttleListAdapter adapter = new ShuttleListAdapter( getApplicationContext(), R.layout.listviewitem_shuttle, Main.shuttles );
        setListAdapter(adapter);
    }
}
