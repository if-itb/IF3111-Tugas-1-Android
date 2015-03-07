package carrot_eye.states;
import carrot_eye.component.Coordinate_View;
import carrot_eye.main.App;
import carrot_eye.main.AppCore;
import flixel.FlxG;
import flixel.FlxState;
import flixel.text.FlxText;
import flixel.ui.FlxButton;
import openfl.events.Event;
import openfl.events.MouseEvent;

/**
 * ...
 * @author Biolardi Yoshogi (Vsio Stitched / NeithR)
 */
class State_Tracker extends FlxState
{
	private var textYou:FlxText;
	private var textTarget:FlxText;
	private var buttonReturn:FlxButton;
	
	public function new() 
	{
		super();
	}
	
	override public function create():Void
	{
		super.create();
		
		textYou = new FlxText(10, 10, 0, "== You == ", 12);
		add(textYou);	
		textTarget = new FlxText(0, textYou.y, 0, "== Target ==", 12);
		textTarget.x = FlxG.width - textTarget.width - 10;
		add(textTarget);
		
		AppCore.gpsTracker.coordinate.view = new Coordinate_View();
		AppCore.gpsTracker.coordinate.view.textLat.x = 10;
		AppCore.gpsTracker.coordinate.view.textLat.y = 30;
		AppCore.gpsTracker.coordinate.view.textLon.x = 10;
		AppCore.gpsTracker.coordinate.view.textLon.y = AppCore.gpsTracker.coordinate.view.textLat.y+25;		
		add(AppCore.gpsTracker.coordinate.view);
		
		AppCore.targetTracker.coordinate.view = new Coordinate_View();
		AppCore.targetTracker.coordinate.view.textLat.x = FlxG.width - AppCore.targetTracker.coordinate.view.textLat.width - 100;
		AppCore.targetTracker.coordinate.view.textLat.alignment = "right";
		AppCore.targetTracker.coordinate.view.textLat.y = AppCore.gpsTracker.coordinate.view.textLat.y;
		AppCore.targetTracker.coordinate.view.textLon.alignment = "right";
		AppCore.targetTracker.coordinate.view.textLon.x = FlxG.width-AppCore.targetTracker.coordinate.view.textLat.width-100;
		AppCore.targetTracker.coordinate.view.textLon.y = AppCore.targetTracker.coordinate.view.textLat.y+25;
		add(AppCore.targetTracker.coordinate.view);
		
		buttonReturn = new FlxButton(0, 0, "Return", returnToTitle);
		buttonReturn.x = FlxG.width - buttonReturn.width - 10;
		buttonReturn.y = FlxG.height - buttonReturn.height - 10;
		add(buttonReturn);
		
		AppCore.targetTracker.dataGetURL();
	}
	
	public function returnToTitle():Void
	{
		AppCore.gpsTracker.coordinate.view = null;
		AppCore.targetTracker.coordinate.view = null;
		FlxG.switchState(new State_Title());
	}
}