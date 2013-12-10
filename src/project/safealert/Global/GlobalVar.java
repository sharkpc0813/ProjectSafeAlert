package project.safealert.Global;


public class GlobalVar {
	/*default setting*/
	public static final String DEFAULT_SETTING="default_setting";
	/*SMS TEXT*/
	public static final String USERNAME="user_name";
	public static final String USERNUMBER="user_number";
	public static final String SMS_TEXT="sms_text";
	public static final String SMS_SORRY_TEXT="sorry_text";
	
	/* preference */
	public static final String KEY_THRESHOLD="key_threshold";
	public static final String NOTHING ="nothing";
	/* BroadCastReceiver Filter */
	public static final String WIDGET_BRODCAST = "widget_change";
	public static final String WIDGET_UPDATE = "android.appwidget.action.APPWIDGET_UPDATE";
	public static final String ACTION_DIALOG = "project.safealert.ACTION_DIALOG";
	public static final String DIALOG_ACTIVE = "project.safealert.DIALOG_ACTIVE";
	
	/* map intent */
	public static final int MAP_LIST_REQUEST = 1;
	public static final int NEW_PATH = 10;
	public static final int LOAD_PATH = 20;	
	public static final String LOAD_PATH_STR = "project.safealert.map.LOAD_OTHER_PATH";
	public static final String ONLY_SENSOR="¾øÀ½";
	/* map control */
	public static final int DEFAULT_PATH_INTERVAL=250;
	public static final String AREA_SIZE="area_size";
	public static final int SPOT_SIZE=5;
	/* member contract */
	public static final int MEMBER_CONTRACT = 3;	
	public static final String MEMBER_CONTRACT_STR = "project.safealert.members.MEMBER_CONTRACT";
	/* widget */
	public static final String WIDGET_MAP="project.safealert.widget.WidgetMap";
	/* service */	
	public static final String SERVICE ="project.safealert.background.ServiceAlert";
	public static final String SERVICE_STUDY ="project.safealert.background.ServiceAlert_study";
	public static final String  LOCATION_TIME_INTERVAL="location_time_interval";	
	public static final String MESSAGE_DLG ="project.safealert.MESSAGE_DLG";	
	public static final String SERVICE_DIED_DEST_X ="project.safealert.SERVICE_DIED_DEST_X";	
	public static final String SERVICE_DIED_DEST_Y ="project.safealert.SERVICE_DIED_DEST_Y";
	public static final String MESSAGE_WAITING_TIME="project.safealert.MessageAlert";
	public static final String MESSAGE_MAP_LINK="project.safealert.MessageAlert";
	public static final int LOCATION_DISTANCE_INTERVAL =0;
	public static final int SENSOR_TIME_INTERVAL =5000;

	/* db */
	public static final String DB_NAME="MyDB";
	public static final int DB_VERSION=8;	
	public static final String DB_MEMBER_NAME="MyMemberDB";
	public static final int DB_MEMBER_VERSION=2;	
}
