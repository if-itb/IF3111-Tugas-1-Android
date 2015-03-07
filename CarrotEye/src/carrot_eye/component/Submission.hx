package carrot_eye.component;
import haxe.Json;
import openfl.events.Event;
import openfl.events.IOErrorEvent;
import openfl.net.URLLoader;
import openfl.net.URLRequest;
import openfl.net.URLRequestMethod;

/**
 * ...
 * @author Biolardi Yoshogi (Vsio Stitched / NeithR)
 */

typedef Response = {
	var message:String;
	var code:Int;
}
 
class Submission
{
	private var url:String;
	private var isLoading:Bool;
	private var response:Response;
	
	@:isVar public var view(get, set):Submission_View;

	public function new(url_:String) 
	{
		this.url = url_;
		isLoading = false;
		response = { message: "", code: 0}
	}
	
	public function dataPostURL(nim_:String, token_:String):Void
	{			
		var urlPost:String = "http://167.205.32.46/pbd/api/catch";
		var request:URLRequest = new URLRequest(urlPost);
		request.method = URLRequestMethod.POST;
		request.data = Json.stringify( { "nim": nim_, "token": token_ } ); // secret token = tomandjerry

		isLoading = true;
		var loader:URLLoader = new URLLoader();
		loader.addEventListener(Event.COMPLETE, postData);
		loader.addEventListener(IOErrorEvent.IO_ERROR, postDataFailed);
		loader.load(request);
		
		if (view != null) {
			view.textToken.text = "Token: "+token_;
		}
	}
	
	private function postData(e_:Event)
	{
		//trace(e_.target.data);
		if (e_.target.data != null) {
			response = Json.parse(e_.target.data);
			//trace(response);
			set_message(response.message);
			set_code(response.code);
		}
		isLoading = false;
	}
	
	private function postDataFailed(e_:Event)
	{
		//trace(e_.target.data);
		isLoading = false;
		
		if (view != null) {
			view.textToken.text = "Token: ERROR!";
			view.textMessage.text = "Message: ERROR!";
			view.textCode.text = "Code: ERROR!";
		}
	}
	
	// ACCESSOR
	
	public function set_message(value:String):String 
	{
		if (view != null) {
			view.textMessage.text = "Message: "+value;
		}
		return response.message = value;
	}
	
	public function set_code(value:Int):Int 
	{
		if (view != null) {
			view.textCode.text = "Code: "+Std.string(value);
		}		
		return response.code = value;
	}	
	
	function get_view():Submission_View 
	{
		return view;
	}
	
	function set_view(value:Submission_View):Submission_View 
	{
		return view = value;
	}
}