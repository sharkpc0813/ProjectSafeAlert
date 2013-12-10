package project.safealert.database;

import java.util.ArrayList;
import java.util.List;

import project.safealert.Global.GlobalVar;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class MySQLiteMap extends SQLiteOpenHelper {	
	
	private final static String TABLE_PATH="pathinfo";
	
	public MySQLiteMap(Context context) {
		super(context, GlobalVar.DB_NAME, null, GlobalVar.DB_VERSION);
	}
	public MySQLiteMap(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) { 
		//Log.d("DB",">> MySQLite::onCreate()");
		db.execSQL(createTable());
		//Log.d("DB",">> DB Creation Complete");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
		//Log.d("DB",">> MySQLite::onUpgrade()");	
		//drop
		db.execSQL("DROP TABLE "+TABLE_PATH);
		onCreate(db);		
	}
	private String createTable(){
		return ("CREATE TABLE "+TABLE_PATH
				+"(Cnt INTEGER PRIMARY KEY,"
				+" Title TEXT(100),"
				+" Lat DOUBLE,"
				+" Long DOUBLE"
				+");"
				);
	}
	public long sql_insertData(PathInfo info){
		SQLiteDatabase db=this.getWritableDatabase();
		ContentValues val=new ContentValues();
		val.put("Cnt", info.getIndex());
		val.put("Title", info.getTitle());
		val.put("Lat", info.get_lat());
		val.put("Long", info.get_long());
		long rows=db.insert(TABLE_PATH, null, val);
		db.close();
		return rows;
	}
	public String[] sql_selectData(String title){
		//Log.d("DB","MySQLite::sql_selectData()");
		SQLiteDatabase db=this.getReadableDatabase();
		String[] _result=new String[4];
		

		Cursor cursor=db.query(TABLE_PATH,
						new String[] {"Cnt","Title","Lat","Long"},
						"Title='"+title+"'",
						null, null, null, null, null);

		//"MemberID = "+memberId
//		if(cursor==null)
//			Toast.makeText(this, , duration)
			//Log.d("DB","query finish");
	//	else
			//Log.d("DB","query error");
		
		cursor.moveToFirst();
			
		_result[0]=cursor.getString(0);
		_result[1]=cursor.getString(1);
		_result[2]=cursor.getString(2);
		_result[3]=cursor.getString(3);
		
		cursor.close();
		db.close();
		
		return _result;
	}
	public List<PathInfo> sql_selectAll(String title){
		//Log.d("DB","MySQLite::sql_selectAll()");
		List<PathInfo> mems=new ArrayList<PathInfo>();
		SQLiteDatabase db=this.getReadableDatabase();
		String _sql_select_all="SELECT * FROM "+ TABLE_PATH;
		Cursor cursor = db.rawQuery(_sql_select_all, null);
		if(cursor!=null){
			if(cursor.moveToFirst()){
				do{
				//	Log.d("DB","MySQLite::"+title+"?="+cursor.getString(1));
					if(title.compareTo(cursor.getString(1))==0){
					PathInfo member=new PathInfo(cursor.getInt(0),
												cursor.getString(1),
												cursor.getDouble(2),
												cursor.getDouble(3));
					mems.add(member);
					
					}
				}while(cursor.moveToNext());
			}
		}
		else{
		//	Log.d("DB","MySQLite::exception");
			//TODO exception
		}
		cursor.close();
		db.close();
		return mems;		
	}
	public List<String> sql_selectKeySet(){
		//Log.d("DB","MySQLite::sql_selectKeySet()");
		List<String> keyset=new ArrayList<String>();
		SQLiteDatabase db=this.getReadableDatabase();
		String _sql_select_all="SELECT DISTINCT Title FROM "+ TABLE_PATH;
		Cursor cursor = db.rawQuery(_sql_select_all, null);

		if(cursor!=null){
			if(cursor.moveToFirst()){
				do{
					keyset.add(cursor.getString(0));
				}while(cursor.moveToNext());
			}
		}
		else{
			//TODO exception
		}
		cursor.close();
		db.close();
		return keyset;		
	}
	public int sql_curCntPos(){
		//Log.d("DB","MySQLite::sql_tableSize()");
		int tablesize=0;
		SQLiteDatabase db=this.getReadableDatabase();
		String _sql_select_all="SELECT max(Cnt) FROM "+ TABLE_PATH;
		Cursor cursor = db.rawQuery(_sql_select_all, null);

		cursor.moveToFirst();
		tablesize=cursor.getInt(0);
		if(cursor.getInt(0)<0)
			tablesize=0;
		cursor.close();
		db.close();
		return tablesize;		
	}
	public long sql_updateData(PathInfo info){
		//Log.d("DB","MySQLite::sql_updateData()");
		long result=0;
		SQLiteDatabase db=this.getWritableDatabase();
		ContentValues val=new ContentValues();
		val.put("Cnt", info.getIndex());
		val.put("Title", info.getTitle());
		val.put("Lat", info.get_lat());
		val.put("Long", info.get_long());
		result=db.update(TABLE_PATH, val, "Cnt="+info.getIndex(), null);
		
		return result;
	}

	public long sql_deleteData(String title){
		//Log.d("DB","MySQLite::sql_deleteData()");
		long result=0;
		SQLiteDatabase db=this.getWritableDatabase();
		db.delete(TABLE_PATH, "Title='"+title+"'", null);
		return result;
	}
}
