package project.safealert;

import project.safealert.Global.GlobalVar;
import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefManager {
	private SharedPreferences _pref=null;
	private SharedPreferences.Editor _sp_editor=null;
	private Context _context;
	private final String PREF_NAME="evn";
	
	public SharedPrefManager(Context context) {
		_context=context;
		_pref=_context.getSharedPreferences(PREF_NAME,Context.MODE_PRIVATE );//preference ?´ë¦„ ì§? •->env
		 _sp_editor=_pref.edit();
	}

	/*************Methods**************/
	//setValues
	public void putValues(String key, Object value){
		if(Integer.class.isInstance(value))		{
			_sp_editor.putInt(key, (Integer) value);			
		}
		else if(String.class.isInstance(value)){
			_sp_editor.putString(key, (String) value);
		}
		else if(Boolean.class.isInstance(value)){
			_sp_editor.putBoolean(key, (Boolean) value);
		}
		else if(Float.class.isInstance(value)){
			_sp_editor.putFloat(key, (Float) value);
		}
		_sp_editor.commit();
	}
	//getValues
	public float getFloatValue(String key){
		return _pref.getFloat(key,0);//0 is default value
	}
	public String getStringValue(String key){
		return _pref.getString(key,GlobalVar.NOTHING);//0 is default value
	}
	public int getIntegerValue(String key){
		return _pref.getInt(key,0);//0 is default value
	}
	public boolean getBooleanValue(String key){
		return _pref.getBoolean(key,false);//0 is default value
	}
	//other utility
	public boolean checkContains(String key){ 
		return (_pref==null?false:_pref.contains(key));
	}
	public void clearAll(){
		_sp_editor.clear();
		_sp_editor.commit();
	}
	
}
