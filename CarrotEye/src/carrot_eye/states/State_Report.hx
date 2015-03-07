package carrot_eye.states;
import carrot_eye.component.Submission_View;
import carrot_eye.main.AppCore;
import carrot_eye.main.AppGlobal;
import flixel.FlxG;
import flixel.FlxState;
import flixel.ui.FlxButton;

/**
 * ...
 * @author Biolardi Yoshogi (Vsio Stitched / NeithR)
 */
class State_Report extends FlxState
{
	private var buttonReturn:FlxButton;

	public function new() 
	{
		super();
	}
	
	override public function create():Void
	{
		super.create();
		
		AppCore.submission.view = new Submission_View();
		add(AppCore.submission.view);
		
		buttonReturn = new FlxButton(0, 0, "Return", returnToTitle);
		buttonReturn.x = FlxG.width - buttonReturn.width - 10;
		buttonReturn.y = FlxG.height - buttonReturn.height - 10;
		buttonReturn.label.size = 16;
		add(buttonReturn);
		
		AppCore.submission.dataPostURL(AppCore.appGlobal.nim, AppCore.appGlobal.token);
	}
	
	public function returnToTitle():Void
	{
		AppCore.submission.view = null;
		FlxG.switchState(new State_Title());
	}
	
}