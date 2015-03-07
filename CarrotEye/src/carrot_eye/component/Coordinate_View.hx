package carrot_eye.component;
import flixel.group.FlxGroup;
import flixel.text.FlxText;

/**
 * ...
 * @author Biolardi Yoshogi (Vsio Stitched / NeithR)
 */
class Coordinate_View extends FlxGroup
{
	@:isVar public var textLat(get, null):FlxText;
	@:isVar public var textLon(get, null):FlxText;
	
	public function new() 
	{		
		super();
		textLat = new FlxText(0,0,0,"",16);
		textLat.text = "Lat: -";
		textLon = new FlxText(0,0,0,"",16);	
		textLon.text = "Lon: -";
		
		add(textLat);
		add(textLon);
	}
	// ACCESSOR
	
	function get_textLat():FlxText 
	{
		return textLat;
	}
	
	function get_textLon():FlxText 
	{
		return textLon;
	}
	
}