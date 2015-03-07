package carrot_eye.component;

/**
 * ...
 * @author Biolardi Yoshogi (Vsio Stitched / NeithR)
 */
class Coordinate
{
	@:isVar public var latitude(get, set):Float;
	@:isVar public var longitude(get, set):Float;
	@:isVar public var valid_until(get, set):Int;
	
	@:isVar public var view(get, set):Coordinate_View;

	public function new(latitude_:Float=0.0, longitude_:Float=0.0, validUntil_:Int=0)
	{
		this.latitude = latitude_;
		this.longitude = longitude_;
		this.valid_until = validUntil_;
	}
	
	// ACCESSOR
	
	function get_latitude():Float 
	{
		return this.latitude;
	}
	
	function set_latitude(value:Float):Float 
	{
		if (view != null) {
			view.textLat.text = "Lat: "+Std.string(value);
		}
		return this.latitude = value;
	}
	
	function get_valid_until():Int 
	{
		return this.valid_until;
	}
	
	function set_valid_until(value:Int):Int 
	{
		return this.valid_until = value;
	}
	
	function get_longitude():Float 
	{
		return this.longitude;
	}
	
	function set_longitude(value:Float):Float 
	{
		if (view != null) {
			view.textLon.text = "Lon: " + Std.string(value);
		}
		return this.longitude = value;
	}
	
	function get_view():Coordinate_View 
	{
		return view;
	}
	
	function set_view(value:Coordinate_View):Coordinate_View 
	{
		return view = value;
	}
}