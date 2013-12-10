package project.safealert.alert;

import java.nio.FloatBuffer;

import project.safealert.R;
import project.safealert.SharedPrefManager;
import project.safealert.Global.GlobalVar;
import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class GyroAlert extends Activity implements SensorEventListener{

	private SharedPrefManager _pref;
	private GraphView graphView;
	
	private TextView _tv_sensor,_tv_max,_tv_over;
	private EditText _et_threshold;
	private ImageButton _bt_start,_bt_stop,_bt_save;
	
	private SensorManager _sm;
	private Sensor _sensor;
	private boolean configStarted=false;
	private float temp=0;
	private int graphCnt=0;
	private int threshold=0,threshold_cnt=0;
	private FloatBuffer values=FloatBuffer.allocate(100);
//	Handler _handler=new Handler(){
//		@Override
//		public void handleMessage(Message msg){
//			
//		}
//	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_alert);
		this.overridePendingTransition(R.anim.start_enter, R.anim.start_exit);
		//graphView=new GraphView(this);
		_pref=new SharedPrefManager(this);
		initControls();
		initSensor();	
		initEvents();
		initGraph();
		if(_pref.checkContains(GlobalVar.KEY_THRESHOLD))
			_et_threshold.setText(_pref.getIntegerValue(GlobalVar.KEY_THRESHOLD)+"");
	}
	private void initGraph(){
		for(int i=0;i<10;i++)
			values.put(0);
		graphView.setGraphView(values.array(),10, false);
		graphView.invalidate();
	}
	private void initControls(){
		_tv_sensor=(TextView)findViewById(R.id.tv_sensor);
		_tv_max=(TextView)findViewById(R.id.tv_max);
		_tv_over=(TextView)findViewById(R.id.tv_over);
		_bt_start=(ImageButton)findViewById(R.id.bt_start);
		_bt_stop=(ImageButton)findViewById(R.id.bt_stop);
		_bt_save=(ImageButton)findViewById(R.id.bt_save);
		_et_threshold=(EditText)findViewById(R.id.et_threshold);		
		graphView=(GraphView)findViewById(R.id.v_graph);
	}
	private void initSensor(){
		_sm=(SensorManager)getSystemService(SENSOR_SERVICE);		
		_sensor=_sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		_sm.registerListener(GyroAlert.this,_sensor, SensorManager.SENSOR_DELAY_NORMAL);
	}
	private void initEvents(){
		_bt_start.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				//toast
				Toast.makeText(GyroAlert.this, "감지를 시작합니다.", Toast.LENGTH_SHORT).show();
				//init
				configStarted=true;		
				values.clear();
				threshold=0;
				threshold_cnt=0;
				if(_pref.checkContains(GlobalVar.KEY_THRESHOLD))
					threshold=_pref.getIntegerValue(GlobalVar.KEY_THRESHOLD);				 
					
			}
		});
		_bt_stop.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				if(configStarted==true){
				//toast
				Toast.makeText(GyroAlert.this, "감지를 종료합니다.", Toast.LENGTH_SHORT).show();
				//draw graph
				graphView.setGraphView(values.array(),graphCnt, false);
				_tv_max.setText(Float.toString(graphView.getMax()));
				_tv_over.setText(""+threshold_cnt);
				graphView.invalidate();
				//init
				configStarted=false;					
				temp=0;
				graphCnt=0;
				}
			}
		});
		_bt_save.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				int val=Integer.parseInt(_et_threshold.getText().toString());
				//preference
				_pref.putValues(GlobalVar.KEY_THRESHOLD, val);	
				//toast for check
				Toast.makeText(GyroAlert.this, _pref.getIntegerValue(GlobalVar.KEY_THRESHOLD)+"로 설정되었습니다.", Toast.LENGTH_SHORT).show();
				
			}
		});
	}
	@Override
	public void onAccuracyChanged(Sensor event, int arg1) {
		// TODO Auto-generated method stub
	}
	@Override
	public void onSensorChanged(SensorEvent event) {
		if(configStarted){			
			_tv_sensor.setText(Float.toString(event.values[0]));
			values.put(Math.abs(event.values[0]-temp));
			if(Math.abs(event.values[0]-temp)>threshold)
				threshold_cnt++;
			temp=event.values[0];
			graphCnt++;
			
		}
	}
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		this.overridePendingTransition(R.anim.end_enter, R.anim.end_exit);
	}
}
