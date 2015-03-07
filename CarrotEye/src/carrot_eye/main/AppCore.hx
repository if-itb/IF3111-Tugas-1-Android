package carrot_eye.main;
import carrot_eye.component.Geomagnetic_Sensor;
import carrot_eye.component.GPS_Tracker;
import carrot_eye.component.QRCode_Scanner;
import carrot_eye.component.Submission;
import carrot_eye.component.Target_Tracker;

/**
 * ...
 * @author Biolardi Yoshogi (Vsio Stitched / NeithR)
 */
class AppCore
{
	@:isVar static public var appGlobal(get, null):AppGlobal;
	@:isVar static public var targetTracker(get, null):Target_Tracker;
	@:isVar static public var submission(get, null):Submission;
	@:isVar static public var gpsTracker(get, null):GPS_Tracker;
	static public var geomagneticSensor:Geomagnetic_Sensor;
	static public var qrcodeScanner:QRCode_Scanner;
	
	public function new() 
	{
		appGlobal = new AppGlobal();
		targetTracker = new Target_Tracker("http://167.205.32.46/pbd/api/track?nim=13509035");
		gpsTracker = new GPS_Tracker();
		geomagneticSensor = new Geomagnetic_Sensor();
		qrcodeScanner = new QRCode_Scanner();
		submission = new Submission("http://167.205.32.46/pbd/api/catch");
	}
	
	public function run():Void
	{
		//targetTracker.dataGetURL();
		//submission.dataPostURL(nim, "tomandjerry");
	}
	
	// ACCESSOR

	static function get_appGlobal():AppGlobal 
	{
		return appGlobal;
	}
	
	static function get_targetTracker():Target_Tracker
	{
		return targetTracker;
	}
	
	static function get_submission():Submission 
	{
		return submission;
	}
	
	static function get_gpsTracker():GPS_Tracker 
	{
		return gpsTracker;
	}
	
}