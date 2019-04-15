/* Joseph Morrill, Spencer Martin */
package com.josephmorrill.champshuttle;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class ShuttleSchedule extends AppCompatActivity {

    Button spinnerButton;
    Button weeknightButton;
    Button weekendButton;

    Context context;

    DBHelper myDBHelper;
    SQLiteDatabase myDatabase;
    ArrayList<TableRow> spinTimes;
    ArrayList<TableRow> wknTimes;
    ArrayList<TableRow> wkeTimes;

    TableLayout tableLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shuttle_schedule);

        context = getApplicationContext();


        Log.i("FileCopy", "About to copy files");
        copyFile(getString(R.string.schedule_lakeside));
        copyFile(getString(R.string.schedule_spinner));

        myDBHelper = new DBHelper(context, getString(R.string.schedule_database));
        myDatabase = myDBHelper.openDatabase();

        tableLayout = (TableLayout) findViewById(R.id.timeTable);
        spinnerButton = (Button) findViewById(R.id.spinnerButton);
        weeknightButton = (Button) findViewById(R.id.eveningButton);
        weekendButton = (Button) findViewById(R.id.weekendButton);

        spinTimes = myDBHelper.getAllSpinnerTimes();
        wknTimes = myDBHelper.getAllWNTimes();
        wkeTimes = myDBHelper.getAllWETimes();

        spinnerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadSpinner();
            }
        });

        weeknightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadWeekNight();
            }
        });

        weekendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadWeekend();
            }
        });
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if( myDatabase != null ) myDatabase.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_shuttle_schedule, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.menu_schedule_back)
        {
            onBackPressed();
            return true;
        }else if( id == android.R.id.home ){
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void loadSpinner() {

        Cursor res = myDatabase.rawQuery("select * from Spinner", null);

        tableLayout.removeAllViews();

        FrameLayout.LayoutParams relativeParams = new FrameLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
        TableLayout.LayoutParams tableParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
        TableRow.LayoutParams rowParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
        rowParams.weight = 1.0f;
        rowParams.gravity = Gravity.LEFT;
        tableLayout.setLayoutParams(relativeParams);


        //add header row
        TableRow header = new TableRow(context);
        header.setLayoutParams(tableParams);
        TextView tv1 = new TextView(context);
        tv1.setTextColor(Color.BLACK);
        tv1.setLayoutParams(rowParams);
        tv1.setText("Spinner");
        header.addView(tv1);
        TextView tv2 = new TextView(context);
        tv2.setTextColor(Color.BLACK);
        tv2.setLayoutParams(rowParams);
        tv2.setText("Champlain");
        header.addView(tv2);
        tableLayout.addView(header);


        res.moveToFirst();
        while (res.isAfterLast() == false) {
            TableRow tr = new TableRow(context);
            tr.setLayoutParams(tableParams);
            TextView tv = new TextView(context);
            tv.setTextColor(Color.BLACK);
            tv.setLayoutParams(rowParams);
            String time = res.getString(res.getColumnIndex("Spinner"));
            tv.setText(time);
            tr.addView(tv);
            tv = new TextView(context);
            tv.setTextColor(Color.BLACK);
            tv.setLayoutParams(rowParams);
            time = res.getString(res.getColumnIndex("Champlain"));
            tv.setText(time);
            tr.addView(tv);
            tableLayout.addView(tr);

            res.moveToNext();
        }

    }

    public void loadWeekNight() {
        Cursor res = myDatabase.rawQuery("select * from WeekNight", null);

        tableLayout.removeAllViews();

        FrameLayout.LayoutParams relativeParams = new FrameLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
        TableLayout.LayoutParams tableParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
        TableRow.LayoutParams rowParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
        rowParams.weight = 1.0f;
        rowParams.gravity = Gravity.LEFT;
        tableLayout.setLayoutParams(relativeParams);


        //add header row
        TableRow header = new TableRow(context);
        header.setLayoutParams(tableParams);

        TextView tv1 = new TextView(context);
        tv1.setTextColor(Color.BLACK);
        tv1.setTextSize((float) 9.0);
        tv1.setLayoutParams(rowParams);
        tv1.setText("Champlain");
        header.addView(tv1);

        TextView tv2 = new TextView(context);
        tv2.setTextColor(Color.BLACK);
        tv2.setTextSize((float) 9.0);
        tv2.setLayoutParams(rowParams);
        tv2.setText("Spinner");
        header.addView(tv2);

        tv1 = new TextView(context);
        tv1.setTextColor(Color.BLACK);
        tv1.setTextSize((float) 9.0);
        tv1.setLayoutParams(rowParams);
        tv1.setText("Champlain");
        header.addView(tv1);

        tv1 = new TextView(context);
        tv1.setTextColor(Color.BLACK);
        tv1.setTextSize((float) 9.0);
        tv1.setLayoutParams(rowParams);
        tv1.setText("Lakeside");
        header.addView(tv1);

        tv1 = new TextView(context);
        tv1.setTextColor(Color.BLACK);
        tv1.setTextSize((float) 9.0);
        tv1.setLayoutParams(rowParams);
        tv1.setText("Champlain");
        header.addView(tv1);

        tv1 = new TextView(context);
        tv1.setTextColor(Color.BLACK);
        tv1.setTextSize((float) 9.0);
        tv1.setLayoutParams(rowParams);
        tv1.setText("Spinner");
        header.addView(tv1);

        tableLayout.addView(header);


        res.moveToFirst();
        while (res.isAfterLast() == false) {
            TableRow tr = new TableRow(context);
            tr.setLayoutParams(tableParams);

            TextView tv = new TextView(context);
            tv.setTextColor(Color.BLACK);
            tv.setTextSize((float) 10.0);
            tv.setLayoutParams(rowParams);
            String time = res.getString(res.getColumnIndex("Champlain1"));
            tv.setText(time);
            tr.addView(tv);

            tv = new TextView(context);
            tv.setTextColor(Color.BLACK);
            tv.setTextSize((float) 10.0);
            tv.setLayoutParams(rowParams);
            time = res.getString(res.getColumnIndex("Spinner1"));
            tv.setText(time);
            tr.addView(tv);

            tv = new TextView(context);
            tv.setTextColor(Color.BLACK);
            tv.setTextSize((float) 10.0);
            tv.setLayoutParams(rowParams);
            time = res.getString(res.getColumnIndex("Champlain2"));
            tv.setText(time);
            tr.addView(tv);

            tv = new TextView(context);
            tv.setTextColor(Color.BLACK);
            tv.setTextSize((float) 10.0);
            tv.setLayoutParams(rowParams);
            time = res.getString(res.getColumnIndex("Lakeside"));
            tv.setText(time);
            tr.addView(tv);

            tv = new TextView(context);
            tv.setTextColor(Color.BLACK);
            tv.setTextSize((float) 10.0);
            tv.setLayoutParams(rowParams);
            time = res.getString(res.getColumnIndex("Champlain3"));
            tv.setText(time);
            tr.addView(tv);

            tv = new TextView(context);
            tv.setTextColor(Color.BLACK);
            tv.setTextSize((float) 10.0);
            tv.setLayoutParams(rowParams);
            time = res.getString(res.getColumnIndex("Spinner2"));
            tv.setText(time);
            tr.addView(tv);


            tableLayout.addView(tr);

            res.moveToNext();
        }
    }

    public void loadWeekend() {
        Cursor res = myDatabase.rawQuery("select * from Weekend", null);

        tableLayout.removeAllViews();

        FrameLayout.LayoutParams relativeParams = new FrameLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
        TableLayout.LayoutParams tableParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
        TableRow.LayoutParams rowParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
        rowParams.weight = 1.0f;
        rowParams.gravity = Gravity.LEFT;
        tableLayout.setLayoutParams(relativeParams);


        //add header row
        TableRow header = new TableRow(context);
        header.setLayoutParams(tableParams);

        TextView tv1 = new TextView(context);
        tv1.setTextColor(Color.BLACK);
        tv1.setTextSize((float) 9.0);
        tv1.setLayoutParams(rowParams);
        tv1.setText("Spinner");
        header.addView(tv1);

        tv1 = new TextView(context);
        tv1.setTextColor(Color.BLACK);
        tv1.setTextSize((float) 9.0);
        tv1.setLayoutParams(rowParams);
        tv1.setText("Champlain");
        header.addView(tv1);

        tv1 = new TextView(context);
        tv1.setTextColor(Color.BLACK);
        tv1.setTextSize((float) 9.0);
        tv1.setLayoutParams(rowParams);
        tv1.setText("Lakeside");
        header.addView(tv1);

        tv1 = new TextView(context);
        tv1.setTextColor(Color.BLACK);
        tv1.setTextSize((float) 9.0);
        tv1.setLayoutParams(rowParams);
        tv1.setText("Champlain");
        header.addView(tv1);

        tableLayout.addView(header);


        res.moveToFirst();
        while (res.isAfterLast() == false) {
            TableRow tr = new TableRow(context);
            tr.setLayoutParams(tableParams);

            TextView tv = new TextView(context);
            tv.setTextColor(Color.BLACK);
            tv.setLayoutParams(rowParams);
            String time = res.getString(res.getColumnIndex("Spinner"));
            tv.setText(time);
            tr.addView(tv);

            tv = new TextView(context);
            tv.setTextColor(Color.BLACK);
            tv.setLayoutParams(rowParams);
            time = res.getString(res.getColumnIndex("Champlain1"));
            tv.setText(time);
            tr.addView(tv);

            tv = new TextView(context);
            tv.setTextColor(Color.BLACK);
            tv.setLayoutParams(rowParams);
            time = res.getString(res.getColumnIndex("Lakeside"));
            tv.setText(time);
            tr.addView(tv);

            tv = new TextView(context);
            tv.setTextColor(Color.BLACK);
            tv.setLayoutParams(rowParams);
            time = res.getString(res.getColumnIndex("Champlain2"));
            tv.setText(time);
            tr.addView(tv);


            tableLayout.addView(tr);

            res.moveToNext();
        }

    }

    public void copyFile(String fileName) {
        if (!new File(fileName).exists()) {
            try {
                InputStream localInputStream = getAssets().open(fileName);
                FileOutputStream localFileOutputStream = getBaseContext().openFileOutput(fileName, MODE_PRIVATE);

                byte[] arrayOfByte = new byte[1024];
                int offset;
                while ((offset = localInputStream.read(arrayOfByte)) > 0) {
                    localFileOutputStream.write(arrayOfByte, 0, offset);
                }
                localFileOutputStream.close();
                localInputStream.close();
            } catch (IOException localIOException) {
                localIOException.printStackTrace();
                Log.i("FileCopy", "Hit exception");
                return;
            }

            Log.i("FileCopy", "Copied " + fileName + " to local dir " + getBaseContext().getFilesDir().getAbsolutePath());

        } else
            Log.i("FileCopy", "No need to copy file");
    }
}
