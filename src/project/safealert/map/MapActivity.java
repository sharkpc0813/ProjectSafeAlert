package project.safealert.map;


import java.util.List;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.SupportMapFragment;

import android.support.v4.app.FragmentActivity;

import project.safealert.R;
import project.safealert.SharedPrefManager;

import project.safealert.Global.GlobalVar;
import project.safealert.database.MySQLiteMap;
import project.safealert.database.PathInfo;

import android.app.AlertDialog;
import android.app.ProgressDialog;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;

import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class MapActivity extends FragmentActivity implements OnClickListener, android.location.LocationListener, ConnectionCallbacks, OnConnectionFailedListener {

	/*db*/
	private MySQLiteMap msql;//db
	private PathInfo pathInfo;
	private int dbIndex=0;
	/*preference*/
	private SharedPrefManager _pref;	
	/*map*/
	private GoogleMap map;
	//private GoogleMapOptions options = new GoogleMapOptions();
	private LatLng start_pos=null;
	private LatLng dest_pos=null;
	private Polyline polyline;
	private PolylineOptions rectOptions=null;
	private Marker start_mk=null,dest_mk=null; 
	private Circle circle=null;
	private Circle spot=null;
	private LocationManager locationManager=null;
	private boolean newPath=false;
	private Location lastKnowLocation;
	private LocationClient locClient;
	/*dlg*/
	private boolean dlgEditText;
	private EditText input;
	ProgressDialog progressDlg;
	/*ui*/
	private TextView _tv_path;//path config
	private ImageButton _bt_sel_path;//path config
	private CheckBox _cb_study;//study mode check	
	private ImageButton _bt_start,_bt_dest;//set path	
	private ImageButton _bt_save,_bt_cancel;//save,cancel
	//private int remove_drawing;
	/*main handler*/
	private final static int PROGRESS_DLG_SHOW=0; 
	private final static int PROGRESS_DLG_ON=1; 
	private final static int PROGRESS_DLG_DISMISS=2; 
	Handler _mHandler=new Handler(){
		@Override
		public void handleMessage(Message msg){
			if(msg.what==PROGRESS_DLG_SHOW){
				_mHandler.removeMessages(PROGRESS_DLG_SHOW);
				//Log.d("MapThread","handleMessage()::PROGRESS_DLG_SHOW");
				progressDlg=ProgressDialog.show(MapActivity.this, "경로 저장 중", "잠시만 기다려 주십시오."); // ProgressDialog 
				//insert		
				MainThread thread=new MainThread();
				thread.start();	
			}
			if(msg.what==PROGRESS_DLG_ON){
				//Log.d("Map", msg.obj.toString());
				if(msg.obj!=null)
					progressDlg.setMessage(msg.obj.toString()+"%");
			}
			if(msg.what==PROGRESS_DLG_DISMISS){
				progressDlg.setMessage("100%");
				progressDlg.dismiss();
				_tv_path.setText(" "+pathInfo.getTitle());
				Toast.makeText(MapActivity.this, "'"+pathInfo.getTitle()+"' 이 저장되었습니다." ,  Toast.LENGTH_SHORT).show();								
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);
		this.overridePendingTransition(R.anim.start_enter, R.anim.start_exit);
		initLocation();
		initMap();	   
		
		//create db
		msql=new MySQLiteMap(this);	
		dbIndex=msql.sql_curCntPos();
		pathInfo=new PathInfo(dbIndex,null, -1, -1);	
		_pref=new SharedPrefManager(this);
		//var for remove
		//remove_drawing=0;
		
		initControls();
		initEvents();		
	}
	@Override
	protected void onResume() {
		super.onResume();

		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());

		if (resultCode == ConnectionResult.SUCCESS){
			Toast.makeText(getApplicationContext(),"Google 서비스에 연결되었습니다.",Toast.LENGTH_SHORT).show();
		}else{
			GooglePlayServicesUtil.getErrorDialog(resultCode, this, 1);
		}
		locClient.connect();
	//	Log.d("Map"  , "connection : "+locClient.isConnected());		
	}
	@Override
	protected void onActivityResult(int request, int result, Intent intent) {
		super.onActivityResult(request, result, intent);
		if(request==GlobalVar.MAP_LIST_REQUEST){
			if(result==GlobalVar.NEW_PATH)
				updateWhole(null);
			else if(result==GlobalVar.LOAD_PATH)
				updateWhole(intent.getStringExtra(GlobalVar.LOAD_PATH_STR));
		}		
	}

	private void initEvents(){
		_bt_sel_path.setOnClickListener(this);
		_cb_study.setOnClickListener(this);
		_cb_study.setClickable(true);
		_bt_start.setOnClickListener(this);
		_bt_dest.setOnClickListener(this);
		_bt_save.setOnClickListener(this);
		_bt_cancel.setOnClickListener(this);
		map.setOnMapClickListener(new OnMapClickListener() {				
			@Override
			public void onMapClick(LatLng point) {
				if(start_pos==null){
					start_pos=new LatLng(point.latitude, point.longitude);
					start_mk = map.addMarker(new MarkerOptions().position(start_pos)
							.title("출발")
							.icon(BitmapDescriptorFactory
									.fromResource(R.drawable.start_stick)));
					if(dest_pos!=null)//draw line						
						drawLine(start_pos,dest_pos);
				}
				else if(dest_pos==null){	
					dest_pos=new LatLng(point.latitude, point.longitude);
					dest_mk = map.addMarker(new MarkerOptions()
					.position(dest_pos)
					.title("도착")
					.icon(BitmapDescriptorFactory
							.fromResource(R.drawable.dest_stick)));	
					drawLine(start_pos,dest_pos);//draw line	
				}
			}
		});		    
	}

	private void updateWhole(String title){
		//set start marker
		initDrawOnMap();
		if(title!=null){		
			//Log.d("Map","updateWhole()");
			//get info from db
			List<PathInfo> arr=msql.sql_selectAll(title);
			pathInfo.setTitle(arr.get(0).getTitle());
			_tv_path.setText(" "+arr.get(0).getTitle());//set ui		

			//set start marker
			start_pos=new LatLng(arr.get(0).get_lat(), arr.get(0).get_long());
			start_mk = map.addMarker(new MarkerOptions().position(start_pos)
					.title("출발")
					.icon(BitmapDescriptorFactory
							.fromResource(R.drawable.start_stick)));	
			//set dest marker			
			dest_pos=new LatLng(arr.get(arr.size()-1).get_lat(), arr.get(arr.size()-1).get_long());
			dest_mk = map.addMarker(new MarkerOptions()
			.position(dest_pos)
			.title("도착")
			.icon(BitmapDescriptorFactory
					.fromResource(R.drawable.dest_stick)));	
			//draw line			
			//remove_drawing=2;
			for(int i=0;i<arr.size()-1;i++){
				drawSpots(new LatLng(arr.get(i).get_lat(),arr.get(i).get_long()));				
				drawLine(new LatLng(arr.get(i).get_lat(),arr.get(i).get_long()),
						new LatLng(arr.get(i+1).get_lat(),arr.get(i+1).get_long()));	
				//remove_drawing++;
			}
			Toast.makeText(MapActivity.this, "경로가 설정되었습니다.", Toast.LENGTH_SHORT).show();
			newPath=_pref.getBooleanValue(title);
			_cb_study.setChecked(newPath);		

		}
		else{
			dbIndex=msql.sql_curCntPos();
			pathInfo=new PathInfo(dbIndex,null, -1, -1);	
			_tv_path.setText(" 경로 설정");
			_cb_study.setChecked(true);
			newPath=true;			
		}
	}
	@Override
	public void onClick(View v) {
		Intent intent;
		switch(v.getId()){
		case R.id.bt_sel_path:
			intent=new Intent(MapActivity.this,SelectListActivity.class);
			startActivityForResult(intent,GlobalVar.MAP_LIST_REQUEST);
			break;
		case R.id.cb_study:
			if(_cb_study.isChecked()){
				Toast.makeText(this, "경로가 초기화됩니다.", Toast.LENGTH_SHORT).show();
				//직선
				newPath=true;
			}
			else{
				Toast.makeText(this, "초기화를 취소합니다.", Toast.LENGTH_SHORT).show();
				//load
				newPath=false;
			}
			break;
		case R.id.bt_sel_start:
			Toast.makeText(this, "출발지를 선택하세요.", Toast.LENGTH_SHORT).show();
			//기존 마커 제거, 초기화
			removeMarker(start_mk);

			if(polyline!=null){				
				polyline.remove();
				polyline=null;
			}
			start_pos=null;	
			map.clear();
			if(dest_pos!=null)
				dest_mk = map.addMarker(new MarkerOptions().position(dest_pos)
						.title("도착")
						.icon(BitmapDescriptorFactory
								.fromResource(R.drawable.dest_stick)));
			break;
		case R.id.bt_sel_dest:
			Toast.makeText(this, "목적지를 선택하세요.", Toast.LENGTH_SHORT).show();
			//기존 마커 제거, 초기화			
			removeMarker(dest_mk);
			if(polyline!=null){				
				polyline.remove();
				polyline=null;
			}
			dest_pos=null;
			map.clear();
			if(start_pos!=null)
				start_mk = map.addMarker(new MarkerOptions().position(start_pos)
						.title("출발")
						.icon(BitmapDescriptorFactory
								.fromResource(R.drawable.start_stick)));	
			break;
		case R.id.bt_save_path:
			dlgEditText=true;
			if(polyline!=null)//경로가 설정되었는지
			{	
				if(pathInfo.getTitle()!=null){//기존의 경로를 변경한경우					
					_pref.putValues(pathInfo.getTitle(), newPath);
					if(newPath==true){	
						//Log.d("MapThread","PROGRESS_DLG_SHOW");
						_mHandler.sendEmptyMessage(PROGRESS_DLG_SHOW);	
					}
					else{
						_tv_path.setText(" "+pathInfo.getTitle());//set ui
						Toast.makeText(MapActivity.this, "'"+pathInfo.getTitle()+"' 이 저장되었습니다." ,  Toast.LENGTH_SHORT).show();
					}
				}			
				else	//새경로를 만드는 경우
					showDialog("저장","경로의 이름을 입력해주세요.");			
			}
			else{
				Toast.makeText(MapActivity.this, "경로를 다시 설정해주세요.",  Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.bt_cancel_path:
			dlgEditText=false;
			showDialog("저장하셨나요?","경로 설정을 종료하시겠습니까?");
			break;
		}

	}

	private void showDialog(String title, String msg){	

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(title);
		builder.setMessage(msg);

		if(dlgEditText==true){
			// Set an EditText view to get user input
			input = new EditText(this);
			input.setPadding(20, 0, 20, 0);
			builder.setView(input);
		}
		//예 버튼 설정
		builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				if(dlgEditText){

					//검증
					if(input.getText().toString().compareTo("")!=0 && isPrimaryKey(input.getText().toString()))
					{					
						//Log.d("MapThread","PROGRESS_DLG_SHOW");
						pathInfo.setTitle(input.getText().toString());	
						_pref.putValues(input.getText().toString(), newPath);
						_mHandler.sendEmptyMessage(PROGRESS_DLG_SHOW);			
					}
					else{
						Toast.makeText(MapActivity.this, "경로를 다시 입력해주세요.",  Toast.LENGTH_SHORT).show();
					}
				}
				else
					onBackPressed();
				
			}
		});
		//취소 버튼 설정
		builder.setNeutralButton("아니오",new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {

			}
		});

		// 빌더 객체의 create() 메소드 호출하면 대화상자 객체 생성
		builder.create().show();
	}
	private void makeNewPath(){		
		//Log.d("MapThread","MapActivity::makeNewPath()");
		//delete before
		msql.sql_deleteData(pathInfo.getTitle());
		//save start pos
		pathInfo.set_lat(start_pos.latitude);
		pathInfo.set_long(start_pos.longitude);
		pathInfo.setIndex(++dbIndex);
		
		if(msql.sql_updateData(pathInfo)<=0)
			msql.sql_insertData(pathInfo);
		
		float[] distance=new float[1];
		Location.distanceBetween(start_pos.latitude,start_pos.longitude,
				dest_pos.latitude,dest_pos.longitude,distance);
		int ratio=(int) (distance[0]/GlobalVar.DEFAULT_PATH_INTERVAL);
		float ratio_opp=1;

		//remove_drawing=2;
		while(ratio>=ratio_opp)
		{
			//Log.d("Map",""+distance+", "+cumul_distance);
			Message msg=new Message();
			msg.what=PROGRESS_DLG_ON;
			msg.obj=(int)((ratio_opp/ratio)*100);
			//Log.d("Map",ratio_opp+"/"+ratio+" *100="+msg.obj);
			_mHandler.sendMessage(msg);

			double x=((ratio-ratio_opp)*start_pos.latitude+(ratio_opp)*dest_pos.latitude)/ratio;
			double y=((ratio-ratio_opp)*start_pos.longitude+(ratio_opp)*dest_pos.longitude)/ratio;
			//Log.d("Map",x+", "+y);
			//save inter pos
			pathInfo.set_lat(x);
			pathInfo.set_long(y);
			pathInfo.setIndex(++dbIndex);

			if(msql.sql_updateData(pathInfo)<=0)
				msql.sql_insertData(pathInfo);
			
			ratio_opp++;
		//	remove_drawing++;
		}
		//save dest pos
		pathInfo.set_lat(dest_pos.latitude);
		pathInfo.set_long(dest_pos.longitude);
		pathInfo.setIndex(++dbIndex);
		
		if(msql.sql_updateData(pathInfo)<=0)
			msql.sql_insertData(pathInfo);
		
		_mHandler.sendEmptyMessage(PROGRESS_DLG_DISMISS);	
	}

	private void drawLine(LatLng start,LatLng dest){
		rectOptions = new PolylineOptions()
		.add(start)
		.add(dest)
		.zIndex(0)
		.color(Color.RED); // Closes the polyline.
		polyline = map.addPolyline(rectOptions);
	}
	private void drawCircle(LatLng center){
		circle = map.addCircle(new CircleOptions()
		.center(center)
		.radius(_pref.getIntegerValue(GlobalVar.AREA_SIZE))//500m
		.strokeColor(Color.TRANSPARENT)
		.fillColor(Color.argb(60, 255, 255, 0)));
	}
	private void drawSpots(LatLng center){
		spot = map.addCircle(new CircleOptions()
		.center(center)
		.zIndex(1)
		.radius(GlobalVar.SPOT_SIZE)//5m
		.strokeColor(Color.BLACK)
		.fillColor(Color.argb(255, 0, 0, 255)));
	}
	private void initDrawOnMap(){
		//선 초기화
	//	Log.d("Map",remove_drawing+"");
		if(polyline!=null){		
			polyline=null;
		}
		//점 초기화
		if(spot!=null){	
			spot=null;
		}		
		
		//원 초기화
		if(circle!=null){
			circle.remove();
		}
		//마커 초기화
		if(start_pos!=null)
			start_pos=null;
		if(dest_pos!=null)
			dest_pos=null;
		removeMarker(start_mk);
		removeMarker(dest_mk);		
		map.clear();
	}
	private void removeMarker(Marker marker){
		if(marker!=null){
			marker.remove();
			marker=null;
		}
	}
	private void initLocation(){
		//GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());

//		if (resultCode == ConnectionResult.SUCCESS){
//			Log.d("Map","성공");
//		}else{
//			Log.d("Map","실패");
//		}
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		criteria.setAltitudeRequired(false);
		criteria.setBearingRequired(true);
		criteria.setCostAllowed(true);
		criteria.setPowerRequirement(Criteria.POWER_LOW);
		String provider = locationManager.getBestProvider(criteria, true);

		if(provider==null){//사용자가 위치설정동의 안했을때 종료 
			finish();
		}
		/*	else{//사용자가 위치설정 동의 했을때 		
			if (locationManager.isProviderEnabled(locationManager.NETWORK_PROVIDER) == true)
			{               
				// NETWORK_PROVIDER가 Enabled
				provider = LocationManager.NETWORK_PROVIDER;  
				locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, MapActivity.this);
			}
			else {  
				// GPS_PROVIDER가 Enabled 라면..   
				provider = LocationManager.GPS_PROVIDER;   
				locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, MapActivity.this);
			} 		
			locationManager.requestLocationUpdates(provider, 0, 0, MapActivity.this);
		}	*/
		locationManager.requestLocationUpdates(provider, 0, 0, MapActivity.this);
		lastKnowLocation = locationManager.getLastKnownLocation(provider);
		locClient=new LocationClient(this, this, this);
	}
	private void initMap(){
		map = ((SupportMapFragment)getSupportFragmentManager()
				.findFragmentById(R.id.map)).getMap();
		map.setMyLocationEnabled(true);		
	}
	private void setupCarmera(){
		//Location myloc=map.getMyLocation();
		Location myloc = locClient.getLastLocation();

		// Zoom in, animating the camera.
		map.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);	
		// Move the camera
		if(myloc==null){
			if(lastKnowLocation==null)
			{	
				onDestroy();
				Toast.makeText(getApplicationContext(), "서비스가 준비되지 않았습니다.\n재실행해주세요.", Toast.LENGTH_SHORT).show();
			}
			map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastKnowLocation.getLatitude(),lastKnowLocation.getLongitude()), 15));
			drawCircle(new LatLng(lastKnowLocation.getLatitude(),lastKnowLocation.getLongitude()));
			Toast.makeText(this, "마지막 위치를 불러옵니다.", Toast.LENGTH_SHORT).show();
		}
		else
		{
			map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(myloc.getLatitude(),myloc.getLongitude()), 15));
			drawCircle(new LatLng(myloc.getLatitude(),myloc.getLongitude()));
			Toast.makeText(this, "현재 위치를 불러옵니다.", Toast.LENGTH_SHORT).show();
		}
	}
	private void initControls(){
		_tv_path=(TextView)findViewById(R.id.tv_sel_path);
		_bt_sel_path=(ImageButton)findViewById(R.id.bt_sel_path);
		_cb_study=(CheckBox)findViewById(R.id.cb_study);
		_bt_start=(ImageButton)findViewById(R.id.bt_sel_start);
		_bt_dest=(ImageButton)findViewById(R.id.bt_sel_dest);
		_bt_save=(ImageButton)findViewById(R.id.bt_save_path);
		_bt_cancel=(ImageButton)findViewById(R.id.bt_cancel_path);
		_tv_path.setText(" 경로 설정");
		_cb_study.setChecked(false);
	}
	public boolean isPrimaryKey(String title){
		int i;
		List<String> keyset=msql.sql_selectKeySet();
		for(i=0;i<keyset.size();i++){
			if(keyset.get(i).compareTo(title)==0)
				return false;		
		}	
		return true;
	}
	@Override
	public void onLocationChanged(Location location) {		
		//원 초기화
		if(circle!=null)
			circle.remove();
		//현재위치를 가져옴
		double lat =  location.getLatitude();
		double lng = location.getLongitude();
		Toast.makeText(this  , lat+", "+lng,Toast.LENGTH_SHORT).show();
		drawCircle(new LatLng(lat,lng));
		//	locationTag=false;
		//	Toast.makeText(MapActivity.this, "현재 위치를 불러옵니다.",  Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onProviderDisabled(String arg0) {
		// TODO Auto-generated method stub

	}
	@Override
	public void onProviderEnabled(String arg0) {
		// TODO Auto-generated method stub

	}
	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		// TODO Auto-generated method stub

	}
	@Override
	protected void onPause() {
		super.onPause();
		locClient.disconnect();
	}
	@Override
	protected void onDestroy() {		
		super.onDestroy();
				
	}
	@Override
	public void onConnected(Bundle arg0) {
		setupCarmera();
	}
	@Override
	public void onDisconnected() {
		Toast.makeText(this, "Disconnected", Toast.LENGTH_LONG).show();		
	}
	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		Toast.makeText(this, "Connection Failed", Toast.LENGTH_LONG).show();		
	}

	class MainThread extends Thread{
		public MainThread(){}	
		public void run(){			
		//	Log.d("SERVICE",">>MyThread::run()");
			makeNewPath();
		}
	}
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		locationManager.removeUpdates(this);
		this.overridePendingTransition(R.anim.end_enter, R.anim.end_exit);		
	}
}
