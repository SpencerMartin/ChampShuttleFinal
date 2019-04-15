/* Joseph Morrill, Spencer Martin */
package com.josephmorrill.champshuttle;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.TableRow;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;


/**
 * Created by Joseph on 12/13/2015.
 */

// Built using some code from http://stackoverflow.com/questions/5051889/android-pre-filled-db
public class DBHelper extends SQLiteOpenHelper {

    //The Android's default system path of your application database.
    String TARGET_PATH = null;

    private static String DB_NAME;

    public SQLiteDatabase myDatabase;

    private final Context myContext;

    /**
     * Constructor
     * Takes and keeps a reference of the passed context in order to access to the application assets and resources.
     * @param context
     */
    public DBHelper(Context context, String databaseName) {
        super(context, databaseName, null, 1);
        this.myContext = context;
        DB_NAME = databaseName;
        if(android.os.Build.VERSION.SDK_INT >= 4.2){
            TARGET_PATH = context.getApplicationInfo().dataDir + "/databases/" + DB_NAME;
        } else {
            TARGET_PATH = "/data/data/" + context.getPackageName() + "/databases/" + DB_NAME;
        }
        Log.i( "DBHelper()", TARGET_PATH );
        if( !checkDatabaseExists() ){
            // Copy from assets
            try{
                copyDatabase();
            }catch(IOException e){
                Log.e( "DBHelper()", e.toString() );
            }

        }
    }

    /**
     * Check if the database already exist to avoid re-copying the file each time you open the application.
     * @return true if it exists, false if it doesn't
     */
    private boolean checkDatabaseExists(){
        File databaseFile = new File( TARGET_PATH );
        return databaseFile.exists();
    }

    /**
     * Copies your database from your local assets-folder to the just created empty database in the
     * system folder, from where it can be accessed and handled.
     * This is done by transfering bytestream.
     * */
    private void copyDatabase() throws IOException{
        Log.i("DBHelper", "Copying database...");

        // Close open handles
        if( myDatabase != null ) myDatabase.close();

        // Create directories if they don't exist
        File databaseFile = new File( TARGET_PATH );
        if( !databaseFile.exists() ){
            databaseFile.getParentFile().mkdirs();

            //Open your local db as the input stream
            InputStream assetDatabaseFile = myContext.getAssets().open(DB_NAME);

            //Open the empty db as the output stream
            OutputStream localDatabaseFile = new FileOutputStream(TARGET_PATH);

            //transfer bytes from the inputfile to the outputfile
            byte[] buffer = new byte[1024];
            int length;
            while ((length = assetDatabaseFile.read(buffer))>0){
                localDatabaseFile.write(buffer, 0, length);
            }

            //Close the streams
            localDatabaseFile.flush();
            localDatabaseFile.close();
            assetDatabaseFile.close();
        }
    }

    public SQLiteDatabase openDatabase() throws SQLException {
        //Open the database
        if( myDatabase == null ){
            myDatabase = SQLiteDatabase.openDatabase(TARGET_PATH, null, SQLiteDatabase.OPEN_READONLY);
        }
        return myDatabase;
    }

    @Override
    public synchronized void close() {
        if(myDatabase != null)
            myDatabase.close();
        super.close();
    }



    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
    //return cursor
    public Cursor query(String table,String[] columns, String selection,String[] selectionArgs,String groupBy,String having,String orderBy){
        return myDatabase.query(table, columns, selection, selectionArgs, groupBy, having, orderBy);


    }
    public void debugGetTimes(){
        SQLiteDatabase db;
        db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from Spinner", null);

        res.moveToFirst();
        while (res.isAfterLast() == false) {
            String spin = res.getString(res.getColumnIndex("Spinner"));
            String champ = res.getString(res.getColumnIndex("Champlain"));
            Log.i( "Spinner", spin);
            res.moveToNext();
        }
    }
    public ArrayList<TableRow> getAllSpinnerTimes() {
        ArrayList<TableRow> mList = new ArrayList<TableRow>();
        SQLiteDatabase db;
        db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from Spinner", null);
        TableRow header = new TableRow(myContext);
        TextView col1 = new TextView(myContext);
        TextView col2 = new TextView(myContext);

        col1.setText("Spinner");
        col2.setText("Champlain");

        header.addView(col1);
        header.addView(col2);

        mList.add(header);

        res.moveToFirst();
        while (res.isAfterLast() == false) {
            String spin = res.getString(res.getColumnIndex("Spinner"));
            String champ = res.getString(res.getColumnIndex("Champlain"));

            TextView sTV = new TextView(myContext);
            TextView cTV = new TextView(myContext);
            sTV.setText(spin.toString());
            cTV.setText(champ.toString());

            TableRow tr = new TableRow(myContext);
            tr.addView(sTV);
            tr.addView(cTV);

            mList.add(tr);
            res.moveToNext();
        }

        return mList;
    }

    public ArrayList<TableRow> getAllWNTimes() {
        ArrayList<TableRow> mList = new ArrayList<TableRow>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from WeekNight", null);
        TableRow header = new TableRow(myContext);
        TextView col1 = new TextView(myContext);
        TextView col2 = new TextView(myContext);
        TextView col3 = new TextView(myContext);
        TextView col4 = new TextView(myContext);
        TextView col5 = new TextView(myContext);
        TextView col6 = new TextView(myContext);

        col1.setText("Champlain");
        col2.setText("Spinner");
        col3.setText("Champlain");
        col4.setText("Lakeside");
        col5.setText("Champlain");
        col6.setText("Spinner");

        header.addView(col1);
        header.addView(col2);
        header.addView(col3);
        header.addView(col4);
        header.addView(col5);
        header.addView(col6);

        mList.add(header);

        res.moveToFirst();
        while (res.isAfterLast() == false) {
            String champ1 = res.getString(res.getColumnIndex("Champlain1"));
            String spin1 = res.getString(res.getColumnIndex("Spinner1"));
            String champ2 = res.getString(res.getColumnIndex("Champlain2"));
            String lake = res.getString(res.getColumnIndex("Lakeside"));
            String champ3 = res.getString(res.getColumnIndex("Champlain3"));
            String spin2 = res.getString(res.getColumnIndex("Spinner1"));

            TextView c1TV = new TextView(myContext);
            TextView s1TV = new TextView(myContext);
            TextView c2TV = new TextView(myContext);
            TextView lTV = new TextView(myContext);
            TextView c3TV = new TextView(myContext);
            TextView s2TV = new TextView(myContext);

            c1TV.setText(champ1.toString());
            s1TV.setText(spin1.toString());
            c2TV.setText(champ2.toString());
            lTV.setText(lake.toString());
            c3TV.setText(champ3.toString());
            s2TV.setText(spin2.toString());

            TableRow tr = new TableRow(myContext);
            tr.addView(c1TV);
            tr.addView(s1TV);
            tr.addView(c2TV);
            tr.addView(lTV);
            tr.addView(c3TV);
            tr.addView(s2TV);

            mList.add(tr);
            res.moveToNext();
        }
        return mList;
    }

    public ArrayList<TableRow> getAllWETimes() {
        ArrayList<TableRow> mList = new ArrayList<TableRow>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from Weekend", null);
        TableRow header = new TableRow(myContext);
        TextView col1 = new TextView(myContext);
        TextView col2 = new TextView(myContext);
        TextView col3 = new TextView(myContext);
        TextView col4 = new TextView(myContext);

        col1.setText("Spinner");
        col2.setText("Champlain");
        col3.setText("Lakeside");
        col4.setText("Champlain");

        header.addView(col1);
        header.addView(col2);
        header.addView(col3);
        header.addView(col4);

        mList.add(header);

        res.moveToFirst();
        while (res.isAfterLast() == false) {
            String spin = res.getString(res.getColumnIndex("Spinner"));
            String champ1 = res.getString(res.getColumnIndex("Champlain1"));
            String lake = res.getString(res.getColumnIndex("Lakeside"));
            String champ2 = res.getString(res.getColumnIndex("Champlain2"));

            TextView sTV = new TextView(myContext);
            TextView c1TV = new TextView(myContext);
            TextView lTV = new TextView(myContext);
            TextView c2TV = new TextView(myContext);

            sTV.setText(spin.toString());
            c1TV.setText(champ1.toString());
            lTV.setText(lake.toString());
            c2TV.setText(champ2.toString());

            TableRow tr = new TableRow(myContext);
            tr.addView(sTV);
            tr.addView(c1TV);
            tr.addView(lTV);
            tr.addView(c2TV);

            mList.add(tr);
            res.moveToNext();
        }
        return mList;
    }

}