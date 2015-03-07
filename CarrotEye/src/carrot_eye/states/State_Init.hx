package carrot_eye.states;
import carrot_eye.main.AppCore;
import flixel.FlxG;
import flixel.FlxState;

/**
 * ...
 * @author Biolardi Yoshogi (Vsio Stitched / NeithR)
 */
class State_Init extends FlxState
{
	private var appCore:AppCore;

	public function new() 
	{
		super();
	}
	
	override public function create():Void
	{
		super.create();
		
		appCore = new AppCore();
		FlxG.switchState(new State_Title());
	}
	
}