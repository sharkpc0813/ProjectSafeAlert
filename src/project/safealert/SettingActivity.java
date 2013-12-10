package project.safealert;

import java.util.ArrayList;

import project.safealert.Global.GlobalVar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

public class SettingActivity extends Activity {
	private SharedPrefManager _pref;
	private ListView _lv_setting;
	private ArrayList<String> arrList;
	private ArrayAdapter<String> adapter;
	private EditText editInput;
	private ImageView showText=null;
	private int cur_pos;
	
	void settingContent(int pos)
	{
		
		switch(pos)
		{
		case 0:
			editInput = new EditText(this);		
			editInput.setWidth(70);
			editInput.setHeight(200);
			editInput.setText(_pref.getStringValue(GlobalVar.SMS_TEXT));
			showDialog("������ ���ڸ� �Է����ּ���.",0);
			configSetting(GlobalVar.SMS_TEXT, _pref.getStringValue(GlobalVar.SMS_TEXT));
			break;
		case 1:
			editInput = new EditText(this);		
			editInput.setWidth(70);
			editInput.setHeight(200);
			editInput.setText(_pref.getStringValue(GlobalVar.SMS_SORRY_TEXT));
			showDialog("�߸� �������� ���� ���ڸ� �Է����ּ���.",1);
			configSetting(GlobalVar.SMS_SORRY_TEXT, _pref.getStringValue(GlobalVar.SMS_SORRY_TEXT));
			break;
		case 2:
			editInput = new EditText(this);		
			editInput.setWidth(10);
			editInput.setHeight(10);
			editInput.setInputType(InputType.TYPE_CLASS_NUMBER);
			editInput.setText(_pref.getIntegerValue(GlobalVar.MESSAGE_WAITING_TIME)+"");
			showDialog("�޼��� ��� �ð��� �������ּ���.(��)",2);
			configSetting(GlobalVar.MESSAGE_WAITING_TIME, _pref.getIntegerValue(GlobalVar.MESSAGE_WAITING_TIME));
			break;
		case 3:			
			editInput = new EditText(this);		
			editInput.setWidth(10);
			editInput.setHeight(10);
			editInput.setInputType(InputType.TYPE_CLASS_NUMBER);
			editInput.setText(""+_pref.getIntegerValue(GlobalVar.AREA_SIZE));
			showDialog("�ֺ� Ȯ�� ������ �������ּ���.(m)",3);
			configSetting(GlobalVar.AREA_SIZE, _pref.getIntegerValue(GlobalVar.AREA_SIZE));			
			break;
		case 4:
			editInput = new EditText(this);		
			editInput.setWidth(10);
			editInput.setHeight(10);
			editInput.setInputType(InputType.TYPE_CLASS_NUMBER);
			editInput.setText(""+_pref.getIntegerValue(GlobalVar.LOCATION_TIME_INTERVAL));
			showDialog("���ʸ��� ��θ� Ȯ���ϰ� �������� �������ּ���.(��)",4);
			configSetting(GlobalVar.LOCATION_TIME_INTERVAL, (_pref.getIntegerValue(GlobalVar.LOCATION_TIME_INTERVAL)));
			break;
		case 5:			
			showText=new ImageView(this);			
			showText.setBackgroundResource(R.drawable.user_manual);
			showDialogImage("����� �޴���");	
			break;
		case 6:
			showText=new ImageView(this);
			showText.setBackgroundResource(R.drawable.contact);
			showDialogImage("Developer Contact");	
			break;
		}
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);
		this.overridePendingTransition(R.anim.start_enter, R.anim.start_exit);
		_pref=new SharedPrefManager(this);
		
		_lv_setting=(ListView) findViewById(R.id.lv_setting);

		arrList=new ArrayList<String>();
		adapter=new ArrayAdapter<String>(this,R.layout.listview_text,arrList);
		
		_lv_setting.setAdapter(adapter);
		_lv_setting.setTextFilterEnabled(true);		
		initEvents();		
		settingListUpdate();
	}

	public void initEvents()
	{
		_lv_setting.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int pos,
					long arg3) {
				 settingContent(pos);				
			}
		});
	}

	public void settingListUpdate()
	{
		arrList.clear();
		arrList.add("���� ����");	
		arrList.add("��� ����");	
		arrList.add("�޼��� ���� ��� �ð�");	
		arrList.add("�� �ֺ� ����");	
		arrList.add("��� Ȯ�� �ֱ�");
		arrList.add("��� ����");
		arrList.add("������");
		adapter.notifyDataSetChanged();
	}
	public void configSetting(String key, Object value)
	{
		if(Integer.class.isInstance(value))		{
			_pref.putValues(key,(Integer)value);		
		}
		else if(String.class.isInstance(value)){
			_pref.putValues(key,(String)value);
		}
	}
	private void showDialog(String title, int pos)
	{

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(title);
		// Set an EditText view to get user editInput	
		if(editInput!=null)
			builder.setView(editInput);
		cur_pos=pos;
		//�� ��ư ����
		builder.setPositiveButton("��", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				switch(cur_pos)
				{
				case 0:
					_pref.putValues(GlobalVar.SMS_TEXT, editInput.getText().toString());
					break;
				case 1:
					_pref.putValues(GlobalVar.SMS_SORRY_TEXT, editInput.getText().toString());
					break;
				case 2:
					_pref.putValues(GlobalVar.MESSAGE_WAITING_TIME, Integer.parseInt(editInput.getText().toString()));
					break;
				case 3:
					_pref.putValues(GlobalVar.AREA_SIZE, Integer.parseInt(editInput.getText().toString()));
					break;
				case 4:
					_pref.putValues(GlobalVar.LOCATION_TIME_INTERVAL, Integer.parseInt(editInput.getText().toString()));
					break;
				
				}
				editInput=null;
			}
		});
		//��� ��ư ����
		builder.setNeutralButton("���",new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				editInput=null;
			}
		});

		// ���� ��ü�� create() �޼ҵ� ȣ���ϸ� ��ȭ���� ��ü ����
		builder.create().show();
	}
	private void showDialogImage(String title)
	{

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(title);

		if(showText!=null)
			builder.setView(showText);


		// ���� ��ü�� create() �޼ҵ� ȣ���ϸ� ��ȭ���� ��ü ����
		builder.create().show();
	}
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		editInput=null;
		showText=null;
		this.overridePendingTransition(R.anim.end_enter, R.anim.end_exit);
	}
}
