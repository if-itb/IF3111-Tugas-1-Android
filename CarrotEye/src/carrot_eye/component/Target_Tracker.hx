package carrot_eye.component;
import haxe.Json;
import openfl.errors.Error;
import openfl.events.Event;
import openfl.events.IOErrorEvent;
import openfl.Lib;
import openfl.net.URLLoader;
import openfl.net.URLRequest;
import openfl.net.URLRequestMethod;

/**
 * ...
 * @author Biolardi Yoshogi (Vsio Stitched / NeithR)
 */
 
class Target_Tracker
{
	private var url:String;
	@:isVar public var coordinate(get, null):Coordinate;
	@:isVar public var isLoading(get, null):Bool;
	
	public function new(url_:String) 
	{
		url = url_;
		isLoading = false;
		coordinate = new Coordinate();
	}
	
	public function dataGetURL():Void
	{
		var request:URLRequest = new URLRequest(url);
		request.method = URLRequestMethod.GET;
		
		var loader:URLLoader = new URLLoader();
		isLoading = true;
		loader.addEventListener(Event.COMPLETE, getData);
		loader.addEventListener(IOErrorEvent.IO_ERROR, getDataFailed);
		loader.load(request);		
	}
	
	private function getData(e_:Event)
	{
		var values = {lat:0.0, long:0.0, valid_until:0}
		
		//Lib.trace(e_.target.data);
		values = Json.parse(e_.target.data);
		
		this.coordinate.latitude = values.lat;
		this.coordinate.longitude = values.long;
		this.coordinate.valid_until = values.valid_until;
		
		isLoading = false;
	}
	
	public function getDataFailed(e_:Event):Void
	{		
		//trace(e_.target.data);
		
		if (this.coordinate.view != null) {
			this.coordinate.view.textLat.text = "Lat: ERROR!";
			this.coordinate.view.textLon.text = "Lon: ERROR!";
		}
		isLoading = false;
	}
	
	// ACCESSOR
	
	function get_coordinate():Coordinate 
	{
		return coordinate;
	}
	
	function get_isLoading():Bool 
	{
		return isLoading;
	}
	
}