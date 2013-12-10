package project.safealert.members;

import java.util.ArrayList;
import java.util.List;

import project.safealert.R;
import project.safealert.Global.GlobalVar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;

public class MembersActivity extends Activity {
	private ListView _lv_members;
	private ImageButton _tv_members;
	private ArrayList<String> arrList;
	private ArrayAdapter<String> adapter;
	private MySQLiteMember membersql;
	private int remove=0;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_members);
		this.overridePendingTransition(R.anim.start_enter, R.anim.start_exit);
		
		_lv_members=(ListView)findViewById(R.id.lv_members);
		_tv_members=(ImageButton)findViewById(R.id.tv_members);

		//list
		arrList=new ArrayList<String>();
		adapter=new ArrayAdapter<String>(this,R.layout.listview_text,arrList);
		_lv_members.setAdapter(adapter);
		_lv_members.setTextFilterEnabled(true);

		//preference
		membersql=new MySQLiteMember(this);

		//event
		_tv_members.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent pickIntent = new Intent(Intent.ACTION_PICK); 
				//pickIntent.setType(ContactsContract.Contacts.CONTENT_TYPE); 
				pickIntent.setData(ContactsContract.CommonDataKinds.Phone.CONTENT_URI);

				startActivityForResult(pickIntent, GlobalVar.MEMBER_CONTRACT);

			}
		});
		_lv_members.setOnItemClickListener(new OnItemClickListener()  {

			@Override
			public void onItemClick(AdapterView<?> ad, View v, int pos,
					long arg3) {
				remove=pos;
				showDialog("대원 해고",arrList.get(pos)+"님을 해고하시겠습니까?");			
			}		
		});
		
	}
	@Override
	protected void onResume() {		
		super.onResume();
		updateMembers();

	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {		
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode==GlobalVar.MEMBER_CONTRACT && data!=null){
			Cursor cursor = getContentResolver().query(data.getData(), 
					new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, 
				ContactsContract.CommonDataKinds.Phone.NUMBER}, null, null, null);
			cursor.moveToFirst();
			//inser data
			Members mem=new Members(cursor.getString(0),cursor.getString(1));
			//Log.d("MEM",cursor.getString(0)+cursor.getString(1));
			if(membersql.sql_updateData(mem)<=0)//update
				membersql.sql_insertData(mem);//insert
			cursor.close();		
		}
	}
	private void updateMembers(){	
		int i;
		List<String> keyset=membersql.sql_selectKeySet();
		arrList.clear();	
		
		for(i=0;i<keyset.size();i++){
			arrList.add(keyset.get(i));		
		}
		adapter.notifyDataSetChanged();
	}
	public void deletePath(){
		membersql.sql_deleteData(arrList.get(remove));//delete in db
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
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		this.overridePendingTransition(R.anim.end_enter, R.anim.end_exit);
	}
}

