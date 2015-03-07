package carrot_eye.main;

/**
 * ...
 * @author Biolardi Yoshogi (Vsio Stitched / NeithR)
 */
class AppGlobal
{
	
	@:isVar public var nim(get, null):String;
	@:isVar public var token(get, null):String;
	
	public function new() 
	{
		nim = "13509035";
		token = "tomandjerry";
	}
	
	function get_nim():String 
	{
		return nim;
	}
	
	function get_token():String 
	{
		return token;
	}
}