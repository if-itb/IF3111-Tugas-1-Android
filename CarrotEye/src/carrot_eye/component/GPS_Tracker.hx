package carrot_eye.component;

/**
 * ...
 * @author Biolardi Yoshogi (Vsio Stitched / NeithR)
 */
class GPS_Tracker
{
	@:isVar public var coordinate(get, null):Coordinate;
	@:isVar public var isLoading(get, null):Bool;

	public function new() 
	{
		isLoading = false;
		coordinate = new Coordinate();		
	}

	// ACCESSOR
	
	function get_isLoading():Bool 
	{
		return isLoading;
	}
	
	function get_coordinate():Coordinate 
	{
		return this.coordinate;
	}
	
}