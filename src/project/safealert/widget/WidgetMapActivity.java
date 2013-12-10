package project.safealert.widget;

import java.util.List;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
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


import android.content.Context;
import android.graphics.Color;

import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

public class WidgetMapActivity extends FragmentActivity implements android.location.LocationListener, ConnectionCallbacks, OnConnectionFailedListener {
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
	private Location lastKnowLocation;
	private LocationClient locClient;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_widget_map);
		this.overridePendingTransition(R.anim.start_enter, R.anim.start_exit);
		//this.overridePendingTransition(R.anim.end_enter, R.anim.end_exit);
		initLocation();
		initMap();	   
		//create db
		msql=new MySQLiteMap(this);	
		dbIndex=msql.sql_curCntPos();
		pathInfo=new PathInfo(dbIndex,null, -1, -1);	
		
		_pref=new SharedPrefManager(this);
		
		updateWhole(getIntent().getStringExtra(GlobalVar.WIDGET_MAP));	
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
	}

	private void updateWhole(String title){
		//set start marker
		initDrawOnMap();
		//Log.d("Map","updateWhole()");
		//get info from db
		List<PathInfo> arr=msql.sql_selectAll(title);
		pathInfo.setTitle(arr.get(0).getTitle());

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
		for(int i=0;i<arr.size()-1;i++){
			drawSpots(new LatLng(arr.get(i).get_lat(),arr.get(i).get_long()));				
			drawLine(new LatLng(arr.get(i).get_lat(),arr.get(i).get_long()),
					new LatLng(arr.get(i+1).get_lat(),arr.get(i+1).get_long()));	
		}
		Toast.makeText(WidgetMapActivity.this, "경로가 설정되었습니다.", Toast.LENGTH_SHORT).show();


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
			finish();
		}
		locationManager.requestLocationUpdates(provider, 0, 0, WidgetMapActivity.this);
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
		//Toast.makeText(this  , lat+", "+lng,Toast.LENGTH_SHORT).show();
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
	public void onBackPressed() {
		super.onBackPressed();
		this.overridePendingTransition(R.anim.end_enter, R.anim.end_exit);
	}
	@Override
	protected void onDestroy() {		
		super.onDestroy();
		locationManager.removeUpdates(this);
		
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
	
}
