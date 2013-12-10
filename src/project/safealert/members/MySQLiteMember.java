package project.safealert.members;

import java.util.ArrayList;
import java.util.List;

import project.safealert.Global.GlobalVar;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class MySQLiteMember extends SQLiteOpenHelper {	
	
	private final static String TABLE_PATH="protectmembers";
	
	public MySQLiteMember(Context context) {
		super(context, GlobalVar.DB_MEMBER_NAME, null, GlobalVar.DB_MEMBER_VERSION);
	}
	public MySQLiteMember(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) { 
		//Log.d("DB",">> MySQLiteMember::onCreate()");
		db.execSQL(createTable());
		//Log.d("DB",">> DB Creation Complete");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
		//Log.d("DB",">> MySQLiteMember::onUpgrade()");	
		//drop
		db.execSQL("DROP TABLE "+TABLE_PATH);
		onCreate(db);		
	}
	private String createTable(){
		return ("CREATE TABLE "+TABLE_PATH
				+"(Name TEXT(100) PRIMARY KEY,"
				+" Number TEXT(100)"
				+");"
				);
	}
	public long sql_insertData(Members member){
		SQLiteDatabase db=this.getWritableDatabase();
		ContentValues val=new ContentValues();	
		val.put("Name", member.getName());
		val.put("Number", member.getNumber());
		long rows=db.insert(TABLE_PATH, null, val);
		db.close();
		return rows;
	}
	public String[] sql_selectData(String name){
		//Log.d("DB","MySQLiteMember::sql_selectData()");
		SQLiteDatabase db=this.getReadableDatabase();
		String[] _result=new String[2];
		

		Cursor cursor=db.query(TABLE_PATH,
						new String[] {"Name","Number"},
						"Name='"+name+"'",
						null, null, null, null, null);

		//"MemberID = "+memberId
		if(cursor==null)
			return null;
		
		cursor.moveToFirst();
			
		_result[0]=cursor.getString(0);
		_result[1]=cursor.getString(1);
		cursor.close();
		db.close();
		
		return _result;
	}
	public List<String> sql_selectKeySet(){
		//Log.d("DB","MySQLiteMember::sql_selectKeySet()");
		List<String> keyset=new ArrayList<String>();
		SQLiteDatabase db=this.getReadableDatabase();
		String _sql_select_all="SELECT * FROM "+ TABLE_PATH;
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
	public List<Members> sql_selectAll(){
		//Log.d("DB","MySQLite::sql_selectAll()");
		List<Members> mems=new ArrayList<Members>();
		SQLiteDatabase db=this.getReadableDatabase();
		String _sql_select_all="SELECT * FROM "+ TABLE_PATH;
		Cursor cursor = db.rawQuery(_sql_select_all, null);
		if(cursor!=null){
			if(cursor.moveToFirst()){
				do{
					Members member=new Members(cursor.getString(0),cursor.getString(1));
					mems.add(member);
					//Log.d("DB","MySQLite::sql_selectAll()"+cursor.getString(0)+cursor.getString(1));
				}while(cursor.moveToNext());
			}
		}
		else{
			//TODO exception
		}
		cursor.close();
		db.close();
		return mems;		
	}
	public long sql_updateData(Members member){
		//Log.d("DB","MySQLiteMember::sql_updateData()");
		long result=0;
		SQLiteDatabase db=this.getWritableDatabase();
		ContentValues val=new ContentValues();	
		val.put("Name", member.getName());
		val.put("Number", member.getNumber());
		
		result=db.update(TABLE_PATH, val, "Name='"+member.getName()+"'", null);
		
		return result;
	}

	public long sql_deleteData(String name){
		//Log.d("DB","MySQLiteMember::sql_deleteData()");
		long result=0;
		SQLiteDatabase db=this.getWritableDatabase();
		db.delete(TABLE_PATH, "Name='"+name+"'", null);
		return result;
	}
}
