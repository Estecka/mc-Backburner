package tk.estecka.backburner.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import net.minecraft.text.Text;

public class ModMenu
implements ModMenuApi
{
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory(){
		return parent -> {
			var config = ConfigBuilder.create().setParentScreen(parent).setTitle(Text.literal("Backburner"));
			config.getOrCreateCategory(Text.literal("HUD"));
			return config.build();
		};
	}
}
