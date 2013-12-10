package project.safealert;


import project.safealert.Global.GlobalVar;
import project.safealert.alert.GyroAlert;
import project.safealert.map.MapActivity;
import project.safealert.members.MembersActivity;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

public class MainActivity extends Activity {
	private SharedPrefManager _pref;	
	private ImageButton _bt_alert,_bt_path,_bt_help,_bt_numbers;
	private AnimationDrawable frameAnimation;
	private LinearLayout ll_anim;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		setContentView(R.layout.activity_main);

		defaultSetting();

		_bt_alert=(ImageButton)findViewById(R.id.bt_alert);
		_bt_path=(ImageButton)findViewById(R.id.bt_path);
		_bt_help=(ImageButton)findViewById(R.id.bt_help);
		_bt_numbers=(ImageButton)findViewById(R.id.bt_numbers);
		
		ll_anim=(LinearLayout)findViewById(R.id.ll_anim);
		
		// 이미지를 동작시키기위해  AnimationDrawable 객체를 가져온다.      
		frameAnimation = (AnimationDrawable) ll_anim.getBackground();
		frameAnimation.start();
	
		
		_bt_alert.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				Intent intent=new Intent(MainActivity.this,GyroAlert.class);	
				startActivity(intent);

			}
		});
		_bt_path.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				Intent intent=new Intent(MainActivity.this,MapActivity.class);	
				startActivity(intent);

			}
		});
		_bt_numbers.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				Intent intent=new Intent(MainActivity.this,MembersActivity.class);	
				startActivity(intent);

			}
		});
		_bt_help.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				Intent intent=new Intent(MainActivity.this,SettingActivity.class);	
				startActivity(intent);

			}
		});		
	}
	public void defaultSetting()
	{
		_pref=new SharedPrefManager(this);
		if(_pref.getBooleanValue(GlobalVar.DEFAULT_SETTING)==false){			
			TelephonyManager mgr = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
			_pref.putValues(GlobalVar.USERNAME, mgr.getLine1Number());
			_pref.putValues(GlobalVar.USERNUMBER, mgr.getLine1Number());
			_pref.putValues(GlobalVar.SMS_TEXT, "연락한번 부탁드려요~\n[도와줘요 방범대!]");
			_pref.putValues(GlobalVar.SMS_SORRY_TEXT, "잘못보냈습니다.^^;\n[도와줘요 방범대!]");
			_pref.putValues(GlobalVar.AREA_SIZE, 300);
			_pref.putValues(GlobalVar.LOCATION_TIME_INTERVAL, 50);//50초
			_pref.putValues(GlobalVar.MESSAGE_WAITING_TIME, 60);//60초
			_pref.putValues(GlobalVar.KEY_THRESHOLD,20);
			
			Toast.makeText(this, "기본 데이터가 자동 설정되었습니다.\n변경을 원하시면 기타 설정을 이용해주시기 바랍니다.",Toast.LENGTH_LONG).show();
			_pref.putValues(GlobalVar.DEFAULT_SETTING, true);
		}

	}
	protected void onDestroy() {
		super.onDestroy();
		frameAnimation.stop();
	};
}
