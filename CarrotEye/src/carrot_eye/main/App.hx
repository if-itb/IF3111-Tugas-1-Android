package carrot_eye.main;
import carrot_eye.states.State_Init;
import flixel.FlxGame;

/**
 * ...
 * @author Biolardi Yoshogi (Vsio Stitched / NeithR)
 */
class App extends FlxGame
{
	public function new() 
	{
		super(480, 640, State_Init);
	}

}