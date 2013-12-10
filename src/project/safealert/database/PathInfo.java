package project.safealert.database;

public class PathInfo {
	private int index;
	private String title;
	private double _lat,_long;

	public PathInfo(int index,String title,double _lat,double _long) {
		super();
		this.index=index;
		this.title = title;
		this._lat=_lat;
		this._long=_long;
	}
	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
	}	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}	
	public double get_lat() {
		return _lat;
	}
	public void set_lat(double _lat) {
		this._lat = _lat;
	}
	public double get_long() {
		return _long;
	}
	public void set_long(double _long) {
		this._long = _long;
	}		
}
