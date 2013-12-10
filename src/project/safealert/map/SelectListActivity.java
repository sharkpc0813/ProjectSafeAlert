package project.safealert.map;

import java.util.ArrayList;
import java.util.List;

import project.safealert.R;
import project.safealert.Global.GlobalVar;
import project.safealert.database.MySQLiteMap;
import project.safealert.map.MapActivity;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class SelectListActivity extends Activity{
	private MySQLiteMap msql;

	private ListView _lv_path;
	private TextView _tv_addlist;

	private ArrayList<String> arrList;
	private ArrayAdapter<String> adapter;
	private int remove=0;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dlg_map_list);
		_lv_path=(ListView) findViewById(R.id.lv_path_map);
		_tv_addlist=(TextView) findViewById(R.id.tv_addlist_map);

		arrList=new ArrayList<String>();
		adapter=new ArrayAdapter<String>(this,R.layout.listview_text,arrList);
		
		_lv_path.setAdapter(adapter);
		_lv_path.setTextFilterEnabled(true);

		msql=new MySQLiteMap(this);
		
		initEvents();
	}
	@Override
	protected void onResume() {
		updateFromDB();
		super.onResume();
	}
	public void initEvents(){
		_tv_addlist.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent intent=new Intent(SelectListActivity.this,MapActivity.class);
				setResult(GlobalVar.NEW_PATH,intent);
				finish();				
			}			
		});
		_lv_path.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> ad, View v, int pos,
					long id) {		
					Intent intent=new Intent(SelectListActivity.this,MapActivity.class);
					intent.putExtra(GlobalVar.LOAD_PATH_STR, arrList.get(pos));
					setResult(GlobalVar.LOAD_PATH,intent);					
					finish();	
			}
		});
		_lv_path.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> ad, View v,
					int pos, long id) {
				remove=pos;
				showDialog("경로 제거","'"+arrList.get(pos)+"'"+"를 삭제하시겠습니까?");
				return false;
			}
		});			
		
	}
	public void updateFromDB(){
		int i;
		List<String> keyset=msql.sql_selectKeySet();
		arrList.clear();	
		
		for(i=0;i<keyset.size();i++){
			arrList.add(keyset.get(i));		
		}
		adapter.notifyDataSetChanged();
		//Log.d("DB","updateFromDB()::"+arrList.get(0).toString());
	}

	public void deletePath(){
		msql.sql_deleteData(arrList.get(remove));//delete in db
		arrList.remove(remove);//delete in list
		adapter.notifyDataSetChanged();		
	}
	private void showDialog(String title, String msg){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		builder.setTitle(title);
		builder.setMessage(msg);
		// 예 버튼 설정
		builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				deletePath();				
			}
		});
		// 취소 버튼 설정
		builder.setNeutralButton("아니오",new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {

			}
		});

		builder.create().show();
	}
}
