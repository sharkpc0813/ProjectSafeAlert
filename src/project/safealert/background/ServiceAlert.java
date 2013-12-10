package project.safealert.background;

import java.util.ArrayList;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.Vibrator;
import android.telephony.SmsManager;
import android.widget.Toast;
import project.safealert.SharedPrefManager;
import project.safealert.Global.GlobalVar;
import project.safealert.database.MySQLiteMap;
import project.safealert.database.PathInfo;

public class ServiceAlert extends Service implements SensorEventListener,LocationListener{
	private static final String RELOAD_TITLE="reload_title";
	private static final int VIB_TIME=1000;
	private static final int MSG_SMS=11211;
	private static final int PREVENT_DUP=2;


	//절전모드일경우에도 사용가능하도록 셋팅에 필요
	PowerManager pm;
	PowerManager.WakeLock wakeLock;

	/*db*/
	private MySQLiteMap msql;
	private ArrayList<PathInfo> arrList;
	private PathInfo startPos,destPos;
	private int index;
	/*preference*/
	private SharedPrefManager _pref;	
	private int threshold=0;	
	private boolean isStudyMode=false;
	/*thread control*/
	private String targetPath=null;	
	/*sensor*/
	private SensorManager _sm;
	private Sensor _sensor;
	private Vibrator vib;
	private float tempX=0;
	/*SMS*/
	final SmsManager sms=SmsManager.getDefault();	
	private double mapLinkLat,mapLinkLong;
	private boolean sendstat;
	/*map*/
	private LocationManager locationManager;	
	private boolean locChangeLock;
	private Location lastLocation;
	//private int curPos=0;
	/*main handler*/
	Handler _mHandler=new Handler(){
		@Override
		public void handleMessage(Message msg){			
			if(msg.what==MSG_SMS){
				//Log.d("SERVICE", "handleMessage()");	
				while(_mHandler.hasMessages(MSG_SMS))//중복 다이얼로그 방지
					_mHandler.removeMessages(MSG_SMS);
				vib.vibrate(VIB_TIME);	

				createDialog(getApplicationContext());
				_mHandler.sendEmptyMessageDelayed(PREVENT_DUP,GlobalVar.SENSOR_TIME_INTERVAL);//마구잡이 문자 방지하기위해 핸들러 처리
			}
			if(msg.what==PREVENT_DUP){
				sendstat=true;					
			}
		}
	};

	@Override
	public void onCreate() {
		super.onCreate();
		//Log.d("SERVICE",">>ServiceAlert::onCreate()");
		msql=new MySQLiteMap(this);		
		_pref=new SharedPrefManager(this);

		//절전모드에서도 CPU가 계속 사용중이도록 셋팅
		pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "WakeLock");		
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		//Log.d("SERVICE",">>ServiceAlert::onStartCommand()");
		initSensor();
		//get threshold
		threshold=_pref.getIntegerValue(GlobalVar.KEY_THRESHOLD);
		sendstat=true;
		//lock unlocking
		locChangeLock=true;
		//	MyThread _myThread=new MyThread(this,_mHandler);
		//	_myThread.start();	

		if(intent!=null){
			targetPath=intent.getStringExtra(GlobalVar.SERVICE);
			_pref.putValues(RELOAD_TITLE, targetPath);	
			serviceFirst();
		}
		else{//service died
			targetPath=_pref.getStringValue(RELOAD_TITLE);
			serviceDied();
		}
		return START_STICKY;
	}
	private void serviceFirst()
	{
		initLocation();
		/*map*/
		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());
		if (resultCode == ConnectionResult.SUCCESS){
			Toast.makeText(getApplicationContext(),"Google 서비스에 연결되었습니다.",Toast.LENGTH_SHORT).show();
		}
		else{
			Toast.makeText(getApplicationContext(),"Google 서비스에 연결되지 않았습니다.",Toast.LENGTH_SHORT).show();
		}

		if(targetPath.compareTo(GlobalVar.ONLY_SENSOR)!=0
				&& targetPath.compareTo(GlobalVar.NOTHING)!=0)//센서만 선택이 아니라면 맵설정 추가
		{
			//Log.d("SERVICE",targetPath+" ");
			initArrayList(targetPath);

			//mode setting
			isStudyMode=_pref.getBooleanValue(targetPath);
		}


		if(isStudyMode){
			Toast.makeText(getApplicationContext(),"학습모드로 실행합니다.",Toast.LENGTH_SHORT).show();

			index=msql.sql_curCntPos();

			startPos=new PathInfo(arrList.get(0).getIndex(),
					arrList.get(0).getTitle(),
					arrList.get(0).get_lat(), 
					arrList.get(0).get_long());
			destPos=new PathInfo(arrList.get(arrList.size()-1).getIndex(),
					arrList.get(arrList.size()-1).getTitle(),
					arrList.get(arrList.size()-1).get_lat(), 
					arrList.get(arrList.size()-1).get_long());
			arrList.clear();
			arrList=null;
			//arrList.add(startPos); //last
			msql.sql_deleteData(startPos.getTitle());
			if(msql.sql_updateData(startPos)<=0)
				msql.sql_insertData(startPos);
			_pref.putValues(GlobalVar.SERVICE_DIED_DEST_X, (float)destPos.get_lat()*100000);
			_pref.putValues(GlobalVar.SERVICE_DIED_DEST_Y, (float)destPos.get_long()*100000);
		}
		else{
			Toast.makeText(getApplicationContext(),"방범모드로 실행합니다.",Toast.LENGTH_SHORT).show();
		}
	}
	private void serviceDied()
	{
		initLocation();
		/*map*/
		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());

		if (resultCode == ConnectionResult.SUCCESS){
			Toast.makeText(getApplicationContext(),"Google 서비스에 연결되었습니다.",Toast.LENGTH_SHORT).show();
		}
		else{
			Toast.makeText(getApplicationContext(),"Google 서비스에 연결되지 않았습니다.",Toast.LENGTH_SHORT).show();
		}
		
		if(targetPath.compareTo(GlobalVar.ONLY_SENSOR)!=0
				&& targetPath.compareTo(GlobalVar.NOTHING)!=0)//센서만 선택이 아니라면 맵설정 추가
		{
			initArrayList(targetPath);
			
			//mode setting
			if(arrList!=null)
				isStudyMode=_pref.getBooleanValue(targetPath);
		}


		if(isStudyMode){
			Toast.makeText(getApplicationContext(),"학습모드로 실행합니다.",Toast.LENGTH_SHORT).show();
			index=arrList.get(arrList.size()-1).getIndex();
			arrList.clear();
			arrList=null;
			destPos=new PathInfo(0,
					targetPath,
					_pref.getFloatValue(GlobalVar.SERVICE_DIED_DEST_X)/100000, 
					_pref.getFloatValue(GlobalVar.SERVICE_DIED_DEST_Y)/100000);
		}
		else{
			Toast.makeText(getApplicationContext(),"방범모드로 실행합니다.",Toast.LENGTH_SHORT).show();
		}
	}
	@Override
	public void onDestroy() {
		super.onDestroy();
		//Log.d("SERVICE",">>ServiceAlert::onDestroy()");
		_sm.unregisterListener(ServiceAlert.this);		
		vib.cancel();
		//db update
		locChangeLock=false;
		if(locationManager!=null)
			locationManager.removeUpdates(this);
		if(isStudyMode){
			destPos.setIndex(++index);	
			if(msql.sql_updateData(destPos)<=0)
				msql.sql_insertData(destPos);
			_pref.putValues(targetPath, false);	
		}
		_pref.putValues(RELOAD_TITLE, GlobalVar.NOTHING);
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	private void initSensor(){
		_sm=(SensorManager)getSystemService(SENSOR_SERVICE);		
		_sensor=_sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		_sm.registerListener(ServiceAlert.this,_sensor, SensorManager.SENSOR_DELAY_NORMAL);
		vib=(Vibrator)getSystemService(Context.VIBRATOR_SERVICE);//get vibrator
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		wakeLock.acquire();	
		//Log.d("SERVICE", Math.abs(event.values[0]-tempX)+"");
		if(sendstat){
			if(Math.abs(event.values[0]-tempX)>threshold ){
				sendstat=false;			
				_mHandler.sendEmptyMessage(MSG_SMS);//마구잡이 문자 방지하기위해 핸들러 처리			
			}
			tempX=event.values[0];
		}
		wakeLock.release();
	}

	private void initArrayList(String title){
		arrList=(ArrayList<PathInfo>) msql.sql_selectAll(title);
		if(arrList!=null){
			if(arrList.size()==0){
				Toast.makeText(this, "데이터가 없습니다.", Toast.LENGTH_SHORT).show();
			}
		}
		else
			Toast.makeText(this, "경로가 존재하지 않습니다.", Toast.LENGTH_SHORT).show();

	}
	private void initLocation(){
		GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		criteria.setAltitudeRequired(false);
		criteria.setBearingRequired(true);
		criteria.setCostAllowed(true);
		criteria.setPowerRequirement(Criteria.POWER_LOW);
		String provider = locationManager.getBestProvider(criteria, true);
		if(provider==null){//사용자가 위치설정동의 안했을때 종료 
			onDestroy();
		}
		locationManager.requestLocationUpdates(provider,				
				(_pref.getIntegerValue(GlobalVar.LOCATION_TIME_INTERVAL))*100,
				GlobalVar.LOCATION_DISTANCE_INTERVAL, 
				ServiceAlert.this);		
		
			lastLocation=locationManager.getLastKnownLocation(provider);
	}

	/**      * Dialog Activity 호출 (PendingIntent)      */ 
	private void createDialog(Context context){   
		AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		String mapLink=null;
		if(mapLinkLat==0&&mapLinkLong==0)	
			mapLink="https://maps.google.com/maps?q="+lastLocation.getLatitude()+","+lastLocation.getLongitude();
		else
			mapLink="https://maps.google.com/maps?q="+mapLinkLat+","+mapLinkLong;
		Intent intent = new Intent(GlobalVar.MESSAGE_DLG);		
		intent.putExtra(GlobalVar.MESSAGE_MAP_LINK, mapLink);
		//
		PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, 0);        
		alarmManager.set(AlarmManager.RTC, System.currentTimeMillis(), pIntent);   
	}

	@Override
	public void onLocationChanged(Location location) {
		wakeLock.acquire();
		//현재위치를 가져옴
		if(location!=null&&location.getLatitude()!=0.0&&location.getLongitude()!=0.0){
			mapLinkLat=location.getLatitude();
			mapLinkLong=location.getLongitude();
			if(targetPath.compareTo(GlobalVar.ONLY_SENSOR)!=0){
				if(isStudyMode)
				{
					if(locChangeLock){
						//vib.vibrate(VIB_TIME);
						Toast.makeText(getApplicationContext(),"경로저장중",Toast.LENGTH_SHORT).show();
						if(msql.sql_updateData(new PathInfo(++index,targetPath,
								location.getLatitude(),location.getLongitude()))<=0)
							msql.sql_insertData(new PathInfo(++index,targetPath,
									location.getLatitude(),location.getLongitude()));
					}
				}
				else
					sencePathPos(location.getLatitude(),location.getLongitude());
			}
		}		
		wakeLock.release();
	}

	private void sencePathPos(double _lat,double _long){
		double min=999999999;
		for(int i=0;i<arrList.size();i++){
			double distance=Math.pow(
					Math.pow(_lat-arrList.get(i).get_lat(),2)+Math.pow(_long-arrList.get(i).get_long(),2)
					, 0.5);
			//Log.d("SERVICE",""+distance);
			if(distance<min){
				min=distance;
			}
		}
		//Log.d("SERVICE",""+min+", "+_pref.getIntegerValue(GlobalVar.AREA_SIZE));
		if(min>_pref.getIntegerValue(GlobalVar.AREA_SIZE))
		{
			Toast.makeText(this, "경로를 이탈하셨습니다.", Toast.LENGTH_SHORT).show();
			_mHandler.sendEmptyMessage(MSG_SMS);//마구잡이 문자 방지하기위해 핸들러 처리

		}
		else
			Toast.makeText(this, "경로를 따라갑니다.", Toast.LENGTH_SHORT).show();
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



}
