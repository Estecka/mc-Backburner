package tk.estecka.backburner.config;

import java.util.HashMap;
import java.util.Map;
import com.google.common.collect.ImmutableMap;
import tk.estecka.backburner.config.ConfigIO.Property;

public class Config
extends ConfigIO.AFixedCoded
{
	public String rootCommand = "note";
	public boolean addFeedback = true;
	public boolean delFeedback = true;

	public int hudX = 8;
	public int hudY = 32;
	public int hudWdt = 128;

	public float anchorX = 0;

	public float hudScale = 0.55f;
	public boolean allowFractional = false;


	private final Map<String, Property<?>> codec = new HashMap<>(){{
		put( "command.root", Property.String(()->rootCommand, v->rootCommand=v) );
		put( "feedback.addition", Property.Boolean(()->addFeedback, v->addFeedback=v) );
		put( "feedback.removal",  Property.Boolean(()->delFeedback, v->delFeedback=v) );
		put( "hud.x", Property.Integer(()->hudX, v->hudX=v) );
		put( "hud.y", Property.Integer(()->hudY, v->hudY=v) );
		put( "hud.anchorX", Property.Float(()->anchorX, v->anchorX=v) );
		put( "hud.width", Property.Integer(()->hudWdt, v->hudWdt=v) );
		put( "hud.scale", Property.Float(()->hudScale, v->hudScale=v) );
		put( "hud.scale.allowFractional", Property.Boolean(()->allowFractional, v->allowFractional=v) );
	}};

	public Map<String, Property<?>> GetProperties(){
		return ImmutableMap.copyOf(this.codec);
	}
}
