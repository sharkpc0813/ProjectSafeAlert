package project.safealert.widget;

import java.util.ArrayList;
import java.util.List;

import project.safealert.R;
import project.safealert.SharedPrefManager;
import project.safealert.Global.GlobalVar;
import project.safealert.database.MySQLiteMap;
import project.safealert.map.MapActivity;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class WidgetActivity extends Activity{	
	private static final String ACTIVATION="activation";
	private MySQLiteMap msql;
	private SharedPrefManager _pref;
	private ListView _lv_path;
	private TextView _tv_addlist;
	private Button _bt_begin;
	private ToggleButton _tb_study;
	private ImageButton _bt_widget_map;
	private EditText _et_sensor;
	private ArrayList<String> arrList;
	private ArrayAdapter<String> adapter;
	private int remove=0;
	private String selectedItem;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_widget);

		String network=getOnlineType();
		if(network!=null)
			Toast.makeText(WidgetActivity.this, network, Toast.LENGTH_LONG).show();
		
		initControls();

		msql=new MySQLiteMap(this);
		_pref=new SharedPrefManager(this);
		selectedItem=null;

		//set threshold
		if(_pref.checkContains(GlobalVar.KEY_THRESHOLD))
			_et_sensor.setText(_pref.getIntegerValue(GlobalVar.KEY_THRESHOLD)+"");
		//set activation
		if(_pref.getBooleanValue(ACTIVATION))
			_bt_begin.setText("    종료    ");
		else
			_bt_begin.setText("    실행    ");
		
		initEvents();
	}

	@Override
	protected void onResume() {
		super.onResume();
		updateFromDB();		
		this.overridePendingTransition(R.anim.start_enter, R.anim.start_exit);
		if(selectedItem==null)
			_bt_widget_map.setVisibility(_bt_widget_map.INVISIBLE);
	}
	@Override
	protected void onPause() {
		super.onPause();
		this.overridePendingTransition(R.anim.end_enter, R.anim.end_exit);
	}
	private void initControls(){
		_lv_path=(ListView) findViewById(R.id.lv_path);
		_tv_addlist=(TextView) findViewById(R.id.tv_addlist);
		_bt_begin=(Button) findViewById(R.id.bt_start_widget);
		_tb_study=(ToggleButton) findViewById(R.id.tb_study);
		_et_sensor=(EditText) findViewById(R.id.et_sensor_widget);
		_bt_widget_map=(ImageButton) findViewById(R.id.bt_widget_map);
		arrList=new ArrayList<String>();
		adapter=new ArrayAdapter<String>(this,R.layout.listview_text,arrList);

		_lv_path.setAdapter(adapter);
		_lv_path.setTextFilterEnabled(true);
		_lv_path.setDrawSelectorOnTop(true);
	}
	public void initEvents(){

		_tv_addlist.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent intent=new Intent(WidgetActivity.this,MapActivity.class);	
				startActivity(intent);
				finish();				
			}			
		});
		_bt_widget_map.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if(selectedItem!=null&&selectedItem.compareTo("없음")!=0){
					Intent intent=new Intent(WidgetActivity.this,WidgetMapActivity.class);	
					WidgetActivity.this.overridePendingTransition(R.anim.end_enter, R.anim.end_exit);
					intent.putExtra(GlobalVar.WIDGET_MAP, selectedItem);
					startActivity(intent);
				}				
			}			
		});
		_lv_path.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> ad, View v, int pos,
					long id) {		

				ArrayList<View> arr=ad.getTouchables();
				for(int i=0;i<arr.size();i++)
					arr.get(i).setBackgroundColor(Color.WHITE);

				v.setBackgroundColor(Color.YELLOW);
				selectedItem=arrList.get(pos);
				if(pos!=0)
				{
					_bt_widget_map.setVisibility(_bt_widget_map.VISIBLE);
				}
				else
				{
					_bt_widget_map.setVisibility(_bt_widget_map.INVISIBLE);
				}
				//study mode				
				_tb_study.setChecked(_pref.getBooleanValue(selectedItem));
			}
		});		
		_lv_path.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> ad, View v,
					int pos, long id) {
				if(pos!=0){//UI
					remove=pos;
					showDialog("경로 제거","'"+arrList.get(pos)+"'"+"를 삭제하시겠습니까?");
				}
				return false;
			}
		});		
		_bt_begin.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				boolean isActivated=_pref.getBooleanValue(ACTIVATION);
				//실행
				if(!isActivated){
					if(selectedItem!=null){
						Toast.makeText(WidgetActivity.this, "'"+selectedItem+"'이(가) 설정되었습니다.", Toast.LENGTH_SHORT).show();			

						int val=Integer.parseInt(_et_sensor.getText().toString());
						//preference
						_pref.putValues(GlobalVar.KEY_THRESHOLD, val);	
						Intent intent=new Intent(GlobalVar.WIDGET_UPDATE);
						//change widget image
						intent.putExtra(GlobalVar.WIDGET_BRODCAST, true);
						sendBroadcast(intent);	
						//Service
						onStartService(selectedItem);
						finish();
						_pref.putValues(ACTIVATION, true);
					}
					else
						Toast.makeText(WidgetActivity.this, "경로를 선택해주십시오.", Toast.LENGTH_SHORT).show();
				}
				else //종료
				{
					_pref.putValues(ACTIVATION, false);
					Toast.makeText(WidgetActivity.this, "종료되었습니다.", Toast.LENGTH_SHORT).show();			
					Intent intent=new Intent(GlobalVar.WIDGET_UPDATE);
					intent.putExtra(GlobalVar.WIDGET_BRODCAST, false);
					sendBroadcast(intent);
					onStopService();
					finish();
				}
			}			
		});
		_tb_study.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				boolean on = ((ToggleButton) v).isChecked();
				if(on)
				{
					_pref.putValues(selectedItem, true);
					Toast.makeText(WidgetActivity.this, "학습모드를 켜놓습니다.", Toast.LENGTH_SHORT).show();					
				}
				else
				{
					_pref.putValues(selectedItem, false);
					Toast.makeText(WidgetActivity.this, "학습모드를 꺼놓습니다.", Toast.LENGTH_SHORT).show();
				}

			}
		});
		//		_bt_shut.setOnClickListener(new OnClickListener() {
		//			@Override
		//			public void onClick(View arg0) {

		//
		//			}			
		//		});

	}
	public void updateFromDB(){
		int i;
		List<String> keyset=msql.sql_selectKeySet();
		arrList.clear();	
		arrList.add(GlobalVar.ONLY_SENSOR);//UI
		for(i=0;i<keyset.size();i++){
			arrList.add(keyset.get(i));		
		}
		adapter.notifyDataSetChanged();
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

		// 빌더 객체의 create() 메소드 호출하면 대화상자 객체 생성
		builder.create().show();
	}

	//service
	public void onStartService(String str){
		//Log.d("SERVICE",">>WidgetActivity::onStartService()");
		Intent intent=new Intent(GlobalVar.SERVICE);
		intent.putExtra(GlobalVar.SERVICE, str);
		if(_pref.getBooleanValue(str))
			intent.putExtra(GlobalVar.SERVICE_STUDY, true);
		else
			intent.putExtra(GlobalVar.SERVICE_STUDY, false);
		startService(intent);

	}
	public void onStopService(){
		//Log.d("SERVICE",">>WidgetActivity::onStopService()");
		Intent intent=new Intent(GlobalVar.SERVICE);
		stopService(intent);
	}
	 private String getOnlineType() {   
		 try {          
			 ConnectivityManager conMan = (ConnectivityManager) getApplication().getSystemService(Context.CONNECTIVITY_SERVICE);   
			 State wifi = conMan.getNetworkInfo(1).getState(); // wifi 
			 if (wifi == NetworkInfo.State.CONNECTED || wifi == NetworkInfo.State.CONNECTING)
				 return null;           
			 State mobile = conMan.getNetworkInfo(0).getState();       
			 if (mobile == NetworkInfo.State.CONNECTED || mobile == NetworkInfo.State.CONNECTING)
				 return null;      
			 } 
		 catch (NullPointerException e) {   
			 return "네트워크를 연결해 주십시오.";
			 }       
		 return "네트워크를 연결해 주십시오.";
	}
}
