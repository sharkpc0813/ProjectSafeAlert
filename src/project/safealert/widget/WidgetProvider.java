package project.safealert.widget;

import project.safealert.R;
import project.safealert.Global.GlobalVar;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class WidgetProvider extends AppWidgetProvider {

	private int widget_res;

	@Override  
	public void onEnabled(Context context) {  
		super.onEnabled(context);   
	}   
	@Override 
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, 
			int[] appWidgetIds) {  

		super.onUpdate(context, appWidgetManager, appWidgetIds);     
		widget_res=R.drawable.widget_normal;
		
		for(int i=0; i<appWidgetIds.length; i++){  
			int appWidgetId = appWidgetIds[i];   
			RemoteViews views = new RemoteViews(context.getPackageName(),widget_res);         
			appWidgetManager.updateAppWidget(appWidgetId, views);    
		}    
	}       
	@Override 
	public void onDeleted(Context context, int[] appWidgetIds) {    
		super.onDeleted(context, appWidgetIds);     
	}       
	@Override  
	public void onDisabled(Context context) { 
		super.onDisabled(context);  
	}        
	/*********** initUI   ************/  
	public void initUI(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {      
		//Log.i(TAG, "WidgetProvider::initUI()"); 
		RemoteViews views = new RemoteViews(context.getPackageName(),
				R.layout.button_widget);        
		views.setImageViewResource(R.id.bt_widget, widget_res);
		
		Intent dialogIntent             = new Intent(GlobalVar.ACTION_DIALOG);       
		PendingIntent dialogPIntent         = PendingIntent.getBroadcast(context, 0, dialogIntent       , 0);           
		views.setOnClickPendingIntent(R.id.bt_widget, dialogPIntent);      
		for(int appWidgetId : appWidgetIds) {  
			appWidgetManager.updateAppWidget(appWidgetId, views);     
		}    
	}      
	/*********** Receiver   ************/  
	@Override 
	public void onReceive(Context context, Intent intent) {   
		super.onReceive(context, intent);           
		String action = intent.getAction();       
		//Log.d(TAG, "onReceive() action = " + action);      
		// Default Recevier    
		if(AppWidgetManager.ACTION_APPWIDGET_ENABLED.equals(action)){   

		}        
		else if(AppWidgetManager.ACTION_APPWIDGET_UPDATE.equals(action)){    
			AppWidgetManager manager = AppWidgetManager.getInstance(context); 
			//widget image resource change
			boolean activated=intent.getBooleanExtra(GlobalVar.WIDGET_BRODCAST, false);			
			if(activated)
				widget_res=R.drawable.widget_click;
			else
				widget_res=R.drawable.widget_normal;
			initUI(context, manager, manager.getAppWidgetIds(new ComponentName(context, getClass())));    
			
		}   
		else if(AppWidgetManager.ACTION_APPWIDGET_DELETED.equals(action)){         

		}     
		else if(AppWidgetManager.ACTION_APPWIDGET_DISABLED.equals(action)){   

		} // Custom Recevier  
		else  if(GlobalVar.ACTION_DIALOG.equals(action)){    
			createDialog(context);       
		}    
	}            
	/**      * Dialog Activity È£Ãâ (PendingIntent)      */ 
	private void createDialog(Context context){     
		//Log.d(TAG, "createDialog()");      
		AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		Intent Intent = new Intent(GlobalVar.DIALOG_ACTIVE);
		PendingIntent pIntent = PendingIntent.getActivity(context, 0, Intent, 0);        
		alarmManager.set(AlarmManager.RTC, System.currentTimeMillis(), pIntent);   
	} 
}

