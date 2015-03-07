package carrot_eye.component;
import flixel.group.FlxGroup;
import flixel.text.FlxText;

/**
 * ...
 * @author Biolardi Yoshogi (Vsio Stitched / NeithR)
 */
class Submission_View extends FlxGroup
{
	@:isVar public var textToken(get, null):FlxText;	
	@:isVar public var textMessage(get, null):FlxText;
	@:isVar public var textCode(get, null):FlxText;

	public function new() 
	{		
		super();
		
		textToken = new FlxText(50, 200, 400, "Token: -", 12);
		textMessage = new FlxText(textToken.x, textToken.y+50, 400, "Message: -", 12);
		textCode = new FlxText(textToken.x, textToken.y + 75, 0, "Code: -", 12);
		
		add(textToken);
		add(textMessage);
		add(textCode);
	}
	
	// ACCESSOR
	
	function get_textToken():FlxText 
	{
		return textToken;
	}
	
	function get_textMessage():FlxText 
	{
		return textMessage;
	}
	
	function get_textCode():FlxText 
	{
		return textCode;
	}
	
}