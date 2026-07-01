package tk.estecka.backburner.config;

import java.util.Optional;
import net.minecraft.network.chat.Component;

public enum EWriteAction
{
	TOP("top"),
	BOTTOM("bottom"),
	;

	private final String name;

	private EWriteAction(String name){
		this.name = name;
	}

	static public EWriteAction parse(String name)
	throws IllegalArgumentException
	{
		for (EWriteAction e : values())
			if (e.toString().equals(name))
				return e;

		throw new IllegalArgumentException("Invalid enum value:" + name);
	}

	@Override
	public String toString(){
		return this.name;
	}

	public String TranslationKey() {
		return "backburner.config.writeAction." + this.name;
	}

	public Component TranslatableName() {
		return Component.translatable(this.TranslationKey());
	}

	public Optional<Component[]> Tooltip() {
		return Optional.of(new Component[]{ Component.translatable(this.TranslationKey()+".tooltip") });
	}

}
