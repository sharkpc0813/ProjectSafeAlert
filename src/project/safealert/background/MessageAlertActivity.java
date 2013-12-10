package project.safealert.background;


import java.util.List;

import project.safealert.R;
import project.safealert.SharedPrefManager;
import project.safealert.Global.GlobalVar;
import project.safealert.members.Members;
import project.safealert.members.MySQLiteMember;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.SmsManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MessageAlertActivity extends Activity{
	private static final String DISP_TEXT="초 후에 자동으로 알립니다.)";
	private static final int DISP_COUNT=1717;
	private Button bt_ok,bt_cancel;
	private MySQLiteMember membersql;
	private SharedPrefManager _pref;
	private TextView tv_count,tv_text;
	private boolean isFinished;
	private String mapLink;
	/*SMS*/
	final SmsManager sms=SmsManager.getDefault();	
	//private boolean sendstat;
	
	Handler _mHandler=new Handler(){
		@Override
		public void handleMessage(Message msg){
			if(msg.what==DISP_COUNT){
				tv_count.setText("("+msg.obj.toString()+DISP_TEXT);	
				if((Integer)msg.obj!=0)
				{
					Message message=new Message();
					message.what=DISP_COUNT;
					message.obj=(Integer)msg.obj-1;
					_mHandler.sendMessageDelayed(message, 1000);
				}
				else{
					sendSmsToAgents(_pref.getStringValue(GlobalVar.SMS_TEXT)+"\n"+mapLink);
					tv_text.setText("\n방범대원들에게 알림하였습니다!");
					tv_count.setText("잘못 보냈다고 메세지를 보낼까요?");
					//finish();	
					isFinished=true;
				}
			}
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		setContentView(R.layout.dlg_message);
		mapLink=getIntent().getStringExtra(GlobalVar.MESSAGE_MAP_LINK);
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
				| WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
				| WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);		
		bt_ok=(Button) findViewById(R.id.bt_msg_ok);
		bt_cancel=(Button) findViewById(R.id.bt_msg_cancel);	
		tv_text=(TextView)findViewById(R.id.tv_text);
		tv_count=(TextView)findViewById(R.id.tv_count);
		
		membersql=new MySQLiteMember(this);
		_pref=new SharedPrefManager(this);
		
		initEvents();
		//start messaging
		isFinished=false;
		if(!_mHandler.hasMessages(DISP_COUNT))
		{
			Message msg=new Message();
			msg.what=DISP_COUNT;
			msg.obj=_pref.getIntegerValue(GlobalVar.MESSAGE_WAITING_TIME);
			_mHandler.sendMessageDelayed(msg, 1000);
		}
	}

	public void initEvents(){
		bt_ok.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if(isFinished)
				{
					sendSmsToAgents(_pref.getStringValue(GlobalVar.SMS_SORRY_TEXT));
					Toast.makeText(MessageAlertActivity.this, "문자를 전송하였습니다.", Toast.LENGTH_SHORT).show();
					finish();
				}
				else
				{
					while(_mHandler.hasMessages(DISP_COUNT))
						_mHandler.removeMessages(DISP_COUNT);
					if(mapLink!=null)
						sendSmsToAgents(_pref.getStringValue(GlobalVar.SMS_TEXT)+"\n"+mapLink);
					Toast.makeText(MessageAlertActivity.this, "문자를 전송하였습니다.", Toast.LENGTH_SHORT).show();
					finish();
				}
				
			}			
		});
		bt_cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				finish();				
			}			
		});		
	}

	public void sendSmsToAgents(String text)
	{
		List<Members> memlist=membersql.sql_selectAll();
		for(int i=0;i<memlist.size();i++)
			sms.sendTextMessage(memlist.get(i).getNumber(), _pref.getStringValue(GlobalVar.USERNUMBER), text, null, null);	
	}
}
